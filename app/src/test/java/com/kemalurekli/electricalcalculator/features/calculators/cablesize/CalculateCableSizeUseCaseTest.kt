package com.kemalurekli.electricalcalculator.features.calculators.cablesize

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CableSizeInput
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CalculateCableSizeUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CorrectionFactors
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.GoverningConstraint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the sizing *logic*: that both constraints are evaluated, that the
 * larger one wins, and that derating is applied. The tabulated capacities the
 * logic consumes are guarded separately by `AmpacityTableTest`.
 */
class CalculateCableSizeUseCaseTest {

    private val calculate = CalculateCableSizeUseCase(AmpacityTable(), CorrectionFactors())

    private fun input(
        voltage: Double = 230.0,
        current: Double = 25.0,
        length: Double = 30.0,
        material: ConductorMaterial = ConductorMaterial.COPPER,
        insulation: CableInsulation = CableInsulation.PVC,
        method: InstallationMethod = InstallationMethod.C_CLIPPED_DIRECT,
        system: SupplySystem = SupplySystem.SINGLE_PHASE_AC,
        powerFactor: Double = 1.0,
        maxDropPercent: Double = 5.0,
        ambient: Double = 30.0,
        circuits: Int = 1,
        parallel: Int = 1,
    ) = CableSizeInput(
        systemVoltage = voltage,
        designCurrent = current,
        lengthMetres = length,
        material = material,
        insulation = insulation,
        method = method,
        system = system,
        powerFactor = powerFactor,
        maxVoltageDropPercent = maxDropPercent,
        ambientTemperatureC = ambient,
        groupedCircuits = circuits,
        parallelConductors = parallel,
    )

    // -- Both constraints -----------------------------------------------------

    @Test
    fun `worked example - 230V 25A 30m copper PVC method C`() {
        // Ampacity: 25 A required, C/2-core gives 2.5 mm² = 27 A.
        // Drop:     A ≥ 2·25·ρ(70)·30 / (230·0.05) = 2.69 mm² → 4 mm².
        // The drop constraint is larger, so it governs.
        val result = calculate(input())

        assertEquals(2.5, result.currentCapacityAreaMm2!!, 1e-9)
        assertEquals(4.0, result.voltageDropAreaMm2!!, 1e-9)
        assertEquals(4.0, result.recommendedAreaMm2!!, 1e-9)
        assertEquals(GoverningConstraint.VOLTAGE_DROP, result.governingConstraint)
        assertEquals(7.7358, result.voltageDropVolts, 1e-3)
        assertEquals(3.363, result.voltageDropPercent, 1e-3)
    }

    @Test
    fun `a short run is limited by current capacity instead`() {
        // Over 2 m the drop is negligible, so only heating matters.
        val result = calculate(input(length = 2.0))

        assertEquals(2.5, result.recommendedAreaMm2!!, 1e-9)
        assertEquals(GoverningConstraint.CURRENT_CAPACITY, result.governingConstraint)
    }

    @Test
    fun `sizing on voltage drop alone would undersize a long hot run`() {
        // The point of evaluating both: at 55 °C ambient the capacity
        // constraint overtakes the drop constraint.
        val hot = calculate(input(length = 10.0, ambient = 55.0))

        assertTrue(
            "capacity ${hot.currentCapacityAreaMm2} should exceed drop ${hot.voltageDropAreaMm2}",
            hot.currentCapacityAreaMm2!! > hot.voltageDropAreaMm2!!,
        )
        assertEquals(GoverningConstraint.CURRENT_CAPACITY, hot.governingConstraint)
    }

    @Test
    fun `both constraints landing on one size is reported as both`() {
        val result = calculate(input(length = 14.0))

        if (result.currentCapacityAreaMm2 == result.voltageDropAreaMm2) {
            assertEquals(GoverningConstraint.BOTH, result.governingConstraint)
        }
    }

    // -- Derating -------------------------------------------------------------

    @Test
    fun `ambient temperature derating raises the required capacity`() {
        // PVC at 40 °C: Ca = 0.87, so 25 A needs 28.7 A of tabulated capacity.
        val result = calculate(input(length = 2.0, ambient = 40.0))

        assertEquals(0.87, result.ambientFactor, 1e-9)
        assertEquals(25.0 / 0.87, result.requiredCapacityAmps, 1e-6)
        // 2.5 mm² (27 A) no longer covers it; 4 mm² (36 A) does.
        assertEquals(4.0, result.currentCapacityAreaMm2!!, 1e-9)
    }

