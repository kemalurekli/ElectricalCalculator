package com.kemalurekli.electricalcalculator.features.glossary.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.firstCharacter
import com.kemalurekli.electricalcalculator.core.common.util.localizedComparator
import com.kemalurekli.electricalcalculator.core.common.util.uppercaseLocalized
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteKind
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossarySearch
import com.kemalurekli.electricalcalculator.features.glossary.domain.SearchableTerm
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

/** A term with its text resolved and its cross-references named. */
@Immutable
data class GlossaryTermUiModel(
    val key: String,
    val name: String,
    /**
     * The English name, or null when it is the same words as [name] and would
     * only be repeating itself.
     */
    val englishName: String?,
    val definition: String,
    val symbol: String?,
    val unit: String?,
    val seeAlso: ImmutableList<GlossaryLink>,
    val calculator: CalculatorId?,
    val referenceTopic: String?,
)

/** A named jump to another term. */
@Immutable
data class GlossaryLink(val key: String, val name: String)

/** One letter's worth of terms. */
@Immutable
data class GlossarySection(
    val letter: String,
    val terms: ImmutableList<GlossaryTermUiModel>,
)

@Immutable
data class GlossaryUiState(
    val query: String = "",
    /** Grouped A–Z while browsing; a single unlabelled group while searching. */
    val sections: ImmutableList<GlossarySection> = persistentListOf(),
    val expandedKey: String? = null,
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val hasNoResults: Boolean get() = isSearching && sections.isEmpty()
    val termCount: Int get() = sections.sumOf { it.terms.size }
}

/**
 * The glossary.
 *
 * ### Why this one has a ViewModel when the reference index does not
 *
 * The reference index is a static list and renders straight from its catalog.
 * This screen has to resolve 117 terms into the reader's language, sort them
 * with a locale collator, group them by letter and rank them against a query —
 * work that does not belong in composition and that is worth doing once rather
 * than on every recomposition.
 *
 * ### Sorting
 *
 * A–Z is not the same sequence in every language, so ordering goes through
 * [localizedComparator] rather than [String.compareTo]. In Turkish that puts
 * *Çalışma* after *Cihaz* instead of after *Z*, which is where a naive
 * code-point sort would file it.
 */
class GlossaryViewModel(
    private val stringResolver: StringResolver,
    private val savedStateHandle: SavedStateHandle,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    /**
     * The pinned keys on this shelf.
     *
     * Kept beside the list rather than folded into each row: pinning does not
     * change what is listed or how it is ordered, and rebuilding every row to
     * flip one star would throw away the expanded card the reader is reading.
     */
    val pinned: StateFlow<Set<String>> = favoritesRepository.observeAll()
        .map { items -> items.filter { it.kind == FavoriteKind.GLOSSARY }.map { it.key }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FAVORITES_TIMEOUT_MILLIS),
            initialValue = emptySet(),
        )

    fun onToggleFavorite(key: String) {
        viewModelScope.launch { favoritesRepository.toggle(FavoriteItem(FavoriteKind.GLOSSARY, key)) }
    }

    private val _uiState = MutableStateFlow(GlossaryUiState())
    val uiState: StateFlow<GlossaryUiState> = _uiState.asStateFlow()

    init {
        // Restores an in-progress search across process death, not just rotation.
        rebuild(savedStateHandle[KEY_QUERY] ?: "")
    }

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
        rebuild(value)
    }

    fun onClearQuery() = onQueryChange("")

    /** Opens a term, or closes it if it is the one already open. */
    fun onToggleExpanded(key: String) {
        _uiState.update {
            it.copy(expandedKey = if (it.expandedKey == key) null else key)
        }
    }

    /**
     * Opens a term arrived at from outside — a search hit, or a deep link.
     *
     * Identical to following a see-also link, and named separately because the
     * caller is a navigation effect rather than a tap and reads better for it.
     */
    fun onOpenTerm(key: String) = onFollowLink(key)

    /** Follows a see-also link: opens that term and clears the search hiding it. */
    fun onFollowLink(key: String) {
        savedStateHandle[KEY_QUERY] = ""
        rebuild("")
        _uiState.update { it.copy(expandedKey = key) }
    }

    private fun rebuild(query: String) {
        val collator = localizedComparator()
        val searchable = GlossaryCatalog.all
            .map { term ->
                SearchableTerm(
                    term = term,
                    name = stringResolver.get(term.term),
                    definition = stringResolver.get(term.definition),
                )
            }
            .sortedWith(compareBy(collator) { it.name })

        val matches = GlossarySearch.filter(searchable, query)

        _uiState.update {
            it.copy(
                query = query,
                sections = when {
                    query.isBlank() -> groupByLetter(matches, collator)
                    matches.isEmpty() -> persistentListOf()
                    // Results are ranked, so imposing letter headings on them
                    // would fight the order that makes them useful.
                    else -> persistentListOf(
                        GlossarySection(
                            letter = "",
                            terms = matches.map(::toUiModel).toImmutableList(),
                        ),
                    )
                },
                // A result the user can no longer see must not stay open behind
                // the search.
                expandedKey = it.expandedKey?.takeIf { key -> matches.any { m -> m.term.key == key } },
            )
        }
    }

    /**
     * The heading a term is filed under: its first *character* as a reader sees
     * one.
     *
     * The two halves are both language questions rather than string ones, and
     * both live in `:core:common` for it — see [firstCharacter] for why this is
     * not `take(1)`, and [uppercaseLocalized] for why it is not `uppercase()`.
     */
    private fun initialOf(name: String): String = name.firstCharacter().uppercaseLocalized()

    private fun groupByLetter(
        items: List<SearchableTerm>,
        collator: Comparator<String>,
    ): ImmutableList<GlossarySection> = items
        .groupBy { initialOf(it.name) }
        .toList()
        .sortedWith(compareBy(collator) { (letter, _) -> letter })
        .map { (letter, terms) ->
            GlossarySection(letter = letter, terms = terms.map(::toUiModel).toImmutableList())
        }
        .toImmutableList()

    private fun toUiModel(item: SearchableTerm): GlossaryTermUiModel {
        val term = item.term
        return GlossaryTermUiModel(
            key = term.key,
            name = item.name,
            // In an English build the two are the same words, and a second line
            // repeating the title would be noise.
            englishName = term.englishTerm.takeIf { !it.equals(item.name, ignoreCase = true) },
            definition = item.definition,
            symbol = term.symbol,
            unit = term.unit,
            seeAlso = term.seeAlso
                .mapNotNull { key -> GlossaryCatalog.termOrNull(key)?.let { key to it } }
                .map { (key, related) ->
                    GlossaryLink(key = key, name = stringResolver.get(related.term))
                }
                .toImmutableList(),
            calculator = term.calculator,
            referenceTopic = term.referenceTopic,
        )
    }

    private companion object {
        const val FAVORITES_TIMEOUT_MILLIS = 5_000L

        const val KEY_QUERY = "glossary_query"
    }
}
