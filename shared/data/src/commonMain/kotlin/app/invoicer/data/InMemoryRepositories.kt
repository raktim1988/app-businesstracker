package app.invoicer.data

import app.invoicer.domain.model.Client
import app.invoicer.domain.model.Invoice
import app.invoicer.domain.repository.ClientRepository
import app.invoicer.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** v0.1 storage. Replace with a Room/SQLDelight implementation behind the same interface. */
class InMemoryInvoiceRepository : InvoiceRepository {
    private val store = MutableStateFlow<Map<String, Invoice>>(emptyMap())

    override fun observeAll() = store.map { it.values.sortedByDescending { i -> i.number } }

    override suspend fun get(id: String) = store.value[id]

    override suspend fun upsert(invoice: Invoice) = store.update { it + (invoice.id to invoice) }

    override suspend fun nextNumber(year: Int): String {
        val prefix = "$year-"
        val last = store.value.values
            .map { it.number }
            .filter { it.startsWith(prefix) }
            .maxOfOrNull { it.removePrefix(prefix).toInt() } ?: 0
        return prefix + (last + 1).toString().padStart(4, '0')
    }
}

class InMemoryClientRepository : ClientRepository {
    private val store = MutableStateFlow<Map<String, Client>>(emptyMap())

    override fun observeAll() = store.map { it.values.sortedBy { c -> c.name } }

    override suspend fun upsert(client: Client) = store.update { it + (client.id to client) }
}
