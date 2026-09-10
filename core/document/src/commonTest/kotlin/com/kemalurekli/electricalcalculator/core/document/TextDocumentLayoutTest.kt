package com.kemalurekli.electricalcalculator.core.document

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The page arithmetic, against a font where every character is six points wide.
 *
 * A fake measurer rather than a real one: the assertions are about where the
 * notice sits and when a page breaks, and a real typeface's kerning would make
 * every one of them a range rather than a number.
 */
class TextDocumentLayoutTest {

    private val measurer = TextMeasurer { text, _ -> text.length * 6f }

    private fun pages(lines: Int, notice: String = "Doğrulanmadan kullanılamaz.") =
        TextDocumentLayout.pages(
            title = "Gerilim düşümü",
            subtitle = "VoltageBoard · 10.09.2026",
            body = List(lines) { DocumentBlock.Line("Satır $it") },
            notice = notice,
            measurer = measurer,
        )

    @Test
    fun `a short calculation is one page`() {
        assertEquals(1, pages(10).size)
    }

    @Test
    fun `a long one breaks`() {
        assertTrue(pages(200).size > 1)
    }

    @Test
    fun `every page carries the notice and not only the last`() {
        val drawn = pages(200)

        assertTrue(drawn.size > 1, "the fixture has to span pages for this to mean anything")
        drawn.forEachIndexed { index, page ->
            assertTrue(
                page.filterIsInstance<PdfOp.Text>().any { "Doğrulanmadan" in it.text },
                "page ${index + 1} has no notice",
            )
        }
    }

    @Test
    fun `no line is drawn into the notice`() {
        val page = pages(200).first()
        val text = page.filterIsInstance<PdfOp.Text>()
        val noticeTop = text.filter { "Doğrulanmadan" in it.text }.minOf { it.y }
        val lastLine = text.filter { it.style == PdfStyle.TEXT }.maxOf { it.y }

        assertTrue(lastLine < noticeTop, "a line at $lastLine overlaps the notice at $noticeTop")
    }

    @Test
    fun `the title is on the first page only`() {
        val drawn = pages(200)
        val titles = drawn.map { page ->
            page.filterIsInstance<PdfOp.Text>().count { it.style == PdfStyle.TITLE }
        }

        assertEquals(1, titles.first())
        // A title on a continuation sheet reads as a second document starting.
        assertTrue(titles.drop(1).all { it == 0 })
    }

    @Test
    fun `a divider is a rule rather than three dashes`() {
        val page = TextDocumentLayout.pages(
            title = "T",
            subtitle = "S",
            body = listOf(DocumentBlock.Line("a"), DocumentBlock.Divider, DocumentBlock.Line("b")),
            notice = "n",
            measurer = measurer,
        ).single()

        assertTrue(page.filterIsInstance<PdfOp.Text>().none { "—" in it.text })
        // Two: the divider and the rule above the notice.
        assertEquals(2, page.filterIsInstance<PdfOp.Rule>().size)
    }

    @Test
    fun `a long line wraps rather than running off the page`() {
        val page = TextDocumentLayout.pages(
            title = "T",
            subtitle = "S",
            body = listOf(DocumentBlock.Line("kelime ".repeat(60).trim())),
            notice = "n",
            measurer = measurer,
        ).first()
        val body = page.filterIsInstance<PdfOp.Text>().filter { it.style == PdfStyle.TEXT }

        assertTrue(body.size > 1, "a line of 420 points had to break somewhere")
        assertTrue(body.all { measurer.widthOf(it.text, PdfStyle.TEXT) <= TextDocumentLayout.PAGE.width })
    }
}
