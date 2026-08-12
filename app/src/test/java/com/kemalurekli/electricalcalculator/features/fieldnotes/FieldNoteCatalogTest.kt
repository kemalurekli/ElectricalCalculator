package com.kemalurekli.electricalcalculator.features.fieldnotes

import com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.fieldnotes.domain.FieldNoteCatalog
import com.kemalurekli.electricalcalculator.features.fieldnotes.domain.FieldNoteCategory
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Structural checks over the field notes.
 *
 * No test can tell whether a note is *true* — that is what the
 * `field-note-claims` row of `docs/verification-backlog.md` exists for. What is
 * checkable is that every note is reachable, that no cross-reference points at
 * something that has been renamed out from under it, and that the shelf keeps
 * its distance from the three it sits beside.
 */
class FieldNoteCatalogTest {

    private val notes = FieldNoteCatalog.all

    @Test
    fun `the catalog is not empty`() {
        // A parser or a generator that emitted nothing would make every other
        // assertion here vacuously true.
        assertTrue("No field notes in the catalog", notes.isNotEmpty())
    }

    @Test
    fun `keys are unique`() {
        val keys = notes.map { it.key }
        assertEquals("Duplicate field note key", keys.size, keys.distinct().size)
    }

    @Test
    fun `keys are lower snake case`() {
        val pattern = Regex("""^[a-z0-9]+(_[a-z0-9]+)*$""")
        notes.forEach { note ->
            assertTrue("Key is not lower_snake_case: ${note.key}", pattern.matches(note.key))
        }
    }

    @Test
    fun `every note is reachable by its key`() {
        notes.forEach { note ->
            assertEquals(note, FieldNoteCatalog.noteOrNull(note.key))
        }
    }

    @Test
    fun `an unknown key resolves to nothing rather than throwing`() {
        // A stale deep link or a renamed cross-reference reaches this, and it
        // has to land softly.
        assertNull(FieldNoteCatalog.noteOrNull("no_such_note"))
    }

    @Test
    fun `every note has a title and a body`() {
        notes.forEach { note ->
            assertTrue("${note.key} has no title resource", note.titleRes != 0)
            assertTrue("${note.key} has no body resource", note.bodyRes != 0)
        }
    }

    @Test
    fun `no two notes share a title or a body resource`() {
        // Catches the copy-paste that leaves a new note showing an old one's
        // text — which no other check here would notice.
        val titles = notes.map { it.titleRes }
        val bodies = notes.map { it.bodyRes }
        assertEquals("Two notes share a title resource", titles.size, titles.distinct().size)
        assertEquals("Two notes share a body resource", bodies.size, bodies.distinct().size)
    }

    // -- The cross-links are what keep this from being a fourth silo ----------

    @Test
    fun `every glossary term a note leans on exists`() {
        notes.forEach { note ->
            note.glossaryTerms.forEach { key ->
                assertTrue(
                    "${note.key} points at glossary term \"$key\", which does not exist",
                    GlossaryCatalog.termOrNull(key) != null,
                )
            }
        }
    }

    @Test
    fun `term lists have no duplicates`() {
        notes.forEach { note ->
            assertEquals(
                "${note.key} lists the same term twice",
                note.glossaryTerms.size,
                note.glossaryTerms.distinct().size,
            )
        }
    }

    @Test
    fun `every referenced calculator is in the catalog`() {
        val known = CalculatorCatalog().all.map { it.id }.toSet()
        notes.mapNotNull { note -> note.calculator?.let { note.key to it } }
            .forEach { (key, id) ->
                assertTrue("$key points at calculator $id, which is not in the catalog", id in known)
            }
    }

    @Test
    fun `every referenced reference topic exists`() {
        val known = ReferenceCatalog.all.map { it.key }.toSet()
        notes.mapNotNull { note -> note.referenceTopic?.let { note.key to it } }
            .forEach { (key, topic) ->
                assertTrue("$key points at reference topic \"$topic\", which does not exist", topic in known)
            }
    }

    @Test
    fun `every note carries at least one way out of the shelf`() {
        // The rule this shelf is built on is that a note explains and the other
        // three shelves define, tabulate and compute. A note with no link is a
        // note that has stopped pointing anywhere, which is the first step to
        // it restating what the glossary already says.
        notes.forEach { note ->
            val hasLink = note.glossaryTerms.isNotEmpty() ||
                note.calculator != null ||
                note.referenceTopic != null
            assertTrue("${note.key} links to nothing", hasLink)
        }
    }

    // -- Categories ----------------------------------------------------------

    @Test
    fun `every declared category carries at least one note`() {
        // A category with no notes is a filter chip that leads to an empty list.
        // Categories are added with their content, not ahead of it.
        FieldNoteCategory.entries.forEach { category ->
            assertTrue(
                "Category $category is declared but has no notes",
                FieldNoteCatalog.inCategory(category).isNotEmpty(),
            )
        }
    }

    @Test
    fun `inCategory returns exactly the notes filed under it`() {
        FieldNoteCategory.entries.forEach { category ->
            assertEquals(
                notes.filter { it.category == category },
                FieldNoteCatalog.inCategory(category),
            )
        }
    }
}
