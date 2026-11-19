package app.invoicer.android

import app.invoicer.android.ui.LanguagePrefs
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.invoicer.android.security.AppLock
import app.invoicer.android.security.LockScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.invoicer.android.ui.InvoicerRoot

class MainActivity : FragmentActivity() {
    private var locked by mutableStateOf(false)
    private var prompting = false
    private var backgroundedAt = 0L

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(LanguagePrefs.wrap(newBase))

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as InvoicerApp).container
        setContent {
            InvoicerTheme {
                InvoicerRoot(container)
                if (locked) {
                    LockScreen(onUnlock = ::promptUnlock)
                    LaunchedEffect(Unit) { promptUnlock() }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!AppLock.isEnabled(this) || !AppLock.isAvailable(this)) {
            locked = false
            return
        }
        val away = SystemClock.elapsedRealtime() - backgroundedAt
        if (!AppLock.unlockedThisSession || (backgroundedAt != 0L && away > AppLock.RELOCK_AFTER_MILLIS)) locked = true
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) backgroundedAt = SystemClock.elapsedRealtime()
    }

    private fun promptUnlock() {
        if (prompting) return
        prompting = true
        AppLock.authenticate(this, getString(R.string.unlock_prompt)) { ok ->
            prompting = false
            if (ok) {
                AppLock.unlockedThisSession = true
                locked = false
            }
        }
    }
}

@Composable
private fun InvoicerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Color(0xFF0B5FFF)),
        content = content,
    )
}
