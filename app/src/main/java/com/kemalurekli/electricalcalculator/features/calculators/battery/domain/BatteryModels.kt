package com.kemalurekli.electricalcalculator.features.calculators.battery.domain

/**
 * A validated set of battery runtime inputs.
 *
 * @param capacityAh nameplate capacity, which is only meaningful alongside the
 *   rate it was measured at — see [ratedDischargeHours].
 * @param ratedDischargeHours the discharge time the capacity is rated for,
 *   `H` in Peukert's equation. Lead-acid is normally rated at C/20, so 20;
 *   lithium is often rated at C/1 or C/5.
 * @param bankVoltage nominal DC voltage of the bank.
 * @param loadPowerWatts the load drawn from the system, in W.
 * @param systemEfficiency inverter and wiring efficiency as a fraction. Use 1.0
 *   for a load fed directly from DC.
 * @param depthOfDischarge the usable fraction of a full discharge. Draining a
 *   lead-acid bank below about 50 % shortens its life sharply; lithium tolerates
 *   80–90 %.
 * @param peukertExponent `k`, how sharply usable capacity falls as the
 *   discharge rate rises. 1.0 is an ideal battery.
 */
data class BatteryInput(
    val capacityAh: Double,
    val ratedDischargeHours: Double,
    val bankVoltage: Double,
    val loadPowerWatts: Double,
    val systemEfficiency: Double,
    val depthOfDischarge: Double,
    val peukertExponent: Double,
)

/**
 * The outcome of a battery runtime calculation.
 *
 * @param runtimeHours usable runtime down to the depth-of-discharge limit.
 * @param idealRuntimeHours the same figure with Peukert ignored (`k = 1`).
 *   Reported alongside so the size of the effect is visible rather than
 *   implicit — at high discharge rates the gap is large.
 * @param dischargeCurrentAmps the DC current drawn from the bank.
 * @param cRate discharge current as a multiple of the nameplate capacity.
 * @param energyDeliveredWh the energy the load actually receives.
 */
data class BatteryResult(
    val runtimeHours: Double,
    val idealRuntimeHours: Double,
    val dischargeCurrentAmps: Double,
    val cRate: Double,
    val energyDeliveredWh: Double,
    val usableCapacityAh: Double,
)
