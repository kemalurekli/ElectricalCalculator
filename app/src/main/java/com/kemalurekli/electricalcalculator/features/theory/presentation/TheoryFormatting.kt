package com.kemalurekli.electricalcalculator.features.theory.presentation

import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.theory.domain.NumberStyle
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryNumber
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryStep
import java.util.Locale

/**
 * The one place a [Locale] meets a theory solution.
 *
 * Every solver produces numbers and slotted sentences; nothing downstream of a
 * solver formats anything itself. That is the point of the split — see
 * [TheoryStep] for the argument, and `TheoryFormattingTest` for the properties it
 * buys, which hold for every topic in the catalog without any of them being
 * checked one at a time.
 *
 * `locale` is a defaulted parameter rather than a read of `Locale.getDefault()`
 * inside, for the reason the calculators' explainers take one: a test that cannot
 * pin the locale is a test of whatever machine ran it.
 */

/**
 * The number as it appears inside a derivation — with its unit.
 *
 * The calculators write bare numbers into a substitution and put the unit only on
 * the result. This shelf keeps the unit on both, because a theory substitution
 * mixes quantities in a way a calculator's does not: `230 V / 529 Ω` shows why
 * the answer is in amps, and `230 / 529` leaves the reader to remember.
 */
internal fun TheoryNumber.format(locale: Locale = Locale.getDefault()): String = when (style) {
    NumberStyle.FIXED -> withUnit(NumberFormatter.format(value, decimals, locale))
    NumberStyle.SIGNIFICANT -> withUnit(NumberFormatter.formatSignificant(value, decimals, locale))
    // formatWithSiPrefix carries the unit itself, since the prefix belongs to it.
    NumberStyle.SI_PREFIX ->
        if (unit.isEmpty()) NumberFormatter.formatSignificant(value, decimals, locale)
        else NumberFormatter.formatWithSiPrefix(value, unit, decimals, locale)
}

/**
 * The number without its unit, for a result card that renders the two separately.
 *
 * An SI-prefixed value is the exception: its prefix belongs to the unit, so
 * splitting them would print `3.183` beside `Ω` and lose the mega. Those come
 * back whole here and with an empty [unitLabel].
 */
internal fun TheoryNumber.formatValue(locale: Locale = Locale.getDefault()): String = when (style) {
    NumberStyle.FIXED -> NumberFormatter.format(value, decimals, locale)
    NumberStyle.SIGNIFICANT -> NumberFormatter.formatSignificant(value, decimals, locale)
    NumberStyle.SI_PREFIX -> format(locale)
}

/** The unit a result card should print beside [formatValue]. */
internal val TheoryNumber.unitLabel: String
    get() = if (style == NumberStyle.SI_PREFIX) "" else unit

/**
 * Fills a step's slots with its numbers and hands back the shared display type.
 *
 * [TheoryStep.formula] is copied across untouched, which is what makes the
 * symbolic line language-neutral by construction rather than by review.
 */
internal fun TheoryStep.toCalculationStep(locale: Locale = Locale.getDefault()) = CalculationStep(
    labelRes = labelRes,
    formula = formula,
    substitution = operands.foldIndexed(substitution) { index, line, operand ->
        line.replace("{$index}", operand.format(locale))
    },
    result = result.format(locale),
)

private fun TheoryNumber.withUnit(text: String) = if (unit.isEmpty()) text else "$text $unit"
