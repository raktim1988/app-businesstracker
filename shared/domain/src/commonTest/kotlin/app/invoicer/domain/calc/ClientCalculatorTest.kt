package app.invoicer.domain.calc

import app.invoicer.domain.model.Client
import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.InvoiceLine
import app.invoicer.domain.model.InvoiceStatus
import app.invoicer.domain.model.InvoiceWorkflow
import app.invoicer.domain.model.Money
import app.invoicer.domain.model.VatRate
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClientCalculatorTest {
    private val today = LocalDate(2026, 11, 18)

    private fun inv(status: InvoiceStatus, cents: Long, due: LocalDate, clientId: String = "c1", name: String = "Acme") = Invoice(
        id = "$status$cents", number = "n", clientId = clientId, clientName = name, status = status,
        issueDate = LocalDate(2026, 10, 1), dueDate = due,
        lines = listOf(InvoiceLine("x", 1_000, Money(cents), VatRate.ZERO)),
    )

    @Test
    fun summarySkipsDraftsAndSplitsOverdue() {
        val s = ClientCalculator.summarize(
            listOf(
                inv(InvoiceStatus.DRAFT, 100, LocalDate(2026, 12, 1)),
                inv(InvoiceStatus.SENT, 200, LocalDate(2026, 11, 1)),
                inv(InvoiceStatus.SENT, 300, LocalDate(2026, 12, 1)),
                inv(InvoiceStatus.PAID, 400, LocalDate(2026, 10, 15)),
            ),
            today,
        )
        assertEquals(4, s.invoiceCount)
        assertEquals(Money(900), s.invoiced)
        assertEquals(Money(500), s.outstanding)
        assertEquals(Money(200), s.overdue)
    }

    @Test
    fun belongsToUsesIdThenFallsBackToName() {
        val client = Client("c1", "Acme")
        assertTrue(inv(InvoiceStatus.SENT, 1, today).belongsTo(client))
        assertTrue(inv(InvoiceStatus.SENT, 1, today, clientId = "", name = " acme ").belongsTo(client))
        assertFalse(inv(InvoiceStatus.SENT, 1, today, clientId = "other").belongsTo(client))
    }

    @Test
    fun workflowNeverReturnsSentInvoiceToDraft() {
        assertEquals(InvoiceStatus.SENT, InvoiceWorkflow.next(InvoiceStatus.DRAFT))
        assertEquals(InvoiceStatus.PAID, InvoiceWorkflow.next(InvoiceStatus.SENT))
        assertNull(InvoiceWorkflow.next(InvoiceStatus.PAID))
        assertNull(InvoiceWorkflow.undo(InvoiceStatus.SENT))
        assertEquals(InvoiceStatus.SENT, InvoiceWorkflow.undo(InvoiceStatus.PAID))
    }
}
