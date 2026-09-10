package com.kemalurekli.electricalcalculator.features.projects.presentation

import com.kemalurekli.electricalcalculator.core.document.PdfOp
import com.kemalurekli.electricalcalculator.core.document.PdfPageSize
import com.kemalurekli.electricalcalculator.core.document.PdfStyle
import com.kemalurekli.electricalcalculator.core.document.TextMeasurer
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport

/**
 * Where every piece of the schedule goes, without drawing any of it.
 *
 * The two platforms draw with different APIs — `android.graphics.pdf` and
 * `UIGraphicsPDFRenderer` — but the arithmetic between them is the same: column
 * widths from weights, a supply block in two columns, a page break when the
 * next row would cross the bottom margin, and a repeated header after it.
 *
 * That arithmetic is the part with decisions in it, so it is here once. What is
 * left per platform is a loop over [PdfOp] that puts text at coordinates, which
 * is short enough to read side by side and confirm the two agree.
 *
 * Measurement is the one thing the layout cannot do for itself: how wide a
 * string is depends on the font the platform will draw it with, and ellipsising
 * a cell needs that answer before the page exists.
 */
object ScheduleLayout {

    /**
     * Landscape, because a schedule is a wide table: a portrait page fits four
     * of its fourteen columns before the numbers stop being legible.
     */
    val PAGE = PdfPageSize.A4_LANDSCAPE

    private const val MARGIN = 36f
    private const val ROW_HEIGHT = 15f
    private const val SUPPLY_ROW_HEIGHT = 12f
    private const val COLUMN_GAP = 4f
    private const val LABEL_WIDTH = 130f
    private const val RULE_OFFSET = 4f
    private const val NOTICE_LINE_HEIGHT = 10f
    private const val ELLIPSIS = "…"

    /**
     * Hand-set rather than measured, and in the order the columns appear.
     *
     * Measuring the content would let one long cable reference widen a column
     * on one schedule and not the next, so the same schedule always lays out
     * the same way.
     */
    private val COLUMN_WEIGHTS =
        floatArrayOf(2f, 1f, 0.9f, 0.9f, 1.2f, 0.9f, 0.8f, 0.9f, 1f, 1.7f, 1f, 1f, 1.1f, 1.1f)

    /** One list of operations per page, in draw order. */
    fun pages(report: ScheduleReport, measurer: TextMeasurer): List<List<PdfOp>> {
        val widths = columnWidths(report.columns.size)
        // Measured before anything is placed, because it decides where the
        // table has to stop. A notice added after the fact would have been
        // drawn over the last row of a full page.
        val noticeLines = wrap(report.notice, PAGE.width - 2 * MARGIN, measurer)
        val floor = PAGE.height - MARGIN - noticeLines.size * NOTICE_LINE_HEIGHT - ROW_HEIGHT
        val pages = mutableListOf<MutableList<PdfOp>>()
        var page = mutableListOf<PdfOp>()
        var y = header(report, widths, page, measurer)

        report.rows.forEach { row ->
            if (y + ROW_HEIGHT > floor) {
                pages += page
                page = mutableListOf()
                // The header is repeated on every page. A continuation sheet of
                // bare numbers is unreadable on its own, and schedules get
                // separated.
                y = columnHeader(report.columns, widths, MARGIN, page, measurer)
            }
            var x = MARGIN
            row.forEachIndexed { index, cell ->
                // A row longer than the header is a bug upstream; drawing the
                // extra cells off the page would hide it, so they are not drawn.
                if (index >= widths.size) return@forEachIndexed
                page += PdfOp.Text(fit(cell, widths[index], PdfStyle.BODY, measurer), x, y, PdfStyle.BODY)
                x += widths[index] + COLUMN_GAP
            }
            y += ROW_HEIGHT
        }

        pages += page
        // On every page, not only the last. Schedules get separated, and a
        // continuation sheet handed over on its own would otherwise carry
        // fourteen columns of figures and nothing saying what they are.
        pages.forEach { drawNotice(it, noticeLines) }
        return pages
    }

