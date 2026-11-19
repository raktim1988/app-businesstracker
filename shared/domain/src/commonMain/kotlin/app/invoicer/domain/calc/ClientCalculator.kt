package app.invoicer.domain.calc

import app.invoicer.domain.model.Client
import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.InvoiceStatus
import app.invoicer.domain.model.Money
import kotlinx.datetime.LocalDate

/** Invoices typed by name only have no clientId, so fall back to a case-insensitive name match. */
fun Invoice.belongsTo(client: Client): Boolean =
    if (clientId.isNotEmpty()) clientId == client.id else clientName.trim().equals(client.name.trim(), ignoreCase = true)

data class ClientSummary(
    val invoiceCount: Int,
    val invoiced: Money,
    val outstanding: Money,
    val overdue: Money,
)

object ClientCalculator {
    /** Drafts are not counted as invoiced: they have not been sent yet. */
    fun summarize(invoices: List<Invoice>, today: LocalDate): ClientSummary {
        fun gross(i: Invoice) = InvoiceCalculator.totals(i.lines).gross
        fun sum(list: List<Invoice>) = list.fold(Money.ZERO) { acc, i -> acc + gross(i) }
        val sent = invoices.filter { it.status == InvoiceStatus.SENT }
        return ClientSummary(
            invoiceCount = invoices.size,
            invoiced = sum(invoices.filter { it.status != InvoiceStatus.DRAFT }),
            outstanding = sum(sent),
            overdue = sum(sent.filter { it.isOverdue(today) }),
        )
    }
}
