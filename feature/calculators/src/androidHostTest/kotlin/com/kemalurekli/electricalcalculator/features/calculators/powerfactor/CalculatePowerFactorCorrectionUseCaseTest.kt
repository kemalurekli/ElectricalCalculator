package com.kemalurekli.electricalcalculator.features.calculators.powerfactor

import com.kemalurekli.electricalcalculator.core.domain.model.CapacitorConnection
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.CalculatePowerFactorCorrectionUseCase
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.PowerFactorInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class CalculatePowerFactorCorrectionUseCaseTest {

    private val calculate = CalculatePowerFactorCorrectionUseCase()

    private fun input(
        activePowerWatts: Double = 100_000.0,
        existing: Double = 0.75,
        target: Double = 0.95,
        voltage: Double = 400.0,
        frequency: Double = 50.0,
        connection: CapacitorConnection = CapacitorConnection.DELTA,
        system: SupplySystem = SupplySystem.THREE_PHASE_AC,
    ) = PowerFactorInput(
        activePowerWatts = activePowerWatts,
        existingPowerFactor = existing,
        targetPowerFactor = target,
        voltage = voltage,
        frequencyHz = frequency,
        connection = connection,
        system = system,
    )

    // -- Worked reference case ---------------------------------------------------

    @Test
    fun `worked example - 100 kW from 0point75 to 0point95 at 400V 50Hz delta`() {
        val result = calculate(input())

        assertEquals(55_323.3, result.requiredCapacitorVar, 0.1)
        assertEquals(88_191.7, result.reactiveBeforeVar, 0.1)
        assertEquals(32_868.4, result.reactiveAfterVar, 0.1)
        assertEquals(133_333.3, result.apparentBeforeVa, 0.1)
        assertEquals(105_263.2, result.apparentAfterVa, 0.1)
        assertEquals(28_070.2, result.releasedCapacityVa, 0.1)
        assertEquals(192.4501, result.currentBeforeAmps, 1e-4)
        assertEquals(151.9343, result.currentAfterAmps, 1e-4)
        assertEquals(366.874e-6, result.capacitancePerPhaseFarads, 1e-9)
    }

    // -- Core relationship ---------------------------------------------------------

    @Test
    fun `required reactive power is the difference of the two tangents`() {
        val result = calculate(input())
        val expected = 100_000.0 * (
            kotlin.math.tan(kotlin.math.acos(0.75)) - kotlin.math.tan(kotlin.math.acos(0.95))
            )

        assertEquals(expected, result.requiredCapacitorVar, 1e-6)
    }

    @Test
    fun `active power is unchanged by correction`() {
        // The point of correction: P stays put while Q and S fall.
        val result = calculate(input())

        assertEquals(
            result.apparentBeforeVa * 0.75,
            result.apparentAfterVa * 0.95,
            1e-6,
        )
    }

    @Test
    fun `correcting to a higher target needs more capacitor power`() {
        val modest = calculate(input(target = 0.90)).requiredCapacitorVar
        val ambitious = calculate(input(target = 0.99)).requiredCapacitorVar

        assertTrue(ambitious > modest)
    }

    @Test
    fun `a worse starting power factor needs more capacitor power`() {
        val poor = calculate(input(existing = 0.60)).requiredCapacitorVar
        val decent = calculate(input(existing = 0.85)).requiredCapacitorVar

        assertTrue(poor > decent)
    }

    @Test
    fun `correcting to unity cancels the reactive power entirely`() {
        val result = calculate(input(target = 1.0))

        assertEquals(0.0, result.reactiveAfterVar, 1e-6)
        assertEquals(result.reactiveBeforeVar, result.requiredCapacitorVar, 1e-6)
        assertEquals(result.apparentAfterVa, 100_000.0, 1e-6)
    }

    @Test
    fun `no correction is needed when the target equals the existing factor`() {
        val result = calculate(input(existing = 0.90, target = 0.90))

        assertEquals(0.0, result.requiredCapacitorVar, 1e-6)
        assertEquals(0.0, result.releasedCapacityVa, 1e-6)
    }

    @Test
    fun `capacitor power scales linearly with active power`() {
        val small = calculate(input(activePowerWatts = 50_000.0)).requiredCapacitorVar
        val large = calculate(input(activePowerWatts = 100_000.0)).requiredCapacitorVar

        assertEquals(2.0 * small, large, 1e-6)
    }

    // -- Released capacity -------------------------------------------------------------

    @Test
    fun `released capacity is the drop in apparent power`() {
        val result = calculate(input())

        assertEquals(
            result.apparentBeforeVa - result.apparentAfterVa,
            result.releasedCapacityVa,
            1e-9,
        )
        assertTrue(result.releasedCapacityVa > 0.0)
    }

    @Test
    fun `current falls in the same proportion as apparent power`() {
        val result = calculate(input())

        assertEquals(
            result.apparentBeforeVa / result.apparentAfterVa,
            result.currentBeforeAmps / result.currentAfterAmps,
            1e-9,
        )
    }

    // -- Capacitance and connection -----------------------------------------------------

    @Test
    fun `delta needs a third of the capacitance of star`() {
        // Each delta unit sits across the full line voltage, so it produces
        // three times the reactive power for the same capacitance.
        val delta = calculate(input(connection = CapacitorConnection.DELTA))
        val star = calculate(input(connection = CapacitorConnection.STAR))

        assertEquals(star.capacitancePerPhaseFarads / 3.0, delta.capacitancePerPhaseFarads, 1e-12)
        // The reactive power required is the same either way.
        assertEquals(star.requiredCapacitorVar, delta.requiredCapacitorVar, 1e-9)
    }

    @Test
    fun `capacitance follows Q over omega U squared`() {
        val result = calculate(input(connection = CapacitorConnection.STAR))
        val omega = 2.0 * PI * 50.0
        val expected = result.requiredCapacitorVar / (omega * 400.0 * 400.0)

        assertEquals(expected, result.capacitancePerPhaseFarads, 1e-12)
    }

    @Test
    fun `a higher frequency needs less capacitance`() {
        val fifty = calculate(input(frequency = 50.0)).capacitancePerPhaseFarads
        val sixty = calculate(input(frequency = 60.0)).capacitancePerPhaseFarads

        assertEquals(50.0 / 60.0, sixty / fifty, 1e-9)
    }

    @Test
    fun `a higher voltage needs far less capacitance`() {
        // C goes as 1/U², so doubling the voltage quarters the capacitance.
        val low = calculate(input(voltage = 400.0)).capacitancePerPhaseFarads
        val high = calculate(input(voltage = 800.0)).capacitancePerPhaseFarads

        assertEquals(low / 4.0, high, 1e-12)
    }

    // -- Single phase -----------------------------------------------------------------------

    @Test
    fun `single phase ignores the connection and uses the supply voltage directly`() {
        val delta = calculate(input(system = SupplySystem.SINGLE_PHASE_AC, connection = CapacitorConnection.DELTA))
        val star = calculate(input(system = SupplySystem.SINGLE_PHASE_AC, connection = CapacitorConnection.STAR))

        assertEquals(star.capacitancePerPhaseFarads, delta.capacitancePerPhaseFarads, 1e-12)

        val omega = 2.0 * PI * 50.0
        assertEquals(
            delta.requiredCapacitorVar / (omega * 400.0 * 400.0),
            delta.capacitancePerPhaseFarads,
            1e-12,
        )
    }

    @Test
    fun `single phase current omits the root three factor`() {
        val result = calculate(input(system = SupplySystem.SINGLE_PHASE_AC))

        assertEquals(result.apparentBeforeVa / 400.0, result.currentBeforeAmps, 1e-9)
    }
}