    private fun drawNotice(page: MutableList<PdfOp>, lines: List<String>) {
        val top = PAGE.height - MARGIN - lines.size * NOTICE_LINE_HEIGHT
        page += PdfOp.Rule(MARGIN, PAGE.width - MARGIN, top - RULE_OFFSET)
        lines.forEachIndexed { index, line ->
            page += PdfOp.Text(line, MARGIN, top + index * NOTICE_LINE_HEIGHT, PdfStyle.LABEL)
        }
    }

    /**
     * Breaks [text] on spaces to fit [width].
     *
     * Word by word rather than by character count: the notice is a sentence in
     * twelve languages, and German compounds and Vietnamese diacritics make a
     * count of characters a poor guess at how wide any of them will draw.
     */
    private fun wrap(text: String, width: Float, measurer: TextMeasurer): List<String> {
        val lines = mutableListOf<String>()
        var line = StringBuilder()
        text.split(' ').forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (measurer.widthOf(candidate, PdfStyle.LABEL) <= width || line.isEmpty()) {
                line = StringBuilder(candidate)
            } else {
                lines += line.toString()
                line = StringBuilder(word)
            }
        }
        if (line.isNotEmpty()) lines += line.toString()
        return lines
    }

    private fun header(
        report: ScheduleReport,
        widths: FloatArray,
        page: MutableList<PdfOp>,
        measurer: TextMeasurer,
    ): Float {
        var y = MARGIN + PdfStyle.TITLE.size
        page += PdfOp.Text(report.title, MARGIN, y, PdfStyle.TITLE)
        y += PdfStyle.TITLE.size

        // Two columns of parameters, so nine fields do not push the table onto
        // a second page before a single circuit has been drawn.
        val half = (report.supply.size + 1) / 2
        val columnWidth = (PAGE.width - 2 * MARGIN) / 2f
        report.supply.forEachIndexed { index, field ->
            val column = if (index < half) 0 else 1
            val row = if (index < half) index else index - half
            val x = MARGIN + column * columnWidth
            val lineY = y + (row + 1) * SUPPLY_ROW_HEIGHT
            page += PdfOp.Text("${field.label}:", x, lineY, PdfStyle.LABEL)
            page += PdfOp.Text(field.value, x + LABEL_WIDTH, lineY, PdfStyle.BODY)
        }
        y += half * SUPPLY_ROW_HEIGHT + ROW_HEIGHT

        return columnHeader(report.columns, widths, y, page, measurer)
    }

    private fun columnHeader(
        columns: List<String>,
        widths: FloatArray,
        top: Float,
        page: MutableList<PdfOp>,
        measurer: TextMeasurer,
    ): Float {
        var x = MARGIN
        columns.forEachIndexed { index, column ->
            page += PdfOp.Text(fit(column, widths[index], PdfStyle.HEADER, measurer), x, top, PdfStyle.HEADER)
            x += widths[index] + COLUMN_GAP
        }
        val ruleY = top + RULE_OFFSET
        page += PdfOp.Rule(MARGIN, PAGE.width - MARGIN, ruleY)
        return ruleY + ROW_HEIGHT
    }

    private fun columnWidths(count: Int): FloatArray {
        val weights = if (count == COLUMN_WEIGHTS.size) COLUMN_WEIGHTS else FloatArray(count) { 1f }
        val available = PAGE.width - 2 * MARGIN - COLUMN_GAP * (count - 1)
        val total = weights.sum()
        return FloatArray(count) { available * weights[it] / total }
    }

    /**
     * Trims [text] to [width], ending in an ellipsis.
     *
     * Written out rather than using a platform truncator: `TextUtils.ellipsize`
     * is Android's and iOS truncates through a paragraph style, and the two
     * disagree about where to cut. One implementation means the same schedule
     * breaks in the same place on both.
     */
    private fun fit(text: String, width: Float, style: PdfStyle, measurer: TextMeasurer): String {
        if (measurer.widthOf(text, style) <= width) return text
        var end = text.length
        while (end > 0 && measurer.widthOf(text.take(end) + ELLIPSIS, style) > width) {
            end--
        }
        return if (end == 0) "" else text.take(end) + ELLIPSIS
    }
}
