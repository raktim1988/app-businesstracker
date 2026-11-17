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

/** Field rules shared by all platforms. Each returns an error message or null when valid. */
object FieldValidation {
    const val MAX_QUANTITY_MILLI = 99_999_999L // 99.999,999
    const val MAX_RATE_CENTS = 100_000_000L // € 1.000.000,00

    fun client(value: String): String? = when {
        value.isBlank() -> "Client is required"
        value.trim().length < 2 -> "Enter at least 2 characters"
        value.trim().length > 80 -> "Maximum 80 characters"
        else -> null
    }

    fun description(value: String): String? = when {
        value.isBlank() -> "Description is required"
        value.trim().length > 120 -> "Maximum 120 characters"
        else -> null
    }

    fun quantity(value: String): String? {
        if (value.isBlank()) return "Quantity is required"
        val scaled = parseScaled(value, 3) ?: return "Use a number, max 3 decimals"
        return when {
            scaled <= 0 -> "Must be greater than 0"
            scaled > MAX_QUANTITY_MILLI -> "Too large"
            else -> null
        }
    }

    fun rate(value: String): String? {
        if (value.isBlank()) return "Rate is required"
        val scaled = parseScaled(value, 2) ?: return "Use an amount, max 2 decimals"
        return when {
            scaled <= 0 -> "Must be greater than 0"
            scaled > MAX_RATE_CENTS -> "Too large"
            else -> null
        }
    }
}
