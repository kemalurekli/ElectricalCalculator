package com.kemalurekli.electricalcalculator.core.common.util

/**
 * A double taken apart into its exact decimal digits, so it can be rounded and
 * rendered without a platform formatter.
 *
 * The value is `± 0.<digits> × 10^pointPos` — that is, [pointPos] says how many
 * of [digits] fall before the decimal point. It may be zero or negative (the
 * number is smaller than one) or larger than the digit count (there are
 * trailing zeros that are not stored).
 *
 * ### Why the digits are exact and not `toString`
 *
 * Every finite double is `m × 2^e` for integers m and e, so every one of them
 * has a finite decimal expansion — usually a long one. `0.1` is really
 * `0.1000000000000000055511151231257827…`, and the difference decides which way
 * a rounding goes.
 *
 * The first attempt at this class used `Double.toString`, on the theory that
 * `DecimalFormat` rounds the shortest round-tripping representation. Measuring
 * the JDK showed otherwise, and two of the app's own tests disagree in a way
 * that pins the behaviour exactly:
 *
 * | value | shortest | exact | 4dp |
 * |---|---|---|---|
 * | `2.345` | `2.345` | `2.34500000000000019…` | **2.35** — above the tie |
 * | `735.49875` | `735.49875` | `735.49874999999997…` | **735.4987** — below it |
 *
 * Both look like a `…5` to be rounded half-up. Only the exact expansion tells
 * them apart, and `HALF_UP` then applies to what is left: a genuine tie, like
 * `0.125`, still rounds away from zero.
 *
 * Getting this wrong would have shifted results across every calculator in the
 * app by one in the last displayed digit — the kind of defect that is noticed
 * a year later by someone checking a figure by hand.
 *
 * The expansion is computed here rather than with a big-number library because
 * it is only ever multiplication by five: `m / 2^k` is `m × 5^k / 10^k`.
 */
internal class DecimalDigits private constructor(
    val negative: Boolean,
    val digits: String,
    val pointPos: Int,
) {

    val isZero: Boolean get() = digits == "0"

    /**
     * Rounds so that at most [decimals] digits remain after the decimal point.
     *
     * Half away from zero, matching `RoundingMode.HALF_UP`. The sign lives in
     * [negative] and the digits are unsigned, so "up" and "away from zero" are
     * the same operation.
     */
    fun roundToDecimals(decimals: Int): DecimalDigits = roundToLength(pointPos + decimals)

    /** Rounds to [count] significant digits. */
    fun roundToSignificant(count: Int): DecimalDigits = roundToLength(count)

    /** Digits after the decimal point once trailing zeros are dropped. */
    fun trailingDecimals(): Int = (digits.trimEnd('0').length - pointPos).coerceAtLeast(0)

    private fun roundToLength(keep: Int): DecimalDigits {
        if (isZero || keep >= digits.length) return this
        if (keep < 0) return ZERO

        val kept = digits.substring(0, keep)
        // HALF_UP on exact digits is this simple: the discarded remainder is at
        // least half exactly when its first digit is five or more. What made
        // `735.49875` round down is not the rule but the digits — its exact
        // expansion has a 4 here, not a 5.
        val roundUp = digits[keep] >= '5'

        if (!roundUp) {
            val trimmed = kept.trimEnd('0')
            return if (trimmed.isEmpty()) ZERO else DecimalDigits(negative, trimmed, pointPos)
        }

        val carried = incrementDecimalString(kept)
        // "999" + 1 is "1000": one digit longer, so the point moves right.
        val grew = carried.length > kept.length
        return DecimalDigits(
            negative = negative,
            digits = carried.trimEnd('0').ifEmpty { "0" },
            pointPos = if (grew) pointPos + 1 else pointPos,
        )
    }

    companion object {

        private val ZERO = DecimalDigits(negative = false, digits = "0", pointPos = 1)

        /** Takes [value] apart. Callers must exclude non-finite values first. */
        fun of(value: Double): DecimalDigits {
            if (value == 0.0) return ZERO

            val bits = value.toRawBits()
            val biasedExponent = ((bits ushr 52) and 0x7FF).toInt()
            val fraction = bits and 0x000F_FFFF_FFFF_FFFFL

            // Subnormals carry no implicit leading bit and share the smallest
            // exponent; normals get the bit back and their own exponent.
            var significand = if (biasedExponent == 0) fraction else fraction or (1L shl 52)
            var exponent = if (biasedExponent == 0) -1074 else biasedExponent - 1075

            // Every factor of two dropped here is one fewer multiplication by
            // five below. For a value like 2.5 it takes the work to nothing.
            while (exponent < 0 && significand % 2 == 0L) {
                significand /= 2
                exponent++
            }

            val negative = value < 0.0
            if (exponent >= 0) {
                // An integer: multiply out the remaining powers of two.
                var digits = significand.toString()
                repeat(exponent) { digits = doubleDecimalString(digits) }
                return normalised(negative, digits, scale = 0)
            }

            // value = significand / 2^k = significand × 5^k / 10^k
            val k = -exponent
            var digits = significand.toString()
            repeat(k) { digits = quintupleDecimalString(digits) }
            return normalised(negative, digits, scale = k)
        }

        /**
         * Builds from an unscaled digit string and the number of those digits
         * that fall after the decimal point.
         */
        private fun normalised(negative: Boolean, unscaled: String, scale: Int): DecimalDigits {
            val withoutLeading = unscaled.trimStart('0')
            if (withoutLeading.isEmpty()) return ZERO
            // Leading zeros carry no information and no position; trailing ones
            // are dropped only after the point has been fixed by the length.
            val pointPos = withoutLeading.length - scale
            return DecimalDigits(
                negative = negative,
                digits = withoutLeading.trimEnd('0').ifEmpty { "0" },
                pointPos = pointPos,
            )
        }
    }
}

/** Adds one to an unsigned decimal digit string, growing it on a full carry. */
private fun incrementDecimalString(digits: String): String {
    val out = digits.toCharArray()
    for (i in out.indices.reversed()) {
        if (out[i] != '9') {
            out[i] = out[i] + 1
            return out.concatToString()
        }
        out[i] = '0'
    }
    return "1" + out.concatToString()
}

private fun doubleDecimalString(digits: String): String = multiplyDecimalString(digits, 2)

private fun quintupleDecimalString(digits: String): String = multiplyDecimalString(digits, 5)

/** Long multiplication of an unsigned decimal string by a single-digit factor. */
private fun multiplyDecimalString(digits: String, factor: Int): String {
    val out = CharArray(digits.length + 1)
    var carry = 0
    for (i in digits.indices.reversed()) {
        val product = (digits[i] - '0') * factor + carry
        out[i + 1] = '0' + (product % 10)
        carry = product / 10
    }
    out[0] = '0' + carry
    val result = out.concatToString()
    return if (result[0] == '0') result.substring(1) else result
}
