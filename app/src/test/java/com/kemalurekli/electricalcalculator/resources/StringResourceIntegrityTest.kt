package com.kemalurekli.electricalcalculator.resources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Structural checks over `strings.xml` itself.
 *
 * These catch a class of bug that compiles, passes every other test, and only
 * shows up as broken text on a user's screen — a literal `%` surviving into the
 * UI, or a translation drifting out of sync with the base language.
 *
 * The files are parsed from disk rather than through `R`, because the defect
 * lives in the resource markup and is invisible once aapt has compiled it.
 */
class StringResourceIntegrityTest {

    private data class StringEntry(
        val name: String,
        val body: String,
        val isFormatted: Boolean,
        val isTranslatable: Boolean,
    )

    private val baseStrings = parse("src/main/res/values/strings.xml")
    private val turkishStrings = parse("src/main/res/values-tr/strings.xml")

    @Test
    fun `a literal percent is only used in strings marked not formatted`() {
        // `stringResource(id)` with no arguments performs no String.format, so a
        // `%%` escape would reach the user verbatim. A string containing a bare
        // `%` and no positional argument must therefore declare
        // formatted="false", which is also what stops lint reading it as a
        // conversion specifier.
        (baseStrings + turkishStrings).forEach { entry ->
            val hasPositionalArgs = POSITIONAL_ARG.containsMatchIn(entry.body)
            val hasPercent = entry.body.contains('%')
            if (hasPercent && !hasPositionalArgs) {
                assertTrue(
                    "${entry.name} contains a literal % but is not marked formatted=\"false\"",
                    !entry.isFormatted,
                )
            }
        }
    }

    @Test
    fun `no string escapes a percent as double percent without taking arguments`() {
        (baseStrings + turkishStrings).forEach { entry ->
            if (entry.body.contains("%%")) {
                assertTrue(
                    "${entry.name} uses %% but takes no arguments, so it renders literally",
                    POSITIONAL_ARG.containsMatchIn(entry.body),
                )
            }
        }
    }

    @Test
    fun `every translatable base string has a Turkish translation`() {
        val expected = baseStrings.filter { it.isTranslatable }.map { it.name }.toSet()
        val actual = turkishStrings.map { it.name }.toSet()

        assertEquals(
            "Missing Turkish translations",
            emptySet<String>(),
            expected - actual,
        )
    }

    @Test
    fun `the Turkish file has no strings the base file lacks`() {
        val base = baseStrings.map { it.name }.toSet()
        val turkish = turkishStrings.map { it.name }.toSet()

        // A leftover key after a rename is dead weight that silently never renders.
        assertEquals("Orphaned Turkish strings", emptySet<String>(), turkish - base)
    }

    @Test
    fun `translations take the same positional arguments as the base string`() {
        val base = baseStrings.associateBy { it.name }

        turkishStrings.forEach { translated ->
            val original = base[translated.name] ?: return@forEach
            assertEquals(
                "${translated.name} argument count differs between languages",
                positionalArgs(original.body),
                positionalArgs(translated.body),
            )
        }
    }

    @Test
    fun `symbols that engineers read unchanged are not translated`() {
        // Some abbreviations are read the same way in every language an
        // electrician works in, and translating them makes a string *harder* to
        // read: a Turkish electrician reads "DC", writes "DC" on a drawing, and
        // sees "DA" as a mistake.
        //
        // Whether a given abbreviation is universal is a per-language question,
        // so the forbidden forms are declared per locale rather than assumed.
        // For Romance languages the native "CC" / "CA" are the standard forms
        // and belong here as the *expected* spelling, not the forbidden one —
        // which is exactly why this is a table and not a blanket rule.
        val forbiddenByLocale = mapOf(
            "values-tr" to mapOf("DA" to "DC", "AA" to "AC"),
        )

        val filesByLocale = mapOf("values-tr" to turkishStrings)

        forbiddenByLocale.forEach { (locale, replacements) ->
            val entries = filesByLocale.getValue(locale)
            replacements.forEach { (wrong, right) ->
                val pattern = Regex("""\b$wrong\b""")
                entries.forEach { entry ->
                    assertTrue(
                        "$locale/${entry.name} uses \"$wrong\"; write \"$right\" — " +
                            "it is not localised",
                        !pattern.containsMatchIn(entry.body),
                    )
                }
            }
        }
    }

    @Test
    fun `no string is defined twice in the same file`() {
        listOf("values" to baseStrings, "values-tr" to turkishStrings).forEach { (label, entries) ->
            val names = entries.map { it.name }
            assertEquals("$label has duplicate string names", names.size, names.distinct().size)
        }
    }

    /** The distinct positional indices a template references, e.g. {1, 2}. */
    private fun positionalArgs(body: String): Set<Int> =
        POSITIONAL_ARG.findAll(body).map { it.groupValues[1].toInt() }.toSet()

    private fun parse(relativePath: String): List<StringEntry> {
        // Unit tests run with the module directory as the working directory.
        val file = File(relativePath)
        assertTrue("Missing resource file: $relativePath", file.exists())

        return STRING_ELEMENT.findAll(file.readText()).map { match ->
            val attributes = match.groupValues[1]
            StringEntry(
                name = NAME_ATTR.find(attributes)!!.groupValues[1],
                body = match.groupValues[2],
                isFormatted = !attributes.contains("formatted=\"false\""),
                isTranslatable = !attributes.contains("translatable=\"false\""),
            )
        }.toList()
    }

    private companion object {
        val STRING_ELEMENT = Regex("""<string\s+([^>]*)>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        val NAME_ATTR = Regex("""name="([^"]+)"""")
        val POSITIONAL_ARG = Regex("""%(\d+)\$""")
    }
}
