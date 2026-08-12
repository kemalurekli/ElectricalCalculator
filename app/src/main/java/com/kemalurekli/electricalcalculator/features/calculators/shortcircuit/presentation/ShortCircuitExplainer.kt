package com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.ShortCircuitInput
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.ShortCircuitResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

/**
 * Turns a short-circuit calculation into the worked solution behind it.
 *
 * The two currents are worked separately and in full, including the impedance
 * each is built on, because the entire point of this calculator is that they are
 * not interchangeable. Laid out one after the other, the reason they differ
 * stops being an assertion in the notes: the resistance line changes because the
 * conductor is hot, and the voltage factor changes because the standard says to
 * assume the supply is low. Both effects push the same way, and a reader who has
 * seen the two chains will not quote the maximum where the minimum was needed.
 *
 * On a line-to-neutral fault the resistance substitution shows the line and
 * neutral terms as separate addends. A reduced neutral is the case most likely
 * to fail to trip, and folding the two into one number would hide the conductor
 * that caused it.
 */
internal fun explainShortCircuit(
    input: ShortCircuitInput,
    result: ShortCircuitResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale)

    val divisor = input.faultType.voltageDivisor
    val coldResistivity = input.material.resistivityAt20C
    val hotResistivity = input.material.resistivityAt(input.insulation.maxConductorTemperatureC)
    val returnConductors = if (input.faultType.usesNeutralReturn) 2 else 1

    /** `ρ · L / (A · n)`, with the neutral term appended when it is in the loop. */
    fun resistanceSubstitution(resistivity: Double): String {
        val line = "${n(resistivity, RESISTIVITY_DECIMALS)} × ${n(input.lengthMetres)} / " +
            "(${n(input.crossSectionMm2)} × ${input.parallelConductors})"
        if (!input.faultType.usesNeutralReturn) return line

        val neutral = "${n(resistivity, RESISTIVITY_DECIMALS)} × ${n(input.lengthMetres)} / " +
            "(${n(input.neutralCrossSectionMm2)} × ${input.parallelConductors})"
        return "$line + $neutral"
    }

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.sc_step_supply_impedance,
        formula = "Z_s = c_max · U / (k · I_s)",
        substitution = "${n(ShortCircuitInput.VOLTAGE_FACTOR_MAX, RATIO_DECIMALS)} × " +
            "${n(input.nominalVoltage)} / (${n(divisor, RATIO_DECIMALS)} × " +
            "${n(input.supplyFaultCurrentAmps)})",
        result = "${n(result.supplyImpedanceOhms, IMPEDANCE_DECIMALS)} Ω",
    )

    steps += CalculationStep(
        labelRes = R.string.sc_step_resistance_cold,
        formula = "R₂₀ = ρ₂₀ · L / (A · n)",
        substitution = resistanceSubstitution(coldResistivity),
        result = "${n(result.cableResistanceColdOhms, IMPEDANCE_DECIMALS)} Ω",
    )

    steps += CalculationStep(
        labelRes = R.string.sc_step_resistance_hot,
        formula = "R_θ = ρ(θ) · L / (A · n)",
        substitution = resistanceSubstitution(hotResistivity),
        result = "${n(result.cableResistanceHotOhms, IMPEDANCE_DECIMALS)} Ω",
    )

    steps += CalculationStep(
        labelRes = R.string.sc_step_reactance,
        formula = "X = m · x · L / 1000 / n",
        substitution = "$returnConductors × ${n(input.reactancePerKmOhms, RATIO_DECIMALS)} × " +
            "${n(input.lengthMetres)} / ${n(METRES_PER_KM, 0)} / ${input.parallelConductors}",
        result = "${n(result.cableReactanceOhms, IMPEDANCE_DECIMALS)} Ω",
    )

    steps += CalculationStep(
        labelRes = R.string.sc_step_loop_cold,
        formula = "Z_cold = Z_s + √(R₂₀² + X²)",
        substitution = "${n(result.supplyImpedanceOhms, IMPEDANCE_DECIMALS)} + " +
            "√(${n(result.cableResistanceColdOhms, IMPEDANCE_DECIMALS)}² + " +
            "${n(result.cableReactanceOhms, IMPEDANCE_DECIMALS)}²)",
        result = "${n(result.loopImpedanceColdOhms, IMPEDANCE_DECIMALS)} Ω",
    )

    steps += CalculationStep(
        labelRes = R.string.sc_step_maximum_current,
        formula = "I_max = c_max · U / (k · Z_cold)",
        substitution = "${n(ShortCircuitInput.VOLTAGE_FACTOR_MAX, RATIO_DECIMALS)} × " +
            "${n(input.nominalVoltage)} / (${n(divisor, RATIO_DECIMALS)} × " +
            "${n(result.loopImpedanceColdOhms, IMPEDANCE_DECIMALS)})",
        result = "${n(result.maximumFaultCurrentAmps)} A",
    )

    steps += CalculationStep(
        labelRes = R.string.sc_step_loop_hot,
        formula = "Z_hot = Z_s + √(R_θ² + X²)",
        substitution = "${n(result.supplyImpedanceOhms, IMPEDANCE_DECIMALS)} + " +
            "√(${n(result.cableResistanceHotOhms, IMPEDANCE_DECIMALS)}² + " +
            "${n(result.cableReactanceOhms, IMPEDANCE_DECIMALS)}²)",
        result = "${n(result.loopImpedanceHotOhms, IMPEDANCE_DECIMALS)} Ω",
    )

    steps += CalculationStep(
        labelRes = R.string.sc_step_minimum_current,
        formula = "I_min = c_min · U / (k · Z_hot)",
        substitution = "${n(ShortCircuitInput.VOLTAGE_FACTOR_MIN, RATIO_DECIMALS)} × " +
            "${n(input.nominalVoltage)} / (${n(divisor, RATIO_DECIMALS)} × " +
            "${n(result.loopImpedanceHotOhms, IMPEDANCE_DECIMALS)})",
        result = "${n(result.minimumFaultCurrentAmps)} A",
    )

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val RESISTIVITY_DECIMALS = 6
private const val IMPEDANCE_DECIMALS = 4
private const val METRES_PER_KM = 1_000.0
