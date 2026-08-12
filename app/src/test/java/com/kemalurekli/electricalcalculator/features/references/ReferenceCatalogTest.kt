package com.kemalurekli.electricalcalculator.features.references

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.references.domain.CalloutKind
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceBlock
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * What can and cannot be tested here.
 *
 * The reference content is transcribed from published standards, so no test can
 * prove a value right — that is a review job, and the app says as much by
 * citing a source on every topic. What a test *can* do is guarantee the shape:
 * that nothing is empty, that every key is unique and resolvable, that no
 * symbol was left as an untranslatable literal where it should be prose, and
 * that figures shared with the calculators are read from one place rather than
 * typed twice.
 */
class ReferenceCatalogTest {

    private val topics = ReferenceCatalog.all

    // -- Structure ------------------------------------------------------------------

    @Test
    fun `every topic is reachable by its key`() {
        topics.forEach { topic ->
            assertEquals(topic, ReferenceCatalog.topicOrNull(topic.key))
        }
    }

    @Test
    fun `an unknown key resolves to nothing rather than throwing`() {
        // The detail screen renders empty for a stale deep link; it must not
        // be handed an exception instead.
        assertNull(ReferenceCatalog.topicOrNull("no_such_topic"))
    }

    @Test
    fun `topic keys are unique`() {
        val keys = topics.map { it.key }

        assertEquals(keys.size, keys.distinct().size)
    }

    @Test
    fun `every topic has content`() {
        // Counted per block rather than per row: a procedure or an explanation
        // has no table rows and is not therefore empty.
        topics.forEach { topic ->
            assertTrue("${topic.key} has no sections", topic.sections.isNotEmpty())
            topic.sections.forEach { section ->
                assertTrue("${topic.key} has a section with no blocks", section.blocks.isNotEmpty())
                section.blocks.forEach { block ->
                    assertTrue("${topic.key} has an empty block", block.itemCount() > 0)
                }
            }
        }
    }

    @Test
    fun `no prose, procedure or callout has an unresolved string`() {
        topics.forEach { topic ->
            topic.sections.forEach { section ->
                section.blocks.forEach { block ->
                    block.stringResources().forEach { res ->
                        assertTrue("${topic.key} has an unresolved resource", res != 0)
                    }
                }
            }
        }
    }

    /** How many things a block renders. Zero means it should not have been written. */
    private fun ReferenceBlock.itemCount(): Int = when (this) {
        is ReferenceBlock.Table -> rows.size
        is ReferenceBlock.Prose -> paragraphsRes.size
        is ReferenceBlock.Ordered -> stepsRes.size
        is ReferenceBlock.Comparison -> rows.size
        is ReferenceBlock.SymbolGrid -> symbols.size
        is ReferenceBlock.SymbolComparison -> pairs.size
        is ReferenceBlock.Callout -> 1
    }

    /** Every string id a block carries directly; table text is checked separately. */
    private fun ReferenceBlock.stringResources(): List<Int> = when (this) {
        is ReferenceBlock.Table -> emptyList()
        is ReferenceBlock.Prose -> paragraphsRes
        is ReferenceBlock.Ordered -> stepsRes
        is ReferenceBlock.Comparison -> columnsRes + rows.map { it.labelRes }
        is ReferenceBlock.SymbolGrid -> symbols.map { it.nameRes }
        is ReferenceBlock.SymbolComparison ->
            listOf(leftLabelRes, rightLabelRes) + pairs.map { it.nameRes }
        is ReferenceBlock.Callout -> listOf(textRes)
    }

    @Test
    fun `every topic cites a source`() {
        // Reference data is only as trustworthy as its provenance, so the
        // field is not optional and this makes sure it stays that way.
        topics.forEach { topic ->
            assertTrue("${topic.key} has no source", topic.sourceRes != 0)
        }
    }

