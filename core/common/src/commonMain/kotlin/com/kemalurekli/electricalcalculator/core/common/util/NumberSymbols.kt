package com.kemalurekli.electricalcalculator.core.common.util

/**
 * The two characters that differ between locales when writing a number.
 *
 * This replaces `java.util.Locale` in [NumberFormatter]. Not because `Locale`
 * is unavailable off the JVM — though it is — but because passing a locale
 * invites each platform to format numbers with its own formatter, and
 * `DecimalFormat` and `NSNumberFormatter` are free to disagree about the same
 * double. The app's one design rule is that the same code draws the same
 * pixels on both platforms, so the formatting is done in common Kotlin and the
 * locale's contribution is narrowed to exactly what varies: which character
 * separates the units from the fraction, and which groups the thousands.
 *
 * Everything else a locale could bring — digit shapes, grouping of four, the
 * Indian lakh grouping — is deliberately not supported. The app ships in
 * English and Turkish and formats engineering quantities; a value that reads
 * differently on two devices would be a defect, not a courtesy.
 */
data class NumberSymbols(
    val decimalSeparator: Char,
    val groupingSeparator: Char,
) {
    companion object {
        /** `1,234.5` — English, and the fallback when a platform is unhelpful. */
        val Point: NumberSymbols = NumberSymbols(decimalSeparator = '.', groupingSeparator = ',')

        /** `1.234,5` — Turkish, German and most of Europe. */
        val Comma: NumberSymbols = NumberSymbols(decimalSeparator = ',', groupingSeparator = '.')
    }
}

/**
 * The symbols for the device's current locale.
 *
 * Resolved per platform, and the only part of number formatting that is.
 */
expect fun currentNumberSymbols(): NumberSymbols
