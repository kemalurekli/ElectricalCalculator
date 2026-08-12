package com.kemalurekli.electricalcalculator.features.calculators.transformer

import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.CalculateTransformerCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.TransformerInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * Several cases are pinned against figures a substation engineer knows by
 * heart — 1000 kVA at 400 V is 1443 A, 630 kVA is 909 A. Those act as an
 * independent check on the formula, not just on the arithmetic.
 */
class CalculateTransformerCurrentUseCaseTest {

    private val calculate = CalculateTransformerCurrentUseCase()

    private fun input(
        ratingKva: Double = 1000.0,
        primary: Double = 34_500.0,
        secondary: Double = 400.0,
        impedancePercent: Double = 6.0,
        system: SupplySystem = SupplySystem.THREE_PHASE_AC,
    ) = TransformerInput(
        ratingKva = ratingKva,
        primaryVoltage = primary,
        secondaryVoltage = secondary,
        impedanceVoltagePercent = impedancePercent,
        system = system,
    )

    // -- Full load current ----------------------------------------------------

    @Test
    fun `three phase full load current matches the standard figures`() {
        // 1000 kVA at 400 V is the canonical 1443 A.
        assertEquals(1443.3757, calculate(input()).secondaryCurrent, 1e-4)
        // 630 kVA at 400 V is the canonical 909 A.
        assertEquals(
            909.3266,
            calculate(input(ratingKva = 630.0)).secondaryCurrent,
            1e-4,
        )
    }

    @Test
    fun `three phase uses the root three phase factor`() {
        val result = calculate(input())

        assertEquals(1_000_000.0 / (sqrt(3.0) * 34_500.0), result.primaryCurrent, 1e-9)
        assertEquals(16.7348, result.primaryCurrent, 1e-4)
    }

    @Test
    fun `single phase divides by the voltage alone`() {
        // S = U · I, with no √3 and — critically — no factor of 2. The cable
        // length multiplier does not apply to a power calculation.
        val result = calculate(
            input(ratingKva = 25.0, primary = 11_000.0, secondary = 230.0, system = SupplySystem.SINGLE_PHASE_AC),
        )

        assertEquals(25_000.0 / 230.0, result.secondaryCurrent, 1e-9)
        assertEquals(108.6957, result.secondaryCurrent, 1e-4)
        assertEquals(25_000.0 / 11_000.0, result.primaryCurrent, 1e-9)
    }

    @Test
    fun `three phase draws less current than single phase for the same rating`() {
        val single = calculate(input(system = SupplySystem.SINGLE_PHASE_AC)).secondaryCurrent
        val three = calculate(input(system = SupplySystem.THREE_PHASE_AC)).secondaryCurrent

        assertEquals(1.0 / sqrt(3.0), three / single, 1e-9)
    }

    @Test
    fun `current scales linearly with rating`() {
        val base = calculate(input(ratingKva = 500.0)).secondaryCurrent
        val doubled = calculate(input(ratingKva = 1000.0)).secondaryCurrent

        assertEquals(2.0 * base, doubled, 1e-9)
    }

    @Test
    fun `current is inversely proportional to voltage`() {
        val low = calculate(input(secondary = 400.0)).secondaryCurrent
        val high = calculate(input(secondary = 800.0)).secondaryCurrent

        assertEquals(low / 2.0, high, 1e-9)
    }

    @Test
    fun `the primary carries less current than the secondary when stepping down`() {
        val result = calculate(input())

        assertTrue(result.primaryCurrent < result.secondaryCurrent)
        // Ampere-turns balance: the current ratio is the inverse of the voltage ratio.
        assertEquals(
            result.voltageRatio,
            result.secondaryCurrent / result.primaryCurrent,
            1e-6,
        )
    }

    // -- Voltage ratio ---------------------------------------------------------

    @Test
    fun `voltage ratio is primary over secondary`() {
        assertEquals(86.25, calculate(input()).voltageRatio, 1e-9)
    }

    @Test
    fun `a step-up transformer has a ratio below one`() {
        val result = calculate(input(primary = 400.0, secondary = 34_500.0))

        assertTrue(result.voltageRatio < 1.0)
        assertTrue(result.primaryCurrent > result.secondaryCurrent)
    }

    // -- Short-circuit figures --------------------------------------------------

    @Test
    fun `short-circuit current follows the impedance voltage`() {
        // u_k = 6 % means rated current flows at 6 % of rated voltage, so a
        // terminal fault draws 100/6 times rated current.
        val result = calculate(input(impedancePercent = 6.0))

        assertEquals(result.secondaryCurrent * 100.0 / 6.0, result.secondaryShortCircuitCurrent, 1e-9)
        assertEquals(24_056.26, result.secondaryShortCircuitCurrent, 1e-2)
    }

    @Test
    fun `a lower impedance gives a higher fault current`() {
        val stiff = calculate(input(impedancePercent = 4.0)).secondaryShortCircuitCurrent
        val soft = calculate(input(impedancePercent = 6.0)).secondaryShortCircuitCurrent

        assertTrue(stiff > soft)
        assertEquals(6.0 / 4.0, stiff / soft, 1e-9)
    }

    @Test
    fun `short-circuit power follows the same ratio as the current`() {
        val result = calculate(input(impedancePercent = 6.0))

        assertEquals(1000.0 * 100.0 / 6.0, result.shortCircuitPowerKva, 1e-9)
        assertEquals(16_666.67, result.shortCircuitPowerKva, 1e-2)
    }

    @Test
    fun `short-circuit power is consistent with the short-circuit current`() {
        val result = calculate(input())

        // S_sc = k · U₂ · I_sc, expressed in kVA.
        val derived = SupplySystem.THREE_PHASE_AC.powerPhaseFactor *
            400.0 * result.secondaryShortCircuitCurrent / 1000.0
        assertEquals(derived, result.shortCircuitPowerKva, 1e-6)
    }

    // -- Worked reference case ---------------------------------------------------

    @Test
    fun `worked example - 1000 kVA 34point5kV to 400V three phase at 6 percent`() {
        val result = calculate(input())

        assertEquals(16.7348, result.primaryCurrent, 1e-4)
        assertEquals(1443.3757, result.secondaryCurrent, 1e-4)
        assertEquals(86.25, result.voltageRatio, 1e-9)
        assertEquals(24_056.26, result.secondaryShortCircuitCurrent, 1e-2)
        assertEquals(16_666.67, result.shortCircuitPowerKva, 1e-2)
    }
}
