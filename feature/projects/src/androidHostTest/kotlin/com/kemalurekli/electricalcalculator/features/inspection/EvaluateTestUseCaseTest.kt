package com.kemalurekli.electricalcalculator.features.inspection

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.core.domain.table.CorrectionFactors
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.features.design.domain.CircuitDesignInput
import com.kemalurekli.electricalcalculator.features.design.domain.CircuitLoad
import com.kemalurekli.electricalcalculator.features.design.domain.DesignCircuitUseCase
import com.kemalurekli.electricalcalculator.features.inspection.domain.CircuitTest
import com.kemalurekli.electricalcalculator.features.inspection.domain.EvaluateTestUseCase
import com.kemalurekli.electricalcalculator.features.inspection.domain.InsulationTestVoltage
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestKind
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A reading, put beside what the design expected of it.
 *
 * The comparison is the feature. A circuit can pass its loop test against the
 * device maximum and still be wrong — the measurement half an ohm above what
 * the cable should give is a loose termination or an unexpected joint, and only
 * the design knows what the cable should have given.
 */
class EvaluateTestUseCaseTest {

    private val evaluate = EvaluateTestUseCase()
    private val designCircuit = DesignCircuitUseCase(
        ampacityTable = AmpacityTable(),
        correctionFactors = CorrectionFactors(),
        calculateVoltageDrop = CalculateVoltageDropUseCase(),
        calculateEarthFault = CalculateEarthFaultUseCase(),
    )

    private val design = designCircuit(
        CircuitDesignInput(
            load = CircuitLoad.Current(30.0),
            system = SupplySystem.THREE_PHASE_AC,
            systemVoltage = 400.0,
            powerFactor = 0.9,
            lengthMetres = 20.0,
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.PVC,
            method = InstallationMethod.C_CLIPPED_DIRECT,
            ambientTemperatureC = 30.0,
            groupedCircuits = 1,
            parallelConductors = 1,
            maxVoltageDropPercent = 4.0,
            deviceType = ProtectiveDeviceType.MCB_TYPE_C,
            externalImpedanceOhms = 0.35,
            disconnectionTimeSeconds = 0.4,
        ),
    )

    private fun test(kind: TestKind, value: String = "") =
        CircuitTest(circuitId = 1L, kind = kind, value = value)

    @Test
    fun `a loop reading is judged against the device the circuit actually has`() {
        val permitted = design.maximumLoopImpedanceOhms

        val good = evaluate(test(TestKind.LOOP_IMPEDANCE, (permitted - 0.05).toString()), design)
        assertEquals(TestVerdict.PASS, good.verdict)

        val bad = evaluate(test(TestKind.LOOP_IMPEDANCE, (permitted + 0.05).toString()), design)
        assertEquals(TestVerdict.FAIL, bad.verdict)
    }

    @Test
    fun `a loop reading that passes can still be worth looking at`() {
        // The finding the feature exists for. This measurement is inside the
        // device's maximum and half an ohm above what the cable should give;
        // "pass" alone would send the inspector home.
        val calculated = design.loopImpedanceOhms
        val evaluation = evaluate(
            test(TestKind.LOOP_IMPEDANCE, (calculated + 0.05).toString()),
            design,
        )

        assertEquals(TestVerdict.PASS, evaluation.verdict)
        assertEquals(calculated, evaluation.expected!!, 1e-9)
        assertTrue("the ratio should show the excess", evaluation.ratio!! > 1.0)
    }

    @Test
    fun `continuity is compared against the circuit's own conductors, not the whole loop`() {
        // R1 + R2 is measured across the circuit. Comparing it against a loop
        // that includes the supply's Ze would make every reading look far too
        // low, and every installation look like it had a shorter cable than it
        // has.
        val evaluation = evaluate(test(TestKind.CONTINUITY, "0.2"), design)

        assertNotNull(evaluation.expected)
        assertEquals(
            design.loopImpedanceOhms - design.externalImpedanceOhms,
            evaluation.expected!!,
            1e-9,
        )
        // Reported, not judged: there is no codified tolerance on the agreement.
        assertEquals(TestVerdict.RECORDED, evaluation.verdict)
        assertNull(evaluation.limit)
    }

    @Test
    fun `insulation is judged without needing a design at all`() {
        // An inspector on site does not always have the design to hand, and a
        // minimum insulation resistance does not depend on one.
        val evaluation = evaluate(
            CircuitTest(
                circuitId = 1L,
                kind = TestKind.INSULATION,
                value = "0.8",
                insulationVoltage = InsulationTestVoltage.V500,
            ),
            design = null,
        )

        assertEquals(TestVerdict.FAIL, evaluation.verdict)
    }

    @Test
    fun `a reading that is not a number is recorded rather than judged`() {
        listOf("", "   ", "n/a").forEach { raw ->
            val evaluation = evaluate(test(TestKind.LOOP_IMPEDANCE, raw), design)
            assertEquals("'$raw'", TestVerdict.RECORDED, evaluation.verdict)
        }
    }

    @Test
    fun `polarity is the one test whose reading is its verdict`() {
        assertEquals(
            TestVerdict.PASS,
            evaluate(CircuitTest(circuitId = 1L, kind = TestKind.POLARITY, passed = true), design)
                .verdict,
        )
        assertEquals(
            TestVerdict.FAIL,
            evaluate(CircuitTest(circuitId = 1L, kind = TestKind.POLARITY, passed = false), design)
                .verdict,
        )
        assertEquals(
            TestVerdict.RECORDED,
            evaluate(CircuitTest(circuitId = 1L, kind = TestKind.POLARITY), design).verdict,
        )
    }

    @Test
    fun `a loop reading on an undesigned circuit is kept, not refused`() {
        val evaluation = evaluate(test(TestKind.LOOP_IMPEDANCE, "0.45"), design = null)

        assertEquals(TestVerdict.RECORDED, evaluation.verdict)
        assertNull(evaluation.limit)
    }
}
