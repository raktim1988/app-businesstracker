package app.invoicer.android.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import app.invoicer.android.R
import app.invoicer.android.ui.invoices.display
import app.invoicer.domain.calc.InvoiceCalculator
import app.invoicer.domain.model.BusinessProfile
import app.invoicer.domain.model.Client
import app.invoicer.domain.model.Invoice
import java.io.File

/** Draws an A4 invoice with the platform PdfDocument API (no extra dependencies). */
object InvoicePdf {
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40f

    fun create(context: Context, invoice: Invoice, client: Client?, business: BusinessProfile): File {
        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        // Old exports are only needed until they are shared.
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "${context.getString(R.string.pdf_invoice).lowercase()}-${invoice.number}.pdf")
        val doc = PdfDocument()
        try {
            Renderer(context, doc, invoice, client, business).render()
            file.outputStream().use { doc.writeTo(it) }
        } finally {
            doc.close()
        }
        return file
    }

    fun shareIntent(context: Context, file: File, invoice: Invoice, client: Client?, business: BusinessProfile): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject, invoice.number))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.email_body, invoice.number, business.name.ifBlank { "" }))
            client?.email?.takeIf { it.isNotBlank() }?.let { putExtra(Intent.EXTRA_EMAIL, arrayOf(it)) }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(intent, context.getString(R.string.share_chooser)).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private class Renderer(
        val context: Context,
        val doc: PdfDocument,
        val invoice: Invoice,
        val client: Client?,
        val business: BusinessProfile,
    ) {
        private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1B1B1F.toInt(); textSize = 10f }
        private val muted = Paint(ink).apply { color = 0xFF5F6368.toInt(); textSize = 9f }
        private val bold = Paint(ink).apply { typeface = Typeface.DEFAULT_BOLD }
        private val title = Paint(bold).apply { textSize = 22f; color = 0xFF0B57D0.toInt() }
        private val rule = Paint().apply { color = 0xFFD0D5DD.toInt(); strokeWidth = 0.8f }
        private val right = Paint(ink).apply { textAlign = Paint.Align.RIGHT }
        private val rightBold = Paint(bold).apply { textAlign = Paint.Align.RIGHT }
        private val rightMuted = Paint(muted).apply { textAlign = Paint.Align.RIGHT }

        private var page: PdfDocument.Page? = null
        private var canvas: Canvas = Canvas()
        private var pageNumber = 0
        private var y = 0f

        private val colAmount = PAGE_W - MARGIN
        private val colVat = colAmount - 80
        private val colRate = colVat - 50
        private val colQty = colRate - 70
        private val descWidth = colQty - MARGIN - 40

        fun render() {
            newPage()
            header()
            billTo()
            tableHeader()
            invoice.lines.forEach { line ->
                val desc = wrap(line.description, descWidth)
                ensureSpace(desc.size * 13f + 8f)
                desc.forEachIndexed { i, text -> canvas.drawText(text, MARGIN, y + 10 + i * 13f, ink) }
                canvas.drawText(quantity(line.quantityMilli), colQty, y + 10, right)
                canvas.drawText(line.unitPrice.format(), colRate, y + 10, right)
                canvas.drawText("${line.vatRate.percent}%", colVat, y + 10, right)
                canvas.drawText(InvoiceCalculator.lineNet(line).format(), colAmount, y + 10, right)
                y += desc.size * 13f + 8f
            }
            totals()
            footer()
            finishPage()
        }

        private fun newPage() {
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
            canvas = page!!.canvas
            y = MARGIN
        }

        private fun finishPage() {
            page?.let { doc.finishPage(it) }
            page = null
        }

        private fun ensureSpace(height: Float) {
            if (y + height > PAGE_H - 60f) {
                finishPage()
                newPage()
                tableHeader()
            }
        }

        private fun header() {
            var ly = y + 10
            if (business.name.isNotBlank()) {
                canvas.drawText(business.name, MARGIN, ly, bold.apply { textSize = 13f }); ly += 15
            }
            val details = buildList {
                if (business.address.isNotBlank()) add(business.address)
                if (business.kvkNumber.isNotBlank()) add("KvK ${business.kvkNumber}")
                if (business.btwNumber.isNotBlank()) add("BTW ${business.btwNumber}")
                if (business.iban.isNotBlank()) add("IBAN ${business.iban}")
            }
            details.forEach { canvas.drawText(it, MARGIN, ly, muted); ly += 12 }

            val rx = PAGE_W - MARGIN
            canvas.drawText(context.getString(R.string.pdf_invoice), rx, y + 24, title.apply { textAlign = Paint.Align.RIGHT })
            canvas.drawText(invoice.number, rx, y + 42, rightBold)
            canvas.drawText("${context.getString(R.string.label_issue_date)}: ${invoice.issueDate.display()}", rx, y + 58, rightMuted)
            canvas.drawText("${context.getString(R.string.label_due_date)}: ${invoice.dueDate.display()}", rx, y + 70, rightMuted)
            y = maxOf(ly, y + 80) + 16
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, rule)
            y += 18
        }

        private fun billTo() {
            canvas.drawText(context.getString(R.string.pdf_bill_to).uppercase(), MARGIN, y, muted)
            y += 14
            canvas.drawText(invoice.clientName, MARGIN, y, bold.apply { textSize = 11f })
            y += 13
            client?.let { c ->
                listOfNotNull(
                    c.email.takeIf { it.isNotBlank() },
                    c.kvkNumber.takeIf { it.isNotBlank() }?.let { "KvK $it" },
                    c.btwNumber.takeIf { it.isNotBlank() }?.let { "BTW $it" },
                ).forEach { canvas.drawText(it, MARGIN, y, muted); y += 12 }
            }
            y += 16
        }

        private fun tableHeader() {
            canvas.drawText(context.getString(R.string.pdf_description), MARGIN, y + 10, muted)
            canvas.drawText(context.getString(R.string.pdf_qty), colQty, y + 10, rightMuted)
            canvas.drawText(context.getString(R.string.pdf_rate), colRate, y + 10, rightMuted)
            canvas.drawText("BTW", colVat, y + 10, rightMuted)
            canvas.drawText(context.getString(R.string.pdf_amount), colAmount, y + 10, rightMuted)
            y += 16
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, rule)
            y += 8
        }

        private fun totals() {
            val t = InvoiceCalculator.totals(invoice.lines)
            ensureSpace(24f + t.vatLines.size * 14f + 30f)
            y += 4
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, rule)
            y += 16
            fun row(label: String, amount: String, paint: Paint, labelPaint: Paint) {
                canvas.drawText(label, colVat - 60, y, labelPaint.apply { textAlign = Paint.Align.RIGHT })
                canvas.drawText(amount, colAmount, y, paint)
                y += 15
            }
            row(context.getString(R.string.subtotal), t.net.format(), right, right)
            t.vatLines.forEach { row(context.getString(R.string.vat_chip, it.rate.percent), it.vat.format(), right, right) }
            row(context.getString(R.string.total), t.gross.format(), rightBold.apply { textSize = 12f }, rightBold)
        }

        private fun footer() {
            if (business.iban.isBlank()) return
            ensureSpace(40f)
            y += 20
            val total = InvoiceCalculator.totals(invoice.lines).gross.format()
            val text = context.getString(R.string.pdf_pay_to, total, invoice.dueDate.display(), business.iban, invoice.number)
            wrap(text, PAGE_W - 2 * MARGIN).forEach { canvas.drawText(it, MARGIN, y, muted); y += 12 }
        }

        private fun wrap(text: String, width: Float): List<String> {
            val lines = mutableListOf<String>()
            var current = ""
            for (word in text.split(' ')) {
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (ink.measureText(candidate) <= width) {
                    current = candidate
                } else {
                    if (current.isNotEmpty()) lines += current
                    current = word
                }
            }
            if (current.isNotEmpty()) lines += current
            return lines.ifEmpty { listOf("") }
        }

        private fun quantity(milli: Long): String {
            val frac = (milli % 1000).toString().padStart(3, '0').trimEnd('0')
            return if (frac.isEmpty()) "${milli / 1000}" else "${milli / 1000},$frac"
        }
    }
}
