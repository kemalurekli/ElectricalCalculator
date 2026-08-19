package com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityGrade
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityInput
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

/** The heading a verdict is written under, and its one-line meaning. */
internal fun SelectivityGrade.summaryRes(): Int = when (this) {
    SelectivityGrade.SELECTIVE -> R.string.sel_grade_selective
    SelectivityGrade.PARTIAL -> R.string.sel_grade_partial
    SelectivityGrade.NONE -> R.string.sel_grade_none
}

/**
 * The two questions, worked in the order an engineer asks them.
 *
 * The overload check first, because it is settled by the ratings alone and
 * fails independently of the fault level; then the magnetic check, which is
 * where the fault current finally matters. The last step is the headroom —
 * how much more fault current the pair would take before the upstream device
 * joins in — because "selective" and "selective with 12 A to spare" are
 * different findings.
 *
 * The verdict itself is not a step. Every explainer in this app puts numbers in
 * its result lines and leaves the words to the result card, so that the
 * derivation reads the same in every language.
 *
 * A device whose curve the app cannot read produces the ratio step alone. There
 * is no threshold to work out, and inventing a step that says so would put
 * translated prose where a figure belongs.
 */
internal fun explainSelectivity(
    input: SelectivityInput,
    result: SelectivityResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.sel_step_ratio,
        formula = "In(up) / In(down)",
        substitution = "${format(input.upstreamRatingAmps, locale)} / " +
            format(input.downstreamRatingAmps, locale),
        result = format(result.ratio, locale),
    )

    val upstream = result.upstreamInstantaneousAmps
    val multiplier = input.upstreamType.instantaneousMultiplier
    if (upstream != null && multiplier != null) {
        steps += CalculationStep(
            labelRes = R.string.sel_step_threshold,
            formula = "Ia(up) = k · In(up)",
            substitution = "${format(multiplier, locale)} × ${format(input.upstreamRatingAmps, locale)}",
            result = "${format(upstream, locale)} A",
        )
        steps += CalculationStep(
            labelRes = R.string.sel_step_compare,
            formula = "Ia(up) - Ipf",
            substitution = "${format(upstream, locale)} - " +
                format(input.prospectiveFaultAmps, locale),
            result = "${format(upstream - input.prospectiveFaultAmps, locale)} A",
        )
    }

    return steps.toImmutableList()
}

private fun format(value: Double, locale: Locale) =
    NumberFormatter.format(value, decimals = 2, symbols = locale.toNumberSymbols())
