package com.kemalurekli.electricalcalculator.features.fieldnotes

import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.fieldnotes.domain.FieldNoteCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The field notes' links out into the rest of the app.
 *
 * Separated from `FieldNoteCatalogTest`, which moved to `:feature:fieldnotes`
 * with the catalogue: these are the assertions in it needing a catalogue that
 * still lives in `:app`. They go back beside the others when the references and
 * the calculators move — the same arrangement `GlossaryOutboundLinkTest` is in,
 * and for the same reason.
 */
class FieldNoteOutboundLinkTest {

    private val notes = FieldNoteCatalog.all

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
}
