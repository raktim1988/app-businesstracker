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
            Text("No invoices yet", Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.invoices, key = { it.id }) { InvoiceRow(it) }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNewInvoice,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("New invoice") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

@Composable
private fun InvoiceRow(invoice: Invoice) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(invoice.clientName, style = MaterialTheme.typography.titleMedium)
                Text("${invoice.number} · ${invoice.status}", style = MaterialTheme.typography.bodySmall)
            }
            Text(InvoiceCalculator.totals(invoice.lines).gross.format(), style = MaterialTheme.typography.titleMedium)
        }
    }
}
