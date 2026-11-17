package app.invoicer.android.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.invoicer.android.ui.invoices.InvoicesViewModel
import app.invoicer.domain.model.Money

@Composable
fun DashboardScreen(viewModel: InvoicesViewModel, onNewInvoice: () -> Unit) {
    val summary by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        SummaryCard("Outstanding", summary.summary.outstanding)
        SummaryCard("Overdue", summary.summary.overdue)
        SummaryCard("Paid this month", summary.summary.paidThisMonth)
        Button(onClick = onNewInvoice, modifier = Modifier.fillMaxWidth()) { Text("New invoice") }
    }
}

@Composable
private fun SummaryCard(label: String, amount: Money) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(amount.format(), style = MaterialTheme.typography.headlineSmall)
        }
    }
}
