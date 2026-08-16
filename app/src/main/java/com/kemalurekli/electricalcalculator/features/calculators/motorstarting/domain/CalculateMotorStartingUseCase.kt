package com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain

import kotlin.math.sqrt
import javax.inject.Inject

/**
 * The voltage the supply sags to while a motor runs up.
 *
 * ### Why this exists
 *
 * The app's motor calculator already reports full-load current, and its own
 * note says the starting analysis wants the *peak* — the figure that decides
 * whether the lights flicker, whether the contactors hold in, and whether the
 * motor develops enough torque to turn at all. That figure was never computed.
 *
 * ### The method
 *
 * A transformer is a voltage source behind an impedance, and its short-circuit
 * power is what expresses that impedance in units the rest of the problem is
 * already in:
 *
 * ```
 * S_sc = S_transformer / (uk / 100)
 * ΔU/U = S_start / (S_sc + S_start)
 * ```
 *
 * The `+ S_start` in the denominator is not decoration. Dropping it gives the
 * small-dip approximation, which is fine at 3 % and overstates badly at 30 % —
 * precisely the region a motor start lands in, and the region where the answer
 * matters.
 *
 * ### What is left out
 *
 * The cable between transformer and motor, which adds its own impedance and
 * makes the real dip at the motor deeper than this. The star-delta transition
 * peak, which can approach the direct-on-line current for a few cycles. And
 * motor power factor during start, taken as the arithmetic sum rather than the
 * phasor sum — starting power factor is low, so this errs toward a larger dip
 * rather than a smaller one, which is the safe direction to be wrong in.
 */
class CalculateMotorStartingUseCase @Inject constructor() {

    operator fun invoke(input: MotorStartingInput): MotorStartingResult {
        val startingCurrent =
            input.fullLoadCurrentAmps * input.lockedRotorMultiple * input.method.currentFactor

        // Three-phase apparent power, in kVA to match the transformer's rating.
        val startingKva = sqrt(3.0) * input.supplyVoltage * startingCurrent / 1000.0

        val shortCircuitKva = if (input.transformerImpedancePercent > 0.0) {
            input.transformerKva / (input.transformerImpedancePercent / 100.0)
        } else {
            // A zero-impedance transformer is an infinite bus: no dip at all.
            Double.POSITIVE_INFINITY
        }

        val dipFraction = if (shortCircuitKva.isInfinite()) {
            0.0
        } else {
            startingKva / (shortCircuitKva + startingKva)
        }

        val residual = input.supplyVoltage * (1.0 - dipFraction)

        // Torque follows the square of the voltage actually at the terminals,
        // on top of whatever the starting method already gives up.
        val voltageRatio = 1.0 - dipFraction
        val torquePercent = input.method.torqueFactor * voltageRatio * voltageRatio * 100.0

        return MotorStartingResult(
            startingCurrentAmps = startingCurrent,
            startingKva = startingKva,
            shortCircuitKva = shortCircuitKva,
            dipPercent = dipFraction * 100.0,
            residualVoltage = residual,
            startingTorquePercent = torquePercent,
        )
    }
}
