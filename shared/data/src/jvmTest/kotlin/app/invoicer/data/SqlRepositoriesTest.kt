package app.invoicer.data

import app.invoicer.domain.model.Client
import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.InvoiceLine
import app.invoicer.domain.model.InvoiceStatus
import app.invoicer.domain.model.Money
import app.invoicer.domain.model.VatRate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SqlRepositoriesTest {
    private val db = createInMemoryDatabase()
    private val invoices = SqlInvoiceRepository(db)
    private val clients = SqlClientRepository(db)

    private fun invoice(id: String, number: String, status: InvoiceStatus = InvoiceStatus.DRAFT) = Invoice(
        id = id,
        number = number,
        clientId = "c1",
        clientName = "Bakker & Zn",
        status = status,
        issueDate = LocalDate(2026, 11, 1),
        dueDate = LocalDate(2026, 11, 15),
        lines = listOf(
            InvoiceLine("Design", 1_500, Money(7_500), VatRate.HIGH),
            InvoiceLine("Book", 1_000, Money(2_000), VatRate.LOW),
        ),
    )

    @Test
    fun invoiceRoundTripKeepsLinesInOrder() = runBlocking {
        val inv = invoice("i1", "2026-0001")
        invoices.upsert(inv)
        assertEquals(inv, invoices.get("i1"))
        assertEquals(listOf(inv), invoices.observeAll().first())
    }

    @Test
    fun upsertReplacesLinesAndStatus() = runBlocking {
        invoices.upsert(invoice("i1", "2026-0001"))
        val updated = invoice("i1", "2026-0001", InvoiceStatus.SENT).copy(lines = listOf(InvoiceLine("Only", 1_000, Money(100))))
        invoices.upsert(updated)
        assertEquals(updated, invoices.get("i1"))
    }

    @Test
    fun missingInvoiceIsNull() = runBlocking { assertNull(invoices.get("nope")) }

    @Test
    fun nextNumberIsGapFreeAndPerYear() = runBlocking {
        assertEquals("2026-0001", invoices.nextNumber(2026))
        invoices.upsert(invoice("i1", "2026-0001"))
        invoices.upsert(invoice("i2", "2026-0002"))
        assertEquals("2026-0003", invoices.nextNumber(2026))
        assertEquals("2027-0001", invoices.nextNumber(2027))
    }

    @Test
    fun clientsAreSortedByNameIgnoringCase() = runBlocking {
        clients.upsert(Client("b", "bravo", paymentTermDays = 14))
        clients.upsert(Client("a", "Alpha", "a@x.nl", "12345678", "NL123456789B01", 30))
        assertEquals(listOf("Alpha", "bravo"), clients.observeAll().first().map { it.name })
        assertEquals(14, clients.observeAll().first().last().paymentTermDays)
    }
}
