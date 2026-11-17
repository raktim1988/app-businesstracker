package app.invoicer.domain.model

import kotlin.jvm.JvmInline

/** Amount in euro cents. Never use floating point for money. */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {
    operator fun plus(other: Money) = Money(cents + other.cents)
    operator fun minus(other: Money) = Money(cents - other.cents)
    override fun compareTo(other: Money) = cents.compareTo(other.cents)

    /** Dutch-style display, e.g. "€ 1.234,56". */
    fun format(): String {
        val sign = if (cents < 0) "-" else ""
        val abs = if (cents < 0) -cents else cents
        val whole = (abs / 100).toString().reversed().chunked(3).joinToString(".").reversed()
        val fraction = (abs % 100).toString().padStart(2, '0')
        return "$sign€ $whole,$fraction"
    }

    companion object {
        val ZERO = Money(0)
    }
}
