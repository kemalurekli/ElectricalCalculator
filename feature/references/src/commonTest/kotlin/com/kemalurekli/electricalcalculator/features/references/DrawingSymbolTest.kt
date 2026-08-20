package com.kemalurekli.electricalcalculator.features.references

import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import com.kemalurekli.electricalcalculator.features.references.domain.DrawingSymbol
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceBlock
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

/**
 * The drawing symbols.
 *
 * No test can say a symbol looks right — that is an eye's job, done on a device
 * in both themes. What a test can say is that every symbol was actually drawn:
 * a builder that produced no path nodes, or a `symbol { }` left empty during an
 * edit, renders as a blank tile that looks like a loading state and ships
 * unnoticed. It can also hold the two things a reader needs beside the picture,
 * which are the name and the designation letter.
 */
class DrawingSymbolTest {

    private val symbols: List<DrawingSymbol> = ReferenceCatalog.all
        .flatMap { it.sections }
        .flatMap { it.blocks }
        .filterIsInstance<ReferenceBlock.SymbolGrid>()
        .flatMap { it.symbols }

    /** Every path node in a vector, however deeply grouped. */
    private fun VectorGroup.paths(): List<VectorPath> = buildList {
        this@paths.forEach { node: VectorNode ->
            when (node) {
                is VectorPath -> add(node)
                is VectorGroup -> addAll(node.paths())
            }
        }
    }

    /** Both halves of every IEC/ANSI pair. */
    private val pairedImages = ReferenceCatalog.all
        .flatMap { it.sections }
        .flatMap { it.blocks }
        .filterIsInstance<ReferenceBlock.SymbolComparison>()
        .flatMap { block -> block.pairs.flatMap { listOf(it.key to it.left, it.key to it.right) } }

    @Test
    fun `every compared symbol was drawn — on the same grid as its counterpart`() {
        // A pair only works if the two sit level. One drawn on a different
        // viewport would be scaled against the other and the comparison would
        // be showing a size difference that is not there.
        assertTrue(pairedImages.isNotEmpty(), "no symbol comparisons")
        pairedImages.forEach { (key, image) ->
            assertTrue(image.root.paths().isNotEmpty(), "$key has an empty drawing")
            assertEquals(48f, image.viewportWidth, "$key viewport width")
            assertEquals(48f, image.viewportHeight, "$key viewport height")
        }
    }

    @Test
    fun `no IEC and ANSI pair is the same drawing`() {
        // A row where both sides are identical is padding, and the topic says
        // in its own words that only the ones that differ are listed.
        ReferenceCatalog.all
            .flatMap { it.sections }
            .flatMap { it.blocks }
            .filterIsInstance<ReferenceBlock.SymbolComparison>()
            .flatMap { it.pairs }
            .forEach { pair ->
                assertTrue(pair.left !== pair.right, "${pair.key} shows the same drawing on both sides")
            }
    }

    @Test
    fun `there are symbols to look at`() {
        assertTrue(symbols.size >= 50, "no symbols in the catalog")
    }

    @Test
    fun `every symbol was actually drawn`() {
        // The failure this exists for: an empty builder renders a blank tile
        // that reads as a loading state rather than as a bug.
        symbols.forEach { symbol ->
            val paths = symbol.image.root.paths()
            assertTrue(paths.isNotEmpty(), "${symbol.key} has no paths")
            assertTrue(paths.all { it.pathData.isNotEmpty() }, "${symbol.key} has a path with no nodes")
        }
    }

    @Test
    fun `every symbol is drawn on the shared grid`() {
        // A symbol on a different viewport would be silently scaled against its
        // neighbours and stop looking like one family.
        symbols.forEach { symbol ->
            assertEquals(48f, symbol.image.viewportWidth, "${symbol.key} viewport width")
            assertEquals(48f, symbol.image.viewportHeight, "${symbol.key} viewport height")
        }
    }

    @Test
    fun `every symbol has a name`() {
        symbols.forEach { symbol ->
            assertTrue(symbol.name.key.isNotEmpty(), "${symbol.key} has no name")
        }
    }

    @Test
    fun `keys are unique across every symbol topic`() {
        val keys = symbols.map { it.key }

        assertEquals(keys.size, keys.distinct().size, "a symbol key is used twice")
    }

    @Test
    fun `designations are single IEC 81346 letters`() {
        // A lower-case letter or a word here would be a different scheme, and
        // the reader would be matching it against a drawing that uses this one.
        val valid = Regex("^[A-Z]$")

        symbols.mapNotNull { it.designation }.forEach { letter ->
            assertTrue(valid.matches(letter), "\"$letter\" is not an IEC 81346 designation")
        }
    }

    @Test
    fun `the symbols that get confused all carry a distinguishing note`() {
        // The reason this section exists rather than being a picture chart. If
        // an edit ever drops one of these notes, the page becomes a gallery.
        listOf(
            "disconnector",
            "switch_disconnector",
            "circuit_breaker",
            "contactor",
            "rcd",
            "rcbo",
            "current_transformer",
            "voltage_transformer",
            "contact_nc",
        ).forEach { key ->
            val symbol = symbols.single { it.key == key }
            assertTrue(symbol.note != null, "$key has no distinguishing note")
        }
    }
}
