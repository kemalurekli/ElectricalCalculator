package com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial

/**
 * Which fault is being evaluated.
 *
 * The two answer different questions and neither substitutes for the other:
 *
 * - [THREE_PHASE] gives the **largest** current a device may have to interrupt,
 *   so it sets the breaking capacity.
 * - [LINE_TO_NEUTRAL] runs through the neutral as well as the line, so its loop
 *   impedance is roughly double and its current roughly half. It is the one that
 *   decides whether a device sees enough current to trip at all.
 *
 * @param voltageDivisor the `k` in `I = c·U / (k·Z)`. Three-phase current is
 *   derived from the line-to-line voltage, so it carries the √3; a line-to-
 *   neutral fault is driven by the phase voltage directly.
 */
enum class FaultType(val voltageDivisor: Double) {
    THREE_PHASE(voltageDivisor = SQRT_THREE),
    LINE_TO_NEUTRAL(voltageDivisor = 1.0),
    ;

    /** True when the return path is the neutral conductor rather than a phase. */
    val usesNeutralReturn: Boolean get() = this == LINE_TO_NEUTRAL
}

/** File-level: an enum entry cannot read its own companion during construction. */
private const val SQRT_THREE = 1.7320508075688772

/**
 * A validated set of short-circuit inputs.
 *
 * @param faultType which fault is being evaluated.
 * @param nominalVoltage the voltage that drives this fault: line-to-line for
 *   [FaultType.THREE_PHASE], line-to-neutral for [FaultType.LINE_TO_NEUTRAL].
 * @param supplyFaultCurrentAmps the prospective fault current at the origin, of
 *   the **same type** as [faultType]. Taken as the input rather than an
 *   impedance because it is what a distributor declares and what a loop tester
 *   reads; the impedance behind it is derived.
 * @param lengthMetres one-way route length to the point of interest.
 * @param crossSectionMm2 line conductor cross-section.
 * @param neutralCrossSectionMm2 neutral cross-section. Only used for
 *   [FaultType.LINE_TO_NEUTRAL]; a reduced neutral raises the loop impedance and
 *   lowers the fault current, which is exactly the case that fails to trip.
 * @param parallelConductors conductors per phase.
 * @param reactancePerKmOhms cable reactance. An input rather than a hidden
 *   constant: it varies with construction and spacing, and 0.08 Ω/km is only a
 *   typical figure for LV multicore.
 */
data class ShortCircuitInput(
    val faultType: FaultType,
    val nominalVoltage: Double,
    val supplyFaultCurrentAmps: Double,
    val lengthMetres: Double,
    val crossSectionMm2: Double,
    val neutralCrossSectionMm2: Double,
    val parallelConductors: Int,
    val material: ConductorMaterial,
    val insulation: CableInsulation,
    val reactancePerKmOhms: Double,
) {
    companion object {
        /** Typical for LV multicore cable, and the field's default assumption. */
        const val DEFAULT_REACTANCE_PER_KM = 0.08

        /**
         * Voltage factors from IEC 60909 for LV systems.
         *
         * The standard applies a tolerance to the nominal voltage in the
         * direction that makes each answer conservative: the maximum current is
         * computed with the supply 5 % high, the minimum with it 5 % low.
         */
        const val VOLTAGE_FACTOR_MAX = 1.05
        const val VOLTAGE_FACTOR_MIN = 0.95
    }
}

/**
 * The outcome of a short-circuit calculation.
 *
 * Two currents are returned because two different decisions depend on them, and
 * quoting one where the other is needed is the mistake this calculator exists to
 * prevent — the transformer calculator's own notes say its figure "is not the
 * one to use when verifying that a protective device will trip".
 *
 * @param maximumFaultCurrentAmps computed with `c = 1.05` and the conductor
 *   cold. **Select breaking capacity against this.**
 * @param minimumFaultCurrentAmps computed with `c = 0.95` and the conductor at
 *   its maximum operating temperature — a fault at the end of a fully loaded
 *   cable. **Verify disconnection against this.**
 * @param supplyImpedanceOhms impedance behind the origin, derived from the
 *   declared prospective current.
 * @param cableResistanceColdOhms loop resistance of the cable at 20 °C.
 * @param cableResistanceHotOhms the same at the insulation's rated temperature;
 *   copper gains about 20 % between 20 °C and 70 °C, and that is the whole
 *   reason the two currents differ by more than the voltage factor.
 * @param cableReactanceOhms loop reactance of the cable.
 * @param loopImpedanceColdOhms total impedance used for the maximum current.
 * @param loopImpedanceHotOhms total impedance used for the minimum current.
 * @param cableShareOfImpedance the cable's share of the hot loop impedance,
 *   from 0 to 1. Says at a glance whether the run or the supply is what limits
 *   the fault current — and a long run that has taken over is exactly the case
 *   where a device stops seeing enough current to trip instantly.
 */
data class ShortCircuitResult(
    val maximumFaultCurrentAmps: Double,
    val minimumFaultCurrentAmps: Double,
    val supplyImpedanceOhms: Double,
    val cableResistanceColdOhms: Double,
    val cableResistanceHotOhms: Double,
    val cableReactanceOhms: Double,
    val loopImpedanceColdOhms: Double,
    val loopImpedanceHotOhms: Double,
    val cableShareOfImpedance: Double,
)
