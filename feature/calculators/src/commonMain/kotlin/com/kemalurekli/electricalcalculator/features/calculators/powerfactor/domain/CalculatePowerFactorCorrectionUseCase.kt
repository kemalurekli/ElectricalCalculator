package com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain

import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.tan

/**
 * Sizes the capacitor bank needed to raise a power factor.
 *
 * ### Method
 *
 * Correction adds reactive power without touching active power, so P is the
 * fixed side of the triangle and only Q moves:
 *
 * ```
 * Q_c = P · (tan φ₁ − tan φ₂)     [var]
 * ```
 *
 * The capacitance follows from the reactive power of a capacitor, `Q = n · U²
 * · ωC`, where `n` depends on the connection — 3 for delta, 1 for star against
 * the line-to-line voltage:
 *
 * ```
 * C = Q_c / (n · 2π f · U²)       [F]
 * ```
 *
 * ### Why the released capacity matters
 *
 * Correcting the power factor lowers the apparent power for the same real
 * work, which frees capacity in the transformer and cabling. That freed
 * capacity is reported because it is usually the number that justifies the
 * project — it can defer a supply upgrade entirely.
 *
 * ### Assumptions
 *
 * A fixed bank sized for the stated load. A real installation varies, so
 * automatic (stepped) correction is normal; sizing a fixed bank for peak load
 * over-compensates at light load and can push the power factor leading, which
 * some tariffs also penalise. Harmonics are not considered — on a distorted
 * supply, capacitors can resonate with the supply inductance and a detuned
 * reactor is required.
 */
class CalculatePowerFactorCorrectionUseCase() {

    operator fun invoke(input: PowerFactorInput): PowerFactorResult {
        val tanBefore = tan(acos(input.existingPowerFactor.coerceIn(-1.0, 1.0)))
        val tanAfter = tan(acos(input.targetPowerFactor.coerceIn(-1.0, 1.0)))

        val requiredCapacitor = input.activePowerWatts * (tanBefore - tanAfter)

        val apparentBefore = input.activePowerWatts / input.existingPowerFactor
        val apparentAfter = input.activePowerWatts / input.targetPowerFactor

        val phaseFactor = input.system.powerPhaseFactor
        val angularFrequency = 2.0 * PI * input.frequencyHz

        // Single phase has no star/delta distinction; the capacitor simply sits
        // across the supply.
        val divisor = if (input.system == SupplySystem.THREE_PHASE_AC) {
            input.connection.capacitanceDivisor
        } else {
            1.0
        }

        return PowerFactorResult(
            requiredCapacitorVar = requiredCapacitor,
            capacitancePerPhaseFarads = requiredCapacitor /
                (divisor * angularFrequency * input.voltage * input.voltage),
            reactiveBeforeVar = input.activePowerWatts * tanBefore,
            reactiveAfterVar = input.activePowerWatts * tanAfter,
            apparentBeforeVa = apparentBefore,
            apparentAfterVa = apparentAfter,
            currentBeforeAmps = apparentBefore / (phaseFactor * input.voltage),
            currentAfterAmps = apparentAfter / (phaseFactor * input.voltage),
            releasedCapacityVa = apparentBefore - apparentAfter,
        )
    }
}
