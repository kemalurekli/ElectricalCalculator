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

    @Test
    fun `the large PVC conduit sizes are the ones checked against the standard`() {
        // Checked 2026-09-04 and corrected. These eight cells — methods B1 and
        // B2, three loaded conductors, PVC, 150 mm² and above — used to read
        // 275/236, 314/268, 370/315 and 426/360, five to eight per cent above
        // what the harmonised tables give. Every invariant below passed while
        // they were wrong, because being uniformly too high breaks none of
        // them: the column still rose with area, still sat under XLPE, still
        // sat above aluminium. It only under-sized cable.
        //
        // What gave them away was the ratio to the two-conductor column, which
        // falls steadily from 0,888 at 120 mm² to 0,860 at 300 mm² in the
        // standard and jumped back up to 0,917 here.
        val b1 = InstallationMethod.B1_CONDUIT_ON_WALL
        val b2 = InstallationMethod.B2_MULTICORE_IN_CONDUIT
        assertEquals(262.0, capacity(150.0, method = b1)!!, 1e-9)
        assertEquals(296.0, capacity(185.0, method = b1)!!, 1e-9)
        assertEquals(346.0, capacity(240.0, method = b1)!!, 1e-9)
        assertEquals(394.0, capacity(300.0, method = b1)!!, 1e-9)
        assertEquals(225.0, capacity(150.0, method = b2)!!, 1e-9)
        assertEquals(255.0, capacity(185.0, method = b2)!!, 1e-9)
        assertEquals(297.0, capacity(240.0, method = b2)!!, 1e-9)
        assertEquals(339.0, capacity(300.0, method = b2)!!, 1e-9)
    }

    @Test
    fun `the two cells where IEC and BS 7671 disagree keep the IEC figure`() {
        // XLPE, three loaded conductors, free air. BS 7671 Table 4E2A prints
        // 399 and 456 here; the IEC table this app follows prints 395 and 450.
        // Every other cell the two standards share is identical, so this is a
        // real difference between them and not a typo in either. The app says
        // IEC on the tin, so it keeps the IEC figure — and a user checking
        // against the brown book will find these two and should find this note.
        val e = InstallationMethod.E_FREE_AIR
        assertEquals(395.0, capacity(150.0, insulation = CableInsulation.XLPE, method = e)!!, 1e-9)
        assertEquals(450.0, capacity(185.0, insulation = CableInsulation.XLPE, method = e)!!, 1e-9)
    }

    @Test
    fun `no column is a copy of a column from another table`() {
        // Four tables, four methods: sixteen columns that were transcribed
        // independently and should read independently. Two of them agreeing on
        // every size from 2,5 to 300 mm² is not a coincidence — real tables
        // round independently — it is the fingerprint of a column pasted from
        // one table into another.
        //
        // Aluminium on XLPE is where this is easy to do and hard to notice:
        // aluminium carries about 78 % of copper and XLPE adds about 25 %, so
        // aluminium/XLPE lands within a couple of per cent of copper/PVC, and a
        // pasted column looks plausible on every graph.
        //
        // The four below are the ones already in the table when this test was
        // written. They are listed rather than fixed because no source this
        // project can reach carries the non-armoured aluminium tables — see the
        // `ampacity-aluminium` row of docs/verification-backlog.md. Anyone who
        // opens the standard should delete the entry they have checked.
        val known = setOf(
            "ALUMINIUM/XLPE/TWO/B1 == COPPER/PVC/TWO/B1",
            "ALUMINIUM/XLPE/THREE/B2 == COPPER/PVC/THREE/B2",
            "ALUMINIUM/PVC/TWO/C == ALUMINIUM/XLPE/THREE/B1",
            "ALUMINIUM/PVC/TWO/E == ALUMINIUM/XLPE/THREE/C",
        )

        val columns = mutableMapOf<String, Map<Double, Double>>()
        forEveryCombination { material, insulation, method, conductors ->
            val name = "$material/$insulation/$conductors/${short(method)}"
            columns[name] = table.tabulatedSizes(material).mapNotNull { area ->
                table.capacityAmps(area, material, insulation, method, conductors)
                    ?.let { area to it }
            }.toMap()
        }

        val duplicates = mutableListOf<String>()
        columns.keys.sorted().forEachIndexed { index, first ->
            columns.keys.sorted().drop(index + 1).forEach { second ->
                if (first.substringBeforeLast('/') == second.substringBeforeLast('/')) return@forEach
                val shared = columns.getValue(first).keys intersect columns.getValue(second).keys
                if (shared.size < 10) return@forEach
                val same = shared.all { columns.getValue(first)[it] == columns.getValue(second)[it] }
                if (same) duplicates += "$first == $second"
            }
        }

        assertEquals(
            "Columns that read identically across two different tables",
            known,
            duplicates.toSet(),
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
    fun `no aluminium cell claims more capacity than the metal allows`() {
        // The aluminium half of the table is the part no published source
        // reproduces, so this stands in for the source. For the same geometry
        // and the same permitted temperature rise, I²R is the same in either
        // metal, which puts aluminium at √(ρ_cu / ρ_al) = 0,78 of copper. The
        // one aluminium block that has been read against the standard — PVC,
        // three loaded conductors, methods B1, B2 and C, as Schneider's
        // installation guide reproduces IEC table B.52.4 — sits at 0,78, 0,78
        // and 0,77 of its copper column, which is that number.
        //
        // The band is deliberately lopsided. A cell that is *low* costs the
        // user a cable size; a cell that is *high* under-sizes a conductor,
        // which is the direction that burns. So 5 % of headroom above the
        // prediction and 12 % below: enough that the standard's own rounding
        // and the differences in cable diameter between the two metals pass,
        // not enough for a pasted or mistyped column to hide.
        forEveryMethodAndConductorCount(ConductorMaterial.ALUMINIUM) { _, method, conductors ->
            CableInsulation.entries.forEach { insulation ->
                table.tabulatedSizes(ConductorMaterial.ALUMINIUM).forEach { area ->
                    val cu = table.capacityAmps(area, ConductorMaterial.COPPER, insulation, method, conductors)
                    val al = table.capacityAmps(area, ConductorMaterial.ALUMINIUM, insulation, method, conductors)
                    if (cu != null && al != null) {
                        val predicted = cu * METAL_RATIO
                        val error = (al - predicted) / predicted
                        assertTrue(
                            "$area mm² $insulation/$method/$conductors: Al $al is " +
                                "${(error * 100).toInt()} % off the ${predicted.toInt()} A that " +
                                "copper's $cu A predicts",
                            error in -0.12..0.05,
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

    private companion object {
        /** √(0,017241 / 0,028264): what the two resistivities allow. */
        const val METAL_RATIO = 0.781
    }

    private fun short(method: InstallationMethod) = when (method) {
        InstallationMethod.B1_CONDUIT_ON_WALL -> "B1"
        InstallationMethod.B2_MULTICORE_IN_CONDUIT -> "B2"
        InstallationMethod.C_CLIPPED_DIRECT -> "C"
        InstallationMethod.E_FREE_AIR -> "E"
    }
}
