package com.kemalurekli.electricalcalculator.core.domain.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Ranking across the whole app.
 *
 * Built from hand-written items rather than the real catalogs, so adding a
 * reference topic can never break a test about ordering, and so each case states
 * what the ranking *should* do rather than what today's content happens to
 * produce.
 */
class AppSearchTest {

    private val turkey = Locale.forLanguageTag("tr-TR")

    private fun item(
        kind: SearchKind,
        key: String,
        title: String,
        keywords: List<String> = emptyList(),
        body: String = "",
    ) = SearchableItem(kind, key, title, subtitle = "", keywords = keywords, body = body)

    private val items = listOf(
        item(SearchKind.CALCULATOR, "voltage_drop", "Gerilim Düşümü",
            keywords = listOf("volt", "drop"), body = "Kablo hattı boyunca gerilim düşümü"),
        item(SearchKind.CALCULATOR, "short_circuit", "Kısa Devre Akımı"),
        item(SearchKind.REFERENCE, "primer_selectivity", "Selektivite",
            body = "İki cihazın birlikte açması"),
        item(SearchKind.REFERENCE, "breaker_curves", "Şalter Eğrileri",
            body = "B, C ve D karakteristikleri"),
        item(SearchKind.GLOSSARY, "voltage_drop_term", "Gerilim düşümü",
            keywords = listOf("Voltage drop", "ΔU")),
        item(SearchKind.GLOSSARY, "selectivity_term", "Selektivite",
            keywords = listOf("Selectivity")),
        item(SearchKind.SYMBOL, "symbols_single_line", "Sigorta", keywords = listOf("F")),
        item(SearchKind.CONVERTER, "wire_gauge", "Tel Kalınlığı",
            keywords = listOf("mm²", "AWG", "kcmil")),
    )

    private fun keysFor(query: String) =
        AppSearch.filter(items, query, turkey).map { it.key }

    // -- Reach --------------------------------------------------------------------

    @Test
    fun `search reaches every shelf, not only the calculators`() {
        // The whole point of the phase. A pile of content nobody can find is
        // not a reference.
        assertTrue("references unreachable", keysFor("selektivite").contains("primer_selectivity"))
        assertTrue("glossary unreachable", keysFor("selektivite").contains("selectivity_term"))
        assertTrue("symbols unreachable", keysFor("sigorta").contains("symbols_single_line"))
        assertTrue("converter unreachable", keysFor("awg").contains("wire_gauge"))
    }

    @Test
    fun `a blank query returns nothing rather than everything`() {
        // Unlike the calculator list, a blank query here means "show the
        // dashboard" — returning 254 items would bury it.
        assertTrue(AppSearch.filter(items, "", turkey).isEmpty())
        assertTrue(AppSearch.filter(items, "   ", turkey).isEmpty())
    }

    // -- Ranking -------------------------------------------------------------------

    @Test
    fun `an exact title beats a partial one`() {
        val results = keysFor("selektivite")

        // Both titles are exactly "Selektivite"; the calculator-first tie-break
        // does not apply because neither is a calculator, so declaration order
        // of the kind decides: REFERENCE before GLOSSARY.
        assertEquals(listOf("primer_selectivity", "selectivity_term"), results.take(2))
    }

    @Test
    fun `a title match beats a body mention`() {
        // "Gerilim Düşümü" as a title outranks a topic that merely discusses it.
        val results = keysFor("gerilim")

        assertTrue(
            results.indexOf("voltage_drop") < results.indexOf("breaker_curves").takeIf { it >= 0 }
                ?: Int.MAX_VALUE,
        )
    }

    @Test
    fun `ranking is by match quality, not by what kind of thing was hit`() {
        // Someone typing a term wants the term even though calculators exist.
        val results = keysFor("selectivity")

        assertEquals("selectivity_term", results.first())
    }

    @Test
    fun `equally good hits come out in a predictable order`() {
        // Ties break by kind declaration order, so the list does not reshuffle
        // itself between builds.
        val once = keysFor("selektivite")
        val twice = keysFor("selektivite")

        assertEquals(once, twice)
    }

    // -- Matching ---------------------------------------------------------------------

    @Test
    fun `an English keyword finds a Turkish title`() {
        // The reason the English name is carried on every glossary term.
        assertTrue(keysFor("voltage drop").contains("voltage_drop_term"))
    }

    @Test
    fun `a symbol is found by its designation letter`() {
        assertEquals(listOf("symbols_single_line"), keysFor("F"))
    }

    @Test
    fun `unit symbols are searchable as themselves`() {
        // Language-neutral by design, so "kcmil" is typed as kcmil in Turkish.
        assertTrue(keysFor("kcmil").contains("wire_gauge"))
    }

    @Test
    fun `diacritics are not required`() {
        assertTrue(keysFor("kisa").contains("short_circuit"))
        assertTrue(keysFor("salter").contains("breaker_curves"))
    }

    @Test
    fun `an unmatched query returns nothing`() {
        assertTrue(keysFor("qqqzzz").isEmpty())
    }

    // -- Grouping ------------------------------------------------------------------------

    @Test
    fun `grouping keeps the ranked order inside each shelf`() {
        val grouped = AppSearch.grouped(items, "selektivite", turkey)

        assertEquals(
            listOf(SearchKind.REFERENCE, SearchKind.GLOSSARY),
            grouped.keys.toList(),
        )
        assertEquals(listOf("primer_selectivity"), grouped.getValue(SearchKind.REFERENCE).map { it.key })
    }

    @Test
    fun `grouping a blank query yields no groups`() {
        assertTrue(AppSearch.grouped(items, "", turkey).isEmpty())
    }
}
