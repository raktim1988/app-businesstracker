package app.invoicer.android.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.invoicer.android.R
import androidx.compose.material3.Switch
import androidx.fragment.app.FragmentActivity
import app.invoicer.android.security.AppLock
import app.invoicer.android.ui.LanguagePrefs
import app.invoicer.android.ui.components.InfoCard
import app.invoicer.android.ui.editor.ValidatedField
import app.invoicer.domain.model.BusinessProfile
import app.invoicer.domain.validation.FieldValidation
import androidx.compose.material3.Button
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.material3.TextButton
import app.invoicer.android.ui.ThemeMode
import app.invoicer.android.ui.ThemePrefs
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun SettingsScreen(profileViewModel: BusinessProfileViewModel) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(LanguagePrefs.get(context)) }
    val options = listOf(
        null to R.string.language_system,
        "en" to R.string.language_en,
        "nl" to R.string.language_nl,
    )

    val profile by profileViewModel.profile.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
        profile?.let { BusinessSection(it, profileViewModel::save) }
        LockSection()
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        ThemeSection()
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup()) {
            options.forEach { (code, label) ->
                Row(
                    Modifier.fillMaxWidth().selectable(
                        selected = selected == code,
                        role = Role.RadioButton,
                        onClick = {
                            if (selected != code) {
                                selected = code
                                LanguagePrefs.set(context, code)
                                (context as? Activity)?.recreate()
                            }
                        },
                    ).padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == code, onClick = null)
                    Text(stringResource(label), Modifier.padding(start = 12.dp))
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(stringResource(R.string.help_title), style = MaterialTheme.typography.titleMedium)
        InfoCard(stringResource(R.string.help_kor_title), stringResource(R.string.help_kor_body))
        InfoCard(stringResource(R.string.help_eu_title), stringResource(R.string.help_eu_body))
        InfoCard(stringResource(R.string.help_keep_title), stringResource(R.string.help_keep_body))
        Text(stringResource(R.string.help_disclaimer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(stringResource(R.string.about_title), style = MaterialTheme.typography.titleMedium)
        val version = remember { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
        Text(stringResource(R.string.about_version, version), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.about_privacy), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BusinessSection(saved: BusinessProfile, onSave: (BusinessProfile) -> Unit) {
    var name by rememberSaveable { mutableStateOf(saved.name) }
    var address by rememberSaveable { mutableStateOf(saved.address) }
    var kvk by rememberSaveable { mutableStateOf(saved.kvkNumber) }
    var btw by rememberSaveable { mutableStateOf(saved.btwNumber) }
    var iban by rememberSaveable { mutableStateOf(saved.iban) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var justSaved by rememberSaveable { mutableStateOf(false) }
    // Collapsed once a profile has been saved; open on first use so setup is obvious.
    var expanded by rememberSaveable { mutableStateOf(saved.name.isBlank() && saved.address.isBlank()) }

    // Name is optional here so the app stays usable before setup; the PDF falls back to a blank header.
    val nameError = FieldValidation.client(name).takeIf { name.isNotBlank() }
    val kvkError = FieldValidation.kvk(kvk)
    val btwError = FieldValidation.btw(btw)
    val ibanError = FieldValidation.iban(iban)
    val valid = listOf(nameError, kvkError, btwError, ibanError).all { it == null }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.business_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (justSaved && !expanded) Text(stringResource(R.string.saved), color = MaterialTheme.colorScheme.primary)
        TextButton(onClick = { expanded = !expanded }) {
            Text(stringResource(if (expanded) R.string.business_collapse else R.string.business_edit))
        }
    }
    if (!expanded) {
        val lines = listOf(
            saved.name, saved.address,
            listOf(saved.kvkNumber, saved.btwNumber).filter { it.isNotBlank() }.joinToString(" · "),
            saved.iban,
        ).filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            Text(stringResource(R.string.business_not_set), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            InfoCard(saved.name.ifBlank { stringResource(R.string.business_title) }, lines.drop(if (saved.name.isNotBlank()) 1 else 0).joinToString("\n"))
        }
    } else {
    Text(stringResource(R.string.business_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    ValidatedField(name, { name = it.filter(FieldValidation::isClientChar).take(80); justSaved = false }, stringResource(R.string.field_business_name), nameError, Modifier.fillMaxWidth())
    ValidatedField(address, { address = it.replace('\n', ' ').take(120); justSaved = false }, stringResource(R.string.field_address), null, Modifier.fillMaxWidth())
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ValidatedField(kvk, { kvk = it.filter(Char::isDigit).take(8); justSaved = false }, stringResource(R.string.label_kvk), kvkError.takeIf { showErrors }, Modifier.weight(1f), keyboardType = KeyboardType.Number)
        ValidatedField(btw, { btw = it.filter(Char::isLetterOrDigit).uppercase().take(14); justSaved = false }, stringResource(R.string.label_btw), btwError.takeIf { showErrors }, Modifier.weight(1.4f))
    }
    ValidatedField(iban, { iban = it.filter { c -> c.isLetterOrDigit() || c == ' ' }.uppercase().take(42); justSaved = false }, stringResource(R.string.field_iban), ibanError.takeIf { showErrors }, Modifier.fillMaxWidth())
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = {
            if (!valid) {
                showErrors = true
            } else {
                onSave(saved.copy(name = name.trim(), address = address.trim(), kvkNumber = kvk, btwNumber = btw, iban = iban.trim()))
                justSaved = true
                showErrors = false
                expanded = false
            }
        }) { Text(stringResource(R.string.save_business)) }
        if (justSaved) Text(stringResource(R.string.saved), color = MaterialTheme.colorScheme.primary)
    }
    }
    HorizontalDivider(Modifier.padding(vertical = 8.dp))
}

@Composable
private fun LockSection() {
    val context = LocalContext.current
    var available by remember { mutableStateOf(AppLock.isAvailable(context)) }
    // Re-check on resume so setting a screen lock in system settings takes effect without a restart.
    LifecycleResumeEffect(Unit) {
        available = AppLock.isAvailable(context)
        onPauseOrDispose {}
    }
    var enabled by remember { mutableStateOf(AppLock.isEnabled(context) && available) }
    val prompt = stringResource(R.string.lock_enable_prompt)

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.lock_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(if (available) R.string.lock_desc else R.string.lock_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = enabled,
            enabled = available,
            onCheckedChange = { wanted ->
                if (!wanted) {
                    AppLock.setEnabled(context, false)
                    enabled = false
                } else if (context is FragmentActivity) {
                    // Prove the device credential works before enabling, so the user cannot lock themselves out.
                    AppLock.authenticate(context, prompt) { ok ->
                        if (ok) {
                            AppLock.setEnabled(context, true)
                            AppLock.unlockedThisSession = true
                            enabled = true
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun ThemeSection() {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(ThemePrefs.get(context)) }
    val options = listOf(
        ThemeMode.SYSTEM to R.string.theme_system,
        ThemeMode.LIGHT to R.string.theme_light,
        ThemeMode.DARK to R.string.theme_dark,
    )
    Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
    Column(Modifier.selectableGroup()) {
        options.forEach { (mode, label) ->
            Row(
                Modifier.fillMaxWidth().selectable(
                    selected = selected == mode,
                    role = Role.RadioButton,
                    onClick = {
                        if (selected != mode) {
                            selected = mode
                            ThemePrefs.set(context, mode)
                            (context as? Activity)?.recreate()
                        }
                    },
                ).padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected == mode, onClick = null)
                Text(stringResource(label), Modifier.padding(start = 12.dp))
            }
        }
    }
}
