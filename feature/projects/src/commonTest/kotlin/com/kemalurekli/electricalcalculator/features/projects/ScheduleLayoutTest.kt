package com.kemalurekli.electricalcalculator.features.projects

import com.kemalurekli.electricalcalculator.features.design.domain.ReportField
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport
import com.kemalurekli.electricalcalculator.core.document.PdfOp
import com.kemalurekli.electricalcalculator.core.document.PdfStyle
import com.kemalurekli.electricalcalculator.features.projects.presentation.ScheduleLayout
import com.kemalurekli.electricalcalculator.core.document.TextMeasurer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The arithmetic behind the schedule PDF.
 *
 * It is worth a test precisely because it used to be inside the drawing code,
 * where nothing could reach it: a page break that fired one row early would
 * have shown up only as a wasted line on a printout nobody re-reads.
 *
 * The measurer is fixed rather than real. What is being checked is where things
 * go for a given set of widths, not what a font does — and a fixed one makes the
 * ellipsis cut assertable, which a real font's kerning would not.
 */
class ScheduleLayoutTest {

    /** Every character six points wide, so widths are countable. */
    private val measurer = TextMeasurer { text, _ -> text.length * 6f }

    private fun report(rows: Int) = ScheduleReport(
        title = "Blok A",
        supply = listOf(ReportField("Şebeke", "400 V"), ReportField("Malzeme", "Bakır")),
        columns = listOf("Devre", "Yük", "Kesit"),
        rows = List(rows) { index -> listOf("C$index", "16 A", "2,5 mm²") },
        notice = "Bu değerler doğrulanmadan kullanılamaz.",
    )

    @Test
    fun `no row is drawn into the notice`() {
        // The reason the notice is measured before the table is placed. Added
        // afterwards it would have been drawn over the last row of a full page,
        // and a full page is the common case for a real installation.
        val pages = ScheduleLayout.pages(report(200), measurer)
        val full = pages.first()
        val text = full.filterIsInstance<PdfOp.Text>()
        val noticeTop = text.filter { "doğrulanmadan" in it.text }.minOf { it.y }
        val lastRow = text.filter { it.style == PdfStyle.BODY }.maxOf { it.y }

        assertTrue(
            lastRow < noticeTop,
            "a row at ${'$'}lastRow overlaps the notice starting at ${'$'}noticeTop",
        )
    }

    @Test
    fun `every page carries the notice and not only the last`() {
        // Schedules get separated. A continuation sheet handed over on its own
        // would otherwise be fourteen columns of figures and nothing saying
        // what they are.
        val pages = ScheduleLayout.pages(report(200), measurer)

        assertTrue(pages.size > 1, "the fixture has to span pages for this to mean anything")
        pages.forEachIndexed { index, page ->
            assertTrue(
                page.filterIsInstance<PdfOp.Text>().any { "doğrulanmadan" in it.text },
                "page ${'$'}{index + 1} has no notice",
            )
        }
    }

    @Test
    fun `a short schedule is one page`() {
        assertEquals(1, ScheduleLayout.pages(report(5), measurer).size)
    }

    @Test
    fun `rows past the bottom margin start a new page`() {
        // A4 landscape leaves room for roughly thirty rows under the header;
        // two hundred cannot fit on one page whatever the exact figure is.
        assertTrue(ScheduleLayout.pages(report(200), measurer).size > 1)
    }

    @Test
    fun `every page after the first repeats the column header`() {
        val pages = ScheduleLayout.pages(report(200), measurer)

        pages.drop(1).forEach { page ->
            val headers = page.filterIsInstance<PdfOp.Text>().takeWhile { it.style == PdfStyle.HEADER }
            assertEquals(listOf("Devre", "Yük", "Kesit"), headers.map { it.text })
            assertTrue(page.filterIsInstance<PdfOp.Rule>().isNotEmpty())
        }
    }

    @Test
    fun `only the first page carries the title and the supply block`() {
        val pages = ScheduleLayout.pages(report(200), measurer)

        assertTrue(pages.first().filterIsInstance<PdfOp.Text>().any { it.style == PdfStyle.TITLE })
        pages.drop(1).forEach { page ->
            assertTrue(page.filterIsInstance<PdfOp.Text>().none { it.style == PdfStyle.TITLE })
        }
    }

    @Test
    fun `a cell too wide for its column is cut and ellipsised`() {
        val wide = report(1).copy(rows = listOf(listOf("C".repeat(400), "16 A", "2,5 mm²")))

        // The first body cell of the only row — the last one is "2,5 mm²",
        // which fits and taught this test to pass for the wrong reason once.
        val drawn = ScheduleLayout.pages(wide, measurer)
            .first()
            .filterIsInstance<PdfOp.Text>()
            .last { it.style == PdfStyle.HEADER }
            .let { header ->
                ScheduleLayout.pages(wide, measurer)
                    .first()
                    .filterIsInstance<PdfOp.Text>()
                    .first { it.style == PdfStyle.BODY && it.y > header.y }
            }

        // Not asserted at a fixed length: the column width comes from the page
        // and the weights, and pinning the number here would make this a test
        // of the margins.
        assertTrue(drawn.text.endsWith("…"))
        assertTrue(drawn.text.length < 400)
    }

    @Test
    fun `nothing is drawn outside the page`() {
        ScheduleLayout.pages(report(200), measurer).forEach { page ->
            page.filterIsInstance<PdfOp.Text>().forEach { text ->
                assertTrue(text.x >= 0f && text.x < ScheduleLayout.PAGE.width, text.text)
                assertTrue(text.y >= 0f && text.y <= ScheduleLayout.PAGE.height, text.text)
            }
        }
    }
}
