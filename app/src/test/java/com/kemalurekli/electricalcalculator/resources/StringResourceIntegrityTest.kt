package com.kemalurekli.electricalcalculator.resources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Structural checks over every `strings.xml` the app ships.
 *
 * These catch a class of bug that compiles, passes every other test, and only
 * shows up as broken text on a user's screen — a literal `%` surviving into the
 * UI, a positional argument dropped in translation, or a locale drifting out of
 * sync with the base language.
 *
 * The files are parsed from disk rather than through generated accessors,
 * because the defect lives in the resource markup and is invisible once it has
 * been compiled.
 *
 * ### Why it walks the tree
 *
 * It used to name two files: `:app`'s `values` and `values-tr`. That was right
 * when the app was one module and shipped two languages. The screens moved into
 * feature modules for iOS, and then ten more languages arrived — so the same
 * defect now has about a hundred and ninety files to hide in, and naming them
 * would mean remembering to name the next one.
 */
class StringResourceIntegrityTest {

    private data class StringEntry(
        val name: String,
        val body: String,
        val isFormatted: Boolean,
        val isTranslatable: Boolean,
    )

    /** One module's base file and every translation beside it. */
    private data class Bundle(
        val module: String,
        val base: List<StringEntry>,
        val translations: Map<String, List<StringEntry>>,
    )

    private val bundles: List<Bundle> = discover()

    @Test
    fun `the tree is actually being walked`() {
        // Without this, a path change turns every check below into a silent
        // pass over an empty list.
        assertTrue("No composeResources found — the search root moved", bundles.size >= 10)
        assertTrue(
            "No translations found beside the base files",
            bundles.any { it.translations.size >= 10 },
        )
    }

    @Test
    fun `a literal percent is only used in strings marked not formatted`() {
        // `stringResource(id)` with no arguments performs no String.format, so a
        // `%%` escape would reach the user verbatim. A string containing a bare
        // `%` and no positional argument must therefore declare
        // formatted="false", which is also what stops lint reading it as a
        // conversion specifier.
        eachEntry { module, locale, entry ->
            val hasPositionalArgs = POSITIONAL_ARG.containsMatchIn(entry.body)
            if (entry.body.contains('%') && !hasPositionalArgs) {
                assertTrue(
                    "$module/$locale/${entry.name} contains a literal % but is not " +
                        "marked formatted=\"false\"",
                    !entry.isFormatted,
                )
            }
        }
    }

    @Test
    fun `no string escapes a percent as double percent without taking arguments`() {
        eachEntry { module, locale, entry ->
            if (entry.body.contains("%%")) {
                assertTrue(
                    "$module/$locale/${entry.name} uses %% but takes no arguments, " +
                        "so it renders literally",
                    POSITIONAL_ARG.containsMatchIn(entry.body),
                )
            }
        }
    }

    @Test
    fun `every translatable base string is translated in every locale`() {
        bundles.forEach { bundle ->
            val expected = bundle.base.filter { it.isTranslatable }.map { it.name }.toSet()
            bundle.translations.forEach { (locale, entries) ->
                assertEquals(
                    "${bundle.module}/$locale is missing translations",
                    emptySet<String>(),
                    expected - entries.map { it.name }.toSet() - BRAND,
                )
            }
        }
    }

    @Test
    fun `a translation has no strings the base file lacks`() {
        bundles.forEach { bundle ->
            val base = bundle.base.map { it.name }.toSet()
            bundle.translations.forEach { (locale, entries) ->
                // A leftover key after a rename is dead weight that silently
                // never renders.
                assertEquals(
                    "${bundle.module}/$locale has orphaned strings",
                    emptySet<String>(),
                    entries.map { it.name }.toSet() - base,
                )
            }
        }
    }

