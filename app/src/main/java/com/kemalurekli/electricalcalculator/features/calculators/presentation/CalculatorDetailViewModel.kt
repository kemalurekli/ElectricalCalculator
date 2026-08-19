package com.kemalurekli.electricalcalculator.features.calculators.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorCatalog
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State of a single calculator's screen.
 *
 * [id] is null when the route carried a key this build does not recognise —
 * possible via a stale deep link or a downgrade — so the screen can show an
 * error instead of crashing on a missing catalog entry.
 */
data class CalculatorDetailUiState(
    val id: CalculatorId? = null,
    val title: String = "",
    val description: String = "",
    val icon: CalculatorIcon = CalculatorIcon.POWER,
    val isFavorite: Boolean = false,
)

@HiltViewModel
class CalculatorDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    catalog: CalculatorCatalog,
    private val favoritesRepository: FavoritesRepository,
    stringResolver: StringResolver,
) : ViewModel() {

    // Decoded from the type-safe route rather than a raw string key, so a
    // mismatch between navigation and this ViewModel is a compile error.
    private val calculatorId: CalculatorId? =
        CalculatorId.fromKeyOrNull(savedStateHandle.toRoute<Route.Calculator>().calculatorKey)

    private val descriptor = calculatorId?.let(catalog::findById)

    val uiState: StateFlow<CalculatorDetailUiState> = run {
        val base = CalculatorDetailUiState(
            id = descriptor?.id,
            title = descriptor?.let { stringResolver.get(it.titleRes) }.orEmpty(),
            description = descriptor?.let { stringResolver.get(it.descriptionRes) }.orEmpty(),
            icon = descriptor?.icon ?: CalculatorIcon.POWER,
        )

        val favoriteFlow = calculatorId
            ?.let(favoritesRepository::observeIsFavorite)
            ?: flowOf(false)

        favoriteFlow
            .map { isFavorite -> base.copy(isFavorite = isFavorite) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = base,
            )
    }

    fun onToggleFavorite() {
        val id = calculatorId ?: return
        viewModelScope.launch { favoritesRepository.toggle(id) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
