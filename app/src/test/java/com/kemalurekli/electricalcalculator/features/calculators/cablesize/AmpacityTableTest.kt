package com.kemalurekli.electricalcalculator.features.calculators.cablesize

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.LoadedConductors
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.AmpacityTable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the transcribed IEC 60364-5-52 capacity data.
 *
 * The values are data, not derivations, so no test can prove them correct
 * against the standard. What these tests *can* do is assert the physical
 * relationships the table must obey. A mistyped digit almost always breaks one
 * of them — a value that fails to rise with area, XLPE that falls below PVC,
 * aluminium that beats copper — so this catches transcription errors without
 * the printed standard to hand.
 *
 * Spot values are additionally pinned so that any future edit to the table is a
 * deliberate, reviewed change rather than a silent one.
 */
class AmpacityTableTest {

    private val table = AmpacityTable()

    private fun capacity(
        area: Double,
        material: ConductorMaterial = ConductorMaterial.COPPER,
        insulation: CableInsulation = CableInsulation.PVC,
        method: InstallationMethod = InstallationMethod.C_CLIPPED_DIRECT,
        conductors: LoadedConductors = LoadedConductors.THREE,
    ) = table.capacityAmps(area, material, insulation, method, conductors)

    // -- Pinned reference values --------------------------------------------

    @Test
    fun `well known copper PVC values are as tabulated`() {
        // The values an electrician can recite: 2.5 mm² PVC copper, method C,
        // three loaded conductors is 24 A; 1.5 mm² is 17.5 A.
        assertEquals(17.5, capacity(1.5)!!, 1e-9)
        assertEquals(24.0, capacity(2.5)!!, 1e-9)
        assertEquals(32.0, capacity(4.0)!!, 1e-9)
        assertEquals(41.0, capacity(6.0)!!, 1e-9)
        assertEquals(57.0, capacity(10.0)!!, 1e-9)
    }

    @Test
    fun `method B1 differs from method C as the table requires`() {
        assertEquals(
            15.5,
            capacity(1.5, method = InstallationMethod.B1_CONDUIT_ON_WALL)!!,
            1e-9,
        )
        assertEquals(
            17.5,
            capacity(1.5, method = InstallationMethod.C_CLIPPED_DIRECT)!!,
            1e-9,
        )
    }

    // -- Physical consistency ------------------------------------------------

    @Test
    fun `capacity rises with cross-section for every combination`() {
        forEveryCombination { material, insulation, method, conductors ->
            val capacities = table.tabulatedSizes(material).mapNotNull { area ->
                table.capacityAmps(area, material, insulation, method, conductors)
            }
            capacities.zipWithNext().forEach { (smaller, larger) ->
                assertTrue(
                    "$material/$insulation/$method/$conductors: $larger must exceed $smaller",
                    larger > smaller,
                )
            }
        }
    }

    @Test
    fun `XLPE carries at least as much as PVC`() {
        // A 90 degree conductor may run hotter, so it can never carry less.
        forEveryMethodAndConductorCount { material, method, conductors ->
            table.tabulatedSizes(material).forEach { area ->
                val pvc = table.capacityAmps(area, material, CableInsulation.PVC, method, conductors)
                val xlpe = table.capacityAmps(area, material, CableInsulation.XLPE, method, conductors)
                if (pvc != null && xlpe != null) {
                    assertTrue(
                        "$area mm² $material/$method/$conductors: XLPE $xlpe < PVC $pvc",
                        xlpe >= pvc,
                    )
                }
            }
        }
    }

