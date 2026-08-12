package com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain

import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Verifies that an earth fault disconnects in time and that the protective
 * conductor survives it.
 *
 * ### The loop
 *
 * ```
 * Z_s = Z_e + R₁ + R₂
 * ```
 *
 * `R₁` is the line conductor and `R₂` the protective conductor, both at the
 * insulation's rated operating temperature — a fault is most likely on a warm
 * cable, and a warm cable has the highest resistance and so the lowest fault
 * current. Taking them cold would flatter the result in exactly the direction
 * that matters.
 *
 * ### Disconnection
 *
 * ```
 * Z_s,max = c_min · U₀ / I_a          overcurrent device
 * Z_s,max = U_L / I_Δn                residual current device
 * ```
 *
 * These are the same check written two ways, and both are reported because
 * engineers reason in both: *"is my loop under the limit"* and *"does enough
 * current flow to trip it"*.
 *
 * ### Withstand
 *
 * ```
 * S ≥ √(I² · t) / k
 * ```
 *
 * A circuit that disconnects correctly can still cook its earth conductor on
 * the way, so this is a separate verdict rather than a footnote.
 */
class CalculateEarthFaultUseCase @Inject constructor() {

    operator fun invoke(input: EarthFaultInput): EarthFaultResult {
        val resistivity = input.material.resistivityAt(input.insulation.maxConductorTemperatureC)

        val lineResistance = resistivity * input.lengthMetres /
            (input.lineCrossSectionMm2 * input.parallelConductors)
        val protectiveResistance = resistivity * input.lengthMetres /
            (input.protectiveCrossSectionMm2 * input.parallelConductors)

        val loopImpedance = input.externalImpedanceOhms + lineResistance + protectiveResistance

        val operatingCurrent = operatingCurrent(input)
        val maximumPermitted = maximumPermittedImpedance(input, operatingCurrent)

        val faultCurrent = EarthFaultInput.VOLTAGE_FACTOR_MIN * input.phaseVoltage / loopImpedance

        val adiabaticFactor = AdiabaticFactors.forProtectiveConductor(
            material = input.material,
            insulation = input.insulation,
        )
        val adiabaticMinimum =
            sqrt(faultCurrent * faultCurrent * input.clearingTimeSeconds) / adiabaticFactor

        return EarthFaultResult(
            loopImpedanceOhms = loopImpedance,
            maximumPermittedOhms = maximumPermitted,
            faultCurrentAmps = faultCurrent,
            operatingCurrentAmps = operatingCurrent,
            lineResistanceOhms = lineResistance,
            protectiveResistanceOhms = protectiveResistance,
            adiabaticMinimumMm2 = adiabaticMinimum,
            adiabaticFactor = adiabaticFactor,
            tabulatedMinimumMm2 = AdiabaticFactors.tabulatedProtectiveSection(
                input.lineCrossSectionMm2,
            ),
            protectiveCrossSectionMm2 = input.protectiveCrossSectionMm2,
        )
    }

    /**
     * The current that operates the device, or null for an RCD.
     *
     * An RCD responds to the imbalance between line and neutral, not to how big
     * the fault current is, so quoting an operating current for one would be
     * describing a mechanism it does not have.
     */
    private fun operatingCurrent(input: EarthFaultInput): Double? = when {
        input.deviceType.isResidualCurrent -> null
        input.deviceType.instantaneousMultiplier != null ->
            input.deviceType.instantaneousMultiplier * input.deviceRatingAmps
        // CUSTOM: the rating field already holds Ia, read off the device curve.
        else -> input.deviceRatingAmps
    }

    private fun maximumPermittedImpedance(
        input: EarthFaultInput,
        operatingCurrent: Double?,
    ): Double = if (operatingCurrent == null) {
        // Touch voltage, not operating current: the loop may be high as long as
        // the voltage it puts on exposed metal stays safe. This is why a TT
        // installation works at all.
        EarthFaultInput.TOUCH_VOLTAGE_LIMIT / input.deviceRatingAmps
    } else {
        EarthFaultInput.VOLTAGE_FACTOR_MIN * input.phaseVoltage / operatingCurrent
    }
}
