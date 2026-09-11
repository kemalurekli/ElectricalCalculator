package com.kemalurekli.electricalcalculator.features.theory.presentation

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kemalurekli.electricalcalculator.core.common.util.SearchNormalizer
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryCatalog
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryLevel
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryTopic
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_level_advanced
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_level_foundation
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_level_intermediate
import org.jetbrains.compose.resources.StringResource

/** A topic as the list renders it, with its text resolved. */
@Immutable
data class TheoryTopicRow(
    val key: String,
    val level: TheoryLevel,
    val title: String,
    val summary: String,
    /**
     * Whether the topic ends somewhere the reader can put their own numbers.
     *
     * A third of the shelf does. It is the one thing a row can say that the
     * heading above it does not — the level is already the heading — and it is
     * the difference between a page to read and a page to use.
     */
    val hasCalculator: Boolean = false,
)

/** One level's worth of topics. */
@Immutable
data class TheorySection(
    val level: TheoryLevel,
    val title: StringResource,
    val topics: ImmutableList<TheoryTopicRow>,
)

@Immutable
data class TheoryListUiState(
    val query: String = "",
    val filter: TheoryLevel? = null,
    /** Levels that actually carry topics, in declaration order. */
    val levels: ImmutableList<TheoryLevel> = persistentListOf(),
    val sections: ImmutableList<TheorySection> = persistentListOf(),
    /** Whether the advanced shelf is open. */
    val isPro: Boolean = false,
) {
    val hasNoResults: Boolean get() = sections.isEmpty()

    /** Whether a row should say it is behind the paywall. */
    fun isLocked(level: TheoryLevel): Boolean = !isPro && level == TheoryLevel.ADVANCED
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
class TheoryListViewModel(
    private val stringResolver: StringResolver,
    private val savedStateHandle: SavedStateHandle,
    private val entitlements: EntitlementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TheoryListUiState())
    val uiState: StateFlow<TheoryListUiState> = _uiState.asStateFlow()

    init {
        // Read here rather than in the screen, so the screen stays a function
        // of its state and can be drawn in a preview or a test without a
        // billing graph behind it.
        //
        // Seeded synchronously and then followed. A StateFlow always has an
        // answer, and the cache means that answer is already the right one on a
        // cold launch; collecting alone would leave the first composition
        // drawing a gate over content the reader has paid for, for as long as
        // the dispatcher took to get to it.
        _uiState.update { it.copy(isPro = entitlements.isPro.value) }
        entitlements.isPro
            .onEach { pro -> _uiState.update { it.copy(isPro = pro) } }
            .launchIn(viewModelScope)
    }

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
                else TheorySection(level, level.title(), topics.toImmutableList())
            }
            .toImmutableList()

        // Rebuilt from scratch on every keystroke, so anything that is not
        // about the query has to be put back — and read from its own source
        // rather than copied off the old state. The entitlement is the one
        // such field, and a paying reader was watching the Pro badge reappear
        // on rows they own the moment they typed.
        _uiState.value = TheoryListUiState(
            isPro = entitlements.isPro.value,
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
        title = stringResolver.get(topic.title),
        summary = stringResolver.get(topic.summary),
        hasCalculator = topic.calculator != null,
    )

    private fun levelOrNull(name: String): TheoryLevel? =
        TheoryLevel.entries.firstOrNull { it.name == name }

    private companion object {
        const val KEY_QUERY = "theory_query"
        const val KEY_FILTER = "theory_filter"
    }
}

/** The heading a level renders under. */
fun TheoryLevel.title(): StringResource = when (this) {
    TheoryLevel.FOUNDATION -> Res.string.th_level_foundation
    TheoryLevel.INTERMEDIATE -> Res.string.th_level_intermediate
    TheoryLevel.ADVANCED -> Res.string.th_level_advanced
}