    @Test
    fun `no reference text is blank`() {
        topics.forEach { topic ->
            topic.sections.forEach { section ->
                section.rows.forEachIndexed { index, row ->
                    listOfNotNull(row.label, row.value, row.note).forEach { text ->
                        when (text) {
                            is ReferenceText.Symbol -> assertTrue(
                                "${topic.key} row $index has a blank symbol",
                                text.text.isNotBlank(),
                            )

                            is ReferenceText.Localized -> assertTrue(
                                "${topic.key} row $index has an unresolved resource",
                                text.res != 0,
                            )

                            is ReferenceText.Quantity -> assertTrue(
                                "${topic.key} row $index has a non-finite quantity",
                                text.value.isFinite(),
                            )

                            is ReferenceText.Range -> assertTrue(
                                "${topic.key} row $index has a backwards range",
                                text.low < text.high,
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `every topic is filed under a category`() {
        // The index groups by category; a topic without one would silently
        // vanish from a section-driven list.
        val categorised = topics.groupBy { it.category }

        assertEquals(topics.size, categorised.values.sumOf { it.size })
        ReferenceCategory.entries.forEach { category ->
            assertTrue(
                "$category has no topics and would render an empty heading",
                categorised[category]?.isNotEmpty() == true,
            )
        }
    }

    // -- Coverage of the standards the topics claim -----------------------------------

    @Test
    fun `the IP table covers every digit both codes define`() {
        // 0 to 6 for solids, 0 to 9 for water. A missing row is a lookup that
        // silently fails for the user standing in front of the enclosure.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("ip_rating"))
        val solids = topic.sections[0].rows.map { (it.label as ReferenceText.Symbol).text }
        val water = topic.sections[1].rows.map { (it.label as ReferenceText.Symbol).text }

        assertEquals((0..6).map(Int::toString), solids)
        assertEquals((0..9).map(Int::toString), water)
    }

    @Test
    fun `the IK table covers IK00 to IK10 in order`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("ik_rating"))
        val codes = topic.sections.single().rows
            .map { (it.label as ReferenceText.Symbol).text }

        assertEquals((0..10).map { "IK%02d".format(it) }, codes)
    }

    @Test
    fun `the IK energies rise monotonically`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("ik_rating"))
        // IK00 is "not protected" rather than an energy, so it is skipped.
        val energies = topic.sections.single().rows
            .drop(1)
            .map { (it.value as ReferenceText.Quantity).value }

        assertEquals(energies.sorted(), energies)
        assertEquals(0.14, energies.first(), 1e-9)
        assertEquals(20.0, energies.last(), 1e-9)
    }

    @Test
    fun `the harmonised colour table names every conductor of a three-phase circuit`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("conductor_colours"))
        val labels = topic.sections[0].rows.map { (it.label as ReferenceText.Symbol).text }

        assertTrue(labels.containsAll(listOf("L1", "L2", "L3", "N", "PE")))
    }

    @Test
    fun `the superseded colour tables carry a warning`() {
        // These exist because the plant exists; presenting them without the
        // overlap warning would be worse than omitting them.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("conductor_colours"))
        val superseded = topic.sections.filter { it.titleRes != topic.sections[0].titleRes }

        superseded.drop(1).forEach { section ->
            assertNotNull("A superseded colour table has no footnote", section.footnoteRes)
        }
    }

    // -- Agreement with the rest of the app ----------------------------------------------

    @Test
    fun `the resistivity table is read from the model the calculators use`() {
        // Typing the constants a second time is how a reference screen ends up
        // contradicting the calculator on the next tab.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("materials"))
        val values = topic.sections[0].rows.map { (it.value as ReferenceText.Quantity).value }

        ConductorMaterial.entries.forEachIndexed { index, material ->
            assertEquals(
                "${material.name} resistivity is not the model's value",
                material.resistivityAt20C,
                values[index],
                0.0,
            )
        }
    }

    @Test
    fun `the density table is read from the same model`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("materials"))
        val values = topic.sections[1].rows.map { (it.value as ReferenceText.Quantity).value }

