package com.kemalurekli.electricalcalculator.features.glossary.domain

import com.kemalurekli.electricalcalculator.core.common.util.SearchNormalizer
import java.util.Locale

/**
 * A glossary entry with its text already resolved for the active locale.
 *
 * The catalog stores resource ids, which only the UI layer can resolve, so the
 * presentation layer builds these and hands them here. That keeps the ranking
 * itself pure and testable without an Android context — the same split
 * [com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorSearch]
 * uses.
 */
data class SearchableTerm(
    val term: GlossaryTerm,
    val name: String,
    val definition: String,
)

/**
 * Ranked search over the glossary.
 *
 * A glossary is used differently from a list of tools: the reader almost always
 * has a specific word in mind, often half-remembered, and often in the other
 * language. So the ranking puts an exact name ahead of a partial one, and both
 * ahead of a term that merely mentions the word in its definition — otherwise
 * searching "earth" would bury *Earthing* under the dozen entries that discuss
 * it.
 *
 * The English name is matched even when the app is running in Turkish. That is
 * the point of carrying it: someone reading an English datasheet types what the
 * datasheet says.
 */
object GlossarySearch {

    /**
     * Returns the entries matching [query], best match first.
     *
     * A blank query returns [items] unchanged, so the caller can pass search
     * text straight through without special-casing the empty state.
     */
    fun filter(
        items: List<SearchableTerm>,
        query: String,
        locale: Locale = Locale.getDefault(),
    ): List<SearchableTerm> {
        val needle = SearchNormalizer.normalise(query, locale)
        if (needle.isEmpty()) return items

        return items
            .mapNotNull { item ->
                val score = score(item, needle, locale)
                if (score == NO_MATCH) null else item to score
            }
            // Stable, so equally-scored entries keep the order they arrived in
            // — which is already alphabetical by the time search runs.
            .sortedBy { (_, score) -> score }
            .map { (item, _) -> item }
    }

    private fun score(item: SearchableTerm, needle: String, locale: Locale): Int {
        val name = SearchNormalizer.normalise(item.name, locale)
        val english = SearchNormalizer.normalise(item.term.englishTerm, locale)

        if (name == needle || english == needle) return EXACT
        if (name.startsWith(needle) || english.startsWith(needle)) return NAME_PREFIX

        // Any word within either name, so "drop" finds "Voltage drop" and
        // "düşüm" finds "Gerilim düşümü".
        if (wordsOf(name).any { it.startsWith(needle) } ||
            wordsOf(english).any { it.startsWith(needle) }
        ) {
            return NAME_WORD_PREFIX
        }

        if (name.contains(needle) || english.contains(needle)) return NAME_CONTAINS

        // The symbol is how a formula names the quantity, so a reader who has
        // met "Zs" on a test sheet can look it up as written.
        val symbol = item.term.symbol
        if (symbol != null && SearchNormalizer.normalise(symbol, locale).contains(needle)) {
            return SYMBOL
        }

        if (SearchNormalizer.normalise(item.definition, locale).contains(needle)) {
            return DEFINITION
        }

        return NO_MATCH
    }

    private fun wordsOf(text: String): List<String> = text.split(' ', '(', ')', '-', '/')

    // Lower is a better match.
    private const val EXACT = 0
    private const val NAME_PREFIX = 1
    private const val NAME_WORD_PREFIX = 2
    private const val NAME_CONTAINS = 3
    private const val SYMBOL = 4
    private const val DEFINITION = 5
    private const val NO_MATCH = Int.MAX_VALUE
}
