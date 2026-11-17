package app.invoicer.domain.calc

import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.InvoiceStatus
import app.invoicer.domain.model.Money
import kotlinx.datetime.LocalDate

data class DashboardSummary(
    val outstanding: Money,
    val overdue: Money,
    val paidThisMonth: Money,
)

object DashboardCalculator {
    fun summarize(invoices: List<Invoice>, today: LocalDate): DashboardSummary {
        fun gross(i: Invoice) = InvoiceCalculator.totals(i.lines).gross
        val sent = invoices.filter { it.status == InvoiceStatus.SENT }
        return DashboardSummary(
            outstanding = sent.fold(Money.ZERO) { acc, i -> acc + gross(i) },
            overdue = sent.filter { it.isOverdue(today) }.fold(Money.ZERO) { acc, i -> acc + gross(i) },
            paidThisMonth = invoices
                .filter { it.status == InvoiceStatus.PAID && it.issueDate.year == today.year && it.issueDate.month == today.month }
                .fold(Money.ZERO) { acc, i -> acc + gross(i) },
        )
    }
}
