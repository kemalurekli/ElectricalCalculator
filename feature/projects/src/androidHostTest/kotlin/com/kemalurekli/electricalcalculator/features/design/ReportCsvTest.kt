package com.kemalurekli.electricalcalculator.features.design

import com.kemalurekli.electricalcalculator.features.design.domain.ReportCsv
import com.kemalurekli.electricalcalculator.features.design.domain.ReportField
import com.kemalurekli.electricalcalculator.features.design.domain.ScheduleReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The export, checked on the things that make a file useless rather than ugly.
 *
 * A schedule that arrives one column short is worse than no export at all: the
 * numbers all look plausible and belong to the wrong circuits. Every case here
 * is a way that happens.
 */
class ReportCsvTest {

    private fun report(
        title: String = "Block A",
        supply: List<ReportField> = listOf(ReportField("Site", "Depot")),
        columns: List<String> = listOf("Circuit", "Size"),
        rows: List<List<String>> = listOf(listOf("Lighting", "1.5")),
    ) = ScheduleReport(title, supply, columns, rows)

    @Test
    fun `the supply is written above the table`() {
        // A column of cross-sections means nothing without the ambient and the
        // cable type they came from, so the parameters travel with the file.
        val csv = ReportCsv.render(
            report(supply = listOf(ReportField("Ambient", "30"), ReportField("Ze", "0.35"))),
        )
        val lines = csv.lines()

        assertEquals("Block A", lines[0])
        assertEquals("Ambient,30", lines[1])
        assertEquals("Ze,0.35", lines[2])
        assertEquals("", lines[3])
        assertEquals("Circuit,Size", lines[4])
    }

    @Test
    fun `a circuit name containing a comma does not split the row`() {
        // The failure this format is worst at, and the one a Turkish or English
        // installer hits immediately: "Kitchen, ring" is an ordinary name.
        val csv = ReportCsv.render(report(rows = listOf(listOf("Kitchen, ring", "2.5"))))

        val row = csv.lines().last { it.isNotBlank() }
        assertEquals("\"Kitchen, ring\",2.5", row)
    }

    @Test
    fun `a quote inside a value is doubled, not dropped`() {
        val csv = ReportCsv.render(report(rows = listOf(listOf("""Bay "B"""", "4"))))

        val row = csv.lines().last { it.isNotBlank() }
        assertEquals("\"Bay \"\"B\"\"\",4", row)
    }

    @Test
    fun `a line break inside a value is quoted rather than ending the row`() {
        val csv = ReportCsv.render(report(rows = listOf(listOf("Upper\nfloor", "6"))))

        assertTrue(csv.contains("\"Upper\nfloor\",6"))
    }

    @Test
    fun `every row has as many fields as there are columns`() {
        // The invariant the whole file rests on. A row that is short by one
        // shifts every figure after it into the wrong heading.
        val csv = ReportCsv.render(
            report(
                columns = listOf("Circuit", "Size", "Drop"),
                rows = listOf(
                    listOf("Lighting", "1.5", "1.2"),
                    listOf("Kitchen, ring", "2.5", "2.8"),
                    listOf("", "", ""),
                ),
            ),
        )

        val table = csv.lines().drop(3).filter { it.isNotEmpty() }
        assertEquals(4, table.size)
        table.forEach { line ->
            assertEquals("wrong field count in: $line", 3, countFields(line))
        }
    }

    @Test
    fun `lines end the way the format says they do`() {
        // CRLF, not the platform's line separator: the file is opened on
        // whatever machine the schedule was emailed to.
        val csv = ReportCsv.render(report())
        assertTrue(csv.contains("\r\n"))
        assertTrue(csv.endsWith("\r\n"))
    }

    /** A minimal RFC 4180 reader, so the test parses rather than pattern-matches. */
    private fun countFields(line: String): Int {
        var fields = 1
        var inQuotes = false
        var index = 0
        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' && inQuotes && line.getOrNull(index + 1) == '"' -> index++
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> fields++
            }
            index++
        }
        return fields
    }
}
