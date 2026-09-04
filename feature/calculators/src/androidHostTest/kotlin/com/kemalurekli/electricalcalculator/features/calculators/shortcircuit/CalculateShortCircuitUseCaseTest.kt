package com.kemalurekli.electricalcalculator.features.calculators.shortcircuit

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.CalculateShortCircuitUseCase
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.FaultType
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.ShortCircuitInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateShortCircuitUseCaseTest {

    private val calculate = CalculateShortCircuitUseCase()

    private fun input(
        faultType: FaultType = FaultType.THREE_PHASE,
        voltage: Double = 400.0,
        supplyCurrent: Double = 20_000.0,
        length: Double = 50.0,
        area: Double = 25.0,
        neutralArea: Double = 25.0,
        parallel: Int = 1,
        material: ConductorMaterial = ConductorMaterial.COPPER,
        insulation: CableInsulation = CableInsulation.PVC,
        reactance: Double = ShortCircuitInput.DEFAULT_REACTANCE_PER_KM,
    ) = ShortCircuitInput(
        faultType = faultType,
        nominalVoltage = voltage,
        supplyFaultCurrentAmps = supplyCurrent,
        lengthMetres = length,
        crossSectionMm2 = area,
        neutralCrossSectionMm2 = neutralArea,
        parallelConductors = parallel,
        material = material,
        insulation = insulation,
        reactancePerKmOhms = reactance,
    )

    // -- The identity that anchors the method -----------------------------------------

    @Test
    fun `the voltage factors are the low-voltage pair from the standard`() {
        // Checked 2026-09-04 against IEC 60909-0 Table 1, the 100 V to 1000 V
        // row. The pairing matters as much as the values: 1,05 goes with the
        // conductor cold to give the most a device may have to break, 0,95 with
        // it at rated temperature to give the least it will ever see.
        //
        // The row also allows c_max = 1,10 where the system tolerance is ±10 %
        // rather than ±6 %, which a 230/400 V public supply in Europe is. This
        // app always uses 1,05, so on such a supply its maximum is about 5 %
        // low. Recorded in docs/verification-backlog.md as a decision rather
        // than fixed here, because it is a question about the target market.
        assertEquals(1.05, ShortCircuitInput.VOLTAGE_FACTOR_MAX, 1e-12)
        assertEquals(0.95, ShortCircuitInput.VOLTAGE_FACTOR_MIN, 1e-12)
    }

    @Test
    fun `with no cable the maximum current is the declared supply current`() {
        // The supply impedance is recovered from the declared figure, so a
        // zero-length run must give it straight back. If this drifts, the
        // impedance is being derived with a different voltage factor than the
        // current is computed with, and every other result is wrong with it.
        val result = calculate(input(length = 0.0))

        assertEquals(20_000.0, result.maximumFaultCurrentAmps, 1e-6)
    }

    @Test
    fun `with no cable the two currents differ only by the voltage factors`() {
        val result = calculate(input(length = 0.0))

        assertEquals(
            ShortCircuitInput.VOLTAGE_FACTOR_MIN / ShortCircuitInput.VOLTAGE_FACTOR_MAX,
            result.minimumFaultCurrentAmps / result.maximumFaultCurrentAmps,
            1e-12,
        )
    }

    // -- Worked reference cases ----------------------------------------------------------

    @Test
    fun `worked example - three-phase fault 50 m down 25 mm copper`() {
        val result = calculate(input())

        assertEquals(0.012124, result.supplyImpedanceOhms, 1e-6)
        assertEquals(0.034482, result.cableResistanceColdOhms, 1e-6)
        assertEquals(0.041258, result.cableResistanceHotOhms, 1e-6)
        assertEquals(0.004, result.cableReactanceOhms, 1e-9)
        assertEquals(0.046838, result.loopImpedanceColdOhms, 1e-6)
        assertEquals(0.053576, result.loopImpedanceHotOhms, 1e-6)
        assertEquals(5177.190725, result.maximumFaultCurrentAmps, 1e-3)
        assertEquals(4095.025291, result.minimumFaultCurrentAmps, 1e-3)
    }

    @Test
    fun `worked example - line-to-neutral fault 30 m down 16 mm copper`() {
        val result = calculate(
            input(
                faultType = FaultType.LINE_TO_NEUTRAL,
                voltage = 230.0,
                supplyCurrent = 10_000.0,
                length = 30.0,
                area = 16.0,
                neutralArea = 16.0,
            ),
        )

        assertEquals(2714.041657, result.maximumFaultCurrentAmps, 1e-3)
        assertEquals(2149.384983, result.minimumFaultCurrentAmps, 1e-3)
    }

    // -- Why there are two currents ------------------------------------------------------

    @Test
    fun `the minimum is always below the maximum`() {
        listOf(0.0, 10.0, 50.0, 200.0).forEach { length ->
            val result = calculate(input(length = length))
            assertTrue(
                "minimum exceeded maximum at $length m",
                result.minimumFaultCurrentAmps < result.maximumFaultCurrentAmps,
            )
        }
    }

    @Test
    fun `a hot conductor carries more resistance than a cold one`() {
        // The reason the two currents diverge with length: copper gains about
        // 20 % resistance between 20 °C and its 70 °C PVC rating.
        val result = calculate(input())

        assertTrue(result.cableResistanceHotOhms > result.cableResistanceColdOhms)
        assertEquals(1.1965, result.cableResistanceHotOhms / result.cableResistanceColdOhms, 1e-4)
    }

    @Test
    fun `XLPE runs hotter so its minimum current is lower`() {
        // Same copper, same length; only the rated temperature differs.
        val pvc = calculate(input(insulation = CableInsulation.PVC))
        val xlpe = calculate(input(insulation = CableInsulation.XLPE))

        assertEquals(pvc.maximumFaultCurrentAmps, xlpe.maximumFaultCurrentAmps, 1e-9)
        assertTrue(xlpe.minimumFaultCurrentAmps < pvc.minimumFaultCurrentAmps)
        assertEquals(3898.663279, xlpe.minimumFaultCurrentAmps, 1e-3)
    }

    // -- The neutral return ---------------------------------------------------------------

    @Test
    fun `a line-to-neutral fault sees roughly double the cable resistance`() {
        // Both conductors are in the loop, which is why the single-phase fault
        // current is roughly half the three-phase one.
        val threePhase = calculate(input(faultType = FaultType.THREE_PHASE))
        val lineNeutral = calculate(input(faultType = FaultType.LINE_TO_NEUTRAL))

        assertEquals(
            2.0,
            lineNeutral.cableResistanceColdOhms / threePhase.cableResistanceColdOhms,
            1e-12,
        )
    }

    @Test
    fun `a reduced neutral lowers the fault current`() {
        // The case most likely to fail to trip, and the reason the neutral
        // section is asked for rather than assumed equal to the line.
        val full = calculate(
            input(
                faultType = FaultType.LINE_TO_NEUTRAL,
                voltage = 230.0,
                supplyCurrent = 10_000.0,
                length = 30.0,
                area = 16.0,
                neutralArea = 16.0,
            ),
        )
        val reduced = calculate(
            input(
                faultType = FaultType.LINE_TO_NEUTRAL,
                voltage = 230.0,
                supplyCurrent = 10_000.0,
                length = 30.0,
                area = 16.0,
                neutralArea = 10.0,
            ),
        )

        assertTrue(reduced.minimumFaultCurrentAmps < full.minimumFaultCurrentAmps)
        assertEquals(1750.378242, reduced.minimumFaultCurrentAmps, 1e-3)
    }

    @Test
    fun `the neutral section is ignored on a three-phase fault`() {
        // A three-phase fault never returns through the neutral, so changing it
        // must not move the answer.
        val equal = calculate(input(neutralArea = 25.0))
        val reduced = calculate(input(neutralArea = 6.0))

        assertEquals(equal.minimumFaultCurrentAmps, reduced.minimumFaultCurrentAmps, 1e-12)
    }

    // -- Scaling ---------------------------------------------------------------------------

    @Test
    fun `a longer run gives a smaller fault current`() {
        val near = calculate(input(length = 10.0)).minimumFaultCurrentAmps
        val far = calculate(input(length = 200.0)).minimumFaultCurrentAmps

        assertTrue(far < near)
    }

    @Test
    fun `cable resistance is proportional to length`() {
        val short = calculate(input(length = 25.0)).cableResistanceColdOhms
        val long = calculate(input(length = 100.0)).cableResistanceColdOhms

        assertEquals(4.0, long / short, 1e-12)
    }

    @Test
    fun `parallel conductors divide the cable impedance`() {
        val single = calculate(input(parallel = 1))
        val double = calculate(input(parallel = 2))

        assertEquals(2.0, single.cableResistanceColdOhms / double.cableResistanceColdOhms, 1e-12)
        assertEquals(2.0, single.cableReactanceOhms / double.cableReactanceOhms, 1e-12)
        assertTrue(double.minimumFaultCurrentAmps > single.minimumFaultCurrentAmps)
    }

    @Test
    fun `aluminium is more resistive so its fault current is lower`() {
        val copper = calculate(input(material = ConductorMaterial.COPPER))
        val aluminium = calculate(input(material = ConductorMaterial.ALUMINIUM))

        assertTrue(aluminium.minimumFaultCurrentAmps < copper.minimumFaultCurrentAmps)
        assertEquals(2736.925077, aluminium.minimumFaultCurrentAmps, 1e-3)
    }

    @Test
    fun `a stiffer supply gives a larger fault current`() {
        val weak = calculate(input(supplyCurrent = 5_000.0)).maximumFaultCurrentAmps
        val stiff = calculate(input(supplyCurrent = 40_000.0)).maximumFaultCurrentAmps

        assertTrue(stiff > weak)
    }

    // -- Where the impedance sits -----------------------------------------------------------

    @Test
    fun `at the origin the supply owns all of the impedance`() {
        val result = calculate(input(length = 0.0))

        assertEquals(0.0, result.cableShareOfImpedance, 1e-12)
    }

    @Test
    fun `a long run takes over the loop`() {
        // The figure that says whether the run or the supply is what limits the
        // fault current — and a run that has taken over is the one where a
        // device stops seeing enough current to trip instantly.
        val result = calculate(input(length = 200.0))

        assertTrue(result.cableShareOfImpedance > 0.9)
    }

    @Test
    fun `the cable share never leaves the unit interval`() {
        listOf(0.0, 1.0, 50.0, 500.0).forEach { length ->
            val share = calculate(input(length = length)).cableShareOfImpedance
            assertTrue("share out of range at $length m", share in 0.0..1.0)
        }
    }

    // -- Reactance -----------------------------------------------------------------------------

    @Test
    fun `reactance is an input, not a hidden constant`() {
        val typical = calculate(input(reactance = 0.08))
        val higher = calculate(input(reactance = 0.15))

        assertTrue(higher.cableReactanceOhms > typical.cableReactanceOhms)
        assertTrue(higher.minimumFaultCurrentAmps < typical.minimumFaultCurrentAmps)
    }

    @Test
    fun `zero reactance reduces the cable impedance to its resistance`() {
        val result = calculate(input(reactance = 0.0))

        assertEquals(
            result.supplyImpedanceOhms + result.cableResistanceColdOhms,
            result.loopImpedanceColdOhms,
            1e-12,
        )
    }
}
