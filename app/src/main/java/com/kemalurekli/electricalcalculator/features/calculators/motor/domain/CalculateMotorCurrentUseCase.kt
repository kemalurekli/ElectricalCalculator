package com.kemalurekli.electricalcalculator.features.calculators.motor.domain

import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Computes the full load current of a motor.
 *
 * ### The efficiency step is not optional
 *
 * A motor's nameplate power is its **mechanical output at the shaft**. The
 * electrical power it draws is larger, by the efficiency:
 *
 * ```
 * P_in = P_out / η
 * ```
 *
 * Omitting that division — treating the nameplate kW as if it were electrical
 * input — under-reports the current by 8–15 % for a typical induction motor.
 * That is enough to undersize a cable or a protective device, so the efficiency
 * is a required input here rather than an optional refinement.
 *
 * ### Current
 *
 * With `k` the phase factor (1 for DC and single phase, √3 for three phase):
 *
 * ```
 * I = P_in / (k · U · cos φ)
 * ```
 *
 * Power factor is dropped on DC, where there is no phase angle.
 *
 * ### Power triangle
 *
 * ```
 * S = P_in / cos φ            Q = √(S² − P_in²)
 * ```
 *
 * ### Assumptions
 *
 * Efficiency and power factor are both taken at full load; both fall
 * substantially at part load, so the result is not valid for a lightly loaded
 * motor. Starting current is the steady-state locked-rotor value and excludes
 * the initial DC transient.
 */
class CalculateMotorCurrentUseCase @Inject constructor() {

    operator fun invoke(input: MotorInput): MotorResult {
        val outputWatts = input.powerUnit.toWatts(input.ratedPower)
        val inputWatts = outputWatts / input.efficiency

        // No phase angle on DC, so the full input power is active power.
        val powerFactor = if (input.system.isAc) input.powerFactor else 1.0

        val fullLoadCurrent = inputWatts /
            (input.system.powerPhaseFactor * input.voltage * powerFactor)

        val apparentPower = inputWatts / powerFactor
        // Guarded against a negative under the root, which floating-point error
        // can produce at unity power factor where S and P are equal.
        val reactivePower = sqrt(
            (apparentPower * apparentPower - inputWatts * inputWatts).coerceAtLeast(0.0),
        )

        return MotorResult(
            fullLoadCurrent = fullLoadCurrent,
            startingCurrent = fullLoadCurrent * input.startingCurrentRatio,
            inputPowerWatts = inputWatts,
            apparentPowerVa = apparentPower,
            reactivePowerVar = reactivePower,
            lossesWatts = inputWatts - outputWatts,
        )
    }
}
