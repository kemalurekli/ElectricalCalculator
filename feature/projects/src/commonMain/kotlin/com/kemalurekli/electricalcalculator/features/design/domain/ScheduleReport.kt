package com.kemalurekli.electricalcalculator.features.design.domain

/**
 * A circuit schedule as a document, independent of how it is rendered.
 *
 * ### Why a model rather than a string builder
 *
 * The same schedule has to come out as a spreadsheet a designer opens on a
 * laptop and, later, as a page an inspector signs. Those differ in layout and
 * in nothing else: the same headings, the same rows, the same order. Writing
 * each renderer from the domain objects would let the two drift, and the drift
 * would be invisible until someone compared a printout against a file.
 *
 * ### Why the strings are already resolved
 *
 * The headings are translated and the figures are formatted for the reader's
 * locale, both of which need Android. Doing that at the boundary and handing
 * this a table of finished text keeps the renderers pure — the CSV writer here
 * has no dependency on the platform and would move to a shared module
 * unchanged, which is the point of building the model this way at all.
 */
data class ScheduleReport(
    val title: String,
    /** The supply the whole schedule was designed against, as label/value pairs. */
    val supply: List<ReportField>,
    val columns: List<String>,
    val rows: List<List<String>>,
    /**
     * The sentence the document carries about itself.
     *
     * It is part of the report rather than of either renderer because it has to
     * appear in both: the reader accepted a disclaimer inside the app, and the
     * client or inspector holding the export never saw it. A document that
     * leaves the app has to say what it is on its own face.
     */
    val notice: String,
)

data class ReportField(
    val label: String,
    val value: String,
)

/**
 * Renders a schedule as RFC 4180 comma-separated values.
 *
 * ### Why the supply is written above the table
 *
 * A schedule of cross-sections means nothing without the ambient and the cable
 * type they were derived from. A file that carries only the table invites the
 * reader to apply it to a different installation, so the parameters travel with
 * it as a short block of label/value rows before the header line.
 *
 * ### Why numbers are not localised here
 *
 * The figures arrive already formatted for the reader — `3,479` for a Turkish
 * reader — and a comma inside a comma-separated field is exactly the collision
 * this format is worst at. Quoting would keep the file valid but would land the
 * number in a spreadsheet as text. So the caller passes machine-readable
 * figures for CSV and display figures for anything a person reads directly,
 * and the two renderers are given different tables rather than one table that
 * is wrong for one of them.
 */
object ReportCsv {

    private const val SEPARATOR = ','
    private const val LINE_BREAK = "\r\n"

    fun render(report: ScheduleReport): String = buildString {
        append(escape(report.title)).append(LINE_BREAK)
        report.supply.forEach { field ->
            append(escape(field.label)).append(SEPARATOR)
            append(escape(field.value)).append(LINE_BREAK)
        }
        // With the preamble, not after the table. The table has to stay the
        // tail of the file: everything below the header line is a data row, and
        // a sentence down there would arrive in a spreadsheet as a short row
        // that shifts nothing but reads as a circuit with no cross-section.
        append(escape(report.notice)).append(LINE_BREAK)
        // A blank line, so a spreadsheet's import preview shows the table as a
        // table rather than folding the preamble into its first column.
        append(LINE_BREAK)
        append(report.columns.joinToString(SEPARATOR.toString()) { escape(it) }).append(LINE_BREAK)
        report.rows.forEach { row ->
            append(row.joinToString(SEPARATOR.toString()) { escape(it) }).append(LINE_BREAK)
        }
    }

    /**
     * Quotes a field only when it has to be.
     *
     * A separator, a quote or a line break inside a value would otherwise end
     * the field early — the classic way a schedule with a circuit called
     * "Kitchen, ring" arrives one column short and nobody notices.
     */
    private fun escape(value: String): String {
        val needsQuoting = value.any { it == SEPARATOR || it == '"' || it == '\n' || it == '\r' }
        if (!needsQuoting) return value
        return "\"" + value.replace("\"", "\"\"") + "\""
    }
}
