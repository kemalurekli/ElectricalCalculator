package com.kemalurekli.electricalcalculator.features.calculators.solarstring

import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.CalculateSolarStringUseCase
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.SolarStringInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The cold case is the one these tests are really about. A string sized on the
 * nameplate 25 °C voltage exceeds the inverter's maximum on the first cold clear
 * morning, which destroys equipment before the array has generated anything.
 */
class CalculateSolarStringUseCaseTest {

    private val calculate = CalculateSolarStringUseCase()

    private fun input(
        voc: Double = 49.5,
        vmp: Double = 41.5,
        beta: Double = -0.27,
        minTemp: Double = -10.0,
        maxTemp: Double = 70.0,
        inverterMax: Double = 1_000.0,
        mpptMin: Double = 200.0,
    ) = SolarStringInput(
        moduleVocVolts = voc,
        moduleVmpVolts = vmp,
        voltageCoefficientPercentPerK = beta,
        minimumCellTemperatureC = minTemp,
        maximumCellTemperatureC = maxTemp,
        inverterMaxDcVolts = inverterMax,
        inverterMpptMinVolts = mpptMin,
    )

    // -- Worked reference case ------------------------------------------------------

    @Test
    fun `worked example - a 49point5 V module on a 1000 V inverter`() {
        // Voc(-10) = 49,5 × (1 + (-0,27) × (-35) / 100) = 49,5 × 1,0945 = 54,178
        // n_max = ⌊1000 / 54,178⌋ = 18
        // Vmp(70) = 41,5 × (1 + (-0,27) × 45 / 100) = 41,5 × 0,8785 = 36,458
        // n_min = ⌈200 / 36,458⌉ = 6
        val result = calculate(input())

        assertEquals(54.1778, result.vocAtMinimumTemperature, 1e-4)
        assertEquals(36.4577, result.vmpAtMaximumTemperature, 1e-4)
        assertEquals(18, result.maximumModules)
        assertEquals(6, result.minimumModules)
        assertTrue(result.isFeasible)
    }

    // -- The direction of each correction ---------------------------------------------

    @Test
    fun `a cold module produces more than its nameplate`() {
        // The whole reason this calculation exists.
        val result = calculate(input(minTemp = -20.0))

        assertTrue(result.vocAtMinimumTemperature > 49.5)
    }

    @Test
    fun `a hot module produces less than its nameplate`() {
        val result = calculate(input(maxTemp = 75.0))

        assertTrue(result.vmpAtMaximumTemperature < 41.5)
    }

    @Test
    fun `at 25 degrees the corrections do nothing`() {
        val result = calculate(input(minTemp = 25.0, maxTemp = 25.0))

        assertEquals(49.5, result.vocAtMinimumTemperature, 1e-9)
        assertEquals(41.5, result.vmpAtMaximumTemperature, 1e-9)
    }

    // -- The rounding is not symmetric ---------------------------------------------------

    @Test
    fun `the maximum rounds down and the string stays under the limit`() {
        // One module too many is the damaging direction, so the maximum can
        // never be the value that just exceeds the inverter.
        val result = calculate(input())

        assertTrue(
            "a full string exceeds the inverter maximum",
            result.stringVocAtMaximum <= 1_000.0,
        )
        assertTrue(
            "one more module would still fit, so the maximum is too low",
            (result.maximumModules + 1) * result.vocAtMinimumTemperature > 1_000.0,
        )
    }

    @Test
    fun `the minimum rounds up and the string stays inside the MPPT window`() {
        val result = calculate(input())

        assertTrue(
            "the shortest string falls below the MPPT window",
            result.stringVmpAtMinimum >= 200.0,
        )
        assertTrue(
            "one fewer module would still track, so the minimum is too high",
            (result.minimumModules - 1) * result.vmpAtMaximumTemperature < 200.0,
        )
    }

    // -- Sensitivity ------------------------------------------------------------------------

    @Test
    fun `a colder site allows fewer modules in series`() {
        val mild = calculate(input(minTemp = 0.0)).maximumModules
        val cold = calculate(input(minTemp = -25.0)).maximumModules

        assertTrue("the colder site should be more restrictive", cold < mild)
    }

    @Test
    fun `a flatter temperature coefficient allows more modules`() {
        // Thin film runs nearer -0,2 %/K, crystalline nearer -0,35.
        val steep = calculate(input(beta = -0.40)).maximumModules
        val flat = calculate(input(beta = -0.20)).maximumModules

        assertTrue(flat > steep)
    }

    @Test
    fun `a module and inverter that cannot work together are reported as such`() {
        // A 1500 V module string on a 300 V inverter window: the cold maximum
        // falls below the MPPT minimum and no string length satisfies both.
        val result = calculate(input(inverterMax = 60.0, mpptMin = 200.0))

        assertFalse(result.isFeasible)
    }

    @Test
    fun `an inverter smaller than one module admits no string at all`() {
        val result = calculate(input(inverterMax = 40.0))

        assertEquals(0, result.maximumModules)
        assertFalse(result.isFeasible)
    }
}
