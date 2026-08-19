package com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.ConduitFillInput
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.ConduitFillResult
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.FillRule
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

/**
 * Turns a conduit fill calculation into the worked solution behind it.
 *
 * Every cable in the bundle appears by name in the area sum rather than being
 * collapsed into a total. A fill result that disagrees with the reader's own
 * arithmetic almost always comes down to a quantity or a diameter typed wrong,
 * and the summed line is where that becomes obvious.
 *
 * The permitted limit is written into the spare-area step instead of standing on
 * its own, because on its own it would be a table lookup dressed up as a
 * calculation. Inside the subtraction it does what the reader needs: says which
 * percentage this bundle is being judged against, at the moment the judgement is
 * made.
 */
internal fun explainConduitFill(
    input: ConduitFillInput,
    result: ConduitFillResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale.toNumberSymbols())

    val permittedAfterOneMore = when (input.rule) {
        FillRule.NEC_TABLE_1 -> FillRule.necTable1(result.cableCount + 1)
        FillRule.CUSTOM -> input.customLimitFraction
    }

    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.cf_step_conduit_area,
        formula = "A = π · D² / 4",
        substitution = "π × ${n(input.conduitInnerDiameterMm)}² / 4",
        result = "${n(result.conduitAreaMm2)} mm²",
    )

    steps += CalculationStep(
        labelRes = R.string.cf_step_cable_area,
        formula = "A_c = Σ(n · π · d² / 4)",
        substitution = input.cables.joinToString(" + ") { entry ->
            "${entry.quantity} × π × ${n(entry.diameterMm)}² / 4"
        },
        result = "${n(result.cableAreaMm2)} mm²",
    )

    steps += CalculationStep(
        labelRes = R.string.cf_step_fill,
        formula = "fill = A_c / A · 100",
        substitution = "${n(result.cableAreaMm2)} / ${n(result.conduitAreaMm2)} × 100",
        result = "${n(result.fillFraction * PERCENT)} %",
    )

    steps += CalculationStep(
        labelRes = R.string.cf_step_spare,
        formula = "A_spare = A · fill_max − A_c",
        substitution = "${n(result.conduitAreaMm2)} × " +
            "${n(result.permittedFraction, RATIO_DECIMALS)} − ${n(result.cableAreaMm2)}",
        result = "${n(result.spareAreaMm2)} mm²",
    )

    steps += CalculationStep(
        labelRes = R.string.cf_step_additional,
        formula = "d_add = √(4 · (A · fill_max' − A_c) / π)",
        substitution = "√(4 × (${n(result.conduitAreaMm2)} × " +
            "${n(permittedAfterOneMore, RATIO_DECIMALS)} − ${n(result.cableAreaMm2)}) / π)",
        result = "${n(result.largestAdditionalCableMm)} mm",
    )

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATIO_DECIMALS = 3
private const val PERCENT = 100.0