    @Test
    fun `translations take the same positional arguments as the base string`() {
        bundles.forEach { bundle ->
            val base = bundle.base.associateBy { it.name }
            bundle.translations.forEach { (locale, entries) ->
                entries.forEach { translated ->
                    val original = base[translated.name] ?: return@forEach
                    assertEquals(
                        "${bundle.module}/$locale/${translated.name} takes different " +
                            "arguments from the base string",
                        positionalArgs(original.body),
                        positionalArgs(translated.body),
                    )
                }
            }
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
        // For the Romance languages the native "CC" / "CA" are the standard
        // forms and would belong here as the *expected* spelling, not the
        // forbidden one — which is why this is a table with one row in it
        // rather than a blanket rule with exceptions.
        val forbiddenByLocale = mapOf(
            "values-tr" to mapOf("DA" to "DC", "AA" to "AC"),
        )

        bundles.forEach { bundle ->
            forbiddenByLocale.forEach { (locale, replacements) ->
                val entries = bundle.translations[locale] ?: return@forEach
                replacements.forEach { (wrong, right) ->
                    val pattern = Regex("""\b$wrong\b""")
                    entries.forEach { entry ->
                        assertTrue(
                            "${bundle.module}/$locale/${entry.name} uses \"$wrong\"; " +
                                "write \"$right\" — it is not localised",
                            !pattern.containsMatchIn(entry.body),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `no string is defined twice in the same file`() {
        bundles.forEach { bundle ->
            (mapOf("values" to bundle.base) + bundle.translations).forEach { (locale, entries) ->
                val names = entries.map { it.name }
                assertEquals(
                    "${bundle.module}/$locale has duplicate string names",
                    names.size,
                    names.distinct().size,
                )
            }
        }
    }

    /** The distinct positional indices a template references, e.g. {1, 2}. */
    private fun positionalArgs(body: String): Set<Int> =
        POSITIONAL_ARG.findAll(body).map { it.groupValues[1].toInt() }.toSet()

    private fun eachEntry(block: (module: String, locale: String, entry: StringEntry) -> Unit) {
        bundles.forEach { bundle ->
            (mapOf("values" to bundle.base) + bundle.translations).forEach { (locale, entries) ->
                entries.forEach { block(bundle.module, locale, it) }
            }
        }
    }

    /** Every `composeResources` directory in the repository, plus `:app`'s own `res`. */
    private fun discover(): List<Bundle> {
        // Unit tests run with the module directory as the working directory.
        val root = File("..")
        val resourceRoots = root.walkTopDown()
            .maxDepth(RESOURCE_DEPTH)
            .filter { it.isDirectory && it.name == "composeResources" }
            .filter { !it.path.contains("/build/") }
            .toList() + File(root, "app/src/main/res")

        return resourceRoots.mapNotNull { dir ->
            val base = File(dir, "values/strings.xml").takeIf { it.exists() } ?: return@mapNotNull null
            val translations = dir.listFiles()
                .orEmpty()
                .filter { it.isDirectory && it.name.startsWith("values-") }
                .mapNotNull { localeDir ->
                    File(localeDir, "strings.xml").takeIf { it.exists() }
                        ?.let { localeDir.name to parse(it) }
                }
                .toMap()
            Bundle(module = dir.path.substringAfter("../").substringBefore("/src/"), base = parse(base), translations = translations)
        }
    }

    private fun parse(file: File): List<StringEntry> =
        STRING_ELEMENT.findAll(file.readText()).map { match ->
            val attributes = match.groupValues[1]
            StringEntry(
                name = NAME_ATTR.find(attributes)!!.groupValues[1],
                body = match.groupValues[2],
                isFormatted = !attributes.contains("formatted=\"false\""),
                isTranslatable = !attributes.contains("translatable=\"false\""),
            )
        }.toList()

    private companion object {
        val STRING_ELEMENT = Regex("""<string\s+([^>]*)>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        val NAME_ATTR = Regex("""name="([^"]+)"""")
        val POSITIONAL_ARG = Regex("""%(\d+)\$""")

        /** Deep enough for `feature/<name>/src/commonMain/composeResources`. */
        const val RESOURCE_DEPTH = 6

        /**
         * The product name is the same word in every language, so a locale that
         * omits it is falling back to the base file on purpose rather than
         * missing a translation.
         */
        val BRAND = setOf("app_name")
    }
}
