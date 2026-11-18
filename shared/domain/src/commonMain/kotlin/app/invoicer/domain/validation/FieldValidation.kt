package app.invoicer.domain.validation

/** Parses "1,5" or "1.5" into an integer scaled by 10^[decimals]; null if invalid. */
fun parseScaled(text: String, decimals: Int): Long? {
    val clean = text.trim().replace(',', '.')
    if (!Regex("""\d+(\.\d*)?""").matches(clean)) return null
    val parts = clean.split('.', limit = 2)
    val fraction = parts.getOrElse(1) { "" }
    if (fraction.length > decimals) return null
    return (parts[0] + fraction.padEnd(decimals, '0')).toLongOrNull()
}

/** Language-neutral validation result; each platform maps it to a localized message. */
enum class FieldError {
    ClientRequired, ClientTooShort, ClientTooLong, ClientInvalidChars,
    DescriptionRequired, DescriptionTooLong,
    QuantityRequired, QuantityFormat, QuantityNotPositive, QuantityTooLarge,
    RateRequired, RateFormat, RateNotPositive, RateTooLarge,
    EmailInvalid, KvkInvalid, BtwInvalid, PaymentTermInvalid,
}

/** Field rules shared by all platforms. Each returns an error code or null when valid. */
object FieldValidation {
    const val MAX_QUANTITY_MILLI = 99_999_999L // 99.999,999
    const val MAX_RATE_CENTS = 100_000_000L // € 1.000.000,00

    /** Letters (any language, so accents are fine), digits, spaces and . - ' & only. */
    fun isClientChar(c: Char) = c.isLetterOrDigit() || c == ' ' || c == '.' || c == '-' || c == '\'' || c == '&'

    fun client(value: String): FieldError? = when {
        value.isBlank() -> FieldError.ClientRequired
        value.trim().length < 2 -> FieldError.ClientTooShort
        value.trim().length > 80 -> FieldError.ClientTooLong
        !value.all(::isClientChar) -> FieldError.ClientInvalidChars
        else -> null
    }

    fun description(value: String): FieldError? = when {
        value.isBlank() -> FieldError.DescriptionRequired
        value.trim().length > 120 -> FieldError.DescriptionTooLong
        else -> null
    }

    fun quantity(value: String): FieldError? {
        if (value.isBlank()) return FieldError.QuantityRequired
        val scaled = parseScaled(value, 3) ?: return FieldError.QuantityFormat
        return when {
            scaled <= 0 -> FieldError.QuantityNotPositive
            scaled > MAX_QUANTITY_MILLI -> FieldError.QuantityTooLarge
            else -> null
        }
    }

    fun rate(value: String): FieldError? {
        if (value.isBlank()) return FieldError.RateRequired
        val scaled = parseScaled(value, 2) ?: return FieldError.RateFormat
        return when {
            scaled <= 0 -> FieldError.RateNotPositive
            scaled > MAX_RATE_CENTS -> FieldError.RateTooLarge
            else -> null
        }
    }

    private val emailRegex = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val btwRegex = Regex("""NL\d{9}B\d{2}""")

    /** Optional: blank is fine. */
    fun email(value: String): FieldError? =
        if (value.isBlank() || emailRegex.matches(value.trim())) null else FieldError.EmailInvalid

    /** Optional: blank is fine; a Dutch KvK number has 8 digits. */
    fun kvk(value: String): FieldError? =
        if (value.isBlank() || Regex("""\d{8}""").matches(value.trim())) null else FieldError.KvkInvalid

    /** Optional: blank is fine; format NL123456789B01. */
    fun btw(value: String): FieldError? =
        if (value.isBlank() || btwRegex.matches(value.trim().uppercase())) null else FieldError.BtwInvalid

    fun paymentTermDays(value: String): FieldError? {
        val days = value.trim().toIntOrNull()
        return if (days != null && days in 1..120) null else FieldError.PaymentTermInvalid
    }
}
