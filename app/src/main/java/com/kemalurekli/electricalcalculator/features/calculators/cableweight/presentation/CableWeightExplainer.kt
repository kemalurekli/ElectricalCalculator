package com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightInput
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale
import kotlin.math.PI

/**
 * Turns a cable weight calculation into the worked solution behind it.
 *
 * The solution splits where the certainty does. Conductor mass follows from
 * geometry and density and is exact; everything after the overall diameter
 * treats the rest of the section as solid compound, which is an approximation of
 * a manufacturer's design choice. The steps are laid out in that order so the
 * reader can see where the arithmetic stops being derivation and starts being an
 * estimate — and the estimate half is absent entirely when no diameter was
 * given, rather than being filled in with a guess.
 */
internal fun explainCableWeight(
    input: CableWeightInput,
    result: CableWeightResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale)

    val conductorAreaMm2 = input.crossSectionMm2 * input.conductorCount

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.cw_step_conductor_area,
        formula = "A_c = n · A",
        substitution = "${input.conductorCount} × ${n(input.crossSectionMm2)}",
        result = "${n(conductorAreaMm2)} mm²",
    )

    steps += CalculationStep(
        labelRes = R.string.cw_step_volume,
        formula = "V = A_c · L / 1000",
        substitution = "${n(conductorAreaMm2)} × ${n(input.lengthMeters)} / " +
            n(MM2_METRE_PER_DM3, 0),
        result = "${n(result.conductorVolumeDm3)} dm³",
    )

    steps += CalculationStep(
        labelRes = R.string.cw_step_conductor_mass,
        formula = "m_c = V · δ",
        substitution = "${n(result.conductorVolumeDm3)} × " +
            n(input.material.densityKgPerDm3, RATIO_DECIMALS),
        result = "${n(result.conductorMassKg)} kg",
    )

    steps += CalculationStep(
        labelRes = R.string.cw_step_conductor_per_metre,
        formula = "m_c/L",
        substitution = "${n(result.conductorMassKg)} / ${n(input.lengthMeters)}",
        result = "${n(result.conductorMassPerMeterKg, MASS_DECIMALS)} kg/m",
    )

    val diameter = input.overallDiameterMm
    val nonConductorMass = result.nonConductorMassKg
    val totalMass = result.totalMassKg
    val totalPerMetre = result.totalMassPerMeterKg
    val fraction = result.conductorMassFraction

    if (diameter != null &&
        nonConductorMass != null &&
        totalMass != null &&
        totalPerMetre != null &&
        fraction != null
    ) {
        val overallArea = PI * diameter * diameter / 4.0

        steps += CalculationStep(
            labelRes = R.string.cw_step_overall_area,
            formula = "A_t = π · D² / 4",
            substitution = "π × ${n(diameter)}² / 4",
            result = "${n(overallArea)} mm²",
        )

        steps += CalculationStep(
            labelRes = R.string.cw_step_sheath_mass,
            formula = "m_s = (A_t − A_c) · L · δ_s / 1000",
            substitution = "(${n(overallArea)} − ${n(conductorAreaMm2)}) × " +
                "${n(input.lengthMeters)} × " +
                "${n(input.insulation.densityKgPerDm3, RATIO_DECIMALS)} / " +
                n(MM2_METRE_PER_DM3, 0),
            result = "${n(nonConductorMass)} kg",
        )

        steps += CalculationStep(
            labelRes = R.string.cw_step_total_mass,
            formula = "m = m_c + m_s",
            substitution = "${n(result.conductorMassKg)} + ${n(nonConductorMass)}",
            result = "${n(totalMass)} kg",
        )

        steps += CalculationStep(
            labelRes = R.string.cw_step_total_per_metre,
            formula = "m/L",
            substitution = "${n(totalMass)} / ${n(input.lengthMeters)}",
            result = "${n(totalPerMetre, MASS_DECIMALS)} kg/m",
        )

        steps += CalculationStep(
            labelRes = R.string.cw_step_conductor_share,
            formula = "f = m_c / m · 100",
            substitution = "${n(result.conductorMassKg)} / ${n(totalMass)} × 100",
            result = "${n(fraction * PERCENT)} %",
        )
    }

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val MASS_DECIMALS = 4
private const val PERCENT = 100.0

/** mm²·m per dm³: 1 mm² × 1 m = 0.001 dm³. */
private const val MM2_METRE_PER_DM3 = 1_000.0
