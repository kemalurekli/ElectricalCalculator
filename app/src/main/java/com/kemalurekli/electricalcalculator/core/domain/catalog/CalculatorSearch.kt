package com.kemalurekli.electricalcalculator.core.domain.catalog

import com.kemalurekli.electricalcalculator.core.common.util.SearchNormalizer
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorDescriptor
import java.util.Locale

/**
 * A catalog entry with its text already resolved for the active locale.
 *
 * [CalculatorCatalog] stores string resource ids, which only the UI layer can
 * resolve. Search matches against the resolved strings, so the presentation
 * layer builds these and hands them to [CalculatorSearch] — keeping the ranking
 * logic itself pure and unit-testable without an Android context.
 */
data class SearchableCalculator(
    val descriptor: CalculatorDescriptor,
    val title: String,
    val description: String,
)

/**
 * Ranked search over the calculator catalog.
 *
 * Matching is case- and diacritic-insensitive within the locale, and ranks by
 * how directly the query hit: a title prefix outranks a title substring, which
 * outranks a keyword synonym, which outranks a description mention. That order
 * is what makes typing "vo" surface "Voltage Drop" first rather than whichever
 * calculator happens to mention voltage in its description.
 */
object CalculatorSearch {

    /**
     * Returns the entries matching [query], best match first.
     *
     * A blank query returns [items] unchanged, so callers can pass search text
     * straight through without special-casing the empty state.
     */
    fun filter(
        items: List<SearchableCalculator>,
        query: String,
        locale: Locale = Locale.getDefault(),
    ): List<SearchableCalculator> {
        val normalisedQuery = SearchNormalizer.normalise(query, locale)
        if (normalisedQuery.isEmpty()) return items

        return items
            .mapNotNull { item ->
                val score = score(item, normalisedQuery, locale)
                if (score == NO_MATCH) null else item to score
            }
            // `sortedBy` is stable, so equally-scored entries keep catalog order.
            .sortedBy { (_, score) -> score }
            .map { (item, _) -> item }
    }

    private fun score(item: SearchableCalculator, query: String, locale: Locale): Int {
        val title = SearchNormalizer.normalise(item.title, locale)
        if (title.startsWith(query)) return TITLE_PREFIX
        if (title.contains(query)) return TITLE_CONTAINS

        // Any word within the title, so "drop" matches "Voltage Drop".
        if (title.split(' ').any { it.startsWith(query) }) return TITLE_WORD_PREFIX

        if (item.descriptor.searchKeywords.any {
                SearchNormalizer.normalise(it, locale).contains(query)
            }
        ) {
            return KEYWORD
        }
        if (SearchNormalizer.normalise(item.description, locale).contains(query)) {
            return DESCRIPTION
        }

        return NO_MATCH
    }

    // Lower is a better match.
    private const val TITLE_PREFIX = 0
    private const val TITLE_WORD_PREFIX = 1
    private const val TITLE_CONTAINS = 2
    private const val KEYWORD = 3
    private const val DESCRIPTION = 4
    private const val NO_MATCH = Int.MAX_VALUE
}
