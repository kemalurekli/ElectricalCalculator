package com.kemalurekli.electricalcalculator.features.references

import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * The glossary's links out into the rest of the app.
 *
 * Separated from `GlossaryCatalogTest`, which moved to `:feature:glossary` with
 * the catalogue: these two assertions are the ones in that file needing a
 * catalogue still living in `:app`. They go back to sit beside the others when
 * the references and the calculators move.
 *
 * A dangling link here is not cosmetic — it is a definition offering to take
 * the reader somewhere and then not doing it.
 */
class GlossaryOutboundLinkTest {

    @Test
    fun `every referenced calculator is in the catalog`() {
        val catalog = CalculatorCatalog()

        GlossaryCatalog.all.mapNotNull { it.calculator }.distinct().forEach { id ->
            assertNotNull("$id is not in the calculator catalog", catalog.findById(id))
        }
    }

    @Test
    fun `every referenced reference topic exists`() {
        GlossaryCatalog.all.forEach { term ->
            term.referenceTopic?.let { key ->
                assertNotNull(
                    "${term.key} points at missing reference topic $key",
                    ReferenceCatalog.topicOrNull(key),
                )
            }
        }
    }
}
