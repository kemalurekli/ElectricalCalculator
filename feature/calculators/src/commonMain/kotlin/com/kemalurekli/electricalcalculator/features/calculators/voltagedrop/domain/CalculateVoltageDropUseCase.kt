package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain


/**
 * Computes cable voltage drop.
 *
 * ### Method
 *
 * Conductor resistance at the operating temperature:
 *
 * ```
 * R = ρ(θ) · L / (A · n)          [Ω]
 * ρ(θ) = ρ₂₀ · [1 + α₂₀ · (θ − 20)]
 * ```
 *
 * Voltage drop, with `k` the supply multiplier (2 for DC and single phase,
 * √3 for three phase):
 *
 * ```
 * ΔU = k · I · R · cos φ          [V]
 * ```
 *
 * Power loss over every current-carrying conductor:
 *
 * ```
 * P = m · I² · R                  [W]   (m = 2, or 3 for three phase)
 * ```
 *
 * ### Assumptions
 *
 * Conductor **reactance is neglected**. That is the standard simplified method
 * and is accurate for the small cross-sections where resistance dominates
 * (roughly ≤ 16 mm²). On large conductors reactance becomes significant and the
 * result here is optimistic; for those, manufacturer mV/A/m data should be used.
 * This limitation is surfaced to the user in the calculator's engineering notes
 * rather than left implicit.
 *
 * The use case is deliberately pure: no Android types, no I/O, no suspension.
 * That is what makes the engineering verifiable in plain unit tests.
 */
class CalculateVoltageDropUseCase() {

    operator fun invoke(input: VoltageDropInput): VoltageDropResult {
        val resistivity = input.material.resistivityAt(input.conductorTemperatureC)

        // Effective conductor area: running conductors in parallel multiplies
        // the area available to the current, dividing resistance by n.
        val effectiveAreaMm2 = input.crossSectionMm2 * input.parallelConductors
        val resistance = resistivity * input.lengthMetres / effectiveAreaMm2

        // Power factor applies only to AC; on DC there is no phase angle, and
        // folding a cos φ into it would silently under-report the drop.
        val powerFactor = if (input.system.isAc) input.powerFactor else 1.0

        val voltageDrop = input.system.lengthMultiplier *
            input.loadCurrent *
            resistance *
            powerFactor

        val dropPercentage = voltageDrop / input.systemVoltage * PERCENT

        val powerLoss = input.system.lossConductorCount *
            input.loadCurrent * input.loadCurrent *
            resistance

        return VoltageDropResult(
            voltageDrop = voltageDrop,
            dropPercentage = dropPercentage,
            voltageAtLoad = input.systemVoltage - voltageDrop,
            conductorResistance = resistance,
            powerLossWatts = powerLoss,
            status = VoltageDropStatus.forPercentage(dropPercentage),
        )
    }

    private companion object {
        const val PERCENT = 100.0
    }
}
