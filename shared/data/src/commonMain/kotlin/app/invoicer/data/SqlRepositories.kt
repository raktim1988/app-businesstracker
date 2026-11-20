package app.invoicer.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.invoicer.data.db.InvoicerDatabase
import app.invoicer.data.db.Invoice_line
import app.invoicer.domain.model.BusinessProfile
import app.invoicer.domain.model.Client
import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.InvoiceLine
import app.invoicer.domain.model.InvoiceStatus
import app.invoicer.domain.model.Money
import app.invoicer.domain.model.VatRate
import app.invoicer.domain.repository.BusinessProfileRepository
import app.invoicer.domain.repository.ClientRepository
import app.invoicer.domain.repository.InvoiceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

private fun nowMillis() = Clock.System.now().toEpochMilliseconds()

class SqlClientRepository(
    private val db: InvoicerDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ClientRepository {
    private val q get() = db.invoicerQueries

    override fun observeAll(): Flow<List<Client>> =
        q.selectClients().asFlow().mapToList(dispatcher).map { rows ->
            rows.map { Client(it.id, it.name, it.email, it.kvk_number, it.btw_number, it.payment_term_days.toInt()) }
        }

    override suspend fun upsert(client: Client) = withContext(dispatcher) {
        q.transaction {
            val version = (q.clientVersion(client.id).executeAsOneOrNull() ?: 0L) + 1
            q.upsertClient(
                client.id, client.name, client.email, client.kvkNumber, client.btwNumber,
                client.paymentTermDays.toLong(), nowMillis(), version,
            )
        }
    }

    override suspend fun delete(id: String) {
        withContext(dispatcher) { q.deleteClient(id) }
    }
}

class SqlInvoiceRepository(
    private val db: InvoicerDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : InvoiceRepository {
    private val q get() = db.invoicerQueries

    override fun observeAll(): Flow<List<Invoice>> = combine(
        q.selectInvoices().asFlow().mapToList(dispatcher),
        q.selectAllLines().asFlow().mapToList(dispatcher),
    ) { invoices, lines ->
        val byInvoice = lines.groupBy { it.invoice_id }
        invoices.map { row ->
            Invoice(
                id = row.id,
                number = row.number,
                clientId = row.client_id,
                clientName = row.client_name,
                status = InvoiceStatus.valueOf(row.status),
                issueDate = LocalDate.parse(row.issue_date),
                dueDate = LocalDate.parse(row.due_date),
                lines = byInvoice[row.id].orEmpty().map { it.toDomain() },
            )
        }
    }

    override suspend fun get(id: String): Invoice? = withContext(dispatcher) {
        val row = q.selectInvoice(id).executeAsOneOrNull() ?: return@withContext null
        Invoice(
            id = row.id,
            number = row.number,
            clientId = row.client_id,
            clientName = row.client_name,
            status = InvoiceStatus.valueOf(row.status),
            issueDate = LocalDate.parse(row.issue_date),
            dueDate = LocalDate.parse(row.due_date),
            lines = q.selectLines(id).executeAsList().map { it.toDomain() },
        )
    }

    override suspend fun upsert(invoice: Invoice) = withContext(dispatcher) {
        q.transaction {
            val version = (q.invoiceVersion(invoice.id).executeAsOneOrNull() ?: 0L) + 1
            q.upsertInvoice(
                invoice.id, invoice.number, invoice.clientId, invoice.clientName, invoice.status.name,
                invoice.issueDate.toString(), invoice.dueDate.toString(), nowMillis(), version,
            )
            q.deleteLines(invoice.id)
            invoice.lines.forEachIndexed { index, line ->
                q.insertLine(
                    invoice.id, index.toLong(), line.description, line.quantityMilli,
                    line.unitPrice.cents, line.vatRate.percent.toLong(),
                )
            }
        }
    }

    override suspend fun deleteDraft(id: String) {
        withContext(dispatcher) {
            q.transaction {
                if (q.selectInvoice(id).executeAsOneOrNull()?.status == InvoiceStatus.DRAFT.name) {
                    q.deleteLines(id)
                    q.deleteInvoice(id)
                }
            }
        }
    }

    override suspend fun nextNumber(year: Int): String = withContext(dispatcher) {
        val prefix = "$year-"
        val last = q.invoiceNumbersWithPrefix(prefix).executeAsList()
            .mapNotNull { it.removePrefix(prefix).toIntOrNull() }
            .maxOrNull() ?: 0
        prefix + (last + 1).toString().padStart(4, '0')
    }

    private fun Invoice_line.toDomain() = InvoiceLine(
        description = description,
        quantityMilli = quantity_milli,
        unitPrice = Money(unit_price_cents),
        vatRate = VatRate.entries.first { it.percent.toLong() == vat_percent },
    )
}

class SqlBusinessProfileRepository(
    private val db: InvoicerDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BusinessProfileRepository {
    private val q get() = db.invoicerQueries

    override fun observe(): Flow<BusinessProfile> =
        q.selectProfile().asFlow().mapToOneOrNull(dispatcher).map { row ->
            row?.let {
                BusinessProfile(it.name, it.address, it.kvk_number, it.btw_number, it.iban, it.default_payment_term_days.toInt())
            } ?: BusinessProfile()
        }

    override suspend fun save(profile: BusinessProfile) = withContext(dispatcher) {
        q.transaction {
            val version = (q.profileVersion().executeAsOneOrNull() ?: 0L) + 1
            q.upsertProfile(
                profile.name, profile.address, profile.kvkNumber, profile.btwNumber, profile.iban,
                profile.defaultPaymentTermDays.toLong(), nowMillis(), version,
            )
        }
    }
}
