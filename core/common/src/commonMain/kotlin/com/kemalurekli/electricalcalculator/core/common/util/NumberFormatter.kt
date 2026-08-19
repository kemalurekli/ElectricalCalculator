package com.kemalurekli.electricalcalculator.core.common.util

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
 *    not `1.25 A`, so formatting goes through [NumberSymbols].
 * 2. **Parsing accepts both separators.** Physical keyboards, soft keyboards and
 *    copy-pasted values disagree about `.` versus `,`, and rejecting one of them
 *    is a constant source of user error in calculator apps.
 *
 * ### Why this no longer uses DecimalFormat
 *
 * It did, and `java.text` does not exist off the JVM. It could have become an
 * `expect`/`actual` over `DecimalFormat` and `NSNumberFormatter`, and that
 * would have been less code — but two platform formatters are two chances to
 * render the same number differently, and this app's design language has
 * exactly one rule: the same code draws the same pixels on both platforms.
 *
 * So the rounding and rendering happen here, in common Kotlin, on the exact
 * decimal digits [DecimalDigits] extracts. The locale keeps only what genuinely
 * varies, which is two characters.
 *
 * [DecimalDigits] is where the care went: it reproduces what `DecimalFormat`
 * and `BigDecimal` did before, down to which side of a halfway case a value
 * falls on, because that is the difference between this being a port and being
 * a quiet change to every figure the app prints.
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
        symbols: NumberSymbols = currentNumberSymbols(),
    ): String {
        require(decimals >= 0) { "decimals must not be negative, was $decimals" }
        if (!value.isFinite()) return value.toString()

        return render(DecimalDigits.of(value).roundToDecimals(decimals), decimals, symbols)
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
        symbols: NumberSymbols = currentNumberSymbols(),
    ): String {
        require(significantDigits > 0) {
            "significantDigits must be positive, was $significantDigits"
        }
        if (!value.isFinite()) return value.toString()
        if (value == 0.0) return format(0.0, 0, symbols)

        val rounded = DecimalDigits.of(value).roundToSignificant(significantDigits)
        return render(rounded, rounded.trailingDecimals(), symbols)
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
        symbols: NumberSymbols = currentNumberSymbols(),
    ): String {
        if (!value.isFinite()) return value.toString()
        if (value == 0.0) return format(0.0, 0, symbols)

        val magnitude = abs(value)
        if (magnitude in PLAIN_MIN..PLAIN_MAX) {
            return formatSignificant(value, significantDigits, symbols)
        }

        val exponent = floor(log10(magnitude)).toInt()
        val mantissa = value / 10.0.pow(exponent)

        return buildString {
            append(formatSignificant(mantissa, significantDigits, symbols))
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
        symbols: NumberSymbols = currentNumberSymbols(),
    ): String {
        if (!value.isFinite()) return "$value $unit"
        val magnitude = abs(value)
        if (magnitude == 0.0) return "${format(0.0, 0, symbols)} $unit"

        val exponentIndex = SI_PREFIXES.indexOfLast { magnitude >= it.threshold }
            .takeIf { it >= 0 } ?: 0
        val prefix = SI_PREFIXES[exponentIndex]
        val scaled = value / prefix.threshold

        return "${formatSignificant(scaled, significantDigits, symbols)} ${prefix.symbol}$unit"
    }

    /**
     * Writes out digits that have already been rounded.
     *
     * [maxDecimals] is a ceiling rather than a target — trailing zeros are
     * dropped, which is what turns `12.50` into `12.5`.
     */
    private fun render(
        value: DecimalDigits,
        maxDecimals: Int,
        symbols: NumberSymbols,
    ): String {
        val digits = value.digits
        val point = value.pointPos

        val whole = when {
            point <= 0 -> "0"
            // The digits ran out before the decimal point did: 1235 with the
            // point four places further right is 12,350,000.
            point >= digits.length -> digits + "0".repeat(point - digits.length)
            else -> digits.substring(0, point)
        }
        val fraction = when {
            point >= digits.length -> ""
            // The point falls before the digits start: 1235 with point at -2
            // is 0.001235.
            point <= 0 -> "0".repeat(-point) + digits
            else -> digits.substring(point)
        }.take(maxDecimals).trimEnd('0')

        return buildString {
            // A value rounded away to nothing is "0", never "-0".
            if (value.negative && !value.isZero) append('-')
            append(group(whole, symbols.groupingSeparator))
            if (fraction.isNotEmpty()) {
                append(symbols.decimalSeparator)
                append(fraction)
            }
        }
    }

    /** Inserts [separator] every three digits, counting from the right. */
    private fun group(whole: String, separator: Char): String {
        if (whole.length <= GROUP_SIZE) return whole
        return buildString {
            whole.forEachIndexed { index, digit ->
                if (index > 0 && (whole.length - index) % GROUP_SIZE == 0) append(separator)
                append(digit)
            }
        }
    }

    private const val GROUP_SIZE = 3

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

    private const val SUPERSCRIPT_DIGITS = "⁰¹²³⁴⁵⁶⁷⁸⁹"

    /** Optional sign, digits, optional fraction, optional decimal exponent. */
    private val NUMERIC_PATTERN = Regex("""^[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?$""")
}
