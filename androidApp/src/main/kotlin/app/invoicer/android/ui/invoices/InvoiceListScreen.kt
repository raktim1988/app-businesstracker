package app.invoicer.android.ui.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.invoicer.domain.calc.InvoiceCalculator
import app.invoicer.domain.model.Invoice

@Composable
fun InvoiceListScreen(viewModel: InvoicesViewModel, onNewInvoice: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize()) {
        if (state.invoices.isEmpty()) {
            Column(Modifier.padding(16.dp)) {
                Text("Invoices", style = MaterialTheme.typography.headlineMedium)
                Text("No invoices yet", Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Text("Invoices", style = MaterialTheme.typography.headlineMedium) }
                items(state.invoices, key = { it.id }) { InvoiceRow(it) }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNewInvoice,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("New invoice") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).semantics { contentDescription = "New invoice" },
        )
    }
}

@Composable
fun InvoiceRow(invoice: Invoice) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(invoice.clientName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${invoice.number} · ${invoice.status}", style = MaterialTheme.typography.bodySmall)
            }
            Text(InvoiceCalculator.totals(invoice.lines).gross.format(), style = MaterialTheme.typography.titleMedium)
        }
    }
}
