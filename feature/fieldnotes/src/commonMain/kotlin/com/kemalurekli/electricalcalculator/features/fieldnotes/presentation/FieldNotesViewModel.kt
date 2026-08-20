package com.kemalurekli.electricalcalculator.features.fieldnotes.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.util.SearchNormalizer
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteKind
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.features.fieldnotes.domain.FieldNote
import com.kemalurekli.electricalcalculator.features.fieldnotes.domain.FieldNoteCatalog
import com.kemalurekli.electricalcalculator.features.fieldnotes.domain.FieldNoteCategory
import com.kemalurekli.electricalcalculator.features.glossary.domain.GlossaryCatalog
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
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_cat_measurement_and_testing
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_cat_rules_of_thumb
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.fn_cat_safety_and_practice
import org.jetbrains.compose.resources.StringResource

/** A note with its text resolved and its cross-references named. */
@Immutable
data class FieldNoteUiModel(
    val key: String,
    val category: FieldNoteCategory,
    val title: String,
    val body: String,
    val terms: ImmutableList<FieldNoteLink>,
    val calculator: CalculatorId?,
    val referenceTopic: String?,
)

/** A named jump to a glossary term. */
@Immutable
data class FieldNoteLink(val key: String, val name: String)

/** One category's worth of notes. */
@Immutable
data class FieldNoteSection(
    val category: FieldNoteCategory,
    val title: StringResource,
    val notes: ImmutableList<FieldNoteUiModel>,
)

@Immutable
data class FieldNotesUiState(
    val query: String = "",
    val filter: FieldNoteCategory? = null,
    /** Categories that actually carry notes, in declaration order. */
    val categories: ImmutableList<FieldNoteCategory> = persistentListOf(),
    val sections: ImmutableList<FieldNoteSection> = persistentListOf(),
    val expandedKey: String? = null,
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val hasNoResults: Boolean get() = sections.isEmpty()
    val noteCount: Int get() = sections.sumOf { it.notes.size }
}

/**
 * The field notes.
 *
 * Resolves every note into the reader's language once, rather than on each
 * recomposition, for the same reason the glossary does: the text has to be
 * matched against a query, and matching resolved strings is what makes a search
 * find "kaçak akım" in a Turkish build and "residual current" in an English one
 * without the catalog knowing either phrase.
 *
 * ### Filtering and searching are one thing
 *
 * A category chip and a query narrow the same list, and both are applied before
 * grouping. That way an empty result is empty once — there is no state where a
 * category header sits above nothing because the query removed its contents.
 */
