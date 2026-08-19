package com.kemalurekli.electricalcalculator.features.calculators.energycost.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.toNumberSymbols
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.EnergyCostInput
import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.EnergyCostResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

/**
 * Turns a running-cost calculation into the worked solution behind it.
 *
 * The annual hours get their own line. A load's cost is dominated by how long it
 * runs rather than by what it draws, and 2 500 hours written out is what makes
 * that visible — it is the number a customer argues with, not the watts.
 *
 * The comparison steps appear only when there is something to compare. A row
 * reading "saving: 0" where no alternative was described would look like a
 * finding rather than an absence.
 */
internal fun explainEnergyCost(
    input: EnergyCostInput,
    result: EnergyCostResult,
    locale: Locale = Locale.getDefault(),
): ImmutableList<CalculationStep> {
    fun n(value: Double, decimals: Int = DECIMALS) =
        NumberFormatter.format(value, decimals, locale.toNumberSymbols())

    val annualHours = input.hoursPerDay * input.daysPerYear
    val steps = mutableListOf<CalculationStep>()

    steps += CalculationStep(
        labelRes = R.string.ec_step_hours,
        formula = "h = h/day · days",
        substitution = "${n(input.hoursPerDay)} × ${n(input.daysPerYear)}",
        result = "${n(annualHours)} h",
    )

    steps += CalculationStep(
        labelRes = R.string.ec_step_energy,
        formula = "E = P · h / 1000",
        substitution = "${n(input.powerWatts)} × ${n(annualHours)} / ${n(WATTS_PER_KW, 0)}",
        result = "${n(result.annualEnergyKwh)} kWh",
    )

    steps += CalculationStep(
        labelRes = R.string.ec_step_cost,
        formula = "C = E · tariff",
        substitution = "${n(result.annualEnergyKwh)} × ${n(input.tariffPerKwh, RATE_DECIMALS)}",
        result = n(result.annualCost),
    )

    val replacementCost = result.replacementAnnualCost
    val saving = result.annualSaving
    if (replacementCost != null && saving != null) {
        steps += CalculationStep(
            labelRes = R.string.ec_step_replacement,
            formula = "C₂ = P₂ · h / 1000 · tariff",
            substitution = "${n(input.replacementPowerWatts ?: 0.0)} × ${n(annualHours)} / " +
                "${n(WATTS_PER_KW, 0)} × ${n(input.tariffPerKwh, RATE_DECIMALS)}",
            result = n(replacementCost),
        )
        steps += CalculationStep(
            labelRes = R.string.ec_step_saving,
            formula = "ΔC = C − C₂",
            substitution = "${n(result.annualCost)} − ${n(replacementCost)}",
            result = n(saving),
        )
    }

    result.paybackYears?.let { payback ->
        steps += CalculationStep(
            labelRes = R.string.ec_step_payback,
            formula = "t = capital / ΔC",
            substitution = "${n(input.replacementCostToBuy ?: 0.0)} / ${n(saving ?: 0.0)}",
            result = "${n(payback)} a",
        )
    }

    return steps.toImmutableList()
}

private const val DECIMALS = 2
private const val RATE_DECIMALS = 4
private const val WATTS_PER_KW = 1_000.0
