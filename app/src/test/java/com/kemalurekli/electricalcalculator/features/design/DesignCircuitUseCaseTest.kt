package com.kemalurekli.electricalcalculator.features.design

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CorrectionFactors
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropInput
import com.kemalurekli.electricalcalculator.features.design.domain.BindingConstraint
import com.kemalurekli.electricalcalculator.features.design.domain.CircuitDesignInput
import com.kemalurekli.electricalcalculator.features.design.domain.CircuitLoad
import com.kemalurekli.electricalcalculator.features.design.domain.DesignCircuitUseCase
import com.kemalurekli.electricalcalculator.features.design.domain.DesignFailure
import com.kemalurekli.electricalcalculator.features.design.domain.ProtectiveDeviceRatings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The design chain, judged on the thing it exists to get right.
 *
 * Almost nothing here asserts a hand-computed figure. The arithmetic belongs to
 * the four use cases this composes, each of which already has its own tests
 * checking it against worked examples; repeating those numbers here would only
 * duplicate a transcription risk. What is new — and what no existing test can
 * cover — is the *ordering*: that the stages are applied to the same
 * cross-section, that the right current reaches each one, and that the
 * constraint reported as binding really is the one that rejected the size
 * below. Those are relationships, so they are asserted as relationships.
 */
class DesignCircuitUseCaseTest {

    private val ampacityTable = AmpacityTable()
    private val designCircuit = DesignCircuitUseCase(
        ampacityTable = ampacityTable,
        correctionFactors = CorrectionFactors(),
        calculateVoltageDrop = CalculateVoltageDropUseCase(),
        calculateEarthFault = CalculateEarthFaultUseCase(),
    )

    /** An ordinary 400 V three-phase feeder, clipped direct, on a healthy supply. */
    private fun input(
        load: CircuitLoad = CircuitLoad.Current(30.0),
        lengthMetres: Double = 20.0,
        externalImpedanceOhms: Double = 0.35,
        maxVoltageDropPercent: Double = 4.0,
        deviceType: ProtectiveDeviceType = ProtectiveDeviceType.MCB_TYPE_C,
        system: SupplySystem = SupplySystem.THREE_PHASE_AC,
        systemVoltage: Double = 400.0,
    ) = CircuitDesignInput(
        load = load,
        system = system,
        systemVoltage = systemVoltage,
        powerFactor = 0.9,
        lengthMetres = lengthMetres,
        material = ConductorMaterial.COPPER,
        insulation = CableInsulation.PVC,
        method = InstallationMethod.C_CLIPPED_DIRECT,
        ambientTemperatureC = 30.0,
        groupedCircuits = 1,
        parallelConductors = 1,
        maxVoltageDropPercent = maxVoltageDropPercent,
        deviceType = deviceType,
        externalImpedanceOhms = externalImpedanceOhms,
        disconnectionTimeSeconds = 0.4,
    )

    @Test
    fun `a short run is bound by what the conductor can carry`() {
        val result = designCircuit(input(lengthMetres = 5.0))

        assertNotNull(result.crossSectionMm2)
        assertEquals(BindingConstraint.CURRENT_CAPACITY, result.bindingConstraint)
        // The point of saying so: raising the drop allowance would not move it.
        assertTrue(result.voltageDropPercent < result.stages.first { it.constraint == BindingConstraint.VOLTAGE_DROP }.limit)
    }

    @Test
    fun `a tight drop allowance binds, and costs copper`() {
        val short = designCircuit(input(lengthMetres = 5.0))
        val long = designCircuit(
            input(lengthMetres = 150.0, maxVoltageDropPercent = 1.0, externalImpedanceOhms = 0.1),
        )

        assertEquals(BindingConstraint.VOLTAGE_DROP, long.bindingConstraint)
        assertTrue(
            "the drop-bound run should need more copper than the ampacity-bound one",
            long.crossSectionMm2!! > short.crossSectionMm2!!,
        )
        // Same load, so the current capacity requirement has not moved; only
        // the length has. This is the case the app's own field note warns about.
        assertEquals(short.deviceRatingAmps, long.deviceRatingAmps)
    }

    @Test
    fun `on a long run the fault loop bites before the drop does`() {
        // The finding the chain exists to produce, and one no single calculator
        // can reach. At 150 m on an ordinary supply with the usual 4 %
        // allowance, the drop is comfortable at 16 mm² — and the circuit still
        // will not disconnect in 0.4 s, because Zs grows with the same length.
        // An engineer working the calculators one at a time sizes on the drop,
        // finds it passes, and never asks the loop.
        val result = designCircuit(input(lengthMetres = 150.0))

        assertEquals(BindingConstraint.EARTH_FAULT_LOOP, result.bindingConstraint)
        val drop = result.stages.first { it.constraint == BindingConstraint.VOLTAGE_DROP }
        assertTrue("the drop was never the problem here", drop.value < drop.limit)
    }

    @Test
    fun `the cable is sized to carry the device, not merely the load`() {
        // IEC 60364-4-43: Ib <= In <= Iz. A 22 A load takes a 25 A breaker, and
        // 24 A is then a current the breaker will pass indefinitely — so a
        // conductor good for 24 A is not good enough, however comfortably it
        // carries the 22 A actually drawn.
        listOf(7.0, 12.0, 22.0, 29.0, 45.0, 92.0).forEach { amps ->
            val result = designCircuit(input(load = CircuitLoad.Current(amps), lengthMetres = 5.0))
            val rating = result.deviceRatingAmps!!

            assertTrue("$amps A: In below Ib", rating >= amps)
            assertEquals("$amps A: In not a real rating", rating, ProtectiveDeviceRatings.smallestAtLeast(amps))
            assertTrue(
                "$amps A: Iz ${result.deratedCapacityAmps} below In $rating",
                result.deratedCapacityAmps >= rating,
            )
        }
    }

