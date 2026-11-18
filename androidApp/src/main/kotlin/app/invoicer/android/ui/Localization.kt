package app.invoicer.android.ui

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import app.invoicer.android.R
import app.invoicer.domain.model.InvoiceStatus
import app.invoicer.domain.validation.FieldError
import java.util.Locale

@StringRes
fun FieldError.messageRes(): Int = when (this) {
    FieldError.ClientRequired -> R.string.err_client_required
    FieldError.ClientTooShort -> R.string.err_client_short
    FieldError.ClientTooLong -> R.string.err_client_long
    FieldError.ClientInvalidChars -> R.string.err_client_chars
    FieldError.DescriptionRequired -> R.string.err_description_required
    FieldError.DescriptionTooLong -> R.string.err_description_long
    FieldError.QuantityRequired -> R.string.err_quantity_required
    FieldError.QuantityFormat -> R.string.err_quantity_format
    FieldError.QuantityNotPositive, FieldError.RateNotPositive -> R.string.err_not_positive
    FieldError.QuantityTooLarge, FieldError.RateTooLarge -> R.string.err_too_large
    FieldError.RateRequired -> R.string.err_rate_required
    FieldError.RateFormat -> R.string.err_rate_format
    FieldError.EmailInvalid -> R.string.err_email
    FieldError.KvkInvalid -> R.string.err_kvk
    FieldError.BtwInvalid -> R.string.err_btw
    FieldError.PaymentTermInvalid -> R.string.err_term
}

@StringRes
fun InvoiceStatus.labelRes(): Int = when (this) {
    InvoiceStatus.DRAFT -> R.string.status_draft
    InvoiceStatus.SENT -> R.string.status_sent
    InvoiceStatus.PAID -> R.string.status_paid
}

/** App language choice persisted in SharedPreferences; null means follow the system language. */
object LanguagePrefs {
    private const val FILE = "settings"
    private const val KEY = "language"
    val supported = listOf("en", "nl")

    fun get(context: Context): String? =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null)

    fun set(context: Context, language: String?) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().apply {
            if (language == null) remove(KEY) else putString(KEY, language)
        }.apply()
    }

    /** Wraps [base] so resources resolve in the saved language. */
    fun wrap(base: Context): Context {
        val language = get(base)?.takeIf { it in supported } ?: return base
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale.forLanguageTag(language))
        return base.createConfigurationContext(config)
    }
}
