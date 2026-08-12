package com.kemalurekli.electricalcalculator.features.calculators.lighting.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.LightingInput
import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.LightingResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Locale

/**
 * Turns a lumen-method calculation into the worked solution behind it.
 *
 * The unrounded count is its own step. 4,05 and 4,95 both become five fittings
 * and are entirely different conversations — one is a design with margin, the
 * other is one that only just made it — and the rounded figure alone hides which
 * one the reader is looking at.
 */
internal fun explainLighting(
    input: LightingInput,
    result: LightingResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale)

    return persistentListOf(
        CalculationStep(
            labelRes = R.string.lt_step_area,
            formula = "A = L · W",
            substitution = "${n(input.roomLengthMetres)} × ${n(input.roomWidthMetres)}",
            result = "${n(result.areaSquareMetres)} m²",
        ),
        CalculationStep(
            labelRes = R.string.lt_step_room_index,
            formula = "K = (L · W) / (Hm · (L + W))",
            substitution = "${n(result.areaSquareMetres)} / (${n(input.mountingHeightMetres)} × " +
                "(${n(input.roomLengthMetres)} + ${n(input.roomWidthMetres)}))",
            result = n(result.roomIndex, RATIO_DECIMALS),
        ),
        CalculationStep(
            labelRes = R.string.lt_step_exact_count,
            formula = "N = (E · A) / (Φ · UF · MF)",
            substitution = "(${n(input.targetIlluminanceLux)} × ${n(result.areaSquareMetres)}) / " +
                "(${n(input.luminousFluxPerLuminaireLumens)} × " +
                "${n(input.utilisationFactor, RATIO_DECIMALS)} × " +
                "${n(input.maintenanceFactor, RATIO_DECIMALS)})",
            result = n(result.exactLuminaireCount, RATIO_DECIMALS),
        ),
        CalculationStep(
            labelRes = R.string.lt_step_rounded,
            formula = "N = ⌈N⌉",
            substitution = "⌈${n(result.exactLuminaireCount, RATIO_DECIMALS)}⌉",
            result = "${result.luminaireCount}",
        ),
        CalculationStep(
            labelRes = R.string.lt_step_achieved,
            formula = "E = (N · Φ · UF · MF) / A",
            substitution = "(${result.luminaireCount} × " +
                "${n(input.luminousFluxPerLuminaireLumens)} × " +
                "${n(input.utilisationFactor, RATIO_DECIMALS)} × " +
                "${n(input.maintenanceFactor, RATIO_DECIMALS)}) / " +
                n(result.areaSquareMetres),
            result = "${n(result.achievedIlluminanceLux)} lx",
        ),
    )
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
