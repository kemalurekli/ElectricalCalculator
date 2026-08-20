package com.kemalurekli.electricalcalculator.features.calculators.energycost

import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.CalculateEnergyCostUseCase
import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.EnergyCostInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateEnergyCostUseCaseTest {

    private val calculate = CalculateEnergyCostUseCase()

    private fun input(
        watts: Double = 1_000.0,
        hoursPerDay: Double = 10.0,
        daysPerYear: Double = 250.0,
        tariff: Double = 3.0,
        replacementWatts: Double? = null,
        capital: Double? = null,
    ) = EnergyCostInput(
        powerWatts = watts,
        hoursPerDay = hoursPerDay,
        daysPerYear = daysPerYear,
        tariffPerKwh = tariff,
        replacementPowerWatts = replacementWatts,
        replacementCostToBuy = capital,
    )

    // -- Worked reference case ----------------------------------------------------

    @Test
    fun `worked example - 1 kW for 10 hours on 250 working days`() {
        // 1 kW × 2500 h = 2500 kWh; × 3 = 7500
        val result = calculate(input())

        assertEquals(2_500.0, result.annualEnergyKwh, 1e-9)
        assertEquals(7_500.0, result.annualCost, 1e-9)
        assertEquals(30.0, result.dailyCost, 1e-9)
        assertEquals(625.0, result.monthlyCost, 1e-9)
    }

    @Test
    fun `the monthly figure is the annual one spread evenly`() {
        // Not "this month's bill" — a twelfth of the year, which is what a
        // proposal quotes.
        val result = calculate(input())

        assertEquals(result.annualCost / 12.0, result.monthlyCost, 1e-9)
    }

    // -- Comparison and payback --------------------------------------------------------

    @Test
    fun `with no alternative there is nothing to compare or pay back`() {
        val result = calculate(input())

        assertNull(result.replacementAnnualCost)
        assertNull(result.annualSaving)
        assertNull(result.paybackYears)
        assertFalse(result.savesMoney)
    }

    @Test
    fun `worked example - replacing a 1 kW load with a 400 W one`() {
        // 2500 h × 0,4 kW × 3 = 3000/year, saving 4500, capital 9000 → 2 years
        val result = calculate(input(replacementWatts = 400.0, capital = 9_000.0))

        assertEquals(3_000.0, requireNotNull(result.replacementAnnualCost), 1e-9)
        assertEquals(4_500.0, requireNotNull(result.annualSaving), 1e-9)
        assertEquals(2.0, requireNotNull(result.paybackYears), 1e-9)
        assertTrue(result.savesMoney)
    }

    @Test
    fun `a saving without capital reports no payback`() {
        val result = calculate(input(replacementWatts = 400.0))

        assertEquals(4_500.0, requireNotNull(result.annualSaving), 1e-9)
        assertNull(result.paybackYears)
    }

    @Test
    fun `an alternative that costs more to run has no payback`() {
        // Dividing capital by a negative saving would produce a negative number
        // of years, which reads as a very good deal indeed.
        val result = calculate(input(replacementWatts = 1_500.0, capital = 9_000.0))

        assertTrue(requireNotNull(result.annualSaving) < 0.0)
        assertNull(result.paybackYears)
        assertFalse(result.savesMoney)
    }

    @Test
    fun `an alternative that draws the same has no payback either`() {
        val result = calculate(input(replacementWatts = 1_000.0, capital = 9_000.0))

        assertEquals(0.0, requireNotNull(result.annualSaving), 1e-9)
        assertNull(result.paybackYears)
    }

    // -- Behaviour ------------------------------------------------------------------------

    @Test
    fun `cost is linear in every input`() {
        val base = calculate(input()).annualCost

        assertEquals(2.0 * base, calculate(input(watts = 2_000.0)).annualCost, 1e-9)
        assertEquals(2.0 * base, calculate(input(hoursPerDay = 20.0)).annualCost, 1e-9)
        assertEquals(2.0 * base, calculate(input(tariff = 6.0)).annualCost, 1e-9)
    }

    @Test
    fun `an always-on load costs what its hours say`() {
        // 24/7 for a year is 8760 hours, which is the figure that makes standby
        // loads worth arguing about.
        val result = calculate(input(watts = 10.0, hoursPerDay = 24.0, daysPerYear = 365.0))

        assertEquals(87.6, result.annualEnergyKwh, 1e-9)
    }
}
