package com.kemalurekli.electricalcalculator.core.common.util

import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Reads the two characters from the JVM's locale data.
 *
 * Only the symbols are taken. The formatting itself stays in common code, so
 * that an Android and an iOS device render the same value identically — see
 * [NumberSymbols].
 */
actual fun currentNumberSymbols(): NumberSymbols =
    DecimalFormatSymbols.getInstance(Locale.getDefault()).toNumberSymbols()

/** Bridges the call sites that still hold a [Locale]. */
fun Locale.toNumberSymbols(): NumberSymbols =
    DecimalFormatSymbols.getInstance(this).toNumberSymbols()

private fun DecimalFormatSymbols.toNumberSymbols() = NumberSymbols(
    decimalSeparator = decimalSeparator,
    groupingSeparator = groupingSeparator,
)
