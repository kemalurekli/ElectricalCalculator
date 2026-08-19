package com.kemalurekli.electricalcalculator.core.common.util

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.decimalSeparator
import platform.Foundation.groupingSeparator

/**
 * Reads the two characters from the device's locale.
 *
 * `NSLocale` reports separators as strings, and can report an empty one — a
 * locale with no grouping separator is a real thing. Anything that is not a
 * single character falls back to the English default rather than crashing or
 * producing a number with a word in the middle of it.
 */
actual fun currentNumberSymbols(): NumberSymbols {
    val locale = NSLocale.currentLocale
    return NumberSymbols(
        decimalSeparator = locale.decimalSeparator.singleCharOr(NumberSymbols.Point.decimalSeparator),
        groupingSeparator = locale.groupingSeparator.singleCharOr(NumberSymbols.Point.groupingSeparator),
    )
}

private fun String?.singleCharOr(fallback: Char): Char =
    this?.singleOrNull() ?: fallback
