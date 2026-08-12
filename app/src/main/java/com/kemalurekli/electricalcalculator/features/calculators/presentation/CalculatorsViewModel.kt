package com.kemalurekli.electricalcalculator.features.calculators.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorCatalog
import com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorSearch
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculatorUiModel
import com.kemalurekli.electricalcalculator.core.ui.model.titleRes
import com.kemalurekli.electricalcalculator.core.ui.model.toSearchable
import com.kemalurekli.electricalcalculator.core.ui.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State of the calculator list screen.
 *
 * @param sections grouped by category and shown when browsing. Empty while a
 *   search is active.
 * @param searchResults a flat ranked list, shown while [query] is non-blank.
 */
data class CalculatorsUiState(
    val query: String = "",
    val sections: ImmutableList<CalculatorSection> = persistentListOf(),
    val searchResults: ImmutableList<CalculatorUiModel> = persistentListOf(),
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val isEmpty: Boolean get() = if (isSearching) searchResults.isEmpty() else sections.isEmpty()
}

data class CalculatorSection(
    val category: CalculatorCategory,
    val titleRes: Int,
    val items: ImmutableList<CalculatorUiModel>,
)

@HiltViewModel
class CalculatorsViewModel @Inject constructor(
    private val catalog: CalculatorCatalog,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Survives process death, not just rotation, so a long search is not lost
    // if the app is backgrounded mid-typing.
    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    val uiState: StateFlow<CalculatorsUiState> = combine(
        query,
        favoritesRepository.observeFavorites(),
    ) { currentQuery, favorites ->
        buildState(currentQuery, favorites.toSet())
    }.stateIn(
        scope = viewModelScope,
        // Keeps the state alive briefly across configuration changes so the
        // list is not rebuilt — and the database not re-queried — on rotation.
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CalculatorsUiState(),
    )

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    fun onToggleFavorite(id: CalculatorId) {
        viewModelScope.launch { favoritesRepository.toggle(id) }
    }

    private fun buildState(query: String, favorites: Set<CalculatorId>): CalculatorsUiState {
        val searchable = catalog.all.toSearchable(stringResolver)

        if (query.isNotBlank()) {
            return CalculatorsUiState(
                query = query,
                searchResults = CalculatorSearch.filter(searchable, query)
                    .map { it.toUiModel(isFavorite = it.descriptor.id in favorites) }
                    .toImmutableList(),
            )
        }

        val sections = searchable
            .groupBy { it.descriptor.category }
            .map { (category, entries) ->
                CalculatorSection(
                    category = category,
                    titleRes = category.titleRes(),
                    items = entries
                        .map { it.toUiModel(isFavorite = it.descriptor.id in favorites) }
                        .toImmutableList(),
                )
            }
            // Enum order defines the section order, so it is declared in one
            // place rather than duplicated as a sort list here.
            .sortedBy { it.category.ordinal }
            .toImmutableList()

        return CalculatorsUiState(query = query, sections = sections)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val KEY_QUERY = "calculators_query"
    }
}
