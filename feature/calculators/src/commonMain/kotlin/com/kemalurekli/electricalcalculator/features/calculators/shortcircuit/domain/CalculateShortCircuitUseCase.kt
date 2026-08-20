package com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain

import kotlin.math.hypot

/**
 * Computes the prospective fault current at a point in an installation.
 *
 * ### Method
 *
 * The impedance behind the origin is recovered from the declared prospective
 * current, the cable's own impedance is added, and the current at the far end
 * follows:
 *
 * ```
 * Z_supply = c · U / (k · I_supply)
 * R_cable  = ρ(θ) · L / (A · n)      per conductor in the loop
 * Z_cable  = √(R² + X²)
 * I_fault  = c · U / (k · (Z_supply + Z_cable))
 * ```
 *
 * ### Why two currents come out
 *
 * They answer different questions and neither substitutes for the other:
 *
 * - **Maximum** — `c = 1.05`, conductor at 20 °C. The largest current a device
 *   might have to interrupt, so it selects breaking capacity.
 * - **Minimum** — `c = 0.95`, conductor at its rated operating temperature. A
 *   fault at the end of a fully loaded cable, when resistance is highest and
 *   the supply is low. This is what verifies that the device actually trips.
 *
 * Copper gains about 20 % resistance between 20 °C and 70 °C, so on a long run
 * the two figures are far apart, and quoting the wrong one either over-specifies
 * a breaker or — much worse — declares a circuit protected when it is not.
 *
 * ### Scalar impedance addition
 *
 * Supply and cable impedances are added as magnitudes rather than as complex
 * numbers. That is the method IEC 60364 practice and every loop impedance tester
 * use, and it is what the declared figures support: a distributor publishes a
 * prospective current, not an R/X split.
 *
 * The two impedances have different angles — a transformer-fed supply is mostly
 * reactive, an LV cable mostly resistive — so the scalar sum slightly
 * **overestimates** the loop. The resulting current is therefore a little low.
 * That is the safe direction for verifying disconnection, and the conservative
 * direction is stated in the calculator's notes for breaking-capacity work,
 * where a rigorous IEC 60909 study should be used close to the origin.
 */
class CalculateShortCircuitUseCase() {

    operator fun invoke(input: ShortCircuitInput): ShortCircuitResult {
        // The supply impedance is whatever produces the declared current at the
        // origin. Derived with c_max so that it and the maximum current below
        // describe the same network rather than two different ones.
        val supplyImpedance = ShortCircuitInput.VOLTAGE_FACTOR_MAX * input.nominalVoltage /
            (input.faultType.voltageDivisor * input.supplyFaultCurrentAmps)

        val coldResistance = loopResistance(input, ConductorMaterial20C)
        val hotResistance = loopResistance(input, input.insulation.maxConductorTemperatureC)
        val reactance = loopReactance(input)

        val cableCold = hypot(coldResistance, reactance)
        val cableHot = hypot(hotResistance, reactance)

        val loopCold = supplyImpedance + cableCold
        val loopHot = supplyImpedance + cableHot

        return ShortCircuitResult(
            maximumFaultCurrentAmps = current(input, ShortCircuitInput.VOLTAGE_FACTOR_MAX, loopCold),
            minimumFaultCurrentAmps = current(input, ShortCircuitInput.VOLTAGE_FACTOR_MIN, loopHot),
            supplyImpedanceOhms = supplyImpedance,
            cableResistanceColdOhms = coldResistance,
            cableResistanceHotOhms = hotResistance,
            cableReactanceOhms = reactance,
            loopImpedanceColdOhms = loopCold,
            loopImpedanceHotOhms = loopHot,
            cableShareOfImpedance = cableHot / loopHot,
        )
    }

    private fun current(input: ShortCircuitInput, voltageFactor: Double, impedance: Double): Double =
        voltageFactor * input.nominalVoltage / (input.faultType.voltageDivisor * impedance)

    /**
     * Resistance of the whole fault loop through the cable.
     *
     * A three-phase fault is driven by the per-phase impedance, so only the line
     * conductor counts. A line-to-neutral fault returns through the neutral, so
     * both conductors are in the loop — and a reduced neutral makes that loop
     * worse, which is the case most likely to fail to trip.
     */
    private fun loopResistance(input: ShortCircuitInput, temperatureC: Double): Double {
        val resistivity = input.material.resistivityAt(temperatureC)
        val line = resistivity * input.lengthMetres /
            (input.crossSectionMm2 * input.parallelConductors)

        if (!input.faultType.usesNeutralReturn) return line

        val neutral = resistivity * input.lengthMetres /
            (input.neutralCrossSectionMm2 * input.parallelConductors)
        return line + neutral
    }

    /** Reactance of the same loop; the return conductor contributes equally. */
    private fun loopReactance(input: ShortCircuitInput): Double {
        val conductors = if (input.faultType.usesNeutralReturn) 2.0 else 1.0
        return conductors * input.reactancePerKmOhms * input.lengthMetres /
            METRES_PER_KM / input.parallelConductors
    }

    private companion object {
        /** The temperature the tabulated resistivities are referenced to. */
        const val ConductorMaterial20C = 20.0

        const val METRES_PER_KM = 1_000.0
    }
}
