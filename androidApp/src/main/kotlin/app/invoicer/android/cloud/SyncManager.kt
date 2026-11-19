package app.invoicer.android.cloud

import app.invoicer.data.SyncStore
import app.invoicer.data.db.Business_profile
import app.invoicer.data.db.Client
import app.invoicer.data.db.Invoice
import app.invoicer.data.db.Invoice_line
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlin.time.Clock

data class SyncState(val syncing: Boolean = false, val lastSyncMillis: Long? = null, val failed: Boolean = false)

/**
 * Two-way sync between the local database (source of truth) and Supabase.
 * Last write wins per row, decided by `updated_at`. Deletions are not synced yet.
 */
@OptIn(FlowPreview::class)
class SyncManager(private val store: SyncStore, private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
    private val client = Cloud.client
    private val mutex = Mutex()
    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()
    private var pending: Job? = null

    /** Syncs once whenever a session becomes active, e.g. after sign-in or on app start. */
    fun start(localChanges: Flow<*>) {
        val c = client ?: return
        scope.launch { localChanges.debounce(3_000).collect { syncNow() } }
        scope.launch {
            c.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) syncNow()
            }
        }
    }

    fun syncNow() {
        if (client?.auth?.currentUserOrNull() == null) return
        pending?.cancel()
        pending = scope.launch { run() }
    }

    private suspend fun run() = mutex.withLock {
        val c = client ?: return
        val userId = c.auth.currentUserOrNull()?.id ?: return
        _state.update { it.copy(syncing = true) }
        val ok = runCatching { sync(userId) }.isSuccess
        _state.update { SyncState(false, if (ok) Clock.System.now().toEpochMilliseconds() else it.lastSyncMillis, !ok) }
    }

    private suspend fun sync(userId: String) {
        val db = client!!
        syncClients(db, userId)
        syncInvoices(db, userId)
        syncProfile(db, userId)
    }

    private suspend fun syncClients(db: io.github.jan.supabase.SupabaseClient, userId: String) {
        val remote = db.from("client").select().decodeList<ClientDto>().associateBy { it.id }
        val local = store.clients().associateBy { it.id }
        val push = local.values.filter { (remote[it.id]?.updatedAt ?: -1) < it.updated_at }
        if (push.isNotEmpty()) {
            db.from("client").upsert(push.map { it.toDto(userId) }) { onConflict = "user_id,id" }
        }
        remote.values.filter { (local[it.id]?.updated_at ?: -1) < it.updatedAt }.forEach { store.putClient(it.toRow()) }
    }

    private suspend fun syncInvoices(db: io.github.jan.supabase.SupabaseClient, userId: String) {
        val remote = db.from("invoice").select().decodeList<InvoiceDto>().associateBy { it.id }
        val remoteLines = db.from("invoice_line").select().decodeList<LineDto>().groupBy { it.invoiceId }
        val local = store.invoices().associateBy { it.id }
        val localLines = store.lines().groupBy { it.invoice_id }

        for (inv in local.values) {
            if ((remote[inv.id]?.updatedAt ?: -1) >= inv.updated_at) continue
            db.from("invoice").upsert(inv.toDto(userId)) { onConflict = "user_id,id" }
            db.from("invoice_line").delete { filter { eq("invoice_id", inv.id) } }
            val lines = localLines[inv.id].orEmpty().map { it.toDto(userId) }
            if (lines.isNotEmpty()) db.from("invoice_line").insert(lines)
        }
        for (inv in remote.values) {
            if ((local[inv.id]?.updated_at ?: -1) >= inv.updatedAt) continue
            store.putInvoice(inv.toRow(), remoteLines[inv.id].orEmpty().map { it.toRow() })
        }
    }

    private suspend fun syncProfile(db: io.github.jan.supabase.SupabaseClient, userId: String) {
        val remote = db.from("business_profile").select().decodeList<ProfileDto>().firstOrNull()
        val local = store.profile()
        when {
            local != null && (remote?.updatedAt ?: -1) < local.updated_at ->
                db.from("business_profile").upsert(local.toDto(userId)) { onConflict = "user_id" }
            remote != null && (local?.updated_at ?: -1) < remote.updatedAt -> store.putProfile(remote.toRow())
        }
    }
}

@Serializable
private data class ClientDto(
    val userId: String? = null,
    val id: String,
    val name: String,
    val email: String,
    val kvkNumber: String,
    val btwNumber: String,
    val paymentTermDays: Long,
    val updatedAt: Long,
    val version: Long,
)

@Serializable
private data class InvoiceDto(
    val userId: String? = null,
    val id: String,
    val number: String,
    val clientId: String,
    val clientName: String,
    val status: String,
    val issueDate: String,
    val dueDate: String,
    val updatedAt: Long,
    val version: Long,
)

@Serializable
private data class LineDto(
    val userId: String? = null,
    val invoiceId: String,
    val position: Long,
    val description: String,
    val quantityMilli: Long,
    val unitPriceCents: Long,
    val vatPercent: Long,
)

@Serializable
private data class ProfileDto(
    val userId: String? = null,
    val name: String,
    val address: String,
    val kvkNumber: String,
    val btwNumber: String,
    val iban: String,
    val defaultPaymentTermDays: Long,
    val updatedAt: Long,
    val version: Long,
)

private fun Client.toDto(user: String) = ClientDto(user, id, name, email, kvk_number, btw_number, payment_term_days, updated_at, version)
private fun ClientDto.toRow() = Client(id, name, email, kvkNumber, btwNumber, paymentTermDays, updatedAt, version)

private fun Invoice.toDto(user: String) =
    InvoiceDto(user, id, number, client_id, client_name, status, issue_date, due_date, updated_at, version)

private fun InvoiceDto.toRow() = Invoice(id, number, clientId, clientName, status, issueDate, dueDate, updatedAt, version)

private fun Invoice_line.toDto(user: String) =
    LineDto(user, invoice_id, position, description, quantity_milli, unit_price_cents, vat_percent)

private fun LineDto.toRow() = Invoice_line(invoiceId, position, description, quantityMilli, unitPriceCents, vatPercent)

private fun Business_profile.toDto(user: String) =
    ProfileDto(user, name, address, kvk_number, btw_number, iban, default_payment_term_days, updated_at, version)

private fun ProfileDto.toRow() = Business_profile(1, name, address, kvkNumber, btwNumber, iban, defaultPaymentTermDays, updatedAt, version)
