package com.kemalurekli.electricalcalculator.features.calculators.energycost.domain

import javax.inject.Inject

/**
 * Works out what a load costs to run, and what replacing it would save.
 *
 * ```
 * kWh/year = P · h/day · days/year / 1000
 * cost     = kWh/year · tariff
 * payback  = capital / (cost_now − cost_after)
 * ```
 *
 * Arithmetically the simplest calculator in the app, and the one most often
 * asked for out loud — because it is the only one that answers a question the
 * customer asked rather than one the regulations did.
 *
 * ### The currency
 *
 * There is none. The tariff goes in and the costs come out in the same units,
 * whatever they are. Naming a currency would mean localising it, and a tool used
 * across eight languages and a dozen markets has no business assuming which.
 */
class CalculateEnergyCostUseCase @Inject constructor() {

    operator fun invoke(input: EnergyCostInput): EnergyCostResult {
        val annualHours = input.hoursPerDay * input.daysPerYear
        val annualKwh = input.powerWatts * annualHours / WATTS_PER_KW
        val annualCost = annualKwh * input.tariffPerKwh

        val replacementAnnual = input.replacementPowerWatts?.let { watts ->
            watts * annualHours / WATTS_PER_KW * input.tariffPerKwh
        }
        val saving = replacementAnnual?.let { annualCost - it }

        // A payback figure is only meaningful when there is capital to recover
        // and a saving to recover it with. A negative saving means the
        // "improvement" costs more to run, and dividing by it would produce a
        // negative number of years that reads as a very good deal.
        val payback = input.replacementCostToBuy
            ?.takeIf { it > 0.0 }
            ?.let { capital -> saving?.takeIf { it > 0.0 }?.let { capital / it } }

        return EnergyCostResult(
            annualEnergyKwh = annualKwh,
            dailyCost = input.powerWatts * input.hoursPerDay / WATTS_PER_KW * input.tariffPerKwh,
            monthlyCost = annualCost / MONTHS_PER_YEAR,
            annualCost = annualCost,
            replacementAnnualCost = replacementAnnual,
            annualSaving = saving,
            paybackYears = payback,
        )
    }

    private companion object {
        const val WATTS_PER_KW = 1_000.0
        const val MONTHS_PER_YEAR = 12.0
    }
}
