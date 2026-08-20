package com.kemalurekli.electricalcalculator.features.calculators.battery.domain

import kotlin.math.pow

/**
 * Computes how long a battery bank can carry a load.
 *
 * ### Peukert's law is the whole point
 *
 * A battery's nameplate capacity is only valid at the rate it was measured at —
 * lead-acid is normally rated over a 20-hour discharge. Draw the same bank
 * harder and you get materially *less* than the nameplate amp-hours, because
 * faster discharge is less efficient.
 *
 * The naive `t = C / I` ignores this and overestimates runtime, badly at high
 * rates. Peukert's law captures it:
 *
 * ```
 * t = H · (I_rated / I)^k        where I_rated = C / H
 * ```
 *
 * `k = 1` collapses to the naive result; real lead-acid sits around 1.1–1.3 and
 * lithium near 1.05. Both figures are returned so the user can see what the
 * effect costs them rather than having to trust it.
 *
 * ### Depth of discharge
 *
 * Applied to the resulting time, not to the capacity: you discharge for a
 * fraction of the full-discharge duration. Because Peukert is non-linear, the
 * two orderings are not equivalent, and this is the conventional one.
 *
 * ### Assumptions
 *
 * A constant load, a healthy bank at its nameplate capacity, and a nominal
 * temperature. Capacity falls with age and with cold — roughly 20 % at 0 °C for
 * lead-acid — so a design should carry margin beyond this figure.
 */
class CalculateBatteryRuntimeUseCase() {

    operator fun invoke(input: BatteryInput): BatteryResult {
        // Power drawn from the bank is larger than the load by the losses in
        // the inverter and wiring.
        val dischargeCurrent = input.loadPowerWatts /
            (input.bankVoltage * input.systemEfficiency)

        val ratedCurrent = input.capacityAh / input.ratedDischargeHours

        val fullDischargeHours = input.ratedDischargeHours *
            (ratedCurrent / dischargeCurrent).pow(input.peukertExponent)

        val runtimeHours = fullDischargeHours * input.depthOfDischarge
        val idealRuntimeHours = input.capacityAh / dischargeCurrent * input.depthOfDischarge

        return BatteryResult(
            runtimeHours = runtimeHours,
            idealRuntimeHours = idealRuntimeHours,
            dischargeCurrentAmps = dischargeCurrent,
            cRate = dischargeCurrent / input.capacityAh,
            energyDeliveredWh = input.loadPowerWatts * runtimeHours,
            usableCapacityAh = input.capacityAh * input.depthOfDischarge,
        )
    }
}
