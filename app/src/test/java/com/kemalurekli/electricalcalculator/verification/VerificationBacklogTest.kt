package com.kemalurekli.electricalcalculator.verification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Structural checks over `docs/verification-backlog.md`.
 *
 * ### Why a test guards a document
 *
 * The backlog is the release gate for every figure no test can prove — the
 * transcribed tables. A gate that drifts out of date is worse than no gate,
 * because it reads as coverage it no longer has: a table added after the
 * document was written is a table nobody knows needs checking.
 *
 * So the real assertion here is [`every file carrying transcribed data has a
 * backlog row`]: the set of data-bearing files lives in this test, and adding
 * one without registering it fails the build. The rest of the checks keep the
 * document machine-readable enough for that one to work.
 *
 * The file is parsed from disk, like [com.kemalurekli.electricalcalculator
 * .resources.StringResourceIntegrityTest] parses `strings.xml`, because the
 * defect lives in the markup and nothing else ever reads it.
 */
class VerificationBacklogTest {

    private data class BacklogRow(
        val id: String,
        val item: String,
        val where: String,
        val source: String,
        val confidence: String,
        val status: String,
        val howToVerify: String,
    )

    private val rows: List<BacklogRow> = parse()

    // -- The document is readable at all ------------------------------------------------

    @Test
    fun `the backlog registers something`() {
        // A parser that silently matches nothing would make every other test
        // here vacuously pass.
        assertTrue("No rows parsed from $BACKLOG_PATH", rows.size >= 20)
    }

    @Test
    fun `every row identifies itself uniquely`() {
        val ids = rows.map { it.id }
        assertEquals("Duplicate backlog IDs", ids.size, ids.distinct().size)

        ids.forEach { id ->
            assertTrue("Backlog ID is not kebab-case: $id", KEBAB_CASE.matches(id))
        }
    }

    @Test
    fun `every row is fully filled in`() {
        rows.forEach { row ->
            assertTrue("${row.id} has no item description", row.item.isNotBlank())
            assertTrue("${row.id} cites no source", row.source.isNotBlank())
            assertTrue("${row.id} says nothing about how to verify it", row.howToVerify.isNotBlank())
        }
    }

    // -- The document points at things that exist ---------------------------------------

    @Test
    fun `every registered location exists on disk`() {
        // Catches the common rot: a file renamed or moved, leaving a row
        // pointing at nothing.
        rows.forEach { row ->
            val target = File(REPO_ROOT, row.where)
            assertTrue(
                "${row.id} points at a path that does not exist: ${row.where}",
                target.exists(),
            )
        }
    }

    @Test
    fun `status is one of the three the document defines`() {
        rows.forEach { row ->
            assertTrue(
                "${row.id} has status \"${row.status}\"; expected one of $STATUSES",
                row.status in STATUSES,
            )
        }
    }

    @Test
    fun `confidence is one of the levels the document defines`() {
        rows.forEach { row ->
            assertTrue(
                "${row.id} has confidence \"${row.confidence}\"; expected one of $CONFIDENCE_ORDER",
                row.confidence in CONFIDENCE_ORDER,
            )
        }
    }

    @Test
    fun `the register is ordered lowest confidence first`() {
        // The document tells the reader to work top-down because the riskiest
        // rows come first. If a later edit breaks that order, the instruction
        // becomes a lie.
        val ranks = rows.map { CONFIDENCE_ORDER.indexOf(it.confidence) }

        ranks.zipWithNext().forEachIndexed { index, (first, second) ->
            assertTrue(
                "Confidence goes back up at row ${index + 2} (${rows[index + 1].id}): " +
                    "${rows[index].confidence} then ${rows[index + 1].confidence}",
                first <= second,
            )
        }
    }

    // -- The gate itself ------------------------------------------------------------------

    @Test
    fun `every file carrying transcribed data has a backlog row`() {
        val registered = rows.map { it.where }.toSet()

        assertEquals(
            "Transcribed data with no entry in docs/verification-backlog.md. " +
                "A figure nobody knows to check is a figure that ships unchecked — " +
                "add a row for it, then add the path to TRANSCRIBED_DATA below.",
            emptySet<String>(),
            TRANSCRIBED_DATA - registered,
        )
    }

