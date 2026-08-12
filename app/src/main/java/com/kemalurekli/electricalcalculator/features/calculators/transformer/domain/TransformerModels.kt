package com.kemalurekli.electricalcalculator.features.calculators.transformer.domain

import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem

/**
 * A validated set of transformer inputs.
 *
 * @param ratingKva nameplate apparent power S in kVA. Transformers are rated in
 *   apparent power, not active power, because their loading limit is set by
 *   current and voltage rather than by the load's power factor.
 * @param primaryVoltage rated primary voltage U₁ in V, line-to-line for three phase.
 * @param secondaryVoltage rated secondary voltage U₂ in V, line-to-line for three phase.
 * @param impedanceVoltagePercent short-circuit impedance u_k in %, from the
 *   nameplate. Typically 4 % up to 630 kVA and 6 % above.
 * @param system single or three phase. DC is not a transformer arrangement.
 */
data class TransformerInput(
    val ratingKva: Double,
    val primaryVoltage: Double,
    val secondaryVoltage: Double,
    val impedanceVoltagePercent: Double,
    val system: SupplySystem,
)

/**
 * The outcome of a transformer current calculation.
 *
 * @param primaryCurrent full load current on the primary side, in A.
 * @param secondaryCurrent full load current on the secondary side, in A.
 * @param voltageRatio U₁ / U₂, the nameplate ratio.
 * @param secondaryShortCircuitCurrent prospective symmetrical short-circuit
 *   current at the secondary terminals, in A.
 * @param shortCircuitPowerKva apparent power the transformer can deliver into a
 *   terminal fault, in kVA.
 */
data class TransformerResult(
    val primaryCurrent: Double,
    val secondaryCurrent: Double,
    val voltageRatio: Double,
    val secondaryShortCircuitCurrent: Double,
    val shortCircuitPowerKva: Double,
)
