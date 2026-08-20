package com.kemalurekli.electricalcalculator.features.glossary

import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test

/**
 * What can and cannot be tested here.
 *
 * No test can say whether a definition is *right* — that is a review job, and
 * the definitions are deliberately our own words rather than IEC 60050's, which
 * makes review the only check available. What a test can do is guarantee the
 * structure: that no entry is half-written, that every cross-reference resolves,
 * and that a term claiming a calculator or a table is pointing at one that
 * exists. Those are exactly the failures that would ship silently.
 */
class GlossaryCatalogTest {

    private val terms = GlossaryCatalog.all

    // -- Structure ---------------------------------------------------------------

    @Test
    fun `the glossary is not empty`() {
        assertTrue(terms.size > 100, "the catalog has no terms")
    }

    @Test
    fun `keys are unique`() {
        val keys = terms.map { it.key }

        assertEquals(keys.size, keys.distinct().size)
    }

    @Test
    fun `every term is reachable by its key`() {
        terms.forEach { term ->
            assertEquals(term, GlossaryCatalog.termOrNull(term.key))
        }
    }

    @Test
    fun `an unknown key resolves to nothing rather than throwing`() {
        // A stale cross-reference or deep link renders as absent; it must not
        // be handed an exception instead.
        assertNull(GlossaryCatalog.termOrNull("no_such_term"))
    }

    @Test
    fun `every term has a name and a definition`() {
        terms.forEach { term ->
            // A stronger claim than the old `!= 0`, and one the type does not
            // already make: the generator derives the catalog entry and the
            // string name from the same key, so a drift between the two shows
            // up here rather than as a term with no text on the screen.
            assertEquals("gl_${term.key}_term", term.term.key)
            assertEquals("gl_${term.key}_def", term.definition.key)
            assertTrue(term.englishTerm.isNotBlank(), "${term.key} has a blank English name")
        }
    }

    @Test
    fun `keys are lower snake case`() {
        // They appear in saved state and will appear in deep links, so the
        // shape is fixed rather than incidental.
        val shape = Regex("^[a-z][a-z0-9_]*$")

        terms.forEach { term ->
            assertTrue(shape.matches(term.key), "${term.key} is not a well-formed key")
        }
    }

    // -- Cross-references ----------------------------------------------------------

    @Test
    fun `every see-also points at a term that exists`() {
        // A dangling link renders as nothing at all, so the reader is told the
        // related term does not exist. The generator asserts this too; this is
        // the check that survives someone editing the Kotlin directly.
        terms.forEach { term ->
            term.seeAlso.forEach { key ->
                assertNotNull(GlossaryCatalog.termOrNull(key), "${term.key} refers to missing term $key")
            }
        }
    }

    @Test
    fun `no term refers to itself`() {
        terms.forEach { term ->
            assertTrue(term.key !in term.seeAlso, "${term.key} lists itself as a related term")
        }
    }

    @Test
    fun `see-also lists have no duplicates`() {
        terms.forEach { term ->
            assertEquals(term.seeAlso.size, term.seeAlso.distinct().size, "${term.key} repeats a related term")
        }
    }

    // -- Quantities ------------------------------------------------------------------

    @Test
    fun `a unit is only given to a term that has a symbol or is a quantity`() {
        // A unit with nothing to attach to renders as a bare "A" beside a name,
        // which reads as a mistake.
        terms.filter { it.unit != null }.forEach { term ->
            assertTrue(term.unit!!.isNotBlank(), "${term.key} has a blank unit")
        }
    }

    @Test
    fun `no symbol is blank`() {
        terms.mapNotNull { it.symbol }.forEach { symbol ->
            assertTrue(symbol.isNotBlank(), "a term has a blank symbol")
        }
    }

    @Test
    fun `the terms every other one leans on are present`() {
        // These are the words the rest of the glossary and the calculators
        // assume the reader can look up. Losing one in an edit would leave a
        // hole exactly where a newcomer starts.
        listOf(
            "ampacity",
            "voltage_drop",
            "earth_fault_loop_impedance",
            "short_circuit",
            "protective_conductor",
            "rcd",
            "cos_phi",
            "tn_system",
            "tt_system",
            "it_system",
        ).forEach { key ->
            assertNotNull(GlossaryCatalog.termOrNull(key), "$key is missing from the glossary")
        }
    }
}
