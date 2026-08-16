package com.kemalurekli.electricalcalculator.features.theory.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.SearchNormalizer
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryLevel
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryTopic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** A topic as the list renders it, with its text resolved. */
@Immutable
data class TheoryTopicRow(
    val key: String,
    val level: TheoryLevel,
    val title: String,
    val summary: String,
)

/** One level's worth of topics. */
@Immutable
data class TheorySection(
    val level: TheoryLevel,
    @StringRes val titleRes: Int,
    val topics: ImmutableList<TheoryTopicRow>,
)

@Immutable
data class TheoryListUiState(
    val query: String = "",
    val filter: TheoryLevel? = null,
    /** Levels that actually carry topics, in declaration order. */
    val levels: ImmutableList<TheoryLevel> = persistentListOf(),
    val sections: ImmutableList<TheorySection> = persistentListOf(),
) {
    val hasNoResults: Boolean get() = sections.isEmpty()
}

/**
 * The theory shelf's list.
 *
 * Mirrors the general-info list, and for the same reasons: topics are resolved
 * into the reader's language once so a query can be matched against the text they
 * actually see, and the filter and the query narrow the same list before grouping
 * so a level heading never sits above nothing.
 *
 * The one difference worth naming is what a row does. A general-info card opens
 * where it sits; a theory row navigates, because what it opens is a form the
 * reader is going to work in rather than a paragraph they are going to read.
 */
@HiltViewModel
class TheoryListViewModel @Inject constructor(
    private val stringResolver: StringResolver,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TheoryListUiState())
    val uiState: StateFlow<TheoryListUiState> = _uiState.asStateFlow()

    /**
     * Every topic, resolved once at construction.
     *
     * Held rather than rebuilt because this ViewModel does not outlive a language
     * change: the picker recreates the Activity, which drops it along with it.
     */
    private val resolved: List<TheoryTopicRow> = TheoryCatalog.all.map(::toRow)

    init {
        // Restores an in-progress search across process death, not just rotation.
        rebuild(
            query = savedStateHandle[KEY_QUERY] ?: "",
            filter = savedStateHandle.get<String>(KEY_FILTER)?.let(::levelOrNull),
        )
    }

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
        rebuild(query = value, filter = _uiState.value.filter)
    }

    /** Selects a level, or clears the filter if it is the one already active. */
    fun onFilterChange(level: TheoryLevel?) {
        val next = level.takeIf { it != _uiState.value.filter }
        savedStateHandle[KEY_FILTER] = next?.name
        rebuild(query = _uiState.value.query, filter = next)
    }

    private fun rebuild(query: String, filter: TheoryLevel?) {
        val matching = resolved
            .filter { filter == null || it.level == filter }
            .filter { matches(it, query) }

        val sections = TheoryLevel.entries
            .mapNotNull { level ->
                val topics = matching.filter { it.level == level }
                if (topics.isEmpty()) null
                else TheorySection(level, level.titleRes(), topics.toImmutableList())
            }
            .toImmutableList()

        _uiState.value = TheoryListUiState(
            query = query,
            filter = filter,
            // Chips list every level that has content at all, not just the ones
            // surviving the current query — a chip that vanished as you typed
            // would take away the control you use to widen the search.
            levels = TheoryLevel.entries
                .filter { level -> resolved.any { it.level == level } }
                .toImmutableList(),
            sections = sections,
        )
    }

    /**
     * Matches against the title and the summary.
     *
     * Not against the theory body. A reader searching this shelf is looking for a
     * formula by name, and a three-paragraph derivation mentions enough adjacent
     * ideas that including it would make most queries match most topics. The
     * app-wide search does index the body, where a broad hit is what is wanted.
     */
    private fun matches(row: TheoryTopicRow, query: String): Boolean {
        if (query.isBlank()) return true
        return SearchNormalizer.contains(row.title, query) ||
            SearchNormalizer.contains(row.summary, query)
    }

    private fun toRow(topic: TheoryTopic) = TheoryTopicRow(
        key = topic.key,
        level = topic.level,
        title = stringResolver.get(topic.titleRes),
        summary = stringResolver.get(topic.summaryRes),
    )

    private fun levelOrNull(name: String): TheoryLevel? =
        TheoryLevel.entries.firstOrNull { it.name == name }

    private companion object {
        const val KEY_QUERY = "theory_query"
        const val KEY_FILTER = "theory_filter"
    }
}

/** The heading a level renders under. */
@StringRes
fun TheoryLevel.titleRes(): Int = when (this) {
    TheoryLevel.FOUNDATION -> R.string.th_level_foundation
    TheoryLevel.INTERMEDIATE -> R.string.th_level_intermediate
    TheoryLevel.ADVANCED -> R.string.th_level_advanced
}
