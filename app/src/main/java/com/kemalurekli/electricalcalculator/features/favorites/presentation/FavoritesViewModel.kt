package com.kemalurekli.electricalcalculator.features.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorCatalog
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculatorUiModel
import com.kemalurekli.electricalcalculator.core.ui.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val items: ImmutableList<CalculatorUiModel> = persistentListOf(),
    /**
     * Distinguishes "nothing pinned" from "not read yet", so the empty state
     * does not flash on screen while the first database emission is in flight.
     */
    val isLoading: Boolean = true,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val catalog: CalculatorCatalog,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = favoritesRepository.observeFavorites()
        .map { favorites ->
            FavoritesUiState(
                items = favorites
                    .mapNotNull { id -> catalog.findById(id) }
                    .map { it.toUiModel(stringResolver, isFavorite = true) }
                    .toImmutableList(),
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = FavoritesUiState(),
        )

    fun onToggleFavorite(id: CalculatorId) {
        viewModelScope.launch { favoritesRepository.toggle(id) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
