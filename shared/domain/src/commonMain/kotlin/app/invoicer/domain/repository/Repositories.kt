package app.invoicer.domain.repository

import app.invoicer.domain.model.BusinessProfile
import app.invoicer.domain.model.Client
import app.invoicer.domain.model.Invoice
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun observeAll(): Flow<List<Invoice>>
    suspend fun get(id: String): Invoice?
    suspend fun upsert(invoice: Invoice)

    /** Removes a draft; issued invoices must be kept, so non-drafts are left untouched. */
    suspend fun deleteDraft(id: String)

    /** Next gap-free invoice number for [year], e.g. "2026-0003". */
    suspend fun nextNumber(year: Int): String
}

interface ClientRepository {
    fun observeAll(): Flow<List<Client>>
    suspend fun upsert(client: Client)
    suspend fun delete(id: String)
}

interface BusinessProfileRepository {
    fun observe(): Flow<BusinessProfile>
    suspend fun save(profile: BusinessProfile)
}
