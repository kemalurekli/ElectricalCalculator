package com.kemalurekli.electricalcalculator.core.domain.model

/**
 * Cable insulation type, identified by the conductor temperature it is rated
 * for at full load.
 *
 * The rating is what sets both the current-carrying capacity and the resistivity
 * correction: an XLPE cable may run 20 K hotter than PVC, which raises its
 * ampacity but also its resistance.
 *
 * @param maxConductorTemperatureC the rated conductor temperature at full load.
 * @param densityKgPerDm3 density of the compound, used by the cable weight
 *   calculator to estimate the mass of everything that is not conductor.
 *   Cable-grade PVC is filled and plasticised, so it is considerably denser
 *   than cross-linked polyethylene.
 */
enum class CableInsulation(
    val maxConductorTemperatureC: Double,
    val densityKgPerDm3: Double,
) {
    /** Thermoplastic (PVC), 70 °C conductor. */
    PVC(maxConductorTemperatureC = 70.0, densityKgPerDm3 = 1.40),

    /** Thermoset (XLPE / EPR), 90 °C conductor. */
    XLPE(maxConductorTemperatureC = 90.0, densityKgPerDm3 = 0.92),
}

/**
 * Reference installation method from IEC 60364-5-52 Table B.52.1.
 *
 * The method determines how well the cable sheds heat, and so how much current
 * it may carry. Only the methods that dominate real installations are modelled;
 * each maps to one column of the current-carrying capacity tables.
 */
enum class InstallationMethod {
    /** B1 — insulated conductors in conduit on or in a wall. */
    B1_CONDUIT_ON_WALL,

    /** B2 — multi-core cable in conduit on or in a wall. */
    B2_MULTICORE_IN_CONDUIT,

    /** C — cable clipped direct to a surface. */
    C_CLIPPED_DIRECT,

    /** E — multi-core cable in free air or on a perforated tray. */
    E_FREE_AIR,
}

/** How many conductors of a circuit carry load current, which sets the table column. */
enum class LoadedConductors {
    /** DC or single-phase: line and neutral. */
    TWO,

    /** Three-phase: three lines, with a balanced load carrying no neutral current. */
    THREE,
    ;

    companion object {
        fun forSystem(system: SupplySystem): LoadedConductors =
            if (system == SupplySystem.THREE_PHASE_AC) THREE else TWO
    }
}

/**
 * The preferred conductor cross-sections of IEC 60228.
 *
 * Sizing must land on a size that is actually manufactured, so the calculators
 * select from this ladder rather than returning an arbitrary computed area.
 */
object StandardCrossSection {

    /** Nominal areas in mm², ascending. */
    val allMm2: List<Double> = listOf(
        1.5, 2.5, 4.0, 6.0, 10.0, 16.0, 25.0, 35.0, 50.0,
        70.0, 95.0, 120.0, 150.0, 185.0, 240.0, 300.0,
    )

    /** The smallest standard size at or above [minimumAreaMm2], or null if none is large enough. */
    fun smallestAtLeast(minimumAreaMm2: Double): Double? =
        allMm2.firstOrNull { it >= minimumAreaMm2 }
}
