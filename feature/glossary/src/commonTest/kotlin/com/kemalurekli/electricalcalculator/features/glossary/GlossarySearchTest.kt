package com.kemalurekli.electricalcalculator.features.glossary

import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossarySearch
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryTerm
import com.kemalurekli.electricalcalculator.features.glossary.domain.SearchableTerm
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ampacity_def
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.gl_ampacity_term

/**
 * Ranking over the glossary.
 *
 * The cases are built from hand-written entries rather than the real catalog, so
 * that adding a term can never break a test about ordering — and so the
 * expectations state what the ranking *should* do rather than what today's
 * catalog happens to produce.
 */
class GlossarySearchTest {


    private fun entry(
        key: String,
        english: String,
        name: String,
        definition: String = "",
        symbol: String? = null,
    ) = SearchableTerm(
        term = GlossaryTerm(
            key = key,
            englishTerm = english,
            // Never resolved. `filter` reads the key, the English term and
            // the symbol, and takes the localised name and definition as the
            // strings passed in — but the field is typed, so it gets real
            // handles rather than a pair of made-up numbers.
            term = Res.string.gl_ampacity_term,
            definition = Res.string.gl_ampacity_def,
            symbol = symbol,
        ),
        name = name,
        definition = definition,
    )

    private val items = listOf(
        entry("ampacity", "Ampacity", "Akım taşıma kapasitesi", symbol = "Iz"),
        entry("earthing", "Earthing", "Topraklama"),
        entry("earth_electrode", "Earth electrode", "Topraklama elektrodu"),
        entry(
            "voltage_drop",
            "Voltage drop",
            "Gerilim düşümü",
            definition = "Kablo boyunca kaybedilen gerilim.",
            symbol = "ΔU",
        ),
        entry(
            "rcd",
            "RCD",
            "RCD",
            definition = "Kaçak akıma tepki veren koruma cihazı; topraklama ile birlikte çalışır.",
        ),
    )

    private fun keysFor(query: String) =
        GlossarySearch.filter(items, query).map { it.term.key }

    // -- Ranking ---------------------------------------------------------------------

    @Test
    fun `an exact name outranks a term that merely starts with it`() {
        // "Topraklama" is a word in its own right and also the start of
        // "Topraklama elektrodu". The reader almost always wants the former.
        //
        // Only the first two are pinned: the RCD entry legitimately follows,
        // because its definition mentions earthing, and demanding it be absent
        // would be asserting the opposite of what the ranking is for.
        assertEquals(listOf("earthing", "earth_electrode"), keysFor("topraklama").take(2))
    }

    @Test
    fun `a name match outranks a definition mention`() {
        // Otherwise searching "topraklama" would surface the RCD entry, which
        // only discusses it, alongside the term itself.
        val results = keysFor("topraklama")

        assertTrue(results.indexOf("earthing") < results.indexOf("rcd"))
    }

    @Test
    fun `a word inside the name is found`() {
        // "düşüm" is the second word of "Gerilim düşümü".
        assertEquals(listOf("voltage_drop"), keysFor("düşüm"))
    }

    @Test
    fun `a symbol can be looked up as written`() {
        // Someone who has met "Iz" on a cable schedule types "Iz".
        assertEquals(listOf("ampacity"), keysFor("Iz"))
    }

    // -- Both languages ----------------------------------------------------------------

    @Test
    fun `an English term is found while the app is running in Turkish`() {
        // The reason the English name is carried at all: the datasheet in the
        // reader's other hand is in English.
        assertEquals(listOf("ampacity"), keysFor("ampacity"))
        assertEquals(listOf("voltage_drop"), keysFor("voltage drop"))
    }

    @Test
    fun `the Turkish name is found without its diacritics`() {
        // "akim" reaches "Akım taşıma kapasitesi" by its name, and the RCD
        // entry by its definition — the name match has to come first.
        assertEquals("ampacity", keysFor("akim").first())
        assertEquals(listOf("voltage_drop"), keysFor("gerilim dusumu"))
    }

    @Test
    fun `case does not matter in either language`() {
        assertEquals(keysFor("TOPRAKLAMA"), keysFor("topraklama"))
        assertEquals(keysFor("Ampacity"), keysFor("ampacity"))
    }

    // -- Behaviour -----------------------------------------------------------------------

    @Test
    fun `a blank query returns everything unchanged`() {
        // The caller passes search text straight through, so the empty state is
        // this function's job rather than the screen's.
        assertEquals(items, GlossarySearch.filter(items, ""))
        assertEquals(items, GlossarySearch.filter(items, "   "))
    }

    @Test
    fun `an unmatched query returns nothing rather than everything`() {
        assertTrue(keysFor("transformatör").isEmpty())
    }

    @Test
    fun `equally ranked results keep the order they arrived in`() {
        // They arrive alphabetical, and the sort is stable, so search results
        // do not reshuffle themselves between keystrokes.
        val alphabetical = items.sortedBy { it.name }

        val results = GlossarySearch.filter(alphabetical, "topraklama")

        assertEquals(listOf("earthing", "earth_electrode"), results.map { it.term.key }.take(2))
    }
}
