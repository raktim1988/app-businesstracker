package app.invoicer.android.ui.clients

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.invoicer.android.R
import app.invoicer.android.ui.invoices.InvoiceRow
import app.invoicer.domain.calc.ClientSummary
import app.invoicer.domain.model.Client
import app.invoicer.domain.model.Invoice

@Composable
fun ClientDetailScreen(
    client: Client,
    invoices: List<Invoice>,
    summary: ClientSummary,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNewInvoice: () -> Unit,
    onOpenInvoice: (Invoice) -> Unit,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var blocked by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) }
            Text(client.name, style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }

        Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(stringResource(R.string.label_email), client.email)
                DetailRow(stringResource(R.string.label_kvk), client.kvkNumber)
                DetailRow(stringResource(R.string.label_btw), client.btwNumber)
                DetailRow(stringResource(R.string.label_term), stringResource(R.string.payment_term_days, client.paymentTermDays))
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(stringResource(R.string.stat_invoices), summary.invoiceCount.toString(), Modifier.weight(1f))
            Stat(stringResource(R.string.stat_invoiced), summary.invoiced.format(), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(stringResource(R.string.stat_outstanding), summary.outstanding.format(), Modifier.weight(1f))
            Stat(stringResource(R.string.stat_overdue), summary.overdue.format(), Modifier.weight(1f), warn = summary.overdue.cents > 0)
        }

        Button(onClick = onNewInvoice, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(stringResource(R.string.new_invoice_for_client)) }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.edit_client)) }
            OutlinedButton(
                onClick = { if (invoices.isEmpty()) confirmDelete = true else blocked = true },
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.delete_client)) }
        }
        if (blocked) {
            Text(stringResource(R.string.delete_client_blocked), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }

        Text(stringResource(R.string.client_invoices), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        if (invoices.isEmpty()) {
            Text(stringResource(R.string.no_client_invoices), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                invoices.forEach { InvoiceRow(it, Modifier.clickable { onOpenInvoice(it) }) }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_client_title)) },
            text = { Text(stringResource(R.string.delete_client_body)) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text(stringResource(R.string.confirm_delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "–" }, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier, warn: Boolean = false) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(value, style = MaterialTheme.typography.titleMedium, color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, maxLines = 1)
        }
    }
}
