package com.kemalurekli.electricalcalculator.features.converter.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.features.converter.domain.ConvertUnitUseCase
import com.kemalurekli.electricalcalculator.features.converter.domain.ConvertedValue
import com.kemalurekli.electricalcalculator.features.converter.domain.MeasurementUnit
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCatalog
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCategory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class ConverterUiState(
    val categories: ImmutableList<UnitCategory> = persistentListOf(),
    val category: UnitCategory,
    val from: MeasurementUnit,
    val to: MeasurementUnit,
    val input: String = "",
    val result: Double? = null,
    val allUnits: ImmutableList<ConvertedValue> = persistentListOf(),
) {
    /** True while the input is blank or not yet a number. */
    val isEmpty: Boolean get() = result == null
}

/**
 * The unit converter.
 *
 * Converts as the user types rather than behind a button. A conversion is a
 * lookup, not a calculation with a verdict to present, and nothing here can
 * fail in a way worth reporting — an incomplete number simply produces no
 * result yet.
 *
 * For the same reason nothing is written to history: it would bury the
 * calculation records the user actually returns to under a stream of keystrokes.
 */
class ConverterViewModel(
    private val convert: ConvertUnitUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    // -- Editing --------------------------------------------------------------------

    fun onInputChange(value: String) = update { it.copy(input = value) }

    /**
     * Switches category, opening on the pair that category declares.
     *
     * The value is kept: someone converting 400 in one category often wants the
     * same number in another, and clearing it would be busywork.
     */
    fun onCategoryChange(category: UnitCategory) = update {
        it.copy(
            category = category,
            from = category.defaultFrom,
            to = category.defaultTo,
        )
    }

    fun onFromChange(unit: MeasurementUnit) = update { it.copy(from = unit) }

    fun onToChange(unit: MeasurementUnit) = update { it.copy(to = unit) }

    /** Swaps the two units, which is the most common thing to want next. */
    fun onSwap() = update { it.copy(from = it.to, to = it.from) }

    fun onClear() = update { it.copy(input = "") }

    /**
     * Applies [transform], then recomputes. Conversion is cheap and depends on
     * every field, so deriving it in one place beats remembering to refresh it
     * at each call site.
     */
    private fun update(transform: (ConverterUiState) -> ConverterUiState) {
        _uiState.update { current -> transform(current).withConversion() }
    }

    private fun ConverterUiState.withConversion(): ConverterUiState {
        val value = NumberFormatter.parseOrNull(input)
            ?: return copy(result = null, allUnits = persistentListOf())

        return copy(
            result = convert(value, from, to),
            allUnits = convert.toAll(value, from, category).toImmutableList(),
        )
    }

    private fun initialState(): ConverterUiState {
        // Voltage first: the app is an electrical toolkit, and the converter is
        // reached most often from a calculator that was dealing in volts.
        val category = UnitCatalog.all.first()
        return ConverterUiState(
            categories = UnitCatalog.all.toImmutableList(),
            category = category,
            from = category.defaultFrom,
            to = category.defaultTo,
        )
    }
}
