package app.invoicer.domain.model

/**
 * Status flow: Draft -> Sent -> Paid. A sent invoice keeps its number, so it can never go back to
 * Draft; a Paid mark can be undone (back to Sent) to fix a mistake.
 */
object InvoiceWorkflow {
    fun next(status: InvoiceStatus): InvoiceStatus? = when (status) {
        InvoiceStatus.DRAFT -> InvoiceStatus.SENT
        InvoiceStatus.SENT -> InvoiceStatus.PAID
        InvoiceStatus.PAID -> null
    }

    fun undo(status: InvoiceStatus): InvoiceStatus? = when (status) {
        InvoiceStatus.PAID -> InvoiceStatus.SENT
        else -> null
    }
}
