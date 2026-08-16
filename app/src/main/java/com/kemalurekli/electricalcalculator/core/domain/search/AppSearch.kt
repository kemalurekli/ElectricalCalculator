package com.kemalurekli.electricalcalculator.core.domain.search

import com.kemalurekli.electricalcalculator.core.common.util.SearchNormalizer
import java.util.Locale

/** What kind of thing a search hit is, which decides where tapping it goes. */
enum class SearchKind {
    CALCULATOR,
    CONVERTER,
    THEORY,
    REFERENCE,
    GLOSSARY,
    SYMBOL,
    FIELD_NOTE,
}

/**
 * One searchable thing, with its text already resolved for the active locale.
 *
 * The catalogs all store string resource ids, which only the UI layer can
 * resolve, so the presentation layer builds these and hands them here. That
 * keeps the ranking pure and unit-testable — the same split
 * [com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorSearch]
 * has always used, now applied to the whole app.
 *
 * @param key the target's stable identifier, enough to navigate to it.
 * @param title what the reader is looking for.
 * @param subtitle context under the title. Never matched against.
 * @param keywords synonyms and alternate spellings — including the English name
 *   of a term whose title is in another language.
 * @param body longer text worth searching but not worth ranking highly: a
 *   description, a definition, a section heading.
 */
data class SearchableItem(
    val kind: SearchKind,
    val key: String,
    val title: String,
    val subtitle: String,
    val keywords: List<String> = emptyList(),
    val body: String = "",
)

/**
 * Search across everything the app knows.
 *
 * ### Why this exists
 *
 * The library grew to 16 calculators, 35 reference topics, 117 glossary terms
 * and 86 drawing symbols, and search reached only the first of those. A pile of
 * content nobody can find is not a reference — the thing that makes this app
 * *the place you open when the question occurs to you* is that the answer is one
 * search away, whichever of the five shelves it happens to sit on.
 *
 * ### Ranking
 *
 * By how directly the query hit, not by what kind of thing was hit. Someone
 * typing "selectivity" wants the primer even though a calculator exists; someone
 * typing "voltage drop" wants the calculator even though four topics mention it.
 * Match quality answers both without the search having to guess intent.
 *
 * Ties break by [SearchKind] declaration order, so equally-good hits come out in
 * a predictable sequence rather than in whatever order the index happened to be
 * built in.
 */
object AppSearch {

    /** The entries matching [query], best match first. Blank returns nothing. */
    fun filter(
        items: List<SearchableItem>,
        query: String,
        locale: Locale = Locale.getDefault(),
    ): List<SearchableItem> {
        val needle = SearchNormalizer.normalise(query, locale)
        // Unlike the calculator list, a blank query here means "show the
        // dashboard", not "show all 254 things".
        if (needle.isEmpty()) return emptyList()

        return items
            .mapNotNull { item ->
                val score = score(item, needle, locale)
                if (score == NO_MATCH) null else item to score
            }
            .sortedWith(compareBy({ (_, score) -> score }, { (item, _) -> item.kind.ordinal }))
            .map { (item, _) -> item }
    }

    /** The matches grouped by kind, in declaration order, best match first. */
    fun grouped(
        items: List<SearchableItem>,
        query: String,
        locale: Locale = Locale.getDefault(),
    ): Map<SearchKind, List<SearchableItem>> =
        filter(items, query, locale).groupBy { it.kind }

    private fun score(item: SearchableItem, needle: String, locale: Locale): Int {
        val title = SearchNormalizer.normalise(item.title, locale)

        if (title == needle) return EXACT
        if (title.startsWith(needle)) return TITLE_PREFIX
        if (wordsOf(title).any { it.startsWith(needle) }) return TITLE_WORD_PREFIX
        if (title.contains(needle)) return TITLE_CONTAINS

        if (item.keywords.any { SearchNormalizer.normalise(it, locale).startsWith(needle) }) {
            return KEYWORD_PREFIX
        }
        if (item.keywords.any { SearchNormalizer.normalise(it, locale).contains(needle) }) {
            return KEYWORD
        }
        if (item.body.isNotEmpty() &&
            SearchNormalizer.normalise(item.body, locale).contains(needle)
        ) {
            return BODY
        }

        return NO_MATCH
    }

    private fun wordsOf(text: String): List<String> = text.split(' ', '(', ')', '-', '/', ',')

    // Lower is a better match.
    private const val EXACT = 0
    private const val TITLE_PREFIX = 1
    private const val TITLE_WORD_PREFIX = 2
    private const val TITLE_CONTAINS = 3
    private const val KEYWORD_PREFIX = 4
    private const val KEYWORD = 5
    private const val BODY = 6
    private const val NO_MATCH = Int.MAX_VALUE
}
