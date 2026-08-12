package com.kemalurekli.electricalcalculator.features.calculators.energycost.domain

/**
 * A validated set of running-cost inputs.
 *
 * @param powerWatts the load's power draw while it is running.
 * @param hoursPerDay how long it runs.
 * @param daysPerYear how many days a year it runs. 365 for something always on,
 *   250 for a working weekday load.
 * @param tariffPerKwh the energy price, in whatever currency the reader uses —
 *   the calculator never names one.
 * @param replacementPowerWatts the power an alternative would draw, or null when
 *   nothing is being compared.
 * @param replacementCostToBuy what the alternative costs to buy and fit, for the
 *   payback figure. Null when there is nothing to pay back.
 */
data class EnergyCostInput(
    val powerWatts: Double,
    val hoursPerDay: Double,
    val daysPerYear: Double,
    val tariffPerKwh: Double,
    val replacementPowerWatts: Double? = null,
    val replacementCostToBuy: Double? = null,
)

/**
 * The outcome of a running-cost calculation.
 *
 * ### On the payback figure
 *
 * Simple payback — capital divided by annual saving — ignores the cost of money,
 * the tariff rising, and the alternative failing early. It is what a customer
 * asks for and what a proposal is judged on, so it is what the calculator gives,
 * and the notes say what it leaves out rather than the number pretending to be
 * an investment appraisal.
 *
 * @param annualEnergyKwh energy the present load uses in a year.
 * @param annualCost what that costs.
 * @param replacementAnnualCost the same for the alternative, or null.
 * @param annualSaving what changing saves per year, or null.
 * @param paybackYears capital divided by annual saving, or null when nothing is
 *   being compared or the "saving" is negative.
 */
data class EnergyCostResult(
    val annualEnergyKwh: Double,
    val dailyCost: Double,
    val monthlyCost: Double,
    val annualCost: Double,
    val replacementAnnualCost: Double?,
    val annualSaving: Double?,
    val paybackYears: Double?,
) {
    /** True when an alternative was supplied and it actually costs less. */
    val savesMoney: Boolean get() = (annualSaving ?: 0.0) > 0.0
}
