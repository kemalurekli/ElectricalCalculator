package com.kemalurekli.electricalcalculator.features.calculators.motor.domain

import com.kemalurekli.electricalcalculator.core.domain.model.PowerUnit
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem

/**
 * A validated set of motor inputs.
 *
 * @param ratedPower the nameplate rating — **mechanical output at the shaft**,
 *   not electrical input. This distinction is the single most common source of
 *   error in motor current calculations; see [CalculateMotorCurrentUseCase].
 * @param powerUnit the unit [ratedPower] is expressed in.
 * @param efficiency η as a fraction (0 < η ≤ 1).
 * @param powerFactor cos φ at full load. Ignored on a DC supply.
 * @param startingCurrentRatio locked-rotor current as a multiple of full load
 *   current. Around 6–7 for a direct-on-line squirrel cage motor.
 */
data class MotorInput(
    val ratedPower: Double,
    val powerUnit: PowerUnit,
    val voltage: Double,
    val efficiency: Double,
    val powerFactor: Double,
    val startingCurrentRatio: Double,
    val system: SupplySystem,
)

/**
 * The outcome of a motor current calculation.
 *
 * @param fullLoadCurrent line current at rated output, in A.
 * @param startingCurrent locked-rotor current, in A.
 * @param inputPowerWatts electrical power drawn, `P_out / η`.
 * @param apparentPowerVa `P_in / cos φ`, in VA.
 * @param reactivePowerVar the reactive component, in var. Zero on DC.
 * @param lossesWatts `P_in − P_out`, the heat the motor dissipates.
 */
data class MotorResult(
    val fullLoadCurrent: Double,
    val startingCurrent: Double,
    val inputPowerWatts: Double,
    val apparentPowerVa: Double,
    val reactivePowerVar: Double,
    val lossesWatts: Double,
)
