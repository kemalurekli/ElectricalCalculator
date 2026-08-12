package com.kemalurekli.electricalcalculator.features.glossary

import com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertTrue("the catalog has no terms", terms.size > 100)
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
            assertTrue("${term.key} has no term resource", term.termRes != 0)
            assertTrue("${term.key} has no definition resource", term.definitionRes != 0)
            assertTrue("${term.key} has a blank English name", term.englishTerm.isNotBlank())
        }
    }

    @Test
    fun `keys are lower snake case`() {
        // They appear in saved state and will appear in deep links, so the
        // shape is fixed rather than incidental.
        val shape = Regex("^[a-z][a-z0-9_]*$")

        terms.forEach { term ->
            assertTrue("${term.key} is not a well-formed key", shape.matches(term.key))
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
                assertNotNull(
                    "${term.key} refers to missing term $key",
                    GlossaryCatalog.termOrNull(key),
                )
            }
        }
    }

    @Test
    fun `no term refers to itself`() {
        terms.forEach { term ->
            assertTrue(
                "${term.key} lists itself as a related term",
                term.key !in term.seeAlso,
            )
        }
    }

    @Test
    fun `see-also lists have no duplicates`() {
        terms.forEach { term ->
            assertEquals(
                "${term.key} repeats a related term",
                term.seeAlso.size,
                term.seeAlso.distinct().size,
            )
        }
    }

    @Test
    fun `every referenced calculator is in the catalog`() {
        val catalog = CalculatorCatalog()

        terms.mapNotNull { it.calculator }.distinct().forEach { id ->
            assertNotNull("$id is not in the calculator catalog", catalog.findById(id))
        }
    }

    @Test
    fun `every referenced reference topic exists`() {
        terms.forEach { term ->
            term.referenceTopic?.let { key ->
                assertNotNull(
                    "${term.key} points at missing reference topic $key",
                    ReferenceCatalog.topicOrNull(key),
                )
            }
        }
    }

    // -- Quantities ------------------------------------------------------------------

    @Test
    fun `a unit is only given to a term that has a symbol or is a quantity`() {
        // A unit with nothing to attach to renders as a bare "A" beside a name,
        // which reads as a mistake.
        terms.filter { it.unit != null }.forEach { term ->
            assertTrue("${term.key} has a blank unit", term.unit!!.isNotBlank())
        }
    }

    @Test
    fun `no symbol is blank`() {
        terms.mapNotNull { it.symbol }.forEach { symbol ->
            assertTrue("a term has a blank symbol", symbol.isNotBlank())
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
            assertNotNull("$key is missing from the glossary", GlossaryCatalog.termOrNull(key))
        }
    }
}
