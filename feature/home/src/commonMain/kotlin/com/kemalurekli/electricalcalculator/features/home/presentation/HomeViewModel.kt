package com.kemalurekli.electricalcalculator.features.home.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorSearch
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.home.domain.AppSearch
import com.kemalurekli.electricalcalculator.features.home.domain.SearchKind
import com.kemalurekli.electricalcalculator.features.home.domain.SearchableItem
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorUiModel
import com.kemalurekli.electricalcalculator.features.calculators.domain.toSearchable
import com.kemalurekli.electricalcalculator.features.calculators.domain.toUiModel
import com.kemalurekli.electricalcalculator.features.home.domain.SearchIndexBuilder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State of the home dashboard.
 *
 * @param favorites pinned calculators, surfaced above the dashboard so the
 *   tools a user relies on are one tap from launch.
 * @param recent the last few calculations, so returning to a job in progress
 *   does not require a trip through History.
 * @param savedCount total stored calculations, shown on the History card.
 * @param searchResults ranked matches, shown in place of everything else while
 *   [query] is non-blank.
 */
/**
 * A past calculation on the dashboard, carrying the glyph of the calculator
 * that produced it.
 *
 * The icon is resolved here rather than in the composable because the catalog
 * is the one place that knows which glyph a calculator claims, and it is
 * injected into this view model rather than reachable from a `@Composable`.
 */
data class RecentEntry(
    val record: CalculationRecord,
    val icon: CalculatorIcon,
)

data class HomeUiState(
    val query: String = "",
    val favorites: ImmutableList<CalculatorUiModel> = persistentListOf(),
    val recent: ImmutableList<RecentEntry> = persistentListOf(),
    val savedCount: Int = 0,
    /** Matches from every shelf, grouped and in declaration order. */
    val searchResults: ImmutableList<SearchSection> = persistentListOf(),
    val isLoading: Boolean = true,
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val hasNoResults: Boolean get() = isSearching && searchResults.isEmpty()
    val hasFavorites: Boolean get() = !isSearching && favorites.isNotEmpty()
    val hasRecent: Boolean get() = !isSearching && recent.isNotEmpty()
}

/** One shelf's worth of search hits, with the heading it renders under. */
@Immutable
data class SearchSection(
    val kind: SearchKind,
    val hits: ImmutableList<SearchableItem>,
)

class HomeViewModel(
    private val catalog: CalculatorCatalog,
    private val searchIndexBuilder: SearchIndexBuilder,
    private val favoritesRepository: FavoritesRepository,
    private val historyRepository: HistoryRepository,
    private val stringResolver: StringResolver,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Held in SavedStateHandle rather than a plain MutableStateFlow so an
    // in-progress search survives process death, not just rotation.
    private val query: StateFlow<String> =
        savedStateHandle.getStateFlow(KEY_QUERY, "")

    val uiState: StateFlow<HomeUiState> = combine(
        query,
        favoritesRepository.observeFavorites(),
        historyRepository.observeRecent(RECENT_LIMIT),
        historyRepository.observeCount(),
    ) { currentQuery, favoriteIds, recent, savedCount ->
        buildState(currentQuery, favoriteIds, recent, savedCount)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState(),
    )

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    fun onClearQuery() {
        savedStateHandle[KEY_QUERY] = ""
    }

    fun onToggleFavorite(id: CalculatorId) {
        viewModelScope.launch { favoritesRepository.toggle(id) }
    }

    private fun buildState(
        query: String,
        favoriteIds: List<CalculatorId>,
        recent: List<CalculationRecord>,
        savedCount: Int,
    ): HomeUiState {
        val favoriteSet = favoriteIds.toSet()

        if (query.isNotBlank()) {
            return HomeUiState(
                query = query,
                savedCount = savedCount,
                searchResults = AppSearch
                    .grouped(searchIndexBuilder.build(), query)
                    .map { (kind, hits) -> SearchSection(kind, hits.toImmutableList()) }
                    .toImmutableList(),
                isLoading = false,
            )
        }

        return HomeUiState(
            query = query,
            // Driven by the repository's ordering, which is pin order — not
            // catalog order — so the list matches what the user built up.
            favorites = favoriteIds
                .mapNotNull { id -> catalog.findById(id) }
                .map { it.toUiModel(stringResolver, isFavorite = true) }
                .toImmutableList(),
            recent = recent
                .map { RecentEntry(it, catalog[it.calculatorId].icon) }
                .toImmutableList(),
            savedCount = savedCount,
            isLoading = false,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val KEY_QUERY = "home_query"

        /** Enough to resume recent work without the dashboard becoming a list. */
        const val RECENT_LIMIT = 3
    }
}
