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
import androidx.compose.ui.Alignment
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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Recent", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onOpenInvoices) { Text("See all") }
            }
        }
        if (invoices.invoices.isEmpty()) {
            item { GettingStartedCard() }
        } else {
            items(invoices.invoices.take(3), key = { it.id }) { InvoiceRow(it) }
        }
        item { Text("Good to know", style = MaterialTheme.typography.titleMedium) }
        items(TIPS) { TipCard(it.first, it.second) }
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

private val TIPS = listOf(
    "BTW rates" to "Standard 21%, reduced 9%, and 0% for exempt or reverse-charge sales. Pick one per line.",
    "Gap-free numbers" to "Invoice numbers run in sequence (2026-0001, 2026-0002…) with no gaps. Sent invoices are locked.",
    "Payment term" to "14 days is the common default. Add your IBAN so clients can pay straight away.",
    "Must be on every invoice" to "Your name, KvK and BTW number, the client, date, number, description, amount and BTW.",
)

@Composable
private fun GettingStartedCard() {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Get started in 3 steps", style = MaterialTheme.typography.titleMedium)
            Text("1. Fill in the quick invoice above")
            Text("2. Check the BTW and total")
            Text("3. Tap Create invoice, and it shows up here")
        }
    }
}

@Composable
private fun TipCard(title: String, body: String) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
