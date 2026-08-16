package com.kemalurekli.electricalcalculator.features.design.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType

/**
 * What the circuit has to carry.
 *
 * Two ways to say the same thing, because the two audiences arrive with
 * different numbers. A designer reads a load schedule in kilowatts; an
 * electrician reads a nameplate in amperes. Converting one into the other is
 * the first step of the chain, and the app should not make the user do it in
 * another screen and carry the answer back.
 */
sealed interface CircuitLoad {

    /** The load current is known outright. */
    data class Current(val amps: Double) : CircuitLoad

    /** Active power in watts, converted with the system's own phase factor. */
    data class Power(val watts: Double) : CircuitLoad
}

/**
 * Everything one circuit needs to be designed end to end.
 *
 * @param externalImpedanceOhms Ze, the loop impedance of the supply upstream of
 *   this circuit. Measured at the origin or taken from the distributor; there
 *   is no defensible way to derive it, so it is an input.
 * @param maxVoltageDropPercent the limit the drop is judged against. A limit,
 *   not a standard: IEC 60364 gives 4 % as informative guidance and installers
 *   routinely design tighter, so the number belongs to the user.
 * @param disconnectionTimeSeconds the time the circuit must clear an earth
 *   fault in — 0.4 s for a final circuit up to 63 A on a TN system, 5 s for a
 *   distribution circuit.
 */
data class CircuitDesignInput(
    val load: CircuitLoad,
    val system: SupplySystem,
    val systemVoltage: Double,
    val powerFactor: Double,
    val lengthMetres: Double,
    val material: ConductorMaterial,
    val insulation: CableInsulation,
    val method: InstallationMethod,
    val ambientTemperatureC: Double,
    val groupedCircuits: Int,
    val parallelConductors: Int,
    val maxVoltageDropPercent: Double,
    val deviceType: ProtectiveDeviceType,
    val externalImpedanceOhms: Double,
    val disconnectionTimeSeconds: Double,
)

/**
 * The requirement that decided the cable size.
 *
 * This is the output the chain exists to produce. Four separate calculators can
 * each tell a user their own answer; none of them can say *which* of the four
 * is the reason the cable is 16 mm² instead of 10 mm², and that is the fact
 * that tells an engineer what to do next. A drop-bound run gets shorter or is
 * allowed more drop; an ampacity-bound one does not, and no amount of raising
 * the drop limit will move it.
 */
enum class BindingConstraint {

    /** The conductor would overheat: Iz fell below In. */
    CURRENT_CAPACITY,

    /** The conductor would carry the current, but the drop exceeded the limit. */
    VOLTAGE_DROP,

    /** Zs was too high for the device to clear a fault in the required time. */
    EARTH_FAULT_LOOP,

    /** The protective conductor would not survive the fault energy. */
    PROTECTIVE_CONDUCTOR,

    /**
     * Nothing bound it: the smallest tabulated size already satisfied
     * everything. Common on short lighting circuits, and worth saying rather
     * than leaving the reader to infer it from a 1.5 mm² answer.
     */
    NONE,
}

/** How one stage of the chain judged the chosen size. */
data class DesignStage(
    val constraint: BindingConstraint,
    val passes: Boolean,
    /** The figure the stage produced — Iz in amps, drop in per cent, Zs in ohms. */
    val value: Double,
    /** What that figure had to stay within. */
    val limit: Double,
)

/**
 * A finished circuit design.
 *
 * Carries the whole chain, not just its answer. A result a reader cannot
 * audit is one they have to redo by hand before they will sign it.
 *
 * @param crossSectionMm2 null when no tabulated size satisfies every stage, in
 *   which case [stages] still reports how far the largest one got.
 */
data class CircuitDesignResult(
    val designCurrentAmps: Double,
    val deviceRatingAmps: Double?,
    val crossSectionMm2: Double?,
    val protectiveCrossSectionMm2: Double?,
    val bindingConstraint: BindingConstraint,
    val deratedCapacityAmps: Double,
    val voltageDropPercent: Double,
    val voltageDropVolts: Double,
    val loopImpedanceOhms: Double,
    val maximumLoopImpedanceOhms: Double,
    val stages: List<DesignStage>,
    val failure: DesignFailure? = null,
) {
    val hasSolution: Boolean get() = crossSectionMm2 != null

    companion object {
        /**
         * The result for a load no modelled device can protect.
         *
         * Nothing downstream of the device rating can be computed, so the
         * figures are absent rather than zero-shaped guesses.
         */
        fun beyondDeviceRange(designCurrentAmps: Double) = CircuitDesignResult(
            designCurrentAmps = designCurrentAmps,
            deviceRatingAmps = null,
            crossSectionMm2 = null,
            protectiveCrossSectionMm2 = null,
            bindingConstraint = BindingConstraint.NONE,
            deratedCapacityAmps = 0.0,
            voltageDropPercent = 0.0,
            voltageDropVolts = 0.0,
            loopImpedanceOhms = 0.0,
            maximumLoopImpedanceOhms = 0.0,
            stages = emptyList(),
            failure = DesignFailure.LOAD_BEYOND_DEVICE_RANGE,
        )
    }
}

/**
 * Why a design could not be produced.
 *
 * Null on [CircuitDesignResult] means the chain ran to a size. A failure still
 * carries whatever the chain managed to work out, because "no solution" alone
 * does not tell a reader whether they are one size short or designing the wrong
 * circuit entirely.
 */
enum class DesignFailure {

    /** The load exceeds the largest protective device modelled. */
    LOAD_BEYOND_DEVICE_RANGE,

    /** No tabulated cross-section satisfies every stage. */
    NO_TABULATED_SIZE,
}
