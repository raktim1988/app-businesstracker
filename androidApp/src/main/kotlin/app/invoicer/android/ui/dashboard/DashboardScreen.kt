package app.invoicer.android.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.invoicer.android.ui.editor.ClientField
import app.invoicer.android.ui.editor.EditorViewModel
import app.invoicer.android.ui.editor.LineFields
import app.invoicer.android.ui.editor.TotalsBlock
import app.invoicer.android.ui.invoices.InvoiceRow
import app.invoicer.android.ui.invoices.InvoicesViewModel
import app.invoicer.domain.model.Money

@Composable
fun DashboardScreen(
    invoicesViewModel: InvoicesViewModel,
    editorViewModel: EditorViewModel,
    onOpenInvoices: () -> Unit,
    onOpenEditor: () -> Unit,
) {
    val invoices by invoicesViewModel.state.collectAsStateWithLifecycle()
    val editor by editorViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(editor.saved) { if (editor.saved) editorViewModel.reset() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text("Invoicer", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Invoices & hours for freelancers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Outstanding", invoices.summary.outstanding, Modifier.weight(1f))
                StatCard("Overdue", invoices.summary.overdue, Modifier.weight(1f))
                StatCard("Paid (month)", invoices.summary.paidThisMonth, Modifier.weight(1f))
            }
        }
        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Quick invoice", style = MaterialTheme.typography.titleMedium)
                    ClientField(editor, editorViewModel, Modifier.padding(top = 8.dp))
                    LineFields(0, editor.lines[0], editor.showErrors, editorViewModel)
                    TotalsBlock(editor.totals, Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onOpenEditor) { Text("More lines…") }
                        Button(onClick = editorViewModel::save, modifier = Modifier.weight(1f)) {
                            Text("Create invoice")
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Recent", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onOpenInvoices) { Text("See all") }
            }
        }
        if (invoices.invoices.isEmpty()) {
            item { Text("No invoices yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(invoices.invoices.take(3), key = { it.id }) { InvoiceRow(it) }
        }
    }
}

@Composable
private fun StatCard(label: String, amount: Money, modifier: Modifier = Modifier) {
    OutlinedCard(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(amount.format(), style = MaterialTheme.typography.titleSmall, maxLines = 1)
        }
    }
}
