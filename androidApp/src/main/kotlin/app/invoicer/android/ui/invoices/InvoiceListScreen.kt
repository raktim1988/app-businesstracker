package app.invoicer.android.ui.invoices

import app.invoicer.android.ui.labelRes
import androidx.compose.ui.res.stringResource
import app.invoicer.android.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.invoicer.domain.model.InvoiceStatus
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
import androidx.compose.material.icons.filled.Receipt
import app.invoicer.android.ui.components.EmptyState
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
fun InvoiceListScreen(viewModel: InvoicesViewModel, onOpenInvoice: (Invoice) -> Unit, onNewInvoice: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fabLabel = stringResource(R.string.new_invoice_fab)
    val selected = rememberSaveable { mutableStateListOf<String>() }
    var confirm by rememberSaveable { mutableStateOf(false) }
    // Drop selections whose invoices disappeared (deleted here or by a sync).
    LaunchedEffect(state.invoices) { selected.retainAll(state.invoices.filter { it.status == InvoiceStatus.DRAFT }.map { it.id }.toSet()) }
    BackHandler(enabled = selected.isNotEmpty()) { selected.clear() }
    Box(Modifier.fillMaxSize()) {
        if (state.invoices.isEmpty()) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.invoices_title), style = MaterialTheme.typography.headlineMedium)
                EmptyState(Icons.Filled.Receipt, stringResource(R.string.empty_invoices_title), stringResource(R.string.empty_invoices_body))
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    if (selected.isEmpty()) {
                        Column {
                            Text(stringResource(R.string.invoices_title), style = MaterialTheme.typography.headlineMedium)
                            if (state.invoices.any { it.status == InvoiceStatus.DRAFT }) {
                                Text(
                                    stringResource(R.string.select_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.selected_count, selected.size),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { selected.clear() }) { Text(stringResource(R.string.cancel)) }
                            Button(
                                onClick = { confirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            ) { Text(stringResource(R.string.confirm_delete)) }
                        }
                    }
                }
                items(state.invoices, key = { it.id }) { invoice ->
                    val isDraft = invoice.status == InvoiceStatus.DRAFT
                    InvoiceRow(
                        invoice,
                        Modifier.combinedClickable(
                            onClick = {
                                when {
                                    selected.isEmpty() -> onOpenInvoice(invoice)
                                    isDraft -> if (invoice.id in selected) selected.remove(invoice.id) else selected.add(invoice.id)
                                }
                            },
                            onLongClick = { if (isDraft && invoice.id !in selected) selected.add(invoice.id) },
                        ),
                        selected = invoice.id in selected,
                    )
                }
            }
        }
        if (confirm) {
            AlertDialog(
                onDismissRequest = { confirm = false },
                title = { Text(stringResource(R.string.delete_selected_title, selected.size)) },
                text = { Text(stringResource(R.string.delete_selected_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        confirm = false
                        viewModel.deleteDrafts(selected.toList())
                        selected.clear()
                    }) { Text(stringResource(R.string.confirm_delete)) }
                },
                dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } },
            )
        }
        ExtendedFloatingActionButton(
            onClick = onNewInvoice,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.new_invoice_fab)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).semantics { contentDescription = fabLabel },
        )
    }
}

@Composable
fun InvoiceRow(invoice: Invoice, modifier: Modifier = Modifier, selected: Boolean = false) {
    Card(
        modifier.fillMaxWidth(),
        colors = if (selected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors(),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(invoice.clientName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${invoice.number} · ${stringResource(invoice.status.labelRes())}", style = MaterialTheme.typography.bodySmall)
            }
            Text(InvoiceCalculator.totals(invoice.lines).gross.format(), style = MaterialTheme.typography.titleMedium)
        }
    }
}
