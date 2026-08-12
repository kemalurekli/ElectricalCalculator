package com.kemalurekli.electricalcalculator.core.common.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * Formatting and parsing of engineering quantities.
 *
 * Two rules drive the design:
 *
 * 1. **Display follows the locale.** A Turkish or German user expects `1,25 A`,
 *    not `1.25 A`, so formatting always goes through a locale-aware
 *    [DecimalFormat].
 * 2. **Parsing accepts both separators.** Physical keyboards, soft keyboards and
 *    copy-pasted values disagree about `.` versus `,`, and rejecting one of them
 *    is a constant source of user error in calculator apps.
 */
object NumberFormatter {

    private const val DEFAULT_DECIMALS = 2
    private const val DEFAULT_SIGNIFICANT_DIGITS = 4

    /**
     * Formats [value] with a fixed number of decimal places, dropping trailing
     * zeros so that `12.50` reads as `12.5` and `12.00` as `12`.
     */
    fun format(
        value: Double,
        decimals: Int = DEFAULT_DECIMALS,
        locale: Locale = Locale.getDefault(),
    ): String {
        require(decimals >= 0) { "decimals must not be negative, was $decimals" }
        if (!value.isFinite()) return value.toString()

        val pattern = if (decimals == 0) "#,##0" else "#,##0." + "#".repeat(decimals)
        return DecimalFormat(pattern, DecimalFormatSymbols.getInstance(locale))
            .apply { roundingMode = RoundingMode.HALF_UP }
            .format(value)
    }

    /**
     * Formats [value] to a number of significant digits.
     *
     * This is the right rounding for engineering results: `0.00123456` keeps
     * four meaningful digits (`0.001235`) instead of being flattened to `0`
     * by fixed-decimal rounding.
     */
    fun formatSignificant(
        value: Double,
        significantDigits: Int = DEFAULT_SIGNIFICANT_DIGITS,
        locale: Locale = Locale.getDefault(),
    ): String {
        require(significantDigits > 0) {
            "significantDigits must be positive, was $significantDigits"
        }
        if (!value.isFinite()) return value.toString()
        if (value == 0.0) return format(0.0, 0, locale)

        val rounded = BigDecimal(value)
            .round(MathContext(significantDigits, RoundingMode.HALF_UP))
            .stripTrailingZeros()

        // `scale` is the count of digits after the decimal point; a negative
        // scale means the value was rounded above the decimal point.
        val decimals = rounded.scale().coerceAtLeast(0)
        return format(rounded.toDouble(), decimals, locale)
    }

    /**
     * Formats [value] for display where the magnitude is not known in advance.
     *
     * Plain decimal notation while the number is readable as one, and
     * scientific notation outside that — a circular mil is 5.067×10⁻¹⁰ m², and
     * writing it as `0.0000000005067` asks the reader to count zeros.
     *
     * The exponent uses Unicode superscripts so it renders as an exponent in
     * any text style, with no rich-text handling at the call site.
     */
    fun formatEngineering(
        value: Double,
        significantDigits: Int = DEFAULT_SIGNIFICANT_DIGITS,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (!value.isFinite()) return value.toString()
        if (value == 0.0) return format(0.0, 0, locale)

        val magnitude = abs(value)
        if (magnitude in PLAIN_MIN..PLAIN_MAX) {
            return formatSignificant(value, significantDigits, locale)
        }

        val exponent = floor(log10(magnitude)).toInt()
        val mantissa = value / 10.0.pow(exponent)

        return buildString {
            append(formatSignificant(mantissa, significantDigits, locale))
            append("×10")
            append(superscript(exponent))
        }
    }

    /** Renders an integer with Unicode superscript digits, e.g. -10 to ⁻¹⁰. */
    private fun superscript(exponent: Int): String = buildString {
        if (exponent < 0) append('⁻')
        abs(exponent).toString().forEach { append(SUPERSCRIPT_DIGITS[it - '0']) }
    }

    /**
     * Parses user input into a [Double], accepting either `.` or `,` as the
     * decimal separator and tolerating surrounding whitespace.
     *
     * Returns `null` when the text is not a single well-formed number. Input
     * containing both separators is rejected rather than guessed at, because
     * `1,234.5` and `1.234,5` are the same string with opposite meanings in
     * different locales.
     */
    fun parseOrNull(input: String): Double? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val hasDot = trimmed.contains('.')
        val hasComma = trimmed.contains(',')
        if (hasDot && hasComma) return null

        val normalised = if (hasComma) trimmed.replace(',', '.') else trimmed
        // `toDoubleOrNull` accepts forms Kotlin's number syntax allows but a
        // calculator should not, such as "0x1p3", "1d" and "Infinity".
        if (!NUMERIC_PATTERN.matches(normalised)) return null

        return normalised.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    /**
     * Formats [value] with an SI prefix, keeping the mantissa between 1 and
     * 1000 — `1500.0` with unit `W` becomes `1.5 kW`.
     *
     * @param unit the base unit symbol, appended after the prefix.
     */
    fun formatWithSiPrefix(
        value: Double,
        unit: String,
        significantDigits: Int = DEFAULT_SIGNIFICANT_DIGITS,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (!value.isFinite()) return "$value $unit"
        val magnitude = abs(value)
        if (magnitude == 0.0) return "${format(0.0, 0, locale)} $unit"

        val exponentIndex = SI_PREFIXES.indexOfLast { magnitude >= it.threshold }
            .takeIf { it >= 0 } ?: 0
        val prefix = SI_PREFIXES[exponentIndex]
        val scaled = value / prefix.threshold

        return "${formatSignificant(scaled, significantDigits, locale)} ${prefix.symbol}$unit"
    }

    /** Ordered smallest-first so `indexOfLast` selects the largest that fits. */
    private val SI_PREFIXES = listOf(
        SiPrefix("n", 1e-9),
        SiPrefix("µ", 1e-6),
        SiPrefix("m", 1e-3),
        SiPrefix("", 1.0),
        SiPrefix("k", 1e3),
        SiPrefix("M", 1e6),
        SiPrefix("G", 1e9),
    )

    private data class SiPrefix(val symbol: String, val threshold: Double)

    /** Below this or above [PLAIN_MAX], scientific notation is easier to read. */
    private const val PLAIN_MIN = 1e-4

    private const val PLAIN_MAX = 1e9

    private const val SUPERSCRIPT_DIGITS = "\u2070\u00b9\u00b2\u00b3\u2074\u2075\u2076\u2077\u2078\u2079"

    /** Optional sign, digits, optional fraction, optional decimal exponent. */
    private val NUMERIC_PATTERN = Regex("""^[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?$""")
}