    @Test
    fun `copper carries more than aluminium at the same size`() {
        forEveryMethodAndConductorCount(ConductorMaterial.ALUMINIUM) { _, method, conductors ->
            CableInsulation.entries.forEach { insulation ->
                table.tabulatedSizes(ConductorMaterial.ALUMINIUM).forEach { area ->
                    val cu = table.capacityAmps(
                        area, ConductorMaterial.COPPER, insulation, method, conductors,
                    )
                    val al = table.capacityAmps(
                        area, ConductorMaterial.ALUMINIUM, insulation, method, conductors,
                    )
                    if (cu != null && al != null) {
                        assertTrue(
                            "$area mm² $insulation/$method/$conductors: Al $al >= Cu $cu",
                            cu > al,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `two loaded conductors carry more than three`() {
        // A third current-carrying conductor adds heat to the same bundle.
        forEveryMaterialAndInsulation { material, insulation ->
            InstallationMethod.entries.forEach { method ->
                table.tabulatedSizes(material).forEach { area ->
                    val two = table.capacityAmps(area, material, insulation, method, LoadedConductors.TWO)
                    val three = table.capacityAmps(area, material, insulation, method, LoadedConductors.THREE)
                    if (two != null && three != null) {
                        assertTrue(
                            "$area mm² $material/$insulation/$method: 2-core $two < 3-core $three",
                            two >= three,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `free air carries at least as much as enclosed conduit`() {
        // Method E sheds heat freely; B2 is enclosed. B2 can never beat E.
        forEveryMaterialAndInsulation { material, insulation ->
            LoadedConductors.entries.forEach { conductors ->
                table.tabulatedSizes(material).forEach { area ->
                    val enclosed = table.capacityAmps(
                        area, material, insulation, InstallationMethod.B2_MULTICORE_IN_CONDUIT, conductors,
                    )
                    val freeAir = table.capacityAmps(
                        area, material, insulation, InstallationMethod.E_FREE_AIR, conductors,
                    )
                    if (enclosed != null && freeAir != null) {
                        assertTrue(
                            "$area mm² $material/$insulation/$conductors: E $freeAir < B2 $enclosed",
                            freeAir >= enclosed,
                        )
                    }
                }
            }
        }
    }

    // -- Coverage -------------------------------------------------------------

    @Test
    fun `aluminium is not tabulated below 2point5 mm2`() {
        // Aluminium conductors are not manufactured that small; the caller must
        // skip the size rather than be handed a fabricated value.
        assertNull(capacity(1.5, material = ConductorMaterial.ALUMINIUM))
        assertNotNull(capacity(2.5, material = ConductorMaterial.ALUMINIUM))
    }

    @Test
    fun `copper is tabulated from 1point5 mm2 upward`() {
        assertEquals(1.5, table.tabulatedSizes(ConductorMaterial.COPPER).first(), 1e-9)
        assertEquals(300.0, table.tabulatedSizes(ConductorMaterial.COPPER).last(), 1e-9)
    }

    @Test
    fun `every tabulated size resolves for every combination`() {
        forEveryCombination { material, insulation, method, conductors ->
            table.tabulatedSizes(material).forEach { area ->
                assertNotNull(
                    "$area mm² missing for $material/$insulation/$method/$conductors",
                    table.capacityAmps(area, material, insulation, method, conductors),
                )
            }
        }
    }

    // -- Helpers ---------------------------------------------------------------

    private fun forEveryCombination(
        block: (ConductorMaterial, CableInsulation, InstallationMethod, LoadedConductors) -> Unit,
    ) {
        ConductorMaterial.entries.forEach { material ->
            CableInsulation.entries.forEach { insulation ->
                InstallationMethod.entries.forEach { method ->
                    LoadedConductors.entries.forEach { conductors ->
                        block(material, insulation, method, conductors)
                    }
                }
            }
        }
    }

    private fun forEveryMethodAndConductorCount(
        material: ConductorMaterial = ConductorMaterial.COPPER,
        block: (ConductorMaterial, InstallationMethod, LoadedConductors) -> Unit,
    ) {
        InstallationMethod.entries.forEach { method ->
            LoadedConductors.entries.forEach { conductors ->
                block(material, method, conductors)
            }
        }
    }

    private fun forEveryMaterialAndInsulation(
        block: (ConductorMaterial, CableInsulation) -> Unit,
    ) {
        ConductorMaterial.entries.forEach { material ->
            CableInsulation.entries.forEach { insulation ->
                block(material, insulation)
            }
        }
    }
}
