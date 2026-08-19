package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropInput
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

/**
 * Turns a voltage drop calculation into the worked solution behind it.
 *
 * The temperature correction gets its own step. It is the part of this
 * calculation people most often leave out by hand, and seeing ρ move from
 * 0,017241 to 0,020629 makes the 20 % it is worth impossible to miss.
 */
internal fun explainVoltageDrop(
    input: VoltageDropInput,
    result: VoltageDropResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale.toNumberSymbols())

    val resistivity = input.material.resistivityAt(input.conductorTemperatureC)
    val powerFactor = if (input.system.isAc) input.powerFactor else 1.0
    val effectiveArea = input.crossSectionMm2 * input.parallelConductors

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.vd_step_resistivity,
        formula = "ρ(θ) = ρ₂₀ · [1 + α₂₀ · (θ − 20)]",
        substitution = "${n(input.material.resistivityAt20C, RESISTIVITY_DECIMALS)} × " +
            "[1 + ${n(input.material.temperatureCoefficient, RESISTIVITY_DECIMALS)} × " +
            "(${n(input.conductorTemperatureC, 0)} − 20)]",
        result = "${n(resistivity, RESISTIVITY_DECIMALS)} Ω·mm²/m",
    )

    steps += CalculationStep(
        labelRes = R.string.vd_step_resistance,
        formula = "R = ρ(θ) · L / (A · n)",
        substitution = "${n(resistivity, RESISTIVITY_DECIMALS)} × ${n(input.lengthMetres)} / " +
            "${n(effectiveArea)}",
        result = "${n(result.conductorResistance, RESISTANCE_DECIMALS)} Ω",
    )

    steps += CalculationStep(
        labelRes = R.string.vd_step_drop,
        formula = "ΔU = k · I · R · cos φ",
        substitution = "${n(input.system.lengthMultiplier, RATIO_DECIMALS)} × " +
            "${n(input.loadCurrent)} × ${n(result.conductorResistance, RESISTANCE_DECIMALS)} × " +
            n(powerFactor, RATIO_DECIMALS),
        result = "${n(result.voltageDrop)} V",
    )

    steps += CalculationStep(
        labelRes = R.string.vd_step_percentage,
        formula = "ΔU% = ΔU / U · 100",
        substitution = "${n(result.voltageDrop)} / ${n(input.systemVoltage)} × 100",
        result = "${n(result.dropPercentage)} %",
    )

    steps += CalculationStep(
        labelRes = R.string.vd_step_loss,
        formula = "P = m · I² · R",
        substitution = "${input.system.lossConductorCount} × ${n(input.loadCurrent)}² × " +
            n(result.conductorResistance, RESISTANCE_DECIMALS),
        result = "${n(result.powerLossWatts)} W",
    )

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val RESISTANCE_DECIMALS = 4
private const val RESISTIVITY_DECIMALS = 6
