package com.kemalurekli.electricalcalculator.features.theory

import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Where the theory topics point out of their own module.
 *
 * The fourth of these, and the pattern is now settled: a catalogue test moves
 * with its catalogue, and the assertions crossing into another module stay in
 * `:app`, which is the one place that sees them all. See
 * `GlossaryOutboundLinkTest`, `FieldNoteOutboundLinkTest` and
 * `ReferenceCalculatorAgreementTest`.
 */
class TheoryOutboundLinkTest {

    private val topics = TheoryCatalog.all

    fun `every referenced calculator is in the catalog`() {
        val known = CalculatorCatalog().all.map { it.id }.toSet()
        topics.mapNotNull { topic -> topic.calculator?.let { topic.key to it } }
            .forEach { (key, id) ->
                assertTrue("$key points at calculator $id, which is not in the catalog", id in known)
            }
    }

    fun `every referenced reference topic exists`() {
        val known = ReferenceCatalog.all.map { it.key }.toSet()
        topics.mapNotNull { topic -> topic.referenceTopic?.let { topic.key to it } }
            .forEach { (key, reference) ->
                assertTrue(
                    "$key points at reference topic \"$reference\", which does not exist",
                    reference in known,
                )
            }
    }
}
