package com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial

/**
 * A validated set of cable weight inputs.
 *
 * @param crossSectionMm2 nominal area of a single conductor.
 * @param conductorCount how many conductors of that section the cable contains,
 *   counting the neutral and the protective conductor.
 * @param lengthMeters the run, or the length on the drum.
 * @param material copper or aluminium; the density difference is a factor of
 *   more than three.
 * @param overallDiameterMm the cable's outside diameter from its datasheet.
 *   Null when the user does not have it, in which case only the conductor mass
 *   — the part that can be derived exactly — is reported.
 * @param insulation the compound the non-conductor section is treated as.
 *   Ignored when [overallDiameterMm] is null.
 */
data class CableWeightInput(
    val crossSectionMm2: Double,
    val conductorCount: Int,
    val lengthMeters: Double,
    val material: ConductorMaterial,
    val overallDiameterMm: Double? = null,
    val insulation: CableInsulation = CableInsulation.PVC,
)

/**
 * The outcome of a cable weight calculation.
 *
 * The conductor figures are exact. Everything derived from the overall diameter
 * is an estimate, and is null unless a diameter was supplied — reporting a
 * guess as if it were a computed value would be worse than reporting nothing.
 *
 * @param conductorMassKg mass of metal in the whole run.
 * @param conductorMassPerMeterKg the same figure per metre, which is what a
 *   cable schedule quotes.
 * @param conductorVolumeDm3 metal volume, useful when pricing scrap by volume.
 * @param nonConductorMassKg estimated mass of insulation, filler and sheath.
 * @param totalMassKg estimated mass of the complete cable — the figure that
 *   matters for lifting a drum.
 * @param totalMassPerMeterKg estimated complete-cable mass per metre, which is
 *   what a cable tray's load rating is compared against.
 * @param conductorMassFraction conductor share of the total mass. A sanity
 *   check against the datasheet: LV copper cable normally lands between about a
 *   quarter — small sections, where the sheath dominates — and two thirds.
 *   Aluminium sits far lower for the same geometry.
 */
data class CableWeightResult(
    val conductorMassKg: Double,
    val conductorMassPerMeterKg: Double,
    val conductorVolumeDm3: Double,
    val nonConductorMassKg: Double?,
    val totalMassKg: Double?,
    val totalMassPerMeterKg: Double?,
    val conductorMassFraction: Double?,
) {
    /** True when a diameter was supplied and the complete-cable figures exist. */
    val hasTotal: Boolean get() = totalMassKg != null
}
