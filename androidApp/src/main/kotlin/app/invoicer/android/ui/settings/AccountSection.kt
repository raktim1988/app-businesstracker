package app.invoicer.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.invoicer.android.InvoicerApp
import app.invoicer.android.R
import app.invoicer.android.cloud.AccountViewModel
import app.invoicer.android.ui.components.InfoCard

@Composable
fun AccountSection(viewModel: AccountViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sync = (LocalContext.current.applicationContext as InvoicerApp).sync
    val syncState by sync.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Text(stringResource(R.string.account_title), style = MaterialTheme.typography.titleMedium)
    when {
        !state.configured -> Text(
            stringResource(R.string.account_not_configured),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        state.loading -> Unit
        state.email != null -> {
            InfoCard(stringResource(R.string.account_signed_in), state.email.orEmpty())
            Text(
                when {
                    syncState.syncing -> stringResource(R.string.sync_running)
                    syncState.failed -> stringResource(R.string.sync_failed)
                    syncState.lastSyncMillis != null -> stringResource(
                        R.string.sync_last,
                        android.text.format.DateFormat.getTimeFormat(LocalContext.current).format(syncState.lastSyncMillis),
                    )
                    else -> stringResource(R.string.sync_never)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (syncState.failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = sync::syncNow, enabled = !syncState.syncing) { Text(stringResource(R.string.sync_now)) }
                OutlinedButton(onClick = viewModel::signOut) { Text(stringResource(R.string.account_sign_out)) }
            }
        }
        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.account_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text(stringResource(R.string.account_email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.account_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.errorRes != null,
                supportingText = state.errorRes?.let { { Text(stringResource(it)) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { viewModel.signIn(email, password) }, enabled = !state.busy) {
                    Text(stringResource(R.string.account_sign_in))
                }
                OutlinedButton(onClick = { viewModel.signUp(email, password) }, enabled = !state.busy) {
                    Text(stringResource(R.string.account_create))
                }
            }
        }
    }
}
