package com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CapacitorConnection
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem

/**
 * A validated set of power factor correction inputs.
 *
 * @param activePowerWatts the load's active power P. Correction does not change
 *   it — that is the whole point — so it anchors the calculation.
 * @param existingPowerFactor cos φ₁, the measured or billed power factor.
 * @param targetPowerFactor cos φ₂, what the installation should reach. Must
 *   exceed [existingPowerFactor]; correcting downward is not a thing.
 * @param voltage line-to-line voltage for three phase.
 * @param frequencyHz supply frequency, needed for the capacitance.
 * @param connection how the bank is wired. Ignored on single phase.
 */
data class PowerFactorInput(
    val activePowerWatts: Double,
    val existingPowerFactor: Double,
    val targetPowerFactor: Double,
    val voltage: Double,
    val frequencyHz: Double,
    val connection: CapacitorConnection,
    val system: SupplySystem,
)

/**
 * The outcome of a power factor correction calculation.
 *
 * @param requiredCapacitorVar Q_c, the reactive power the bank must supply.
 * @param capacitancePerPhaseFarads the capacitance of one unit, which depends
 *   on the connection.
 * @param releasedCapacityVa the apparent power freed up in the transformer and
 *   cabling. Often the strongest argument for correcting at all: it defers
 *   upgrading the supply.
 */
data class PowerFactorResult(
    val requiredCapacitorVar: Double,
    val capacitancePerPhaseFarads: Double,
    val reactiveBeforeVar: Double,
    val reactiveAfterVar: Double,
    val apparentBeforeVa: Double,
    val apparentAfterVa: Double,
    val currentBeforeAmps: Double,
    val currentAfterAmps: Double,
    val releasedCapacityVa: Double,
)
