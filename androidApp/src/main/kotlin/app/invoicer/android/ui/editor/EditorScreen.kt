package app.invoicer.android.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.invoicer.domain.model.VatRate

@Composable
fun EditorScreen(viewModel: EditorViewModel, onClose: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onClose() }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("New invoice", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = state.clientName,
                onValueChange = viewModel::onClientName,
                label = { Text("Client") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
        }
        itemsIndexed(state.lines) { index, line ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = line.description,
                    onValueChange = { viewModel.onLine(index, line.copy(description = it)) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = line.quantity,
                        onValueChange = { viewModel.onLine(index, line.copy(quantity = it)) },
                        label = { Text("Qty / hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = line.unitPrice,
                        onValueChange = { viewModel.onLine(index, line.copy(unitPrice = it)) },
                        label = { Text("Rate (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VatRate.entries.forEach { rate ->
                        FilterChip(
                            selected = line.vatRate == rate,
                            onClick = { viewModel.onLine(index, line.copy(vatRate = rate)) },
                            label = { Text("${rate.percent}%") },
                        )
                    }
                    TextButton(onClick = { viewModel.removeLine(index) }) { Text("Remove") }
                }
            }
        }
        item {
            OutlinedButton(onClick = viewModel::addLine, modifier = Modifier.fillMaxWidth()) { Text("Add line") }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Subtotal: ${state.totals.net.format()}")
                state.totals.vatLines.forEach { Text("BTW ${it.rate.percent}%: ${it.vat.format()}") }
                Text("Total: ${state.totals.gross.format()}", style = MaterialTheme.typography.titleLarge)
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = viewModel::save, modifier = Modifier.weight(1f)) { Text("Save draft") }
            }
        }
    }
}
