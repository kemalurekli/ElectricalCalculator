package com.kemalurekli.electricalcalculator.features.calculators.power.domain

import com.kemalurekli.electricalcalculator.core.domain.model.PowerFactorType
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem

/**
 * A validated set of power inputs, measured at the supply.
 *
 * @param voltage line-to-line voltage for three phase, otherwise the supply voltage.
 * @param current line current in A.
 * @param powerFactor cos φ magnitude, 0 to 1. Ignored on a DC supply.
 * @param powerFactorType whether the current lags or leads. Sets the sign of Q.
 */
data class PowerInput(
    val voltage: Double,
    val current: Double,
    val powerFactor: Double,
    val powerFactorType: PowerFactorType,
    val system: SupplySystem,
)

/**
 * The three powers and the phase relationship between them.
 *
 * @param activePowerWatts P, the power that does work.
 * @param reactivePowerVar Q, signed: positive when the load consumes reactive
 *   power (lagging), negative when it supplies it (leading).
 * @param apparentPowerVa S, what the supply and cabling must be rated for.
 * @param phaseAngleDegrees φ, the angle between voltage and current.
 * @param tangentPhi Q/P. Many tariffs bill reactive energy against a tan φ
 *   threshold rather than against cos φ, so it is reported directly.
 */
data class PowerResult(
    val activePowerWatts: Double,
    val reactivePowerVar: Double,
    val apparentPowerVa: Double,
    val phaseAngleDegrees: Double,
    val tangentPhi: Double,
)