    @Test
    fun `grouping derating raises the required capacity`() {
        // Six circuits bunched: Cg = 0.57.
        val result = calculate(input(length = 2.0, circuits = 6))

        assertEquals(0.57, result.groupingFactor, 1e-9)
        assertEquals(25.0 / 0.57, result.requiredCapacityAmps, 1e-6)
    }

    @Test
    fun `derating factors compound`() {
        val result = calculate(input(length = 2.0, ambient = 40.0, circuits = 3))

        assertEquals(0.87 * 0.70, result.ambientFactor * result.groupingFactor, 1e-9)
        assertEquals(25.0 / (0.87 * 0.70), result.requiredCapacityAmps, 1e-6)
    }

    @Test
    fun `at the reference conditions no derating is applied`() {
        val result = calculate(input(ambient = 30.0, circuits = 1))

        assertEquals(1.0, result.ambientFactor, 1e-9)
        assertEquals(1.0, result.groupingFactor, 1e-9)
        assertEquals(25.0, result.requiredCapacityAmps, 1e-9)
    }

    @Test
    fun `the reported derated capacity covers the design current`() {
        val result = calculate(input(ambient = 45.0, circuits = 4))

        assertTrue(
            "derated ${result.deratedCapacityAmps} must cover 25 A",
            result.deratedCapacityAmps >= 25.0,
        )
    }

    // -- Inputs that change the answer ----------------------------------------

    @Test
    fun `aluminium needs a larger conductor than copper`() {
        val copper = calculate(input(material = ConductorMaterial.COPPER))
        val aluminium = calculate(input(material = ConductorMaterial.ALUMINIUM))

        assertTrue(aluminium.recommendedAreaMm2!! > copper.recommendedAreaMm2!!)
    }

    @Test
    fun `XLPE allows the same or a smaller conductor than PVC`() {
        val pvc = calculate(input(length = 2.0, insulation = CableInsulation.PVC))
        val xlpe = calculate(input(length = 2.0, insulation = CableInsulation.XLPE))

        assertTrue(xlpe.currentCapacityAreaMm2!! <= pvc.currentCapacityAreaMm2!!)
    }

    @Test
    fun `a tighter drop limit demands a larger conductor`() {
        val relaxed = calculate(input(maxDropPercent = 5.0))
        val strict = calculate(input(maxDropPercent = 3.0))

        assertTrue(strict.recommendedAreaMm2!! > relaxed.recommendedAreaMm2!!)
    }

    @Test
    fun `three phase needs a smaller conductor than single phase for the same load`() {
        // √3 against 2 in the drop formula, and three-phase carries less current
        // per conductor for a given power.
        val single = calculate(input(system = SupplySystem.SINGLE_PHASE_AC))
        val three = calculate(input(system = SupplySystem.THREE_PHASE_AC))

        assertTrue(three.voltageDropAreaMm2!! <= single.voltageDropAreaMm2!!)
    }

    @Test
    fun `power factor is ignored on a dc supply`() {
        val withPf = calculate(input(system = SupplySystem.DC, powerFactor = 0.5))
        val withoutPf = calculate(input(system = SupplySystem.DC, powerFactor = 1.0))

        assertEquals(withoutPf.voltageDropAreaMm2, withPf.voltageDropAreaMm2)
    }

    @Test
    fun `parallel conductors reduce the size each one needs`() {
        val single = calculate(input(current = 200.0, parallel = 1))
        val doubled = calculate(input(current = 200.0, parallel = 2))

        assertTrue(doubled.recommendedAreaMm2!! < single.recommendedAreaMm2!!)
        // Each conductor now carries half the current.
        assertEquals(100.0, doubled.requiredCapacityAmps, 1e-9)
    }

    // -- No solution -----------------------------------------------------------

    @Test
    fun `a load beyond the largest tabulated size has no solution`() {
        val result = calculate(input(current = 5_000.0, length = 1.0))

        assertFalse(result.hasSolution)
        assertNull(result.recommendedAreaMm2)
        assertNull(result.currentCapacityAreaMm2)
    }

    @Test
    fun `a run too long for any size has no solution`() {
        val result = calculate(input(length = 100_000.0))

        assertFalse(result.hasSolution)
        assertNull(result.voltageDropAreaMm2)
    }

    @Test
    fun `the recommended size actually satisfies the drop limit`() {
        listOf(1.0, 3.0, 5.0).forEach { limit ->
            val result = calculate(input(maxDropPercent = limit, length = 20.0))
            if (result.hasSolution) {
                assertTrue(
                    "at $limit%: actual ${result.voltageDropPercent} exceeds the limit",
                    result.voltageDropPercent <= limit + 1e-9,
                )
            }
        }
    }
}