        ConductorMaterial.entries.forEachIndexed { index, material ->
            assertEquals(
                "${material.name} density is not the model's value",
                material.densityKgPerDm3,
                values[index],
                0.0,
            )
        }
    }

    @Test
    fun `the material table covers every material the app supports`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("materials"))

        topic.sections.take(2).forEach { section ->
            assertEquals(ConductorMaterial.entries.size, section.rows.size)
        }
    }

    // -- The protection topics -----------------------------------------------------------------

    @Test
    fun `the breaker curve bands rise from B through D`() {
        // A reader compares these three rows against each other; if they ever
        // stop ascending the table is telling a lie about the hardware.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("breaker_curves"))
        val bands = topic.sections.first().rows
            .map { (it.value as ReferenceText.Symbol).text }

        assertEquals(listOf("3 – 5 × In", "5 – 10 × In", "10 – 20 × In"), bands)
    }

    @Test
    fun `the breaker curve multipliers agree with the earth fault calculator`() {
        // The reference and the calculator must not disagree about what a
        // Type C does. The upper bound of each band is what the calculator uses.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("breaker_curves"))
        val upperBounds = topic.sections.first().rows
            .map { (it.value as ReferenceText.Symbol).text }
            .map { it.substringAfter("– ").removeSuffix(" × In").trim().toDouble() }

        assertEquals(
            listOf(
                ProtectiveDeviceType.MCB_TYPE_B.instantaneousMultiplier,
                ProtectiveDeviceType.MCB_TYPE_C.instantaneousMultiplier,
                ProtectiveDeviceType.MCB_TYPE_D.instantaneousMultiplier,
            ),
            upperBounds,
        )
    }

    @Test
    fun `the RCD table covers every type in ascending capability`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("rcd_types"))
        val types = topic.sections.first().rows
            .map { (it.label as ReferenceText.Symbol).text }

        assertEquals(listOf("AC", "A", "F", "B"), types)
    }

    @Test
    fun `the earthing table names all five arrangements`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("earthing_systems"))
        val systems = topic.sections[1].rows
            .map { (it.label as ReferenceText.Symbol).text }

        assertEquals(listOf("TN-S", "TN-C", "TN-C-S", "TT", "IT"), systems)
    }

    @Test
    fun `every protection topic carries at least one caveat`() {
        // These are the topics where a half-read table causes harm, so each
        // section that needs a warning has one.
        val protection = topics.filter { it.category == ReferenceCategory.PROTECTION_AND_EARTHING }

        protection.forEach { topic ->
            assertTrue(
                "${topic.key} has no footnote anywhere",
                topic.sections.any { it.footnoteRes != null },
            )
        }
    }

    // -- Commissioning and diagnosis ------------------------------------------------------------

    @Test
    fun `the commissioning sequence puts every dead test before the first live one`() {
        // The order is the safety-critical part of that topic. If an edit ever
        // moves "energise" up the list, the page starts telling a reader to
        // work on something nobody has proved is dead.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("commissioning_tests"))
        val steps = topic.sections.first().blocks
            .filterIsInstance<ReferenceBlock.Ordered>()
            .single()
            .stepsRes

        val energise = steps.indexOf(R.string.ref_commissioning_step_6)
        val deadTests = listOf(
            R.string.ref_commissioning_step_2,
            R.string.ref_commissioning_step_3,
            R.string.ref_commissioning_step_4,
            R.string.ref_commissioning_step_5,
        )
        val liveTests = listOf(
            R.string.ref_commissioning_step_7,
            R.string.ref_commissioning_step_8,
            R.string.ref_commissioning_step_9,
        )

        assertTrue("the sequence never energises", energise > 0)
        deadTests.forEach {
            assertTrue("a dead test follows energising", steps.indexOf(it) < energise)
        }
        liveTests.forEach {
            assertTrue("a live test precedes energising", steps.indexOf(it) > energise)
        }
    }

    @Test
    fun `the commissioning topic warns about insulation testing electronics`() {
        // Hundreds of volts DC into a drive is the most expensive mistake the
        // page can prevent, so the warning is not optional decoration.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("commissioning_tests"))
        val callouts = topic.sections.flatMap { it.blocks }
            .filterIsInstance<ReferenceBlock.Callout>()

        assertTrue(
            "no safety callout on the commissioning topic",
            callouts.any { it.kind == CalloutKind.SAFETY },
        )
    }

    @Test
    fun `every diagnosis guide offers more than one cause to eliminate`() {
        // A guide with a single cause is an assertion, not a diagnosis.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("fault_diagnosis"))

        assertTrue("fewer than four guides", topic.sections.size >= 4)
        topic.sections.forEach { section ->
            val causes = section.blocks
                .filterIsInstance<ReferenceBlock.Ordered>()
                .sumOf { it.stepsRes.size }
            assertTrue("a guide lists $causes causes", causes >= 4)
        }
    }

    @Test
    fun `a symbol carries no prose and no locale-specific decimal point`() {
        // Symbol means "reads the same in every language". Two ways that gets
        // broken, both found on a device rather than by a test:
        //
        //  - English words smuggled in beside a figure — "≤ 2 % typical limit",
        //    "3rd, 5th" — which then never reach a translator.
        //  - A decimal point, which a Turkish reader writes as a comma. Numbers
        //    belong in Quantity, which formats for the reader.
        // One long word is a designation — NYAF, Dyn11, ONAN, Zone. Two or more
        // in a phrase is prose that never reached a translator, which is the
        // thing worth catching and needs no allow-list to recognise.
        val words = Regex("""\b[A-Za-z]{4,}\b""")
        val decimalPoint = Regex("""\d\.\d""")

        topics.flatMap { it.sections }.flatMap { it.rows }.forEach { row ->
            listOfNotNull(row.label, row.value, row.note)
                .filterIsInstance<ReferenceText.Symbol>()
                .forEach { symbol ->
                    val found = words.findAll(symbol.text).map { it.value }.toList()
                    if (found.size >= 2) {
                        fail("\"${symbol.text}\" reads as prose inside a Symbol: $found")
                    }
                    assertFalse(
                        "\"${symbol.text}\" has a decimal point; use Quantity",
                        decimalPoint.containsMatchIn(symbol.text),
                    )
                }
        }
    }

    @Test
    fun `no topic repeats a section title`() {
        // The detail screen keys its list by position, so a repeat no longer
        // crashes — but two identical headings on one page is still the
        // signature of a block that was filed as its own section by mistake.
        topics.forEach { topic ->
            val titles = topic.sections.map { it.titleRes }
            assertEquals("${topic.key} repeats a section heading", titles.size, titles.distinct().size)
        }
    }

    // -- Selection guides ------------------------------------------------------------------------

    @Test
    fun `every comparison row has one cell per column`() {
        // The invariant the model promises. A ragged row would silently drop a
        // cell or file one under the wrong heading, which on a page whose whole
        // purpose is comparison is worse than no page.
        topics.flatMap { it.sections }
            .flatMap { it.blocks }
            .filterIsInstance<ReferenceBlock.Comparison>()
            .forEach { comparison ->
                assertTrue("a comparison has no columns", comparison.columnsRes.isNotEmpty())
                assertTrue("a comparison has no rows", comparison.rows.isNotEmpty())
                comparison.rows.forEach { row ->
                    assertEquals(
                        "a comparison row has the wrong number of cells",
                        comparison.columnsRes.size,
                        row.cells.size,
                    )
                    assertTrue("a comparison row has no label", row.labelRes != 0)
                }
            }
    }

    @Test
    fun `every selection guide compares at least two options`() {
        // One column is not a comparison, it is a list.
        val guides = topics.filter { it.category == ReferenceCategory.SELECTION_GUIDES }

        assertTrue("no selection guides", guides.isNotEmpty())
        guides.forEach { topic ->
            val comparison = topic.sections.flatMap { it.blocks }
                .filterIsInstance<ReferenceBlock.Comparison>()
                .singleOrNull()

            assertNotNull("${topic.key} has no comparison", comparison)
            assertTrue(
                "${topic.key} compares fewer than two options",
                requireNotNull(comparison).columnsRes.size >= 2,
            )
        }
    }

    @Test
    fun `every selection guide explains itself before tabulating`() {
        // A bare table says what the options are and not which to pick. The
        // prose ahead of it is where the judgement lives.
        topics.filter { it.category == ReferenceCategory.SELECTION_GUIDES }.forEach { topic ->
            val firstBlock = topic.sections.first().blocks.first()
            assertTrue(
                "${topic.key} opens with a table rather than an explanation",
                firstBlock is ReferenceBlock.Prose,
            )
        }
    }

    // -- The one table that is not from a standard -------------------------------------------

    @Test
    fun `the power factor table declares that it is indicative`() {
        // Every other topic cites a standard. This one must not be mistaken
        // for one, so it carries a footnote saying so.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("power_factors"))

        assertNotNull(topic.sections.single().footnoteRes)
    }

    @Test
    fun `no power factor is quoted above unity`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("power_factors"))

        topic.sections.single().rows.forEach { row ->
            val highest = when (val value = row.value) {
                is ReferenceText.Quantity -> value.value
                is ReferenceText.Range -> value.high
                else -> error("A power factor is neither a quantity nor a range")
            }
            assertTrue("$highest exceeds unity", highest <= 1.0)
            assertTrue("$highest is not positive", highest > 0.0)
        }
    }

    @Test
    fun `power factor ranges are written low to high`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("power_factors"))

        topic.sections.single().rows
            .mapNotNull { it.value as? ReferenceText.Range }
            .forEach { range ->
                assertTrue("$range is written backwards", range.low < range.high)
            }
    }

    @Test
    fun `the standard voltage table is not empty of frequencies`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("standard_voltages"))
        val lowVoltage = topic.sections.first()

        assertFalse(lowVoltage.rows.any { it.note == null })
    }
}
