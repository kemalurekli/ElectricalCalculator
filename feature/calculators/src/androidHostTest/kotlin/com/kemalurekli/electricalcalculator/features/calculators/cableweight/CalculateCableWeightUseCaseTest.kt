package com.kemalurekli.electricalcalculator.features.calculators.cableweight

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightInput
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CalculateCableWeightUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateCableWeightUseCaseTest {

    private val calculate = CalculateCableWeightUseCase()

    private fun input(
        area: Double = 25.0,
        count: Int = 4,
        length: Double = 1_000.0,
        material: ConductorMaterial = ConductorMaterial.COPPER,
        diameter: Double? = 25.0,
        insulation: CableInsulation = CableInsulation.PVC,
    ) = CableWeightInput(
        crossSectionMm2 = area,
        conductorCount = count,
        lengthMeters = length,
        material = material,
        overallDiameterMm = diameter,
        insulation = insulation,
    )

    // -- The anchor identity ---------------------------------------------------------

    @Test
    fun `one square millimetre of copper weighs 8_89 kilograms per kilometre`() {
        // The figure IEC 60228 tabulates. If this drifts, every other weight
        // in the app is wrong by the same factor.
        val result = calculate(input(area = 1.0, count = 1, length = 1_000.0, diameter = null))

        assertEquals(8.89, result.conductorMassKg, 1e-9)
        assertEquals(1.0, result.conductorVolumeDm3, 1e-9)
    }

    @Test
    fun `one square millimetre of aluminium weighs 2_70 kilograms per kilometre`() {
        val result = calculate(
            input(
                area = 1.0,
                count = 1,
                length = 1_000.0,
                material = ConductorMaterial.ALUMINIUM,
                diameter = null,
            ),
        )

        assertEquals(2.70, result.conductorMassKg, 1e-9)
    }

    // -- Worked reference cases ---------------------------------------------------------

    @Test
    fun `worked example - 4x25 copper PVC over a kilometre`() {
        // Catalogue mass for NYY-J 4×25 mm² is about 1.4–1.5 kg/m, so the
        // estimate is sound for a real cable, not just internally consistent.
        val result = calculate(input())

        assertEquals(889.0, result.conductorMassKg, 1e-9)
        assertEquals(0.889, result.conductorMassPerMeterKg, 1e-9)
        assertEquals(100.0, result.conductorVolumeDm3, 1e-9)
        assertEquals(547.223393, result.nonConductorMassKg!!, 1e-6)
        assertEquals(1436.223393, result.totalMassKg!!, 1e-6)
        assertEquals(1.436223, result.totalMassPerMeterKg!!, 1e-6)
        assertEquals(0.618984, result.conductorMassFraction!!, 1e-6)
    }

    @Test
    fun `worked example - a small three-core flex`() {
        // NYM-J 3×1.5 mm², 10.5 mm overall, catalogue about 0.15 kg/m.
        val result = calculate(input(area = 1.5, count = 3, length = 100.0, diameter = 10.5))

        assertEquals(4.0005, result.conductorMassKg, 1e-9)
        assertEquals(0.154931, result.totalMassPerMeterKg!!, 1e-6)
        // The sheath dominates a small cable: barely a quarter of it is metal.
        assertEquals(0.258211, result.conductorMassFraction!!, 1e-6)
    }

    // -- Without a datasheet diameter -------------------------------------------------------

    @Test
    fun `omitting the diameter reports conductor mass only`() {
        // Insulation thickness is the manufacturer's choice and cannot be
        // derived, so nothing is invented in its place.
        val result = calculate(input(diameter = null))

        assertEquals(889.0, result.conductorMassKg, 1e-9)
        assertNull(result.nonConductorMassKg)
        assertNull(result.totalMassKg)
        assertNull(result.totalMassPerMeterKg)
        assertNull(result.conductorMassFraction)
        assertFalse(result.hasTotal)
    }

    @Test
    fun `supplying the diameter reports the complete cable`() {
        assertTrue(calculate(input()).hasTotal)
    }

    // -- Scaling ---------------------------------------------------------------------------

    @Test
    fun `mass is proportional to length`() {
        val short = calculate(input(length = 100.0)).conductorMassKg
        val long = calculate(input(length = 500.0)).conductorMassKg

        assertEquals(5.0, long / short, 1e-9)
    }

    @Test
    fun `mass per metre does not depend on length`() {
        val short = calculate(input(length = 1.0))
        val long = calculate(input(length = 10_000.0))

        assertEquals(short.conductorMassPerMeterKg, long.conductorMassPerMeterKg, 1e-9)
        assertEquals(short.totalMassPerMeterKg!!, long.totalMassPerMeterKg!!, 1e-9)
    }

    @Test
    fun `mass is proportional to cross-section`() {
        val small = calculate(input(area = 25.0, diameter = null)).conductorMassKg
        val large = calculate(input(area = 50.0, diameter = null)).conductorMassKg

        assertEquals(2.0, large / small, 1e-9)
    }

    @Test
    fun `mass is proportional to the conductor count`() {
        val three = calculate(input(count = 3, diameter = null)).conductorMassKg
        val five = calculate(input(count = 5, diameter = null)).conductorMassKg

        assertEquals(5.0 / 3.0, five / three, 1e-9)
    }

    // -- Material and insulation ---------------------------------------------------------------

    @Test
    fun `copper outweighs aluminium by the ratio of their densities`() {
        val copper = calculate(input(diameter = null)).conductorMassKg
        val aluminium = calculate(
            input(material = ConductorMaterial.ALUMINIUM, diameter = null),
        ).conductorMassKg

        assertEquals(8.89 / 2.70, copper / aluminium, 1e-9)
        // The reason aluminium is used on long runs despite needing more section.
        assertTrue(aluminium < copper / 3.0)
    }

    @Test
    fun `the conductor figures ignore the insulation choice`() {
        val pvc = calculate(input(insulation = CableInsulation.PVC))
        val xlpe = calculate(input(insulation = CableInsulation.XLPE))

        assertEquals(pvc.conductorMassKg, xlpe.conductorMassKg, 1e-9)
    }

    @Test
    fun `a PVC sheath is heavier than an XLPE one of the same geometry`() {
        val pvc = calculate(input(insulation = CableInsulation.PVC))
        val xlpe = calculate(input(insulation = CableInsulation.XLPE))

        assertTrue(pvc.totalMassKg!! > xlpe.totalMassKg!!)
        assertEquals(1.40 / 0.92, pvc.nonConductorMassKg!! / xlpe.nonConductorMassKg!!, 1e-9)
    }

    // -- Geometry -------------------------------------------------------------------------------

    @Test
    fun `a fatter sheath adds mass with the square of the diameter`() {
        val slim = calculate(input(diameter = 25.0)).nonConductorMassKg!!
        val fat = calculate(input(diameter = 50.0)).nonConductorMassKg!!

        // Conductor area is common to both, so only the circular terms differ.
        val conductorAreaTerm = 100.0 * 1_000.0 / 1_000.0 * 1.40
        assertEquals(4.0, (fat + conductorAreaTerm) / (slim + conductorAreaTerm), 1e-9)
    }

    @Test
    fun `a diameter that leaves no room for insulation yields no sheath mass`() {
        // 100 mm² of conductor needs at least an 11.28 mm circle; the guard
        // stops a physically impossible entry producing negative mass.
        val result = calculate(input(diameter = 11.0))

        assertEquals(0.0, result.nonConductorMassKg!!, 1e-9)
        assertEquals(result.conductorMassKg, result.totalMassKg!!, 1e-9)
        assertEquals(1.0, result.conductorMassFraction!!, 1e-9)
    }

    @Test
    fun `the conductor fraction stays between zero and one`() {
        val result = calculate(input())

        assertTrue(result.conductorMassFraction!! > 0.0)
        assertTrue(result.conductorMassFraction!! < 1.0)
    }
}
