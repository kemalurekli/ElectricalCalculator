package com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultInput
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

/**
 * Turns an earth fault loop calculation into the worked solution behind it.
 *
 * Two verdicts come out of this calculator and the steps keep them apart. The
 * disconnection chain ends at `Z_s` against `Z_s,max`; the withstand chain runs
 * on past it, through the fault current, to the smallest protective conductor
 * that survives. A circuit can pass the first and fail the second — that is how
 * an installation ends up disconnecting correctly and melting its earth on the
 * way — so the two are never allowed to blur into one number.
 *
 * The resistivity correction leads, because R₁ and R₂ are routinely worked out
 * from a 20 °C table. A fault is most likely on a warm cable, a warm cable is
 * more resistive, and less current flows than the cold figure promises. That is
 * the one direction of error a disconnection check cannot afford, so the step
 * that causes it is shown first.
 *
 * A residual current device has no operating-current step. It responds to the
 * imbalance between line and neutral rather than to the size of the fault
 * current, and inventing an `I_a` for one would describe a mechanism it does not
 * have.
 */
internal fun explainEarthFault(
    input: EarthFaultInput,
    result: EarthFaultResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale.toNumberSymbols())

    val resistivity = input.material.resistivityAt(input.insulation.maxConductorTemperatureC)
    val operatingCurrent = result.operatingCurrentAmps

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.ef_step_resistivity,
        formula = "ρ(θ) = ρ₂₀ · [1 + α₂₀ · (θ − 20)]",
        substitution = "${n(input.material.resistivityAt20C, RESISTIVITY_DECIMALS)} × " +
            "[1 + ${n(input.material.temperatureCoefficient, RESISTIVITY_DECIMALS)} × " +
            "(${n(input.insulation.maxConductorTemperatureC, 0)} − 20)]",
        result = "${n(resistivity, RESISTIVITY_DECIMALS)} Ω·mm²/m",
    )

    steps += CalculationStep(
        labelRes = R.string.ef_step_line_resistance,
        formula = "R₁ = ρ(θ) · L / (A₁ · n)",
        substitution = "${n(resistivity, RESISTIVITY_DECIMALS)} × ${n(input.lengthMetres)} / " +
            "(${n(input.lineCrossSectionMm2)} × ${input.parallelConductors})",
        result = "${n(result.lineResistanceOhms, IMPEDANCE_DECIMALS)} Ω",
    )

    steps += CalculationStep(
        labelRes = R.string.ef_step_protective_resistance,
        formula = "R₂ = ρ(θ) · L / (A₂ · n)",
        substitution = "${n(resistivity, RESISTIVITY_DECIMALS)} × ${n(input.lengthMetres)} / " +
            "(${n(input.protectiveCrossSectionMm2)} × ${input.parallelConductors})",
        result = "${n(result.protectiveResistanceOhms, IMPEDANCE_DECIMALS)} Ω",
    )

    steps += CalculationStep(
        labelRes = R.string.ef_step_loop,
        formula = "Z_s = Z_e + R₁ + R₂",
        substitution = "${n(input.externalImpedanceOhms, IMPEDANCE_DECIMALS)} + " +
            "${n(result.lineResistanceOhms, IMPEDANCE_DECIMALS)} + " +
            n(result.protectiveResistanceOhms, IMPEDANCE_DECIMALS),
        result = "${n(result.loopImpedanceOhms, IMPEDANCE_DECIMALS)} Ω",
    )

    if (operatingCurrent != null) {
        val multiplier = input.deviceType.instantaneousMultiplier
        if (multiplier != null) {
            steps += CalculationStep(
                labelRes = R.string.ef_step_operating_current,
                formula = "I_a = m · I_n",
                substitution = "${n(multiplier, 0)} × ${n(input.deviceRatingAmps)}",
                result = "${n(operatingCurrent)} A",
            )
        }

        steps += CalculationStep(
            labelRes = R.string.ef_step_maximum_impedance,
            formula = "Z_s,max = c · U₀ / I_a",
            substitution = "${n(EarthFaultInput.VOLTAGE_FACTOR_MIN, RATIO_DECIMALS)} × " +
                "${n(input.phaseVoltage)} / ${n(operatingCurrent)}",
            result = "${n(result.maximumPermittedOhms, IMPEDANCE_DECIMALS)} Ω",
        )
    } else {
        steps += CalculationStep(
            labelRes = R.string.ef_step_maximum_impedance_rcd,
            formula = "Z_s,max = U_L / I_Δn",
            substitution = "${n(EarthFaultInput.TOUCH_VOLTAGE_LIMIT, 0)} / " +
                n(input.deviceRatingAmps, RESIDUAL_DECIMALS),
            result = "${n(result.maximumPermittedOhms, IMPEDANCE_DECIMALS)} Ω",
        )
    }

    steps += CalculationStep(
        labelRes = R.string.ef_step_fault_current,
        formula = "I_f = c · U₀ / Z_s",
        substitution = "${n(EarthFaultInput.VOLTAGE_FACTOR_MIN, RATIO_DECIMALS)} × " +
            "${n(input.phaseVoltage)} / ${n(result.loopImpedanceOhms, IMPEDANCE_DECIMALS)}",
        result = "${n(result.faultCurrentAmps)} A",
    )

    steps += CalculationStep(
        labelRes = R.string.ef_step_adiabatic,
        formula = "S ≥ √(I_f² · t) / k",
        substitution = "√(${n(result.faultCurrentAmps)}² × " +
            "${n(input.clearingTimeSeconds, RATIO_DECIMALS)}) / " +
            n(result.adiabaticFactor, 0),
        result = "${n(result.adiabaticMinimumMm2)} mm²",
    )

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val RESISTIVITY_DECIMALS = 6
private const val IMPEDANCE_DECIMALS = 4

/** IΔn is quoted in fractions of an amp: 0.03 for a 30 mA device. */
private const val RESIDUAL_DECIMALS = 3
