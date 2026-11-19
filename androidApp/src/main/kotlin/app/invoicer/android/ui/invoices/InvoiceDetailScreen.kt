package app.invoicer.android.ui.invoices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.invoicer.android.R
import app.invoicer.android.ui.editor.TotalsBlock
import app.invoicer.android.ui.labelRes
import app.invoicer.domain.calc.InvoiceCalculator
import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.InvoiceLine
import app.invoicer.domain.model.InvoiceStatus
import app.invoicer.domain.model.InvoiceWorkflow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
fun InvoiceDetailScreen(
    invoice: Invoice,
    onBack: () -> Unit,
    onOpenClient: (() -> Unit)?,
    onSetStatus: (InvoiceStatus) -> Unit,
    onSharePdf: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val overdue = invoice.isOverdue(today)
    val next = InvoiceWorkflow.next(invoice.status)
    val undo = InvoiceWorkflow.undo(invoice.status)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) }
            Text(invoice.number, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            StatusChip(if (overdue) R.string.status_overdue else invoice.status.labelRes(), error = overdue)
        }

        Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val clientModifier = if (onOpenClient != null) Modifier.clickable(onClick = onOpenClient) else Modifier
                InfoRow(stringResource(R.string.label_client), invoice.clientName, clientModifier, highlight = onOpenClient != null)
                InfoRow(stringResource(R.string.label_issue_date), invoice.issueDate.display())
                InfoRow(stringResource(R.string.label_due_date), invoice.dueDate.display(), highlight = overdue, highlightColor = MaterialTheme.colorScheme.error)
            }
        }

        Text(stringResource(R.string.label_lines), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                invoice.lines.forEach { LineRow(it) }
                HorizontalDivider()
                TotalsBlock(InvoiceCalculator.totals(invoice.lines))
            }
        }

        val hint = when (invoice.status) {
            InvoiceStatus.DRAFT -> R.string.draft_hint
            else -> R.string.locked_hint
        }
        Text(stringResource(hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))

        OutlinedButton(onClick = onSharePdf, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.share_pdf))
        }

        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (undo != null) {
                OutlinedButton(onClick = { onSetStatus(undo) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.undo_paid)) }
            }
            if (next != null) {
                val label = if (next == InvoiceStatus.PAID) R.string.mark_paid else R.string.mark_sent
                Button(onClick = { onSetStatus(next) }, modifier = Modifier.weight(1f)) { Text(stringResource(label)) }
            }
        }
    }
}

@Composable
private fun StatusChip(label: Int, error: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(stringResource(label), Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = if (highlight) highlightColor else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun LineRow(line: InvoiceLine) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(line.description, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${formatQuantity(line.quantityMilli)} × ${line.unitPrice.format()} · ${stringResource(R.string.vat_chip, line.vatRate.percent)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(InvoiceCalculator.lineNet(line).format(), style = MaterialTheme.typography.bodyLarge)
    }
}

private fun formatQuantity(milli: Long): String {
    val whole = milli / 1000
    val frac = (milli % 1000).toString().padStart(3, '0').trimEnd('0')
    return if (frac.isEmpty()) "$whole" else "$whole,$frac"
}

fun LocalDate.display(): String = "%02d-%02d-%d".format(dayOfMonth, monthNumber, year)
