package app.invoicer.android

import app.invoicer.data.AppContainer
import app.invoicer.domain.model.Client
import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.InvoiceLine
import app.invoicer.domain.model.InvoiceStatus
import app.invoicer.domain.model.Money
import app.invoicer.domain.model.VatRate
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Sample data for debug builds only, so screens can be reviewed without typing everything in. */
object DemoData {
    private val clients = listOf(
        Client("demo-c1", "Bakker & Zn", "factuur@bakkerzn.nl", "12345678", "NL123456789B01", 14),
        Client("demo-c2", "Studio Noord", "accounting@studionoord.nl", "23456789", "NL234567890B01", 30),
        Client("demo-c3", "De Vries Consultancy", "info@devries-consultancy.nl", "34567890", "", 21),
        Client("demo-c4", "Jansen Bouw", "", "", "", 30),
        Client("demo-c5", "Amsterdam Fietsen", "hallo@amsterdamfietsen.nl", "45678901", "NL345678901B01", 14),
    )

    suspend fun seed(container: AppContainer) {
        if (container.clientRepository.observeAll().first().isNotEmpty()) return
        clients.forEach { container.clientRepository.upsert(it) }
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        data class Sample(val client: Int, val daysAgo: Int, val status: InvoiceStatus, val lines: List<InvoiceLine>)
        val samples = listOf(
            Sample(0, 50, InvoiceStatus.PAID, listOf(line("Website redesign", 24_000, 7_500, VatRate.HIGH))),
            Sample(1, 40, InvoiceStatus.SENT, listOf(line("UX workshop", 8_000, 9_500, VatRate.HIGH))),
            Sample(2, 20, InvoiceStatus.PAID, listOf(line("Consulting", 12_500, 8_500, VatRate.HIGH), line("Training materials", 1_000, 15_000, VatRate.LOW))),
            Sample(4, 5, InvoiceStatus.SENT, listOf(line("Logo and branding", 16_000, 7_000, VatRate.HIGH))),
            Sample(3, 1, InvoiceStatus.DRAFT, listOf(line("Site visit", 4_000, 6_500, VatRate.HIGH))),
        )
        samples.forEach { sample ->
            val client = clients[sample.client]
            val issue = today.minus(DatePeriod(days = sample.daysAgo))
            container.invoiceRepository.upsert(
                Invoice(
                    id = "demo-i${sample.daysAgo}",
                    number = container.invoiceRepository.nextNumber(issue.year),
                    clientId = client.id,
                    clientName = client.name,
                    issueDate = issue,
                    dueDate = issue.plus(DatePeriod(days = client.paymentTermDays)),
                    lines = sample.lines,
                    status = sample.status,
                ),
            )
        }
    }

    private fun line(description: String, quantityMilli: Long, rateCents: Long, vat: VatRate) =
        InvoiceLine(description, quantityMilli, Money(rateCents), vat)
}
