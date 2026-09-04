package com.kemalurekli.electricalcalculator.features.references

import com.kemalurekli.electricalcalculator.feature.references.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_commissioning_step_2
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_commissioning_step_3
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_commissioning_step_4
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_commissioning_step_5
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_commissioning_step_6
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_commissioning_step_7
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_commissioning_step_8
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_commissioning_step_9
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.references.domain.CalloutKind
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceBlock
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCategory
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceText
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.test.Test
import org.jetbrains.compose.resources.StringResource

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
            assertTrue(topic.sections.isNotEmpty(), "${topic.key} has no sections")
            topic.sections.forEach { section ->
                assertTrue(section.blocks.isNotEmpty(), "${topic.key} has a section with no blocks")
                section.blocks.forEach { block ->
                    assertTrue(block.itemCount() > 0, "${topic.key} has an empty block")
                }
            }
        }
    }

    @Test
    fun `no prose — procedure or callout has an unresolved string`() {
        topics.forEach { topic ->
            topic.sections.forEach { section ->
                section.blocks.forEach { block ->
                    block.stringResources().forEach { res ->
                        assertTrue(res.key.isNotEmpty(), "${topic.key} has an unresolved resource")
                    }
                }
            }
        }
    }

    /** How many things a block renders. Zero means it should not have been written. */
    private fun ReferenceBlock.itemCount(): Int = when (this) {
        is ReferenceBlock.Table -> rows.size
        is ReferenceBlock.Prose -> paragraphs.size
        is ReferenceBlock.Ordered -> steps.size
        is ReferenceBlock.Comparison -> rows.size
        is ReferenceBlock.SymbolGrid -> symbols.size
        is ReferenceBlock.SymbolComparison -> pairs.size
        is ReferenceBlock.Callout -> 1
    }

    /** Every string id a block carries directly; table text is checked separately. */
    private fun ReferenceBlock.stringResources(): List<StringResource> = when (this) {
        is ReferenceBlock.Table -> emptyList()
        is ReferenceBlock.Prose -> paragraphs
        is ReferenceBlock.Ordered -> steps
        is ReferenceBlock.Comparison -> columns + rows.map { it.label }
        is ReferenceBlock.SymbolGrid -> symbols.map { it.name }
        is ReferenceBlock.SymbolComparison ->
            listOf(leftLabel, rightLabel) + pairs.map { it.name }
        is ReferenceBlock.Callout -> listOf(text)
    }

    @Test
    fun `every topic cites a source`() {
        // Reference data is only as trustworthy as its provenance, so the
        // field is not optional and this makes sure it stays that way.
        topics.forEach { topic ->
            assertTrue(topic.source.key.isNotEmpty(), "${topic.key} has no source")
        }
    }

    @Test
    fun `no reference text is blank`() {
        topics.forEach { topic ->
            topic.sections.forEach { section ->
                section.rows.forEachIndexed { index, row ->
                    listOfNotNull(row.label, row.value, row.note).forEach { text ->
                        when (text) {
                            is ReferenceText.Symbol -> assertTrue(text.text.isNotBlank(), "${topic.key} row $index has a blank symbol")

                            is ReferenceText.Localized -> assertTrue(text.res.key.isNotEmpty(), "${topic.key} row $index has an unresolved resource")

                            is ReferenceText.Quantity -> assertTrue(text.value.isFinite(), "${topic.key} row $index has a non-finite quantity")

                            is ReferenceText.Range -> assertTrue(text.low < text.high, "${topic.key} row $index has a backwards range")
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
            assertTrue(categorised[category]?.isNotEmpty() == true, "$category has no topics and would render an empty heading")
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
    fun `the IK table covers IK00 to IK11 in order`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("ik_rating"))
        val codes = topic.sections.single().rows
            .map { (it.label as ReferenceText.Symbol).text }

        assertEquals((0..11).map { "IK" + it.toString().padStart(2, '0') }, codes)
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
        assertEquals(50.0, energies.last(), 1e-9)
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
        val superseded = topic.sections.filter { it.title != topic.sections[0].title }

        superseded.drop(1).forEach { section ->
            assertNotNull(section.footnote, "A superseded colour table has no footnote")
        }
    }

    @Test
    fun `each IK row's hammer delivers the energy the row claims`() {
        // Checked 2026-09-04 against IEC 62262: the ten energies are right, and
        // so is each mass-and-height pair beside them. Published tables differ
        // on which pair to quote — 0,2 kg from 250 mm and 0,25 kg from 200 mm
        // both make an IK04 — so the check that means anything is not which
        // numbers appear but whether they multiply out.
        //
        // m · g · h against the stated joules catches a transposed digit in the
        // drop height, which is the easiest error to make here and the hardest
        // to see: every number stays plausible and the column stays ordered.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("ik_rating"))
        val rows = topic.sections[0].rows
            .filter { it.value is ReferenceText.Quantity }

        assertEquals(11, rows.size, "IK01 to IK11 should each have an energy")
        rows.forEach { row ->
            val joules = (row.value as ReferenceText.Quantity).value
            // IK11 carries an energy and no hammer: the 2021 amendment's test
            // parameters are not published anywhere this project can reach, and
            // a plausible-looking pair would be worse than an empty column.
            val note = row.note as? ReferenceText.Quantity ?: return@forEach
            val kilograms = note.value
            val millimetres = note.unit.substringAfter("· ").removeSuffix(" mm").toInt()
            val delivered = kilograms * 9.81 * millimetres / 1000.0

            assertTrue(
                delivered > joules * 0.95 && delivered < joules * 1.05,
                "${(row.label as ReferenceText.Symbol).text}: ${kilograms} kg from $millimetres mm " +
                    "delivers ${delivered} J, not the $joules J the row states",
            )
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
            assertEquals(material.resistivityAt20C, values[index], "${material.name} resistivity is not the model's value")
        }
    }

    @Test
    fun `the density table is read from the same model`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("materials"))
        val values = topic.sections[1].rows.map { (it.value as ReferenceText.Quantity).value }

        ConductorMaterial.entries.forEachIndexed { index, material ->
            assertEquals(material.densityKgPerDm3, values[index], "${material.name} density is not the model's value")
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
            assertTrue(topic.sections.any { it.footnote != null }, "${topic.key} has no footnote anywhere")
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
            .steps

        val energise = steps.indexOf(Res.string.ref_commissioning_step_6)
        val deadTests = listOf(
            Res.string.ref_commissioning_step_2,
            Res.string.ref_commissioning_step_3,
            Res.string.ref_commissioning_step_4,
            Res.string.ref_commissioning_step_5,
        )
        val liveTests = listOf(
            Res.string.ref_commissioning_step_7,
            Res.string.ref_commissioning_step_8,
            Res.string.ref_commissioning_step_9,
        )

        assertTrue(energise > 0, "the sequence never energises")
        deadTests.forEach {
            assertTrue(steps.indexOf(it) < energise, "a dead test follows energising")
        }
        liveTests.forEach {
            assertTrue(steps.indexOf(it) > energise, "a live test precedes energising")
        }
    }

    @Test
    fun `the commissioning topic warns about insulation testing electronics`() {
        // Hundreds of volts DC into a drive is the most expensive mistake the
        // page can prevent, so the warning is not optional decoration.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("commissioning_tests"))
        val callouts = topic.sections.flatMap { it.blocks }
            .filterIsInstance<ReferenceBlock.Callout>()

        assertTrue(callouts.any { it.kind == CalloutKind.SAFETY }, "no safety callout on the commissioning topic")
    }

    @Test
    fun `every diagnosis guide offers more than one cause to eliminate`() {
        // A guide with a single cause is an assertion, not a diagnosis.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("fault_diagnosis"))

        assertTrue(topic.sections.size >= 4, "fewer than four guides")
        topic.sections.forEach { section ->
            val causes = section.blocks
                .filterIsInstance<ReferenceBlock.Ordered>()
                .sumOf { it.steps.size }
            assertTrue(causes >= 4, "a guide lists $causes causes")
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
                    assertFalse(decimalPoint.containsMatchIn(symbol.text), "\"${symbol.text}\" has a decimal point; use Quantity")
                }
        }
    }

    @Test
    fun `no topic repeats a section title`() {
        // The detail screen keys its list by position, so a repeat no longer
        // crashes — but two identical headings on one page is still the
        // signature of a block that was filed as its own section by mistake.
        topics.forEach { topic ->
            val titles = topic.sections.map { it.title }
            assertEquals(titles.size, titles.distinct().size, "${topic.key} repeats a section heading")
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
                assertTrue(comparison.columns.isNotEmpty(), "a comparison has no columns")
                assertTrue(comparison.rows.isNotEmpty(), "a comparison has no rows")
                comparison.rows.forEach { row ->
                    assertEquals(comparison.columns.size, row.cells.size, "a comparison row has the wrong number of cells")
                    assertTrue(row.label.key.isNotEmpty(), "a comparison row has no label")
                }
            }
    }

    @Test
    fun `every selection guide compares at least two options`() {
        // One column is not a comparison, it is a list.
        val guides = topics.filter { it.category == ReferenceCategory.SELECTION_GUIDES }

        assertTrue(guides.isNotEmpty(), "no selection guides")
        guides.forEach { topic ->
            val comparison = topic.sections.flatMap { it.blocks }
                .filterIsInstance<ReferenceBlock.Comparison>()
                .singleOrNull()

            assertNotNull(comparison, "${topic.key} has no comparison")
            assertTrue(requireNotNull(comparison).columns.size >= 2, "${topic.key} compares fewer than two options")
        }
    }

    @Test
    fun `every selection guide explains itself before tabulating`() {
        // A bare table says what the options are and not which to pick. The
        // prose ahead of it is where the judgement lives.
        topics.filter { it.category == ReferenceCategory.SELECTION_GUIDES }.forEach { topic ->
            val firstBlock = topic.sections.first().blocks.first()
            assertTrue(firstBlock is ReferenceBlock.Prose, "${topic.key} opens with a table rather than an explanation")
        }
    }

    // -- The one table that is not from a standard -------------------------------------------

    @Test
    fun `the power factor table declares that it is indicative`() {
        // Every other topic cites a standard. This one must not be mistaken
        // for one, so it carries a footnote saying so.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("power_factors"))

        assertNotNull(topic.sections.single().footnote)
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
            assertTrue(highest <= 1.0, "$highest exceeds unity")
            assertTrue(highest > 0.0, "$highest is not positive")
        }
    }

    @Test
    fun `power factor ranges are written low to high`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("power_factors"))

        topic.sections.single().rows
            .mapNotNull { it.value as? ReferenceText.Range }
            .forEach { range ->
                assertTrue(range.low < range.high, "$range is written backwards")
            }
    }

    @Test
    fun `the standard voltage table is not empty of frequencies`() {
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("standard_voltages"))
        val lowVoltage = topic.sections.first()

        assertFalse(lowVoltage.rows.any { it.note == null })
    }
}
