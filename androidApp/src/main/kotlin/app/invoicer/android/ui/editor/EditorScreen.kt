package app.invoicer.android.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EditorScreen(viewModel: EditorViewModel, onClose: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onClose() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("New invoice", style = MaterialTheme.typography.headlineSmall)
        }
        item { ClientField(state, viewModel) }
        itemsIndexed(state.lines) { index, line ->
            LineFields(index, line, state.showErrors, viewModel)
            if (state.lines.size > 1) {
                TextButton(onClick = { viewModel.removeLine(index) }) { Text("Remove line") }
            }
            HorizontalDivider(Modifier.padding(top = 4.dp))
        }
        item {
            OutlinedButton(onClick = viewModel::addLine, modifier = Modifier.fillMaxWidth()) { Text("Add line") }
        }
        item { TotalsBlock(state.totals, Modifier.padding(vertical = 8.dp)) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = viewModel::save, modifier = Modifier.weight(1f)) { Text("Save draft") }
            }
        }
    }
}
