package com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation

import com.kemalurekli.electricalcalculator.core.common.util.currentNumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.TransformerInput
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.TransformerResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_step_fault_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_step_fault_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_step_primary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_step_rating
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_step_ratio
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_step_secondary

/**
 * Turns a transformer calculation into the worked solution behind it.
 *
 * The nameplate is in kVA and every current is derived from volt-amperes, so the
 * conversion is its own line rather than an unexplained factor of a thousand
 * appearing inside a division.
 *
 * The short-circuit steps come last and in this order deliberately: `u_k` is a
 * percentage *of rated voltage*, so what it multiplies is the rated secondary
 * current, not the rating. Written out, `I_sc = I₂ · 100 / u_k` makes the
 * definition legible; quoted as a single number it is something the reader has
 * to take on trust.
 */
internal fun explainTransformer(
    input: TransformerInput,
    result: TransformerResult,
    symbols: NumberSymbols = currentNumberSymbols(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, symbols)

    val ratingVa = input.ratingKva * VA_PER_KVA
    val phaseFactor = input.system.powerPhaseFactor

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        label = Res.string.tx_step_rating,
        formula = "S = S_n · 1000",
        substitution = "${n(input.ratingKva)} × ${n(VA_PER_KVA, 0)}",
        result = "${n(ratingVa, 0)} VA",
    )

    steps += CalculationStep(
        label = Res.string.tx_step_primary,
        formula = "I₁ = S / (k · U₁)",
        substitution = "${n(ratingVa, 0)} / (${n(phaseFactor, RATIO_DECIMALS)} × " +
            "${n(input.primaryVoltage)})",
        result = "${n(result.primaryCurrent)} A",
    )

    steps += CalculationStep(
        label = Res.string.tx_step_secondary,
        formula = "I₂ = S / (k · U₂)",
        substitution = "${n(ratingVa, 0)} / (${n(phaseFactor, RATIO_DECIMALS)} × " +
            "${n(input.secondaryVoltage)})",
        result = "${n(result.secondaryCurrent)} A",
    )

    steps += CalculationStep(
        label = Res.string.tx_step_ratio,
        formula = "a = U₁ / U₂",
        substitution = "${n(input.primaryVoltage)} / ${n(input.secondaryVoltage)}",
        result = n(result.voltageRatio, RATIO_DECIMALS),
    )

    steps += CalculationStep(
        label = Res.string.tx_step_fault_current,
        formula = "I_sc = I₂ · 100 / u_k",
        substitution = "${n(result.secondaryCurrent)} × 100 / " +
            n(input.impedanceVoltagePercent),
        result = "${n(result.secondaryShortCircuitCurrent)} A",
    )

    steps += CalculationStep(
        label = Res.string.tx_step_fault_power,
        formula = "S_sc = S_n · 100 / u_k",
        substitution = "${n(input.ratingKva)} × 100 / ${n(input.impedanceVoltagePercent)}",
        result = "${n(result.shortCircuitPowerKva)} kVA",
    )

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val VA_PER_KVA = 1_000.0
