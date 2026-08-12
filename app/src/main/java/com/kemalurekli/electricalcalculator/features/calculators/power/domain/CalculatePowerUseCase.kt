package com.kemalurekli.electricalcalculator.features.calculators.power.domain

import com.kemalurekli.electricalcalculator.core.domain.model.PowerFactorType
import javax.inject.Inject
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Resolves the power triangle from a measured voltage and current.
 *
 * ### Method
 *
 * With `k` the phase factor (1 for DC and single phase, √3 for three phase
 * against the line-to-line voltage):
 *
 * ```
 * S = k · U · I            [VA]
 * P = S · cos φ            [W]
 * Q = ± S · sin φ          [var]
 * ```
 *
 * The sign of Q comes from [PowerFactorType]: an inductive (lagging) load
 * consumes reactive power, a capacitive (leading) one supplies it.
 *
 * `sin φ` is derived as `√(1 − cos²φ)` rather than through `sin(acos(x))`.
 * Both are correct, but the direct form avoids a round trip through the angle
 * and stays exact at cos φ = 1, where the trigonometric route can leave a tiny
 * residue that shows up as a non-zero reactive power on a purely resistive load.
 *
 * ### Assumptions
 *
 * Sinusoidal quantities and a balanced three-phase system. On a distorted
 * supply the apparent power also carries a distortion component, and `S² = P² +
 * Q²` no longer holds — that case needs true-RMS measurement rather than this
 * calculation.
 */
class CalculatePowerUseCase @Inject constructor() {

    operator fun invoke(input: PowerInput): PowerResult {
        val apparentPower = input.system.powerPhaseFactor * input.voltage * input.current

        // DC has no phase angle, so all of it is active power.
        val isAc = input.system.isAc
        val powerFactor = if (isAc) input.powerFactor else 1.0

        val activePower = apparentPower * powerFactor

        // coerceIn guards the root against floating-point overshoot when the
        // power factor is exactly 1.
        val sinPhi = sqrt((1.0 - powerFactor * powerFactor).coerceAtLeast(0.0))
        val reactivePower = if (isAc) {
            apparentPower * sinPhi * input.powerFactorType.reactiveSign
        } else {
            0.0
        }

        val phaseAngle = Math.toDegrees(acos(powerFactor.coerceIn(-1.0, 1.0)))

        return PowerResult(
            activePowerWatts = activePower,
            reactivePowerVar = reactivePower,
            apparentPowerVa = apparentPower,
            phaseAngleDegrees = phaseAngle * if (isAc) input.powerFactorType.reactiveSign else 1.0,
            // Guarded so that a zero active power reports no ratio instead of
            // an infinity that would render as "∞" in the result card.
            tangentPhi = if (activePower == 0.0) 0.0 else reactivePower / activePower,
        )
    }
}
