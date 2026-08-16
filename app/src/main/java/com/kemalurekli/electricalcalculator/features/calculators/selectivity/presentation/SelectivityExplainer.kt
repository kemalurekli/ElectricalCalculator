package com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityGrade
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityInput
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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
 * where the fault current finally matters.
 */
internal fun explainSelectivity(
    input: SelectivityInput,
    result: SelectivityResult,
    strings: StringResolver,
): ImmutableList<CalculationStep> {
    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.sel_step_ratio,
        formula = "In(up) / In(down)",
        substitution = "${format(input.upstreamRatingAmps)} / ${format(input.downstreamRatingAmps)}",
        result = "${format(result.ratio)} " +
            strings.get(
                if (result.overloadSelective) {
                    R.string.sel_step_ratio_clears
                } else {
                    R.string.sel_step_ratio_short
                },
            ),
    )

    val upstream = result.upstreamInstantaneousAmps
    if (upstream != null) {
        steps += CalculationStep(
            labelRes = R.string.sel_step_threshold,
            formula = "Ia(up) = k · In(up)",
            substitution = "${format(input.upstreamType.instantaneousMultiplier ?: 0.0)} × " +
                format(input.upstreamRatingAmps),
            result = "${format(upstream)} A",
        )
        steps += CalculationStep(
            labelRes = R.string.sel_step_compare,
            formula = "Ipf < Ia(up)",
            substitution = "${format(input.prospectiveFaultAmps)} A / ${format(upstream)} A",
            result = strings.get(result.grade.summaryRes()),
        )
    } else {
        // Saying nothing here would leave the reader to assume the check was
        // made and passed.
        steps += CalculationStep(
            labelRes = R.string.sel_step_threshold,
            formula = "Ia(up)",
            substitution = "—",
            result = strings.get(R.string.sel_step_no_curve),
        )
    }

    return steps.toImmutableList().ifEmpty { persistentListOf() }
}

private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)
