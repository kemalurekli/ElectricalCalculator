package com.kemalurekli.electricalcalculator.features.calculators.evse

import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.CalculateEvseUseCase
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.DcFaultDetection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseConnection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseInput
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.RcdRequirement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A charging installation, sized and protected.
 *
 * The RCD cases carry the most weight here. A Type A in front of a charger with
 * no DC detection is a device that looks installed and does nothing, and the
 * app must never arrive at that answer by omission.
 */
class CalculateEvseUseCaseTest {

    private val calculate = CalculateEvseUseCase()

    private fun input(
        pointCount: Int = 1,
        ratedCurrentPerPoint: Double = 32.0,
        connection: EvseConnection = EvseConnection.SINGLE_PHASE,
        supplyVoltage: Double = 230.0,
        simultaneityFactor: Double = 1.0,
        dcFaultDetection: DcFaultDetection = DcFaultDetection.BUILT_IN_6MA,
    ) = EvseInput(
        pointCount, ratedCurrentPerPoint, connection, supplyVoltage,
        simultaneityFactor, dcFaultDetection,
    )

    @Test
    fun `the same current is a very different charger on three phases`() {
        // 32 A single phase at 230 V is 7.4 kW; the same 32 A on three phases
        // at 400 V is 22 kW. The commonest misunderstanding on a domestic job.
        val single = calculate(input())
        val three = calculate(
            input(connection = EvseConnection.THREE_PHASE, supplyVoltage = 400.0),
        )

        assertEquals(7.36, single.powerPerPointKw, 0.01)
        assertEquals(22.17, three.powerPerPointKw, 0.01)
    }

    @Test
    fun `with no diversity the design current is the whole bank`() {
        // The honest default. Charging is continuous, so a bank of six points
        // is six points drawing at once until someone decides otherwise.
        val result = calculate(input(pointCount = 6))

        assertEquals(192.0, result.totalConnectedAmps, 1e-9)
        assertEquals(192.0, result.designCurrentAmps, 1e-9)
        assertTrue(result.continuousLoad)
    }

    @Test
    fun `a simultaneity factor is the user's decision and is applied as given`() {
        val result = calculate(input(pointCount = 6, simultaneityFactor = 0.5))

        assertEquals(192.0, result.totalConnectedAmps, 1e-9)
        assertEquals(96.0, result.designCurrentAmps, 1e-9)
        // And the device follows the diversified figure, not the connected one.
        assertEquals(100.0, result.deviceRatingAmps!!, 1e-9)
    }

    @Test
    fun `a charger without DC detection needs a Type B`() {
        // The answer that matters. A vehicle can put smooth DC into the
        // protective conductor, which blinds a Type A completely.
        val result = calculate(input(dcFaultDetection = DcFaultDetection.NONE))

        assertEquals(RcdRequirement.TYPE_B, result.rcdRequirement)
    }

    @Test
    fun `a charger that declares 6 mA DC detection may sit behind a Type A`() {
        val result = calculate(input(dcFaultDetection = DcFaultDetection.BUILT_IN_6MA))

        assertEquals(RcdRequirement.TYPE_A, result.rcdRequirement)
    }

    @Test
    fun `the device is drawn from ratings that exist`() {
        // 32 A single point takes a 32 A device; 33 A would take 40, not 33.
        assertEquals(32.0, calculate(input()).deviceRatingAmps!!, 1e-9)
        assertEquals(40.0, calculate(input(ratedCurrentPerPoint = 33.0)).deviceRatingAmps!!, 1e-9)
    }

    @Test
    fun `a bank beyond the largest modelled device is refused rather than rounded`() {
        val result = calculate(input(pointCount = 30))

        assertEquals(960.0, result.designCurrentAmps, 1e-9)
        assertNull("no device exists for this, and none should be invented", result.deviceRatingAmps)
    }
}