class FieldNotesViewModel(
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
        .map { items -> items.filter { it.kind == FavoriteKind.FIELD_NOTE }.map { it.key }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FAVORITES_TIMEOUT_MILLIS),
            initialValue = emptySet(),
        )

    fun onToggleFavorite(key: String) {
        viewModelScope.launch { favoritesRepository.toggle(FavoriteItem(FavoriteKind.FIELD_NOTE, key)) }
    }

    private val _uiState = MutableStateFlow(FieldNotesUiState())
    val uiState: StateFlow<FieldNotesUiState> = _uiState.asStateFlow()

    /**
     * Every note, resolved once at construction.
     *
     * Held rather than rebuilt because the ViewModel does not outlive a language
     * change: the picker recreates the Activity, which drops this along with it.
     */
    private val resolved: List<FieldNoteUiModel> = FieldNoteCatalog.all.map(::toUiModel)

    init {
        // Restores an in-progress search across process death, not just rotation.
        rebuild(
            query = savedStateHandle[KEY_QUERY] ?: "",
            filter = savedStateHandle.get<String>(KEY_FILTER)?.let(::categoryOrNull),
        )
    }

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
        rebuild(query = value, filter = _uiState.value.filter)
    }

    fun onClearQuery() = onQueryChange("")

    /** Selects a category, or clears the filter if it is the one already active. */
    fun onFilterChange(category: FieldNoteCategory?) {
        val next = category.takeIf { it != _uiState.value.filter }
        savedStateHandle[KEY_FILTER] = next?.name
        rebuild(query = _uiState.value.query, filter = next)
    }

    /** Opens a note, or closes it if it is the one already open. */
    fun onToggleExpanded(key: String) {
        _uiState.update { it.copy(expandedKey = if (it.expandedKey == key) null else key) }
    }

    /**
     * Opens a note arrived at from outside — a search hit, or a deep link.
     *
     * Clears the query and the filter first. A note reached from the app-wide
     * search would otherwise be hidden behind whatever narrowing this screen was
     * left in, which reads as the link having done nothing.
     */
    fun onOpenNote(key: String) {
        if (FieldNoteCatalog.noteOrNull(key) == null) return
        savedStateHandle[KEY_QUERY] = ""
        savedStateHandle[KEY_FILTER] = null
        rebuild(query = "", filter = null, expandedKey = key)
    }

    private fun rebuild(
        query: String,
        filter: FieldNoteCategory?,
        expandedKey: String? = _uiState.value.expandedKey,
    ) {
        val matching = resolved
            .filter { filter == null || it.category == filter }
            .filter { matches(it, query) }

        val sections = FieldNoteCategory.entries
            .mapNotNull { category ->
                val notes = matching.filter { it.category == category }
                if (notes.isEmpty()) {
                    null
                } else {
                    FieldNoteSection(category, category.title(), notes.toImmutableList())
                }
            }
            .toImmutableList()

        _uiState.value = FieldNotesUiState(
            query = query,
            filter = filter,
            // Chips list every category that has content at all, not just the
            // ones surviving the current query — a chip that vanished as you
            // typed would take away the control you use to widen the search.
            categories = FieldNoteCategory.entries
                .filter { category -> resolved.any { it.category == category } }
                .toImmutableList(),
            sections = sections,
            expandedKey = expandedKey.takeIf { key -> sections.any { s -> s.notes.any { it.key == key } } },
        )
    }

    /**
     * Matches against the title and the body.
     *
     * The body is included because a note is found by what it is about as often
     * as by how it is titled — someone typing "kondansatör" wants the note about
     * stored charge, whose title says neither.
     */
    private fun matches(note: FieldNoteUiModel, query: String): Boolean {
        if (query.isBlank()) return true
        return SearchNormalizer.contains(note.title, query) ||
            SearchNormalizer.contains(note.body, query) ||
            note.terms.any { SearchNormalizer.contains(it.name, query) }
    }

    private fun toUiModel(note: FieldNote) = FieldNoteUiModel(
        key = note.key,
        category = note.category,
        title = stringResolver.get(note.title),
        body = stringResolver.get(note.body),
        terms = note.glossaryTerms
            .mapNotNull { key ->
                GlossaryCatalog.termOrNull(key)?.let {
                    FieldNoteLink(key, stringResolver.get(it.term))
                }
            }
            .toImmutableList(),
        calculator = note.calculator,
        referenceTopic = note.referenceTopic,
    )

    private fun categoryOrNull(name: String): FieldNoteCategory? =
        FieldNoteCategory.entries.firstOrNull { it.name == name }

    private companion object {
        const val FAVORITES_TIMEOUT_MILLIS = 5_000L

        const val KEY_QUERY = "field_notes_query"
        const val KEY_FILTER = "field_notes_filter"
    }
}

/** The heading a category renders under. */
fun FieldNoteCategory.title(): StringResource = when (this) {
    FieldNoteCategory.SAFETY_AND_PRACTICE -> Res.string.fn_cat_safety_and_practice
    FieldNoteCategory.RULES_OF_THUMB -> Res.string.fn_cat_rules_of_thumb
    FieldNoteCategory.MEASUREMENT_AND_TESTING -> Res.string.fn_cat_measurement_and_testing
}
