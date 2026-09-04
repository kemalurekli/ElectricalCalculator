package com.kemalurekli.electricalcalculator.features.calculators.earthfault

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.AdiabaticFactors
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultInput
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateEarthFaultUseCaseTest {

    private val calculate = CalculateEarthFaultUseCase()

    private fun input(
        ze: Double = 0.35,
        voltage: Double = 230.0,
        length: Double = 30.0,
        line: Double = 4.0,
        protective: Double = 2.5,
        parallel: Int = 1,
        material: ConductorMaterial = ConductorMaterial.COPPER,
        insulation: CableInsulation = CableInsulation.PVC,
        deviceType: ProtectiveDeviceType = ProtectiveDeviceType.MCB_TYPE_B,
        rating: Double = 32.0,
        clearingTime: Double = 0.1,
    ) = EarthFaultInput(
        externalImpedanceOhms = ze,
        phaseVoltage = voltage,
        lengthMetres = length,
        lineCrossSectionMm2 = line,
        protectiveCrossSectionMm2 = protective,
        parallelConductors = parallel,
        material = material,
        insulation = insulation,
        deviceType = deviceType,
        deviceRatingAmps = rating,
        clearingTimeSeconds = clearingTime,
    )

    // -- Worked reference case ---------------------------------------------------------

    @Test
    fun `worked example - 32 A type B on 30 m of 4 mm with a 2_5 mm earth`() {
        val result = calculate(input())

        assertEquals(0.154716, result.lineResistanceOhms, 1e-6)
        assertEquals(0.247546, result.protectiveResistanceOhms, 1e-6)
        assertEquals(0.752263, result.loopImpedanceOhms, 1e-6)
        assertEquals(160.0, result.operatingCurrentAmps!!, 1e-9)
        assertEquals(1.365625, result.maximumPermittedOhms, 1e-6)
        assertEquals(290.457043, result.faultCurrentAmps, 1e-5)
        assertTrue(result.disconnectsInTime)
    }

    // -- The curve decides, not the cable ------------------------------------------------

    @Test
    fun `the same circuit passes on a type B and fails on a type C`() {
        // The lesson this calculator exists to teach: nothing about the cable
        // changed. A Type C needs ten times its rating to trip instead of five,
        // so it needs half the loop impedance — and this circuit has not got it.
        val typeB = calculate(input(deviceType = ProtectiveDeviceType.MCB_TYPE_B))
        val typeC = calculate(input(deviceType = ProtectiveDeviceType.MCB_TYPE_C))

        assertEquals(typeB.loopImpedanceOhms, typeC.loopImpedanceOhms, 1e-12)
        assertTrue(typeB.disconnectsInTime)
        assertFalse(typeC.disconnectsInTime)
        assertEquals(0.682813, typeC.maximumPermittedOhms, 1e-6)
    }

    @Test
    fun `each MCB type demands its own multiple of the rating`() {
        val b = calculate(input(deviceType = ProtectiveDeviceType.MCB_TYPE_B))
        val c = calculate(input(deviceType = ProtectiveDeviceType.MCB_TYPE_C))
        val d = calculate(input(deviceType = ProtectiveDeviceType.MCB_TYPE_D))

        assertEquals(160.0, b.operatingCurrentAmps!!, 1e-9)
        assertEquals(320.0, c.operatingCurrentAmps!!, 1e-9)
        assertEquals(640.0, d.operatingCurrentAmps!!, 1e-9)
    }

    @Test
    fun `a larger device needs a lower loop impedance`() {
        val small = calculate(input(rating = 6.0)).maximumPermittedOhms
        val large = calculate(input(rating = 63.0)).maximumPermittedOhms

        assertTrue(large < small)
    }

    // -- The two ways of asking the same question -----------------------------------------

    @Test
    fun `passing on impedance and passing on current are the same check`() {
        listOf(10.0, 32.0, 63.0).forEach { rating ->
            listOf(20.0, 30.0, 80.0).forEach { length ->
                val result = calculate(input(rating = rating, length = length))
                val currentIsEnough = result.faultCurrentAmps >= result.operatingCurrentAmps!!
                assertEquals(
                    "verdicts disagreed at $rating A over $length m",
                    result.disconnectsInTime,
                    currentIsEnough,
                )
            }
        }
    }

    @Test
    fun `a circuit exactly on the limit passes`() {
        // Zs_max = 0.95 · 230 / 160 = 1.365625 Ω; Ze is set so the loop lands
        // precisely there.
        val cable = calculate(input(ze = 0.0)).loopImpedanceOhms
        val result = calculate(input(ze = 1.365625 - cable))

        assertEquals(1.0, result.impedanceUtilisation, 1e-9)
        assertTrue(result.disconnectsInTime)
    }

    // -- Length -----------------------------------------------------------------------------

    @Test
    fun `a long run stops the device seeing enough current`() {
        // The everyday failure: the cable is fine on ampacity and on voltage
        // drop, and still will not trip.
        val near = calculate(input(length = 30.0))
        val far = calculate(input(length = 100.0))

        assertTrue(near.disconnectsInTime)
        assertFalse(far.disconnectsInTime)
        assertEquals(1.690876, far.loopImpedanceOhms, 1e-6)
    }

    @Test
    fun `impedance rises with length`() {
        val short = calculate(input(length = 25.0)).loopImpedanceOhms
        val long = calculate(input(length = 100.0)).loopImpedanceOhms

        assertTrue(long > short)
    }

    @Test
    fun `a bigger earth conductor lowers the loop`() {
        val thin = calculate(input(protective = 1.5)).loopImpedanceOhms
        val thick = calculate(input(protective = 4.0)).loopImpedanceOhms

        assertTrue(thick < thin)
    }

    @Test
    fun `parallel conductors divide both resistances`() {
        val single = calculate(input(parallel = 1))
        val double = calculate(input(parallel = 2))

        assertEquals(2.0, single.lineResistanceOhms / double.lineResistanceOhms, 1e-12)
        assertEquals(
            2.0,
            single.protectiveResistanceOhms / double.protectiveResistanceOhms,
            1e-12,
        )
    }

    // -- Residual current devices -----------------------------------------------------------

    @Test
    fun `an RCD is judged on touch voltage, not on operating current`() {
        val result = calculate(
            input(deviceType = ProtectiveDeviceType.RCD, rating = 0.03),
        )

        assertNull(result.operatingCurrentAmps)
        assertEquals(50.0 / 0.03, result.maximumPermittedOhms, 1e-9)
    }

    @Test
    fun `a 30 mA RCD tolerates a loop no overcurrent device could`() {
        // Why a TT installation works at all: 1667 Ω against the 1.37 Ω a
        // 32 A Type B needs.
        val rcd = calculate(input(deviceType = ProtectiveDeviceType.RCD, rating = 0.03))
        val mcb = calculate(input(deviceType = ProtectiveDeviceType.MCB_TYPE_B))

        assertTrue(rcd.maximumPermittedOhms > mcb.maximumPermittedOhms * 1_000)
        assertTrue(rcd.disconnectsInTime)
    }

    @Test
    fun `a less sensitive RCD permits a lower loop impedance`() {
        val sensitive = calculate(input(deviceType = ProtectiveDeviceType.RCD, rating = 0.03))
        val coarse = calculate(input(deviceType = ProtectiveDeviceType.RCD, rating = 0.3))

        assertEquals(10.0, sensitive.maximumPermittedOhms / coarse.maximumPermittedOhms, 1e-9)
    }

    // -- Custom device ----------------------------------------------------------------------

    @Test
    fun `a custom operating current is used as given`() {
        // Fuses, MCCBs and anything with a published curve: the user reads Ia
        // off it, so no per-device table has to be transcribed.
        val result = calculate(
            input(deviceType = ProtectiveDeviceType.CUSTOM, rating = 250.0),
        )

        assertEquals(250.0, result.operatingCurrentAmps!!, 1e-9)
        assertEquals(0.95 * 230.0 / 250.0, result.maximumPermittedOhms, 1e-9)
    }

    // -- Adiabatic withstand ------------------------------------------------------------------

    @Test
    fun `the protective conductor is checked against the energy it must pass`() {
        val result = calculate(input())

        assertEquals(AdiabaticFactors.COPPER_PVC, result.adiabaticFactor, 1e-9)
        assertEquals(0.798701, result.adiabaticMinimumMm2, 1e-6)
        assertTrue(result.protectiveConductorWithstands)
    }

    @Test
    fun `a longer clearing time demands a larger conductor`() {
        // S grows with √t, so a device four times slower needs twice the earth.
        val fast = calculate(input(clearingTime = 0.1)).adiabaticMinimumMm2
        val slow = calculate(input(clearingTime = 0.4)).adiabaticMinimumMm2

        assertEquals(2.0, slow / fast, 1e-9)
        assertEquals(1.597401, slow, 1e-6)
    }

    @Test
    fun `an undersized earth fails the withstand check while still disconnecting`() {
        // The failure mode the second verdict exists for, and it needs a
        // *stiff* circuit to appear: a short 16 mm² sub-main close to the
        // origin, earthed with 1.5 mm², on a device that takes a second to
        // clear. The loop is tiny so it trips easily — and precisely because
        // the loop is tiny, 1745 A flows through an earth that needs 15 mm².
        //
        // Shrinking the earth instead would not show this: it raises the loop
        // enough that the circuit stops tripping at all, and then the first
        // verdict fails too.
        val result = calculate(
            input(ze = 0.05, length = 5.0, line = 16.0, protective = 1.5, clearingTime = 1.0),
        )

        assertTrue(result.disconnectsInTime)
        assertEquals(1745.077029, result.faultCurrentAmps, 1e-5)
        assertEquals(15.174583, result.adiabaticMinimumMm2, 1e-6)
        assertFalse(result.protectiveConductorWithstands)
        assertFalse(result.isCompliant)
    }

    @Test
    fun `both verdicts are required for compliance`() {
        val good = calculate(input())

        assertTrue(good.disconnectsInTime)
        assertTrue(good.protectiveConductorWithstands)
        assertTrue(good.isCompliant)
    }

    @Test
    fun `thermoset insulation tolerates more energy than thermoplastic`() {
        val pvc = calculate(input(insulation = CableInsulation.PVC))
        val xlpe = calculate(input(insulation = CableInsulation.XLPE))

        assertTrue(xlpe.adiabaticFactor > pvc.adiabaticFactor)
        assertTrue(xlpe.adiabaticMinimumMm2 < pvc.adiabaticMinimumMm2)
    }

    @Test
    fun `aluminium needs a larger protective conductor than copper`() {
        val copper = calculate(input(material = ConductorMaterial.COPPER))
        val aluminium = calculate(input(material = ConductorMaterial.ALUMINIUM))

        assertTrue(aluminium.adiabaticFactor < copper.adiabaticFactor)
        assertEquals(1.012204, aluminium.loopImpedanceOhms, 1e-6)
        assertEquals(0.898193, aluminium.adiabaticMinimumMm2, 1e-6)
    }

    // -- The table route ----------------------------------------------------------------------

    @Test
    fun `the tabulated rule follows the three steps of the standard`() {
        assertEquals(4.0, AdiabaticFactors.tabulatedProtectiveSection(4.0), 1e-12)
        assertEquals(16.0, AdiabaticFactors.tabulatedProtectiveSection(16.0), 1e-12)
        assertEquals(16.0, AdiabaticFactors.tabulatedProtectiveSection(25.0), 1e-12)
        assertEquals(16.0, AdiabaticFactors.tabulatedProtectiveSection(35.0), 1e-12)
        assertEquals(25.0, AdiabaticFactors.tabulatedProtectiveSection(50.0), 1e-12)
        assertEquals(120.0, AdiabaticFactors.tabulatedProtectiveSection(240.0), 1e-12)
    }

    @Test
    fun `the table is usually the more generous of the two routes`() {
        // Which is why the standard permits the calculation as an alternative,
        // and why both are reported instead of one being chosen.
        val result = calculate(input())

        assertTrue(result.tabulatedMinimumMm2 > result.adiabaticMinimumMm2)
    }

    @Test
    fun `the tabulated rule never returns more than the line conductor`() {
        listOf(1.5, 4.0, 16.0, 25.0, 50.0, 300.0).forEach { line ->
            assertTrue(
                "tabulated PE exceeded the line at $line mm²",
                AdiabaticFactors.tabulatedProtectiveSection(line) <= line,
            )
        }
    }

    // -- Ordering of the k factors ---------------------------------------------------------------

    @Test
    fun `the breaker multipliers are the top of each band in the standard`() {
        // Checked 2026-09-04 against IEC 60898-1 (BS EN 60898-1), whose
        // instantaneous bands are B 3–5, C 5–10 and D 10–20 times In, all
        // tripping in under 0.1 s.
        //
        // The calculator takes the *upper* end of each band, and has to: a
        // breaker is only guaranteed to have tripped magnetically once the
        // current passes the top of its band, so proving disconnection against
        // the bottom would prove it for a breaker nobody owns.
        assertEquals(5.0, ProtectiveDeviceType.MCB_TYPE_B.instantaneousMultiplier)
        assertEquals(10.0, ProtectiveDeviceType.MCB_TYPE_C.instantaneousMultiplier)
        assertEquals(20.0, ProtectiveDeviceType.MCB_TYPE_D.instantaneousMultiplier)
    }

    @Test
    fun `the k factors are the four values checked against the standard`() {
        // Checked 2026-09-04 against IEC 60364-5-54 Table A.54.4 — protective
        // conductor as a core in a cable or bunched with other cables —
        // reproduced as BS 7671 Table 54.3. Thermoplastic runs 70 °C to 160 °C,
        // thermoset 90 °C to 250 °C.
        //
        // The shape assertions below cannot catch a mistyped digit that still
        // respects the ordering, so the numbers are pinned. Changing one of them
        // means going back to the table, not to this test.
        assertEquals(115.0, AdiabaticFactors.COPPER_PVC, 1e-12)
        assertEquals(143.0, AdiabaticFactors.COPPER_XLPE, 1e-12)
        assertEquals(76.0, AdiabaticFactors.ALUMINIUM_PVC, 1e-12)
        assertEquals(94.0, AdiabaticFactors.ALUMINIUM_XLPE, 1e-12)
    }

    @Test
    fun `copper outperforms aluminium and thermoset outperforms thermoplastic`() {
        // The content is transcribed and cannot be proven, but its shape can.
        assertTrue(AdiabaticFactors.COPPER_PVC > AdiabaticFactors.ALUMINIUM_PVC)
        assertTrue(AdiabaticFactors.COPPER_XLPE > AdiabaticFactors.ALUMINIUM_XLPE)
        assertTrue(AdiabaticFactors.COPPER_XLPE > AdiabaticFactors.COPPER_PVC)
        assertTrue(AdiabaticFactors.ALUMINIUM_XLPE > AdiabaticFactors.ALUMINIUM_PVC)
    }

    @Test
    fun `every material and insulation pair resolves to a factor`() {
        ConductorMaterial.entries.forEach { material ->
            CableInsulation.entries.forEach { insulation ->
                val k = AdiabaticFactors.forProtectiveConductor(material, insulation)
                assertTrue("$material/$insulation has no usable k", k > 0.0)
            }
        }
    }
}
