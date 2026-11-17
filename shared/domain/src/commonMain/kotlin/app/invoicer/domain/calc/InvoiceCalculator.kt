package app.invoicer.domain.calc

import app.invoicer.domain.model.InvoiceLine
import app.invoicer.domain.model.InvoiceTotals
import app.invoicer.domain.model.Money
import app.invoicer.domain.model.VatLine
import app.invoicer.domain.model.VatRate

object InvoiceCalculator {

    fun lineNet(line: InvoiceLine): Money =
        Money(divideRoundHalfUp(line.quantityMilli * line.unitPrice.cents, 1000))

    /** VAT is rounded once per rate group, not per line, to avoid accumulated rounding error. */
    fun totals(lines: List<InvoiceLine>): InvoiceTotals {
        val vatLines = lines
            .groupBy { it.vatRate }
            .toList()
            .sortedBy { (rate, _) -> rate.percent }
            .map { (rate, group) ->
                val net = group.fold(Money.ZERO) { acc, line -> acc + lineNet(line) }
                VatLine(rate, net, Money(divideRoundHalfUp(net.cents * rate.percent, 100)))
            }
        val net = vatLines.fold(Money.ZERO) { acc, l -> acc + l.net }
        val vat = vatLines.fold(Money.ZERO) { acc, l -> acc + l.vat }
        return InvoiceTotals(net, vatLines, vat, net + vat)
    }

    private fun divideRoundHalfUp(numerator: Long, denominator: Long): Long {
        val half = denominator / 2
        return if (numerator >= 0) (numerator + half) / denominator else (numerator - half) / denominator
    }
}