    @Test
    fun `the size below the chosen one really does fail the constraint reported`() {
        // The claim "this is what bound it" is only worth making if it is
        // checked against the alternative the design rejected.
        val request = input(
            lengthMetres = 150.0,
            maxVoltageDropPercent = 1.0,
            externalImpedanceOhms = 0.1,
        )
        val result = designCircuit(request)

        val sizes = ampacityTable.tabulatedSizes(ConductorMaterial.COPPER)
        val chosenIndex = sizes.indexOf(result.crossSectionMm2)
        assertTrue("the chosen size should not be the smallest tabulated", chosenIndex > 0)

        val drop = CalculateVoltageDropUseCase()(
            VoltageDropInput(
                systemVoltage = request.systemVoltage,
                loadCurrent = result.designCurrentAmps,
                lengthMetres = request.lengthMetres,
                crossSectionMm2 = sizes[chosenIndex - 1],
                material = request.material,
                system = request.system,
                powerFactor = request.powerFactor,
                conductorTemperatureC = request.insulation.maxConductorTemperatureC,
                parallelConductors = request.parallelConductors,
            ),
        )
        assertEquals(BindingConstraint.VOLTAGE_DROP, result.bindingConstraint)
        assertTrue(
            "the size below should have exceeded the drop limit",
            drop.dropPercentage > request.maxVoltageDropPercent,
        )
    }

    @Test
    fun `every stage is reported against the size that was actually chosen`() {
        // A chain whose stages describe different cross-sections is worse than
        // no chain: each line reads as an audit of the answer above it.
        val request = input(lengthMetres = 60.0)
        val result = designCircuit(request)

        val drop = CalculateVoltageDropUseCase()(
            VoltageDropInput(
                systemVoltage = request.systemVoltage,
                loadCurrent = result.designCurrentAmps,
                lengthMetres = request.lengthMetres,
                crossSectionMm2 = result.crossSectionMm2!!,
                material = request.material,
                system = request.system,
                powerFactor = request.powerFactor,
                conductorTemperatureC = request.insulation.maxConductorTemperatureC,
                parallelConductors = request.parallelConductors,
            ),
        )
        assertEquals(drop.dropPercentage, result.voltageDropPercent, TOLERANCE)
        assertTrue("every stage of a solved design must pass", result.stages.all { it.passes })
        assertNull(result.failure)
    }

    @Test
    fun `power is converted through the system's own phase factor`() {
        val threePhase = designCircuit(input(load = CircuitLoad.Power(20_000.0)))
        val singlePhase = designCircuit(
            input(
                load = CircuitLoad.Power(20_000.0),
                system = SupplySystem.SINGLE_PHASE_AC,
                systemVoltage = 230.0,
            ),
        )

        // P / (√3 · 400 · 0.9) against P / (230 · 0.9): the same load draws far
        // more current single phase, which is the whole reason three phase exists.
        assertEquals(32.08, threePhase.designCurrentAmps, 0.01)
        assertEquals(96.62, singlePhase.designCurrentAmps, 0.01)
    }

    @Test
    fun `a load past the largest device is refused rather than rounded down`() {
        val result = designCircuit(input(load = CircuitLoad.Current(700.0)))

        assertEquals(DesignFailure.LOAD_BEYOND_DEVICE_RANGE, result.failure)
        assertNull("a device that does not exist must not be reported", result.deviceRatingAmps)
        assertNull(result.crossSectionMm2)
    }

    @Test
    fun `a supply too weak to clear a fault fails, and says which stage failed`() {
        // Ze alone already exceeds the impedance a Type C device can clear at,
        // so no cross-section can rescue it. The useful output is not "no
        // solution" but *why* — this circuit needs a different device or an
        // RCD, not more copper.
        val result = designCircuit(input(externalImpedanceOhms = 3.0))

        assertEquals(DesignFailure.NO_TABULATED_SIZE, result.failure)
        assertNull(result.crossSectionMm2)
        assertEquals(BindingConstraint.EARTH_FAULT_LOOP, result.bindingConstraint)
    }

    @Test
    fun `the fault loop is judged on the line-to-neutral voltage`() {
        // An earth fault on a 400 V circuit is a line-to-earth fault driven by
        // 230 V. Using 400 V would raise the permitted Zs by √3 and pass
        // circuits that do not disconnect in time.
        val threePhase = designCircuit(input(lengthMetres = 5.0))
        val singlePhase = designCircuit(
            input(
                lengthMetres = 5.0,
                system = SupplySystem.SINGLE_PHASE_AC,
                systemVoltage = 230.0,
            ),
        )

        // Not identical: 400/\u221a3 is 230.94 V, a shade above the nominal 230 V a
        // single-phase circuit is named after. Within a per cent is the point —
        // reading the line-to-line voltage instead would make it \u221a3 times larger
        // and pass circuits that never disconnect.
        val ratio = threePhase.maximumLoopImpedanceOhms / singlePhase.maximumLoopImpedanceOhms
        assertEquals("permitted Zs should track the line-to-neutral voltage", 1.0, ratio, 0.01)
    }

    private companion object {
        const val TOLERANCE = 1e-9
    }
}