    @Test
    fun `the backlog registers nothing this test does not know about`() {
        val registered = rows.map { it.where }.toSet()

        // The other direction, so the two lists cannot drift apart: a new row
        // has to be declared here as well, which is what keeps the check above
        // meaningful.
        assertEquals(
            "Backlog rows point at paths missing from TRANSCRIBED_DATA",
            emptySet<String>(),
            registered - TRANSCRIBED_DATA,
        )
    }

    @Test
    fun `nothing is silently marked verified without saying against what`() {
        // A row moves to `verified` against a stated edition of a standard.
        // "Verified" with no edition recorded is not a verification anyone can
        // re-check when the standard is revised.
        rows.filter { it.status == "verified" }.forEach { row ->
            assertTrue(
                "${row.id} is marked verified but records no edition in its source",
                EDITION_YEAR.containsMatchIn(row.source) ||
                    EDITION_YEAR.containsMatchIn(row.howToVerify),
            )
        }
    }

    // -- Parsing ----------------------------------------------------------------------------

    private fun parse(): List<BacklogRow> {
        val file = File(REPO_ROOT, BACKLOG_PATH)
        assertTrue("Missing $BACKLOG_PATH", file.exists())

        return file.readLines()
            .map(String::trim)
            .filter { it.startsWith("|") && it.endsWith("|") }
            .map { line -> line.trim('|').split('|').map(String::trim) }
            .filter { it.size == COLUMN_COUNT }
            .filterNot { it[0] == "ID" || it[0].startsWith("---") }
            .map { cells ->
                BacklogRow(
                    id = cells[0].trim('`'),
                    item = cells[1],
                    where = cells[2].trim('`'),
                    source = cells[3],
                    confidence = cells[4],
                    status = cells[5],
                    howToVerify = cells[6],
                )
            }
    }

    private companion object {
        /**
         * Unit tests run with the module directory as the working directory,
         * so the repository root — where `docs/` lives — is one level up.
         */
        val REPO_ROOT = File("..")

        const val BACKLOG_PATH = "docs/verification-backlog.md"
        const val COLUMN_COUNT = 7

        val STATUSES = listOf("unverified", "verified", "corrected")

        /** Ordered: the register is written riskiest first. */
        val CONFIDENCE_ORDER = listOf("low", "medium", "high", "n/a")

        val KEBAB_CASE = Regex("""^[a-z0-9]+(-[a-z0-9]+)*$""")

        /** A four-digit year, as in "IEC 60364-5-52:2009". */
        val EDITION_YEAR = Regex("""\b(19|20)\d{2}\b""")

        private const val CALCULATORS =
            "app/src/main/java/com/kemalurekli/electricalcalculator/features/calculators"
        private const val CORE = "app/src/main/java/com/kemalurekli/electricalcalculator/core"
        private const val FEATURES = "app/src/main/java/com/kemalurekli/electricalcalculator/features"

        /**
         * Every place in the app that holds a figure read out of a standard, a
         * catalogue or common practice rather than computed from inputs.
         *
         * Adding a transcribed table without adding it here and to the backlog
         * is the failure this whole test exists to prevent.
         */
        val TRANSCRIBED_DATA = setOf(
            "$CALCULATORS/cablesize/domain/AmpacityTable.kt",
            "$CALCULATORS/cablesize/domain/CorrectionFactors.kt",
            "$CALCULATORS/earthfault/domain/AdiabaticFactors.kt",
            "$CALCULATORS/earthfault/domain/EarthFaultModels.kt",
            "$CALCULATORS/shortcircuit/domain/ShortCircuitModels.kt",
            "$CALCULATORS/conduitfill/domain/ConduitFillModels.kt",
            "$CALCULATORS/trayfill/domain/TrayFillModels.kt",
            "$CALCULATORS/lighting/presentation/LightingExamples.kt",
            "$CORE/domain/model/CableModels.kt",
            "$CORE/domain/model/ConductorMaterial.kt",
            "$CORE/ui/model/SystemVoltageDefaults.kt",
            "$CORE/designsystem/symbol",
            "$FEATURES/references/domain/ReferenceCatalog.kt",
            "$FEATURES/glossary/domain/GlossaryCatalog.kt",
            "$FEATURES/fieldnotes/domain/FieldNoteCatalog.kt",
            "app/src/main/res/values-tr/strings.xml",
        )
    }
}
