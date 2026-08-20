package com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation

import org.jetbrains.compose.resources.StringResource
import com.kemalurekli.electricalcalculator.core.common.util.currentNumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityGrade
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityInput
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_grade_none
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_grade_partial
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_grade_selective
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_step_compare
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_step_ratio
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_step_threshold

/** The heading a verdict is written under, and its one-line meaning. */
internal fun SelectivityGrade.summary(): StringResource = when (this) {
    SelectivityGrade.SELECTIVE -> Res.string.sel_grade_selective
    SelectivityGrade.PARTIAL -> Res.string.sel_grade_partial
    SelectivityGrade.NONE -> Res.string.sel_grade_none
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
    symbols: NumberSymbols = currentNumberSymbols(),
): ImmutableList<CalculationStep> {
    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        label = Res.string.sel_step_ratio,
        formula = "In(up) / In(down)",
        substitution = "${format(input.upstreamRatingAmps, symbols)} / " +
            format(input.downstreamRatingAmps, symbols),
        result = format(result.ratio, symbols),
    )

    val upstream = result.upstreamInstantaneousAmps
    val multiplier = input.upstreamType.instantaneousMultiplier
    if (upstream != null && multiplier != null) {
        steps += CalculationStep(
            label = Res.string.sel_step_threshold,
            formula = "Ia(up) = k · In(up)",
            substitution = "${format(multiplier, symbols)} × ${format(input.upstreamRatingAmps, symbols)}",
            result = "${format(upstream, symbols)} A",
        )
        steps += CalculationStep(
            label = Res.string.sel_step_compare,
            formula = "Ia(up) - Ipf",
            substitution = "${format(upstream, symbols)} - " +
                format(input.prospectiveFaultAmps, symbols),
            result = "${format(upstream - input.prospectiveFaultAmps, symbols)} A",
        )
    }

    return steps.toImmutableList()
}

private fun format(value: Double, symbols: NumberSymbols) =
    NumberFormatter.format(value, decimals = 2, symbols = symbols)
