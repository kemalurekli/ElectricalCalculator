package com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CableSizeInput
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CableSizeResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

/**
 * Turns a cable sizing calculation into the worked solution behind it.
 *
 * The two constraints are walked separately and then compared, because that is
 * the whole shape of the calculation: a cable large enough to carry the current
 * can still be too small to deliver usable voltage, and the answer is the larger
 * of the two requirements. Seeing both numbers before the comparison is what
 * tells the reader whether shortening the run would help — it would, if the
 * voltage-drop size is the one that won.
 *
 * The first five steps are produced even when no standard size satisfies the
 * circuit. They are the reason it failed, so withholding them at exactly the
 * moment the user needs an explanation would be the wrong way round.
 */
internal fun explainCableSize(
    input: CableSizeInput,
    result: CableSizeResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale)

    val resistivity = input.material.resistivityAt(input.insulation.maxConductorTemperatureC)
    val powerFactor = if (input.system.isAc) input.powerFactor else 1.0
    val deratingFactor = result.ambientFactor * result.groupingFactor
    val maxDropVolts = input.systemVoltage * input.maxVoltageDropPercent / PERCENT
    val minimumArea = input.system.lengthMultiplier *
        input.designCurrent *
        resistivity *
        input.lengthMetres *
        powerFactor /
        (maxDropVolts * input.parallelConductors)

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.cs_step_derating,
        formula = "Ca · Cg",
        substitution = "${n(result.ambientFactor, RATIO_DECIMALS)} × " +
            n(result.groupingFactor, RATIO_DECIMALS),
        result = n(deratingFactor, RATIO_DECIMALS),
    )

    steps += CalculationStep(
        labelRes = R.string.cs_step_required_capacity,
        formula = "I_z ≥ I_b / (n · Ca · Cg)",
        substitution = "${n(input.designCurrent)} / (${input.parallelConductors} × " +
            "${n(deratingFactor, RATIO_DECIMALS)})",
        result = "${n(result.requiredCapacityAmps)} A",
    )

    steps += CalculationStep(
        labelRes = R.string.cs_step_resistivity,
        formula = "ρ(θ) = ρ₂₀ · [1 + α₂₀ · (θ − 20)]",
        substitution = "${n(input.material.resistivityAt20C, RESISTIVITY_DECIMALS)} × " +
            "[1 + ${n(input.material.temperatureCoefficient, RESISTIVITY_DECIMALS)} × " +
            "(${n(input.insulation.maxConductorTemperatureC, 0)} − 20)]",
        result = "${n(resistivity, RESISTIVITY_DECIMALS)} Ω·mm²/m",
    )

    steps += CalculationStep(
        labelRes = R.string.cs_step_permitted_drop,
        formula = "ΔU_max = U · p / 100",
        substitution = "${n(input.systemVoltage)} × ${n(input.maxVoltageDropPercent)} / 100",
        result = "${n(maxDropVolts)} V",
    )

    steps += CalculationStep(
        labelRes = R.string.cs_step_minimum_area,
        formula = "A ≥ k · I · ρ(θ) · L · cos φ / (ΔU_max · n)",
        substitution = "${n(input.system.lengthMultiplier, RATIO_DECIMALS)} × " +
            "${n(input.designCurrent)} × ${n(resistivity, RESISTIVITY_DECIMALS)} × " +
            "${n(input.lengthMetres)} × ${n(powerFactor, RATIO_DECIMALS)} / " +
            "(${n(maxDropVolts)} × ${input.parallelConductors})",
        result = "${n(minimumArea)} mm²",
    )

    val byCapacity = result.currentCapacityAreaMm2
    val byDrop = result.voltageDropAreaMm2
    val recommended = result.recommendedAreaMm2

    if (byCapacity != null && byDrop != null && recommended != null) {
        steps += CalculationStep(
            labelRes = R.string.cs_step_selection,
            formula = "A = max(A_I, A_ΔU)",
            // Semicolon, not comma: half the app's readers use a comma as the
            // decimal separator, and "max(16,0, 10,0)" would read as four numbers.
            substitution = "max(${n(byCapacity)}; ${n(byDrop)})",
            result = "${n(recommended)} mm²",
        )

        steps += CalculationStep(
            labelRes = R.string.cs_step_actual_drop,
            formula = "ΔU = k · I · ρ(θ) · L · cos φ / (A · n)",
            substitution = "${n(input.system.lengthMultiplier, RATIO_DECIMALS)} × " +
                "${n(input.designCurrent)} × ${n(resistivity, RESISTIVITY_DECIMALS)} × " +
                "${n(input.lengthMetres)} × ${n(powerFactor, RATIO_DECIMALS)} / " +
                "(${n(recommended)} × ${input.parallelConductors})",
            result = "${n(result.voltageDropVolts)} V",
        )

        steps += CalculationStep(
            labelRes = R.string.cs_step_drop_percentage,
            formula = "ΔU% = ΔU / U · 100",
            substitution = "${n(result.voltageDropVolts)} / ${n(input.systemVoltage)} × 100",
            result = "${n(result.voltageDropPercent)} %",
        )
    }

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val RESISTIVITY_DECIMALS = 6
private const val PERCENT = 100.0
