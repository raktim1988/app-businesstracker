package app.invoicer.domain.model

import kotlinx.datetime.LocalDate

/** Dutch BTW (VAT) rates. */
enum class VatRate(val percent: Int) {
    ZERO(0),
    LOW(9),
    HIGH(21),
}

enum class InvoiceStatus { DRAFT, SENT, PAID }

data class BusinessProfile(
    val name: String = "",
    val kvkNumber: String = "",
    val btwNumber: String = "",
    val iban: String = "",
    val defaultPaymentTermDays: Int = 30,
)

data class Client(
    val id: String,
    val name: String,
    val email: String = "",
    val kvkNumber: String = "",
    val btwNumber: String = "",
    val paymentTermDays: Int = 30,
)

/** [quantityMilli] is the quantity in thousandths (1.5 hours = 1500). */
data class InvoiceLine(
    val description: String,
    val quantityMilli: Long,
    val unitPrice: Money,
    val vatRate: VatRate = VatRate.HIGH,
)

data class Invoice(
    val id: String,
    val number: String,
    val clientId: String,
    val clientName: String,
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val issueDate: LocalDate,
    val dueDate: LocalDate,
    val lines: List<InvoiceLine> = emptyList(),
) {
    fun isOverdue(today: LocalDate) = status == InvoiceStatus.SENT && dueDate < today
}

data class VatLine(val rate: VatRate, val net: Money, val vat: Money)

data class InvoiceTotals(
    val net: Money,
    val vatLines: List<VatLine>,
    val vat: Money,
    val gross: Money,
)
