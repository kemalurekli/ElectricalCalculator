package com.kemalurekli.electricalcalculator.core.domain.model

import kotlin.time.Instant

/**
 * A job, and the parameters every circuit in it shares.
 *
 * ### Why the supply lives here and not on each circuit
 *
 * An installation has one supply. Its voltage, its earth loop impedance at the
 * origin, the ambient the cables run in and the cable type specified for the
 * job are true of the whole board, and repeating them on every circuit invites
 * the failure this container exists to prevent: twelve circuits designed at
 * 30 °C and one at 35 °C because a figure was retyped. Change the ambient here
 * and every circuit is redesigned against it.
 *
 * ### What is deliberately not stored
 *
 * Results. A circuit's cross-section is derived from these parameters and the
 * circuit's own, so storing it would create a second copy that goes stale the
 * moment the ambient changes. Everything is recomputed on read, which is cheap
 * — the chain is arithmetic over a table of twenty rows — and always agrees
 * with the inputs shown beside it.
 *
 * @param id row identifier; [NO_ID] before the project has been saved.
 * @param reference the user's own name for the job. Not validated and not
 *   unique: two boards on one site legitimately share a name.
 * @param externalImpedanceOhms Ze at the origin, measured or given by the
 *   distributor. There is no defensible way to derive it, so it is asked for
 *   once here rather than on every circuit.
 */
data class Project(
    val id: Long = NO_ID,
    val reference: String,
    val site: String,
    val system: SupplySystem,
    val systemVoltage: String,
    val material: ConductorMaterial,
    val insulation: CableInsulation,
    val method: InstallationMethod,
    val ambientTemperatureC: String,
    val maxVoltageDropPercent: String,
    val externalImpedanceOhms: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        const val NO_ID: Long = 0L
    }
}

/** Whether a circuit's load was given as a current or as a power. */
enum class CircuitLoadKind {
    CURRENT,
    POWER,
}

/**
 * One way out of the board.
 *
 * Holds what varies between circuits and nothing else. The device type and
 * disconnection time are here rather than on the project because they genuinely
 * differ within one installation: a final circuit clears in 0.4 s and a
 * distribution circuit in 5 s, on the same supply.
 *
 * Values are strings for the same reason the calculators' form state is: they
 * come from text fields and go back to them, and round-tripping through a
 * `Double` would rewrite what the user typed in the locale's decimal separator.
 *
 * @param position the order the circuit appears in the schedule. Explicit
 *   rather than derived from the insertion time, so a schedule can be
 *   rearranged without rewriting timestamps.
 */
data class Circuit(
    val id: Long = NO_ID,
    val projectId: Long,
    val name: String,
    val loadKind: CircuitLoadKind,
    val load: String,
    val powerFactor: String,
    val lengthMetres: String,
    val groupedCircuits: String,
    val parallelConductors: String,
    val deviceType: String,
    val disconnectionTimeSeconds: String,
    val position: Int,
) {
    companion object {
        const val NO_ID: Long = 0L
    }
}
