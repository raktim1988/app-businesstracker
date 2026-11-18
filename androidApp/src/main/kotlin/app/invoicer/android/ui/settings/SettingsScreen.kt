package app.invoicer.android.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import app.invoicer.android.ui.LanguagePrefs

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(LanguagePrefs.get(context)) }
    val options = listOf(
        null to R.string.language_system,
        "en" to R.string.language_en,
        "nl" to R.string.language_nl,
    )

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
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
    }
}
