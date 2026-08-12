package com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.PowerFactorInput
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.PowerFactorResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale
import kotlin.math.acos
import kotlin.math.tan

/**
 * Turns a power factor correction into the worked solution behind it.
 *
 * The two tangents lead, because they are where the calculation is actually
 * done and where it is most often got wrong. `Q_c = P · (tan φ₁ − tan φ₂)` is
 * not a difference of power factors: cos 0.80 and cos 0.95 are 0.15 apart, but
 * their tangents are 0.75 and 0.33, and it is that 0.42 the bank has to supply.
 * Showing both tangents makes the non-linearity visible instead of buried.
 *
 * The released capacity is worked through as well, since it is usually the
 * number that justifies the project — the same transformer carries more real
 * load afterwards without being touched.
 */
internal fun explainPowerFactor(
    input: PowerFactorInput,
    result: PowerFactorResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale)

    val tanBefore = tan(acos(input.existingPowerFactor.coerceIn(-1.0, 1.0)))
    val tanAfter = tan(acos(input.targetPowerFactor.coerceIn(-1.0, 1.0)))
    val phaseFactor = input.system.powerPhaseFactor

    // Single phase has no star/delta distinction: the capacitor sits across the
    // supply, so nothing divides the reactive power between units.
    val divisor = if (input.system == SupplySystem.THREE_PHASE_AC) {
        input.connection.capacitanceDivisor
    } else {
        1.0
    }

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.pf_step_tan_before,
        formula = "tan φ₁ = tan(arccos(cos φ₁))",
        substitution = "tan(arccos(${n(input.existingPowerFactor, RATIO_DECIMALS)}))",
        result = n(tanBefore, RATIO_DECIMALS),
    )

    steps += CalculationStep(
        labelRes = R.string.pf_step_tan_after,
        formula = "tan φ₂ = tan(arccos(cos φ₂))",
        substitution = "tan(arccos(${n(input.targetPowerFactor, RATIO_DECIMALS)}))",
        result = n(tanAfter, RATIO_DECIMALS),
    )

    steps += CalculationStep(
        labelRes = R.string.pf_step_reactive,
        formula = "Q_c = P · (tan φ₁ − tan φ₂)",
        substitution = "${n(input.activePowerWatts)} × " +
            "(${n(tanBefore, RATIO_DECIMALS)} − ${n(tanAfter, RATIO_DECIMALS)})",
        result = "${n(result.requiredCapacitorVar)} var",
    )

    steps += CalculationStep(
        labelRes = R.string.pf_step_capacitance,
        formula = "C = Q_c / (n · 2π f · U²)",
        substitution = "${n(result.requiredCapacitorVar)} / (${n(divisor, 0)} × 2π × " +
            "${n(input.frequencyHz)} × ${n(input.voltage)}²)",
        result = "${n(result.capacitancePerPhaseFarads * MICRO)} µF",
    )

    steps += CalculationStep(
        labelRes = R.string.pf_step_apparent_before,
        formula = "S₁ = P / cos φ₁",
        substitution = "${n(input.activePowerWatts)} / " +
            n(input.existingPowerFactor, RATIO_DECIMALS),
        result = "${n(result.apparentBeforeVa)} VA",
    )

    steps += CalculationStep(
        labelRes = R.string.pf_step_apparent_after,
        formula = "S₂ = P / cos φ₂",
        substitution = "${n(input.activePowerWatts)} / " +
            n(input.targetPowerFactor, RATIO_DECIMALS),
        result = "${n(result.apparentAfterVa)} VA",
    )

    steps += CalculationStep(
        labelRes = R.string.pf_step_released,
        formula = "ΔS = S₁ − S₂",
        substitution = "${n(result.apparentBeforeVa)} − ${n(result.apparentAfterVa)}",
        result = "${n(result.releasedCapacityVa)} VA",
    )

    steps += CalculationStep(
        labelRes = R.string.pf_step_current_before,
        formula = "I₁ = S₁ / (k · U)",
        substitution = "${n(result.apparentBeforeVa)} / (${n(phaseFactor, RATIO_DECIMALS)} × " +
            "${n(input.voltage)})",
        result = "${n(result.currentBeforeAmps)} A",
    )

    steps += CalculationStep(
        labelRes = R.string.pf_step_current_after,
        formula = "I₂ = S₂ / (k · U)",
        substitution = "${n(result.apparentAfterVa)} / (${n(phaseFactor, RATIO_DECIMALS)} × " +
            "${n(input.voltage)})",
        result = "${n(result.currentAfterAmps)} A",
    )

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val MICRO = 1_000_000.0
