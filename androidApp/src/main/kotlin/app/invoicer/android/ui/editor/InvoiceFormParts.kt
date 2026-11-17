package app.invoicer.android.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.invoicer.domain.model.InvoiceTotals
import app.invoicer.domain.model.VatRate

private fun String.decimalOnly() = filter { it.isDigit() || it == ',' || it == '.' }.take(12)

@Composable
fun ValidatedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    numeric: Boolean = false,
    prefix: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(if (numeric) it.decimalOnly() else it) },
        label = { Text(label) },
        prefix = prefix?.let { { Text(it) } },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
        modifier = modifier,
    )
}

@Composable
fun ClientField(state: EditorState, viewModel: EditorViewModel, modifier: Modifier = Modifier) {
    ValidatedField(
        value = state.clientName,
        onValueChange = viewModel::onClientName,
        label = "Client",
        error = state.clientError.takeIf { state.showErrors },
        modifier = modifier.fillMaxWidth(),
    )
}

/** Errors for numeric fields show as soon as something invalid is typed; required-errors wait for a save attempt. */
@Composable
fun LineFields(
    index: Int,
    line: LineInput,
    showErrors: Boolean,
    viewModel: EditorViewModel,
    showDescription: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showDescription) {
            ValidatedField(
                value = line.description,
                onValueChange = { viewModel.onLine(index, line.copy(description = it.take(120))) },
                label = "Description",
                error = line.descriptionError.takeIf { showErrors },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ValidatedField(
                value = line.quantity,
                onValueChange = { viewModel.onLine(index, line.copy(quantity = it)) },
                label = "Hours / qty",
                error = line.quantityError.takeIf { showErrors || line.quantity.isNotEmpty() },
                numeric = true,
                modifier = Modifier.weight(1f),
            )
            ValidatedField(
                value = line.unitPrice,
                onValueChange = { viewModel.onLine(index, line.copy(unitPrice = it)) },
                label = "Rate",
                error = line.rateError.takeIf { showErrors || line.unitPrice.isNotEmpty() },
                numeric = true,
                prefix = "€ ",
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VatRate.entries.forEach { rate ->
                FilterChip(
                    selected = line.vatRate == rate,
                    onClick = { viewModel.onLine(index, line.copy(vatRate = rate)) },
                    label = { Text("BTW ${rate.percent}%") },
                )
            }
        }
    }
}

@Composable
fun TotalsBlock(totals: InvoiceTotals, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        TotalRow("Subtotal", totals.net.format())
        totals.vatLines.forEach { TotalRow("BTW ${it.rate.percent}%", it.vat.format()) }
        TotalRow("Total", totals.gross.format(), emphasized = true)
    }
}

@Composable
private fun TotalRow(label: String, amount: String, emphasized: Boolean = false) {
    val style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style)
        Text(amount, style = style)
    }
}
