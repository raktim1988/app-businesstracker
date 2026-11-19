package app.invoicer.android.ui

import android.content.Context

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** App theme choice persisted in SharedPreferences; SYSTEM follows the device setting. */
object ThemePrefs {
    private const val FILE = "settings"
    private const val KEY = "theme"

    fun get(context: Context): ThemeMode {
        val saved = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null)
        return ThemeMode.entries.firstOrNull { it.name == saved } ?: ThemeMode.SYSTEM
    }

    fun set(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, mode.name).apply()
    }
}
