package app.invoicer.android.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.invoicer.android.R
import app.invoicer.domain.model.Client

@Composable
fun ClientsScreen(viewModel: ClientsViewModel, onNewClient: () -> Unit) {
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val fabLabel = stringResource(R.string.new_client_fab)
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Text(stringResource(R.string.clients_title), style = MaterialTheme.typography.headlineMedium) }
            if (clients.isEmpty()) {
                item {
                    Text(stringResource(R.string.no_clients), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(clients, key = { it.id }) { ClientCard(it) }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNewClient,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(fabLabel) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).semantics { contentDescription = fabLabel },
        )
    }
}

@Composable
private fun ClientCard(client: Client) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(client.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium)
            }
            Column(Modifier.weight(1f)) {
                Text(client.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (client.email.isNotBlank()) {
                    Text(client.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                val ids = listOfNotNull(
                    client.kvkNumber.takeIf { it.isNotBlank() }?.let { "KvK $it" },
                    client.btwNumber.takeIf { it.isNotBlank() }?.let { "BTW $it" },
                ).joinToString(" · ")
                if (ids.isNotEmpty()) {
                    Text(ids, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                stringResource(R.string.payment_term_days, client.paymentTermDays),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
