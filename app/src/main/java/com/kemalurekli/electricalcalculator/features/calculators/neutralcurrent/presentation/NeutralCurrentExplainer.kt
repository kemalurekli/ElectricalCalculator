package com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.NeutralCurrentInput
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.NeutralCurrentResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Locale

/**
 * Turns a neutral current calculation into the worked solution behind it.
 *
 * The two contributions are kept apart the whole way down, because the point of
 * this calculator is that they behave in opposite directions: the unbalance term
 * goes to zero as the board is balanced, and the triplen term does not move at
 * all. Combining them earlier would produce one number that hides which of the
 * two the reader actually has to fix.
 */
internal fun explainNeutralCurrent(
    input: NeutralCurrentInput,
    result: NeutralCurrentResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale.toNumberSymbols())

    val (i1, i2, i3) = input.lineCurrents
    val average = (i1 + i2 + i3) / PHASES

    return persistentListOf(
        CalculationStep(
            labelRes = R.string.nc_step_unbalance,
            formula = "I_u = √(I₁² + I₂² + I₃² − I₁I₂ − I₂I₃ − I₃I₁)",
            substitution = "√(${n(i1)}² + ${n(i2)}² + ${n(i3)}² − " +
                "${n(i1)}×${n(i2)} − ${n(i2)}×${n(i3)} − ${n(i3)}×${n(i1)})",
            result = "${n(result.fundamentalNeutralAmps)} A",
        ),
        CalculationStep(
            labelRes = R.string.nc_step_triplen,
            formula = "I_3 = 3 · I_avg · h₃ / 100",
            substitution = "3 × ${n(average)} × ${n(input.thirdHarmonicPercent)} / 100",
            result = "${n(result.triplenNeutralAmps)} A",
        ),
        CalculationStep(
            labelRes = R.string.nc_step_total,
            formula = "I_N = √(I_u² + I_3²)",
            substitution = "√(${n(result.fundamentalNeutralAmps)}² + " +
                "${n(result.triplenNeutralAmps)}²)",
            result = "${n(result.neutralCurrentAmps)} A",
        ),
        CalculationStep(
            labelRes = R.string.nc_step_ratio,
            formula = "I_N / I_max · 100",
            substitution = "${n(result.neutralCurrentAmps)} / " +
                "${n(input.highestLineCurrent)} × 100",
            result = "${n(result.neutralToHighestLineRatio * PERCENT)} %",
        ),
    )
}

private const val DECIMALS = 2
private const val PHASES = 3.0
private const val PERCENT = 100.0
