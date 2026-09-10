package com.kemalurekli.electricalcalculator.core.document

/**
 * A page of a document somebody reads a line at a time.
 *
 * A [Divider] rather than a line of dashes: the app's exports separate their
 * sections with "— — —", which reads as a rule on a phone and as three stray
 * characters on paper.
 */
sealed interface DocumentBlock {
    data class Line(val text: String) : DocumentBlock
    data object Divider : DocumentBlock
}

/**
 * Lays out a titled document in portrait A4.
 *
 * The counterpart of a schedule's layout and deliberately much simpler: a
 * schedule is a table whose columns have to be apportioned, and this is a
 * column of sentences. What the two share is the part that matters on paper —
 * a notice at the foot of every page, measured before the body is placed so a
 * full page cannot be drawn over it.
 */
object TextDocumentLayout {

    val PAGE = PdfPageSize.A4_PORTRAIT

    private const val MARGIN = 56f
    private const val LINE_HEIGHT = 16f
    private const val NOTICE_LINE_HEIGHT = 10f
    private const val DIVIDER_SPACE = 10f
    private const val RULE_OFFSET = 4f
    private const val TITLE_GAP = 6f
    private const val SUBTITLE_GAP = 22f

    /**
     * @param subtitle what produced the document and when. Under the title
     *   rather than in a corner: on a page that may be filed loose, the date is
     *   part of what the reader is looking at, not decoration.
     */
    fun pages(
        title: String,
        subtitle: String,
        body: List<DocumentBlock>,
        notice: String,
        measurer: TextMeasurer,
    ): List<List<PdfOp>> {
        val width = PAGE.width - 2 * MARGIN
        val noticeLines = wrap(notice, width, PdfStyle.LABEL, measurer)
        val floor = PAGE.height - MARGIN - noticeLines.size * NOTICE_LINE_HEIGHT - LINE_HEIGHT

        val pages = mutableListOf<MutableList<PdfOp>>()
        var page = mutableListOf<PdfOp>()
        var y = heading(title, subtitle, page)

        body.forEach { block ->
            val lines = when (block) {
                is DocumentBlock.Line -> wrap(block.text, width, PdfStyle.TEXT, measurer)
                DocumentBlock.Divider -> emptyList()
            }
            val height = when (block) {
                is DocumentBlock.Line -> lines.size * LINE_HEIGHT
                DocumentBlock.Divider -> DIVIDER_SPACE * 2
            }
            if (y + height > floor) {
                pages += page
                page = mutableListOf()
                // No repeated title. A schedule's continuation sheet needs its
                // column headings to mean anything; a sentence carries its own
                // label, and a title on every page would read as a new document
                // starting.
                y = MARGIN + PdfStyle.TEXT.size
            }
            when (block) {
                is DocumentBlock.Line -> lines.forEach { line ->
                    page += PdfOp.Text(line, MARGIN, y, PdfStyle.TEXT)
                    y += LINE_HEIGHT
                }

                DocumentBlock.Divider -> {
                    y += DIVIDER_SPACE
                    page += PdfOp.Rule(MARGIN, PAGE.width - MARGIN, y)
                    y += DIVIDER_SPACE
                }
            }
        }

        pages += page
        pages.forEach { drawNotice(it, noticeLines) }
        return pages
    }

    private fun heading(title: String, subtitle: String, page: MutableList<PdfOp>): Float {
        var y = MARGIN + PdfStyle.TITLE.size
        page += PdfOp.Text(title, MARGIN, y, PdfStyle.TITLE)
        y += PdfStyle.SUBTITLE.size + TITLE_GAP
        page += PdfOp.Text(subtitle, MARGIN, y, PdfStyle.SUBTITLE)
        return y + SUBTITLE_GAP
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
     * Word by word rather than by character count: this runs on twelve
     * languages, and German compounds and Vietnamese diacritics make a count of
     * characters a poor guess at how wide any of them will draw. A single word
     * longer than the line is left to overrun rather than cut — a truncated
     * figure is worse than an ugly one.
     */
    private fun wrap(
        text: String,
        width: Float,
        style: PdfStyle,
        measurer: TextMeasurer,
    ): List<String> {
        val lines = mutableListOf<String>()
        var line = StringBuilder()
        text.split(' ').forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (measurer.widthOf(candidate, style) <= width || line.isEmpty()) {
                line = StringBuilder(candidate)
            } else {
                lines += line.toString()
                line = StringBuilder(word)
            }
        }
        if (line.isNotEmpty()) lines += line.toString()
        return lines
    }
}
