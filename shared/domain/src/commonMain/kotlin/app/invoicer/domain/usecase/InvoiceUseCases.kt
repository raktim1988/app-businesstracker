package app.invoicer.domain.usecase

import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.InvoiceStatus
import app.invoicer.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow

fun interface ObserveInvoicesUseCase {
    operator fun invoke(): Flow<List<Invoice>>
}

fun interface SaveInvoiceUseCase {
    /** Returns null on success, or a validation message. */
    suspend operator fun invoke(invoice: Invoice): String?
}

class ObserveInvoicesUseCaseImpl(private val repository: InvoiceRepository) : ObserveInvoicesUseCase {
    override fun invoke() = repository.observeAll()
}

class SaveInvoiceUseCaseImpl(private val repository: InvoiceRepository) : SaveInvoiceUseCase {
    override suspend fun invoke(invoice: Invoice): String? {
        val existing = repository.get(invoice.id)
        return when {
            invoice.clientName.isBlank() -> "Client is required"
            invoice.lines.isEmpty() -> "Add at least one line"
            invoice.lines.any { it.description.isBlank() || it.quantityMilli <= 0 } ->
                "Every line needs a description and quantity"
            invoice.dueDate < invoice.issueDate -> "Due date is before issue date"
            existing != null && existing.status != InvoiceStatus.DRAFT -> "A sent invoice cannot be edited"
            else -> {
                repository.upsert(invoice)
                null
            }
        }
    }
}
