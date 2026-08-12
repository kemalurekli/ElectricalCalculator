package com.kemalurekli.electricalcalculator.features.calculators.battery

import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.BatteryInput
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.CalculateBatteryRuntimeUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateBatteryRuntimeUseCaseTest {

    private val calculate = CalculateBatteryRuntimeUseCase()

    private fun input(
        capacityAh: Double = 100.0,
        ratedHours: Double = 20.0,
        voltage: Double = 48.0,
        loadWatts: Double = 500.0,
        efficiency: Double = 0.90,
        depthOfDischarge: Double = 0.50,
        peukert: Double = 1.15,
    ) = BatteryInput(
        capacityAh = capacityAh,
        ratedDischargeHours = ratedHours,
        bankVoltage = voltage,
        loadPowerWatts = loadWatts,
        systemEfficiency = efficiency,
        depthOfDischarge = depthOfDischarge,
        peukertExponent = peukert,
    )

    // -- Worked reference case ---------------------------------------------------

    @Test
    fun `worked example - 100Ah 48V bank carrying 500W`() {
        val result = calculate(input())

        assertEquals(11.574074, result.dischargeCurrentAmps, 1e-6)
        assertEquals(3.808959, result.runtimeHours, 1e-6)
        assertEquals(4.32, result.idealRuntimeHours, 1e-6)
        assertEquals(1904.4795, result.energyDeliveredWh, 1e-3)
        assertEquals(0.115741, result.cRate, 1e-6)
        assertEquals(50.0, result.usableCapacityAh, 1e-9)
    }

    // -- Peukert -------------------------------------------------------------------

    @Test
    fun `a Peukert exponent of one reproduces the naive result`() {
        // The identity that anchors the whole model: k = 1 means an ideal
        // battery, where t = C / I.
        val result = calculate(input(peukert = 1.0))

        assertEquals(result.idealRuntimeHours, result.runtimeHours, 1e-9)
    }

    @Test
    fun `Peukert shortens runtime whenever the load exceeds the rated rate`() {
        // 500 W draws 11.57 A from a bank rated at 5 A, so the discharge is
        // over twice the rated rate and the loss is significant.
        val result = calculate(input())

        assertTrue(result.runtimeHours < result.idealRuntimeHours)
        val penalty = 1.0 - result.runtimeHours / result.idealRuntimeHours
        assertEquals(0.1183, penalty, 1e-4)
    }

    @Test
    fun `a higher exponent costs more runtime`() {
        val gentle = calculate(input(peukert = 1.05)).runtimeHours
        val harsh = calculate(input(peukert = 1.30)).runtimeHours

        assertTrue(harsh < gentle)
    }

    @Test
    fun `discharging at exactly the rated rate is unaffected by the exponent`() {
        // At I = I_rated the Peukert term is 1^k, so every exponent agrees.
        // 100 Ah over 20 h is 5 A; at 48 V and unity efficiency that is 240 W.
        val gentle = calculate(input(loadWatts = 240.0, efficiency = 1.0, peukert = 1.05))
        val harsh = calculate(input(loadWatts = 240.0, efficiency = 1.0, peukert = 1.30))

        assertEquals(gentle.runtimeHours, harsh.runtimeHours, 1e-9)
        assertEquals(10.0, gentle.runtimeHours, 1e-9)
    }

    @Test
    fun `a load below the rated rate gains runtime rather than losing it`() {
        // Discharging gently is more efficient than the nameplate rating
        // assumes, so Peukert works in the user's favour here.
        val result = calculate(input(loadWatts = 100.0, efficiency = 1.0))

        assertTrue(result.runtimeHours > result.idealRuntimeHours)
    }

    // -- Depth of discharge -----------------------------------------------------------

    @Test
    fun `depth of discharge scales the runtime proportionally`() {
        val half = calculate(input(depthOfDischarge = 0.5)).runtimeHours
        val full = calculate(input(depthOfDischarge = 1.0)).runtimeHours

        assertEquals(full / 2.0, half, 1e-9)
    }

    @Test
    fun `usable capacity reflects the depth of discharge`() {
        assertEquals(50.0, calculate(input(depthOfDischarge = 0.5)).usableCapacityAh, 1e-9)
        assertEquals(80.0, calculate(input(depthOfDischarge = 0.8)).usableCapacityAh, 1e-9)
    }

    // -- Efficiency --------------------------------------------------------------------

    @Test
    fun `inverter losses raise the current drawn from the bank`() {
        val lossless = calculate(input(efficiency = 1.0)).dischargeCurrentAmps
        val realistic = calculate(input(efficiency = 0.90)).dischargeCurrentAmps

        assertTrue(realistic > lossless)
        assertEquals(1.0 / 0.90, realistic / lossless, 1e-9)
    }

    @Test
    fun `a lossless dc system draws load power over bank voltage`() {
        val result = calculate(input(efficiency = 1.0))

        assertEquals(500.0 / 48.0, result.dischargeCurrentAmps, 1e-9)
    }

    // -- Scaling ------------------------------------------------------------------------

    @Test
    fun `a heavier load shortens runtime`() {
        val light = calculate(input(loadWatts = 250.0)).runtimeHours
        val heavy = calculate(input(loadWatts = 1_000.0)).runtimeHours

        assertTrue(heavy < light)
    }

    @Test
    fun `a higher bank voltage draws less current for the same load`() {
        val low = calculate(input(voltage = 24.0)).dischargeCurrentAmps
        val high = calculate(input(voltage = 48.0)).dischargeCurrentAmps

        assertEquals(low / 2.0, high, 1e-9)
    }

    @Test
    fun `c-rate is the discharge current over nameplate capacity`() {
        val result = calculate(input(capacityAh = 200.0))

        assertEquals(result.dischargeCurrentAmps / 200.0, result.cRate, 1e-12)
    }

    @Test
    fun `energy delivered is load power times runtime`() {
        val result = calculate(input())

        assertEquals(500.0 * result.runtimeHours, result.energyDeliveredWh, 1e-9)
    }

    // -- Rated discharge time --------------------------------------------------------------

    @Test
    fun `the same amp-hours quoted at a faster rate describes a stronger battery`() {
        // Counterintuitive but important: 100 Ah measured at C/1 means the cell
        // sustained 100 A for an hour, which is far harder than 5 A for twenty
        // hours. The C/1 cell therefore has the larger Peukert constant and
        // outlasts the C/20 cell at any modest load.
        //
        // This is why a capacity figure is meaningless without the rate it was
        // measured at, and why the rate is a required input here.
        val ratedSlow = calculate(input(ratedHours = 20.0)).runtimeHours
        val ratedFast = calculate(input(ratedHours = 1.0)).runtimeHours

        assertTrue(ratedFast > ratedSlow)
        assertEquals(3.808959, ratedSlow, 1e-6)
        assertEquals(5.969814, ratedFast, 1e-6)
    }

    @Test
    fun `a lithium-like bank outlasts a lead-acid one of the same capacity`() {
        // Lithium tolerates a deeper discharge and has a flatter Peukert curve.
        val leadAcid = calculate(input(depthOfDischarge = 0.5, peukert = 1.25)).runtimeHours
        val lithium = calculate(input(depthOfDischarge = 0.8, peukert = 1.05)).runtimeHours

        assertTrue(lithium > leadAcid)
    }
}
