package com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain

import javax.inject.Inject
import kotlin.math.PI

/**
 * Computes the mass of a cable run.
 *
 * ### Conductor mass is exact
 *
 * ```
 * m = n · A · L · δ / 1000
 * ```
 *
 * with `A` in mm², `L` in m and `δ` in kg/dm³. The divisor converts mm²·m to
 * dm³: one mm² of section over one metre is 1000 mm³, which is 1 cm³, which is
 * 0.001 dm³. One mm² of copper therefore weighs 8.89 kg per kilometre — the
 * figure IEC 60228 tabulates, reproduced here rather than copied.
 *
 * ### Everything else is an estimate
 *
 * Insulation, filler and sheath cannot be derived from the conductor: their
 * thickness is a design choice of the manufacturer. What *can* be done, given
 * the overall diameter from the datasheet, is to treat the section that is not
 * conductor as solid compound of the chosen type:
 *
 * ```
 * A_total = π · D² / 4
 * m_other = (A_total − n · A) · L · δ_insulation / 1000
 * ```
 *
 * Checked against real LV cable this lands within a few percent — a 4×25 mm²
 * PVC copper cable computes at about 1.44 kg/m against a catalogue 1.4–1.5 —
 * but it is still an estimate, and it excludes steel armour entirely. Armoured
 * cable will weigh appreciably more.
 *
 * The estimate is skipped, rather than guessed at, when no diameter is given.
 */
class CalculateCableWeightUseCase @Inject constructor() {

    operator fun invoke(input: CableWeightInput): CableWeightResult {
        val conductorAreaMm2 = input.crossSectionMm2 * input.conductorCount
        val conductorVolumeDm3 = conductorAreaMm2 * input.lengthMeters / MM2_METRE_PER_DM3
        val conductorMass = conductorVolumeDm3 * input.material.densityKgPerDm3

        val overallAreaMm2 = input.overallDiameterMm?.let { PI * it * it / 4.0 }

        // A diameter smaller than the conductor bundle is not a cable, so the
        // caller is expected to have rejected it. Coercing at zero keeps a
        // rounding-level overlap from producing negative mass.
        val nonConductorMass = overallAreaMm2?.let { overall ->
            val otherAreaMm2 = (overall - conductorAreaMm2).coerceAtLeast(0.0)
            otherAreaMm2 * input.lengthMeters / MM2_METRE_PER_DM3 * input.insulation.densityKgPerDm3
        }

        val totalMass = nonConductorMass?.let { conductorMass + it }

        return CableWeightResult(
            conductorMassKg = conductorMass,
            conductorMassPerMeterKg = conductorMass / input.lengthMeters,
            conductorVolumeDm3 = conductorVolumeDm3,
            nonConductorMassKg = nonConductorMass,
            totalMassKg = totalMass,
            totalMassPerMeterKg = totalMass?.div(input.lengthMeters),
            conductorMassFraction = totalMass?.let { conductorMass / it },
        )
    }

    private companion object {
        /** mm²·m per dm³: 1 mm² × 1 m = 0.001 dm³. */
        const val MM2_METRE_PER_DM3 = 1_000.0
    }
}
