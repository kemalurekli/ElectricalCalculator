package com.kemalurekli.electricalcalculator.core.document

/**
 * A page of a document somebody reads a line at a time.
 *
 * A [Divider] rather than a line of dashes: the app's exports separate their
 * sections with "— — —", which reads as a rule on a phone and as three stray
 * characters on paper.
 */
sealed interface DocumentBlock {

    /** A sentence, a label and its value — anything read left to right. */
    data class Line(val text: String) : DocumentBlock

    /** The name of a section: the formula, the working. */
    data class Heading(val text: String) : DocumentBlock

    /**
     * An equation or a substitution, set monospaced.
     *
     * Its own block rather than a [Line] with a different style, because the
     * document's argument is that the reader can follow the arithmetic, and
     * that only works when the figures line up.
     */
    data class Expression(val text: String) : DocumentBlock

    data object Divider : DocumentBlock
}

/**
 * The disclaimer at the foot of every page.
 *
 * Two parts because it is drawn as two: [label] in red and bold — one word, the
 * one a reader must not skip — and [body] in black beside it. The app carries
 * one of these in the reader's own language and one in English, and prints both
 * unless they are the same.
 */
data class DocumentNotice(val label: String, val body: String)

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
    private const val TITLE_GAP = 22f
    private const val HEADING_GAP = 8f
    private const val NOTICE_GAP = 6f
    /**
     * Wider than one space, and on purpose. The preview draws this line in the
     * app's typeface while the width was measured in the PDF's, so a gap of
     * exactly one space closes up on screen. Two and a half survives both and
     * reads as a deliberate pause after the colon.
     */
    private const val LABEL_SPACES = 2.5f

    /**
     * @param body the document itself, in the order it is read: the formula
     *   first, then the figures it produced, then the working. That order was
     *   asked for and it is the right one — a reader checking somebody else's
     *   number wants to see what was applied before they see what came out.
     * @param notices the disclaimer, in one or two languages. Drawn at the foot
     *   of every page, and measured before a single line of body is placed so a
     *   full page can never be drawn over it.
     */
    fun pages(
        title: String,
        body: List<DocumentBlock>,
        notices: List<DocumentNotice>,
        measurer: TextMeasurer,
    ): List<List<PdfOp>> {
        val width = PAGE.width - 2 * MARGIN
        val noticeLines = notices.sumOf { laidOut(it, width, measurer).lineCount() }
        val floor = PAGE.height - MARGIN -
            noticeLines * NOTICE_LINE_HEIGHT -
            (notices.size - 1).coerceAtLeast(0) * NOTICE_GAP -
            LINE_HEIGHT

        val pages = mutableListOf<MutableList<PdfOp>>()
        var page = mutableListOf<PdfOp>()
        var y = heading(title, page)

        body.forEach { block ->
            val lines = when (block) {
                is DocumentBlock.Line -> wrap(block.text, width, PdfStyle.TEXT, measurer)
                is DocumentBlock.Heading -> listOf(block.text)
                is DocumentBlock.Expression -> wrap(block.text, width, PdfStyle.FORMULA, measurer)
                DocumentBlock.Divider -> emptyList()
            }
            val height = when (block) {
                is DocumentBlock.Heading -> HEADING_GAP + lines.size * LINE_HEIGHT
                DocumentBlock.Divider -> DIVIDER_SPACE * 2
                else -> lines.size * LINE_HEIGHT
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

                is DocumentBlock.Heading -> {
                    // Air above a heading and none below it: the gap is what
                    // says the section before this one has ended, and putting
                    // it on both sides leaves the heading floating between two
                    // things instead of belonging to what follows.
                    y += HEADING_GAP
                    page += PdfOp.Text(block.text, MARGIN, y, PdfStyle.HEADING)
                    y += LINE_HEIGHT
                }

                is DocumentBlock.Expression -> lines.forEach { line ->
                    page += PdfOp.Text(line, MARGIN, y, PdfStyle.FORMULA)
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
        pages.forEach { drawNotices(it, notices, width, measurer) }
        return pages
    }

    /**
     * The title, and nothing under it.
     *
     * There used to be a line here naming the app and the date it was made.
     * Both are gone: the app's name on a document somebody hands to a client is
     * the app advertising itself on their letterhead, and a timestamp on a
     * calculation invites the reader to treat it as a record of when something
     * was true. It is a working, not a certificate.
     */
    private fun heading(title: String, page: MutableList<PdfOp>): Float {
        val y = MARGIN + PdfStyle.TITLE.size
        page += PdfOp.Text(title, MARGIN, y, PdfStyle.TITLE)
        return y + TITLE_GAP
    }

    /**
     * One notice per language, stacked at the foot of the page.
     *
     * Each begins with its label in red, on the same line as the first line of
     * its body, so the warning reads as a sentence rather than as a heading
     * with a paragraph under it.
     */
    private fun drawNotices(
        page: MutableList<PdfOp>,
        notices: List<DocumentNotice>,
        width: Float,
        measurer: TextMeasurer,
    ) {
        val blocks = notices.map { laidOut(it, width, measurer) }
        val height = blocks.sumOf { it.lineCount() } * NOTICE_LINE_HEIGHT +
            (blocks.size - 1).coerceAtLeast(0) * NOTICE_GAP
        var y = PAGE.height - MARGIN - height + PdfStyle.NOTICE.size
        page += PdfOp.Rule(MARGIN, PAGE.width - MARGIN, y - PdfStyle.NOTICE.size - RULE_OFFSET)

        blocks.forEachIndexed { index, pieces ->
            if (index > 0) y += NOTICE_GAP
            pieces.forEach { piece ->
                page += PdfOp.Text(piece.text, MARGIN + piece.indent, y, piece.style)
                // The label shares its baseline with the first line of the
                // body, so only the body advances the cursor.
                if (!piece.sameLine) y += NOTICE_LINE_HEIGHT
            }
        }
    }

    /** How many baselines a laid-out notice occupies. */
    private fun List<NoticePiece>.lineCount(): Int = count { !it.sameLine }

    /** One drawn piece of a notice: where it goes and what it is set in. */
    private data class NoticePiece(
        val text: String,
        val indent: Float,
        val style: PdfStyle,
        /** True when the next piece shares this baseline. */
        val sameLine: Boolean,
    )

    /**
     * Wraps a notice, keeping its label on the first line of its body.
     *
     * The first line is short by the width of the label; the rest run the full
     * measure. Counting the pieces gives the height, which is what the page
     * needs before it can decide where its own floor is — hence the same
     * function being called twice, once to measure and once to draw.
     */
    private fun laidOut(
        notice: DocumentNotice,
        width: Float,
        measurer: TextMeasurer,
    ): List<NoticePiece> {
        // A measured space, not a guessed one: the gap has to look like the gap
        // between any two words in the sentence it starts.
        val labelWidth = measurer.widthOf(notice.label, PdfStyle.NOTICE_LABEL) +
            measurer.widthOf(" ", PdfStyle.NOTICE) * LABEL_SPACES
        val lines = wrapIndented(notice.body, width - labelWidth, width, PdfStyle.NOTICE, measurer)
        return buildList {
            add(NoticePiece(notice.label, 0f, PdfStyle.NOTICE_LABEL, sameLine = true))
            lines.forEachIndexed { index, line ->
                add(
                    NoticePiece(
                        text = line,
                        indent = if (index == 0) labelWidth else 0f,
                        style = PdfStyle.NOTICE,
                        sameLine = false,
                    ),
                )
            }
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
    ): List<String> = wrapIndented(text, width, width, style, measurer)

    /** [wrap], with a first line that has something else on it. */
    private fun wrapIndented(
        text: String,
        firstWidth: Float,
        restWidth: Float,
        style: PdfStyle,
        measurer: TextMeasurer,
    ): List<String> {
        val lines = mutableListOf<String>()
        var line = StringBuilder()
        text.split(' ').forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            val available = if (lines.isEmpty()) firstWidth else restWidth
            if (measurer.widthOf(candidate, style) <= available || line.isEmpty()) {
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
