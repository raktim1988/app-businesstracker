package app.invoicer.android.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.invoicer.android.R
import app.invoicer.android.ui.editor.ValidatedField
import app.invoicer.domain.model.Client
import app.invoicer.domain.validation.FieldValidation
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ClientFormScreen(onSave: (Client) -> Unit, onClose: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var kvk by rememberSaveable { mutableStateOf("") }
    var btw by rememberSaveable { mutableStateOf("") }
    var term by rememberSaveable { mutableStateOf("30") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val nameError = FieldValidation.client(name)
    val emailError = FieldValidation.email(email)
    val kvkError = FieldValidation.kvk(kvk)
    val btwError = FieldValidation.btw(btw)
    val termError = FieldValidation.paymentTermDays(term)
    val valid = listOf(nameError, emailError, kvkError, btwError, termError).all { it == null }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Text(stringResource(R.string.client_form_title), style = MaterialTheme.typography.headlineSmall) }
        item {
            ValidatedField(
                value = name,
                onValueChange = { name = it.filter(FieldValidation::isClientChar).take(80) },
                label = stringResource(R.string.field_client),
                error = nameError.takeIf { showErrors },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            ValidatedField(
                value = email,
                onValueChange = { email = it.filterNot(Char::isWhitespace).take(80) },
                label = stringResource(R.string.field_email),
                error = emailError.takeIf { showErrors || email.isNotEmpty() },
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ValidatedField(
                    value = kvk,
                    onValueChange = { kvk = it.filter(Char::isDigit).take(8) },
                    label = stringResource(R.string.field_kvk),
                    error = kvkError.takeIf { showErrors },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
                ValidatedField(
                    value = term,
                    onValueChange = { term = it.filter(Char::isDigit).take(3) },
                    label = stringResource(R.string.field_payment_term),
                    error = termError.takeIf { showErrors || term.isNotEmpty() },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            ValidatedField(
                value = btw,
                onValueChange = { btw = it.filter(Char::isLetterOrDigit).uppercase().take(14) },
                label = stringResource(R.string.field_btw),
                error = btwError.takeIf { showErrors },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = {
                        if (!valid) {
                            showErrors = true
                        } else {
                            onSave(
                                Client(
                                    id = Uuid.random().toString(),
                                    name = name.trim(),
                                    email = email.trim(),
                                    kvkNumber = kvk.trim(),
                                    btwNumber = btw.trim(),
                                    paymentTermDays = term.trim().toInt(),
                                ),
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.save_client)) }
            }
        }
    }
}
