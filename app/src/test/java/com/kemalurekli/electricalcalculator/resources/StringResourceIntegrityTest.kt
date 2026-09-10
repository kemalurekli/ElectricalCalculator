package com.kemalurekli.electricalcalculator.resources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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
        //
        // A note for whoever reads the Turkish file and reaches for a fix.
        // Turkish writes a percentage with the sign in front — %5, not 5 % —
        // and its strings do it as `%%1$s`, which looks like a doubled escape
        // that has swallowed the argument. It has not. Compose Resources does
        // not use String.format at all: `replaceWithArgs` is a plain regex,
        // `%(\d+)\$[ds]`, in common code, so the second `%` opens the match and
        // the first is left standing as the literal sign. Verified on a device
        // in 2026-09: the screen reads "%0,91 — aydınlatma için %3 sınırı
        // içinde". Twenty-three strings depend on this, on both platforms.
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
    fun `a translated formula keeps the symbols its legend explains`() {
        // A formula card draws the formula from a string and its legend from
        // Kotlin: `FormulaVariable("P_in", stringResource(Res.string.mt_var_pin))`.
        // The symbol is code, the formula is translatable — so a translator who
        // renders P_in as P_zu leaves the legend describing a letter that is no
        // longer on the screen. Nothing fails: the card still draws, and only a
        // reader of that language can see that the two halves stopped agreeing.
        val legends = formulaLegends()
        assertTrue("No FormulaVariable declarations found — the search root moved", legends.size >= 10)

        bundles.forEach { bundle ->
            val base = bundle.base.associateBy { it.name }
            bundle.translations.forEach { (locale, entries) ->
                entries.forEach { entry ->
                    if (!entry.name.endsWith("_formula")) return@forEach
                    val symbols = legends[entry.name.removeSuffix("_formula")] ?: return@forEach
                    val original = base[entry.name] ?: return@forEach
                    symbols.forEach { symbol ->
                        if (!original.body.contains(symbol)) return@forEach
                        assertTrue(
                            "${bundle.module}/$locale/${entry.name} drops the symbol " +
                                "\"$symbol\", which its legend still explains",
                            entry.body.contains(symbol),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `a translation keeps the units the base string writes`() {
        // Units are drawn by the screens, not by the strings: `unit = "kW"` is
        // Kotlin, the same in every language. So a string that spells a unit out
        // beside a number has to spell it the way the screen will, or the same
        // quantity ends up with two names a few millimetres apart — a result card
        // reading "5,30 kW" above an exported line reading "5,30 кВт".
        //
        // Only value-and-unit pairs are checked: a bare "A" is the English
        // article as often as it is an ampere. Time is left out entirely —
        // every language writes "8 hours" as a word of its own, and a countdown
        // that says "30 sn sonra" in Turkish is right to.
        val units = unitsUsedInScreens() - TIME_UNITS
        assertTrue("No unit literals found — the search root moved", units.size >= 10)

        bundles.forEach { bundle ->
            val base = bundle.base.associateBy { it.name }
            bundle.translations.forEach { (locale, entries) ->
                entries.forEach { translated ->
                    val original = base[translated.name] ?: return@forEach
                    units.forEach { unit ->
                        if (!MEASURED_VALUE(unit).containsMatchIn(original.body)) return@forEach
                        assertTrue(
                            "${bundle.module}/$locale/${translated.name} drops the unit " +
                                "\"$unit\" that the base string measures in; the screen " +
                                "will still draw it that way",
                            STANDALONE(unit).containsMatchIn(translated.body),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `a locale writes its decimals the way that locale writes them`() {
        // NumberFormatter draws every figure the app computes with the
        // separator of the reader's locale — 0.5 in English, 0,5 in Turkish.
        // Prose that hard-codes the other one puts both on the same screen: a
        // result card reading "0.05 Ω" above a note reading "0,05 Ω".
        //
        // English was the file that had it wrong, in twenty-one places, which
        // is the direction to expect — most of these strings are written by
        // someone whose own keyboard puts a comma there.
        bundles.forEach { bundle ->
            (mapOf("values" to bundle.base) + bundle.translations).forEach { (locale, entries) ->
                val language = locale.removePrefix("values-")
                val wanted = if (locale == "values" || language in POINT_LOCALES) '.' else ','
                val wrong = if (wanted == '.') DECIMAL_COMMA else DECIMAL_POINT
                entries.forEach { entry ->
                    val prose = entry.body
                        .replace(STANDARD_REF, " ")
                        .replace(CLAUSE_REF, " ")
                    val found = wrong.find(prose) ?: return@forEach
                    fail(
                        "${bundle.module}/$locale/${entry.name} writes \"${found.value}\"; " +
                            "this locale's numbers are drawn with '$wanted'",
                    )
                }
            }
        }
    }

    @Test
    fun `a term the project has already replaced does not come back`() {
        // `docs/terminology.md` records six terms that read fluently and are not
        // what the trade writes, each corrected once against that country's own
        // wiring standard. A correction made in one module does not reach the
        // others, and nobody here can read ten of these languages well enough to
        // notice the survivor: Indonesian still said "drop tegangan" in the
        // navigation subtitle, and Dutch still said "kring" in the schedule
        // preview, months after both were replaced everywhere else.
        REPLACED_TERMS.forEach { (locale, rejected) ->
            bundles.forEach { bundle ->
                bundle.translations[locale]?.forEach { entry ->
                    rejected.forEach { (wrong, right) ->
                        assertTrue(
                            "${bundle.module}/$locale/${entry.name} says \"$wrong\"; " +
                                "docs/terminology.md settled on \"$right\"",
                            !wrong.containsMatchIn(entry.body),
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

    /** Every unit literal the screens hand to a field or a result row. */
    private fun unitsUsedInScreens(): Set<String> {
        val sources = File("../feature").walkTopDown()
            .onEnter { it.name != "build" }
            .filter { it.isFile && it.extension == "kt" }
        return sources
            .flatMap { UNIT_LITERAL.findAll(it.readText()) }
            .map { it.groupValues[1] }
            .filter { it.isNotEmpty() && it != "%" && it.first().isLetter() }
            .toSet()
    }

    /** Calculator prefix -> the symbols its formula legend names, read from the screens. */
    private fun formulaLegends(): Map<String, List<String>> {
        val sources = File("../feature").walkTopDown()
            .onEnter { it.name != "build" }
            .filter { it.isFile && it.extension == "kt" }
        return sources
            .flatMap { FORMULA_VARIABLE.findAll(it.readText()) }
            .groupBy({ it.groupValues[2] }, { it.groupValues[1] })
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
        val UNIT_LITERAL = Regex("""unit = "([^"]*)"""")

        /** Seconds, hours and minutes are words in most languages, not symbols. */
        val TIME_UNITS = setOf("s", "h", "min")

        /** A number, or a placeholder that will become one, followed by the unit. */
        fun MEASURED_VALUE(unit: String) =
            Regex("""(?:\d|\$[sd])\s${Regex.escape(unit)}(?![\p{L}\d])""")

        /** The same unit standing on its own anywhere in the translation. */
        fun STANDALONE(unit: String) =
            Regex("""(?<![\p{L}\d])${Regex.escape(unit)}(?![\p{L}\d])""")

        val FORMULA_VARIABLE =
            Regex("""FormulaVariable\(\s*"([^"]+)"\s*,\s*stringResource\(Res\.string\.([a-z]+)_var_""")

        /**
         * Languages that write the decimal point as a point. Everything else
         * this app ships — Turkish, German, French, Spanish, Italian, Dutch,
         * Polish, Portuguese, Russian, Vietnamese, Indonesian — uses a comma.
         */
        val POINT_LOCALES = emptySet<String>()

        val DECIMAL_COMMA = Regex("""(?<![\d,.])\d+,\d+(?![\d,.])""")
        val DECIMAL_POINT = Regex("""(?<![\d,.])\d+\.\d+(?![\d,.])""")

        /**
         * A clause of a standard is not a measurement: NEC 392.22 keeps its
         * point in Turkish, and IEC 60364-4-41 keeps its hyphens everywhere.
         * Both are cut out of the string before the separator is judged, so
         * that a sentence citing a standard is still checked for the decimals
         * it also carries.
         */
        val STANDARD_REF = Regex("""(?:NEC|IEC|EN|BS|ISO|DIN|HD|CEI)\s?[\d-]+(?:\.\d+)*""")

        /**
         * "Table 41.1", in each of the twelve languages that name one — and in
         * whatever case the sentence puts it in, which is why this matches a
         * stem rather than a word: Russian declines it to "таблице", Polish to
         * "tablicy".
         */
        val CLAUSE_REF = Regex(
            """(?:Tab|\u0422\u0430\u0431\u043B|B\u1EA3ng)\p{L}*\s*[\d-]+(?:\.\d+)*""",
            RegexOption.IGNORE_CASE,
        )

        val IGNORE = RegexOption.IGNORE_CASE

        /**
         * The terms of `docs/terminology.md` that were checked, found wrong and
         * replaced — with what they were replaced by. Case-insensitive, because
         * a term at the start of a label is capitalised.
         */
        val REPLACED_TERMS: Map<String, List<Pair<Regex, String>>> = mapOf(
            "values-de" to listOf(Regex("Schutzorgan", IGNORE) to "Schutzeinrichtung"),
            "values-es" to listOf(
                Regex("corriente de dise\u00f1o", IGNORE) to "corriente de empleo",
                Regex("corriente asignada del dispositivo", IGNORE) to "intensidad nominal del dispositivo",
                Regex("corriente admisible del cable", IGNORE) to "intensidad admisible del cable",
            ),
            "values-pt" to listOf(
                Regex("capacidade de corrente", IGNORE) to "corrente admiss\u00edvel",
                Regex("corrente nominal do dispositivo", IGNORE) to "corrente estipulada do dispositivo",
            ),
            "values-vi" to listOf(
                Regex("kh\u1ea3 n\u0103ng t\u1ea3i d\u00f2ng", IGNORE) to "d\u00f2ng \u0111i\u1ec7n cho ph\u00e9p",
                // The agreed terms in full. Vietnamese shortens both of these in
                // prose, which reads perfectly well and leaves one screen saying
                // a different thing from the next.
                Regex("(?<!\u0111i\u1ec7n )d\u00f2ng cho ph\u00e9p", IGNORE) to "d\u00f2ng \u0111i\u1ec7n cho ph\u00e9p",
                Regex("(?<!\u0111i\u1ec7n )d\u00f2ng t\u00ednh to\u00e1n", IGNORE) to "d\u00f2ng \u0111i\u1ec7n t\u00ednh to\u00e1n",
                // The third of the same family, found the same way: TCVN 7447
                // writes a device's rating in full, and the app shortened it in
                // twenty-one places across four modules.
                Regex("(?<!\u0111i\u1ec7n )d\u00f2ng \u0111\u1ecbnh m\u1ee9c", IGNORE) to "d\u00f2ng \u0111i\u1ec7n \u0111\u1ecbnh m\u1ee9c",
            ),
            "values-id" to listOf(Regex("drop tegangan", IGNORE) to "susut tegangan"),
            "values-nl" to listOf(Regex("""\bkring""", IGNORE) to "stroomkring"),
            // One adjective per concept, per language. Each of these three has a
            // settled word for the rating of a *protective device* and had a
            // second one loose in the same screen — a label reading one thing
            // and the formula card under it reading another. The machine
            // ratings keep their own word, which is why these are phrased
            // tightly enough to leave "corrente nominal do transformador" alone.
            "values-fr" to listOf(
                Regex("courant assign\u00e9 de l'appareil", IGNORE) to "calibre de l'appareil",
            ),
        )

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
