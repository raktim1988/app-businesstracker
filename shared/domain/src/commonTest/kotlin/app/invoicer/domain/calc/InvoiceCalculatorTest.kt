package app.invoicer.domain.calc

import app.invoicer.domain.model.InvoiceLine
import app.invoicer.domain.model.Money
import app.invoicer.domain.model.VatRate
import kotlin.test.Test
import kotlin.test.assertEquals

class InvoiceCalculatorTest {

    @Test
    fun `hours times rate with 21 percent vat`() {
        val totals = InvoiceCalculator.totals(
            listOf(InvoiceLine("Consulting", 7500, Money(8500), VatRate.HIGH)),
        )
        assertEquals(Money(63750), totals.net)
        assertEquals(Money(13388), totals.vat) // 13387.5 rounds half up
        assertEquals(Money(77138), totals.gross)
    }

    @Test
    fun `vat is rounded per rate group not per line`() {
        val lines = List(3) { InvoiceLine("Item", 1000, Money(333), VatRate.LOW) }
        val totals = InvoiceCalculator.totals(lines)
        assertEquals(Money(999), totals.net)
        assertEquals(Money(90), totals.vat) // 89.91 once, not 3 x 30
    }

    @Test
    fun `mixed rates are grouped and sorted`() {
        val totals = InvoiceCalculator.totals(
            listOf(
                InvoiceLine("A", 1000, Money(10000), VatRate.HIGH),
                InvoiceLine("B", 1000, Money(10000), VatRate.ZERO),
            ),
        )
        assertEquals(listOf(VatRate.ZERO, VatRate.HIGH), totals.vatLines.map { it.rate })
        assertEquals(Money(2100), totals.vat)
        assertEquals(Money(22100), totals.gross)
    }

    @Test
    fun `money formats in dutch style`() {
        assertEquals("€ 1.234,56", Money(123456).format())
        assertEquals("€ 0,05", Money(5).format())
    }
}
