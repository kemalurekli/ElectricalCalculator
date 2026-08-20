package com.kemalurekli.electricalcalculator.features.calculators.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

class CalculatorDetailViewModel(
    savedStateHandle: SavedStateHandle,
    catalog: CalculatorCatalog,
    private val favoritesRepository: FavoritesRepository,
    stringResolver: StringResolver,
) : ViewModel() {

    // Read as a plain argument rather than through `toRoute<Route.Calculator>()`.
    // The type-safe form was better — a mismatch between the route and this
    // ViewModel was a compile error — but `Route` lives in `:app` with the
    // navigation graph, and this screen no longer does. `CalculatorKeyArg` is
    // the seam: the route declares it and `ElecNavHost` asserts they agree.
    private val calculatorId: CalculatorId? =
        savedStateHandle.get<String>(CalculatorKeyArg)?.let(CalculatorId::fromKeyOrNull)

    private val descriptor = calculatorId?.let(catalog::findById)

    val uiState: StateFlow<CalculatorDetailUiState> = run {
        val base = CalculatorDetailUiState(
            id = descriptor?.id,
            title = descriptor?.let { stringResolver.get(it.title) }.orEmpty(),
            description = descriptor?.let { stringResolver.get(it.description) }.orEmpty(),
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

/**
 * The navigation argument holding which calculator to open.
 *
 * Named here rather than in the route so the ViewModel and the graph cannot
 * drift silently: `Route.Calculator` declares a property of this name, and
 * `ElecNavHost` has a test asserting it.
 */
const val CalculatorKeyArg = "calculatorKey"
