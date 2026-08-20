package com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation

import com.kemalurekli.electricalcalculator.core.common.util.currentNumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicsInput
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicsResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_step_k_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_step_neutral
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_step_rms
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_step_thd

/**
 * Four lines, each answering a different question about the same spectrum.
 *
 * The neutral line is written as a sum over the triplen orders alone, so that
 * a reader can see *which* harmonics ended up in it rather than being handed a
 * figure to trust.
 */
internal fun explainHarmonics(
    input: HarmonicsInput,
    result: HarmonicsResult,
    symbols: NumberSymbols = currentNumberSymbols(),
): ImmutableList<CalculationStep> {
    val steps = mutableListOf<CalculationStep>()
    val present = input.components.filter { it.percentOfFundamental > 0.0 }

    steps += CalculationStep(
        label = Res.string.hm_step_thd,
        formula = "THD = √(Σ Ih²) / I1",
        substitution = present.joinToString(" + ") { "${f(it.percentOfFundamental, symbols)}%(h${it.order})" }
            .ifBlank { "—" },
        result = "${f(result.thdPercent, symbols)} %",
    )

    steps += CalculationStep(
        label = Res.string.hm_step_rms,
        formula = "Irms = I1 · √(1 + THD²)",
        substitution = "${f(input.fundamentalAmps, symbols)} × √(1 + " +
            "${f(result.thdPercent / 100.0, symbols)}²)",
        result = "${f(result.rmsAmps, symbols)} A",
    )

    val triplen = present.filter { it.isTriplen }
    steps += CalculationStep(
        label = Res.string.hm_step_neutral,
        formula = "IN = 3 · √(Σ I(3,9,15)²)",
        substitution = if (!input.balanced) {
            "—"
        } else {
            triplen.joinToString(" + ") { "h${it.order}" }.ifBlank { "—" }
        },
        result = "${f(result.neutralAmps, symbols)} A",
    )

    steps += CalculationStep(
        label = Res.string.hm_step_k_factor,
        formula = "K = Σ (Ih(pu)² · h²)",
        substitution = present.joinToString(" + ") { "h${it.order}²" }.ifBlank { "—" },
        result = f(result.kFactor, symbols),
    )

    return steps.toImmutableList()
}

private fun f(value: Double, symbols: NumberSymbols) =
    NumberFormatter.format(value, decimals = 2, symbols = symbols)
