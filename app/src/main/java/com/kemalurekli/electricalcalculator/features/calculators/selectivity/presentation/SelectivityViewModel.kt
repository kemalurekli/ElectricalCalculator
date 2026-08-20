package com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.common.util.ResourceIdResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.common.util.enumOrNull
import com.kemalurekli.electricalcalculator.core.common.util.pick
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.CheckSelectivityUseCase
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityInput
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Identifies a form field so validation errors can be routed back to it. */
enum class SelectivityField {
    UPSTREAM_RATING,
    DOWNSTREAM_RATING,
    FAULT_CURRENT,
}

@Immutable
data class SelectivityUiState(
    val upstreamType: ProtectiveDeviceType = ProtectiveDeviceType.MCB_TYPE_C,
    val upstreamRating: String = "",
    val downstreamType: ProtectiveDeviceType = ProtectiveDeviceType.MCB_TYPE_B,
    val downstreamRating: String = "",
    val faultCurrent: String = "",
    val errors: Map<SelectivityField, ValidationError> = emptyMap(),
    val result: SelectivityResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
)

/**
 * Two devices in series, and the fault level they stop separating at.
 *
 * The form asks for a prospective fault current because the answer depends on
 * it. A selectivity screen that reported a verdict from two device ratings
 * alone would be answering a different, easier question than the one the user
 * has.
 */
@HiltViewModel
class SelectivityViewModel @Inject constructor(
    private val checkSelectivity: CheckSelectivityUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: ResourceIdResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectivityUiState())
    val uiState: StateFlow<SelectivityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.SELECTIVITY)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing -----------------------------------------------------------

    fun onUpstreamTypeChange(type: ProtectiveDeviceType) = update { it.copy(upstreamType = type) }

    fun onUpstreamRatingChange(value: String) =
        update(SelectivityField.UPSTREAM_RATING) { it.copy(upstreamRating = value) }

    fun onDownstreamTypeChange(type: ProtectiveDeviceType) =
        update { it.copy(downstreamType = type) }

    fun onDownstreamRatingChange(value: String) =
        update(SelectivityField.DOWNSTREAM_RATING) { it.copy(downstreamRating = value) }

    fun onFaultCurrentChange(value: String) =
        update(SelectivityField.FAULT_CURRENT) { it.copy(faultCurrent = value) }

    private fun update(
        field: SelectivityField? = null,
        transform: (SelectivityUiState) -> SelectivityUiState,
    ) {
        _uiState.update { current ->
            transform(current).copy(
                result = null,
                steps = persistentListOf(),
                errors = if (field == null) current.errors else current.errors - field,
            )
        }
    }

    // -- Calculation --------------------------------------------------------------

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<SelectivityField, ValidationError>()

        fun validate(field: SelectivityField, raw: String, max: Double): Double? = NumericInput
            .validate(raw, min = 0.0, max = max)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val upstream = validate(SelectivityField.UPSTREAM_RATING, state.upstreamRating, MAX_RATING)
        val downstream =
            validate(SelectivityField.DOWNSTREAM_RATING, state.downstreamRating, MAX_RATING)
        val fault = validate(SelectivityField.FAULT_CURRENT, state.faultCurrent, MAX_FAULT)

        if (errors.isNotEmpty() || upstream == null || downstream == null || fault == null) {
            _uiState.update {
                it.copy(errors = errors, result = null, steps = persistentListOf())
            }
            return
        }

        val input = SelectivityInput(
            upstreamType = state.upstreamType,
            upstreamRatingAmps = upstream,
            downstreamType = state.downstreamType,
            downstreamRatingAmps = downstream,
            prospectiveFaultAmps = fault,
        )
        val result = checkSelectivity(input)

        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainSelectivity(input, result),
            )
        }

        saveToHistory(state, result)
    }

    fun onApplyExample(example: WorkedExample<SelectivityUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update { SelectivityUiState(isFavorite = it.isFavorite) }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.SELECTIVITY) }
    }

    fun onRestore(recordId: Long) {
        viewModelScope.launch {
            val record = historyRepository.findById(recordId) ?: return@launch
            if (record.calculatorId != CalculatorId.SELECTIVITY) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    upstreamType = inputs.enumOrNull<ProtectiveDeviceType>(KEY_UPSTREAM_TYPE)
                        ?: it.upstreamType,
                    upstreamRating = inputs.pick(KEY_UPSTREAM_RATING, it.upstreamRating),
                    downstreamType = inputs.enumOrNull<ProtectiveDeviceType>(KEY_DOWNSTREAM_TYPE)
                        ?: it.downstreamType,
                    downstreamRating = inputs.pick(KEY_DOWNSTREAM_RATING, it.downstreamRating),
                    faultCurrent = inputs.pick(KEY_FAULT, it.faultCurrent),
                )
            }
            onCalculate()
        }
    }

    private fun saveToHistory(state: SelectivityUiState, result: SelectivityResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.SELECTIVITY,
            title = stringResolver.get(R.string.sel_history_title)
                .format(state.upstreamRating, state.downstreamRating),
            summary = stringResolver.get(result.grade.summaryRes()),
            inputs = mapOf(
                KEY_UPSTREAM_TYPE to state.upstreamType.name,
                KEY_UPSTREAM_RATING to state.upstreamRating,
                KEY_DOWNSTREAM_TYPE to state.downstreamType.name,
                KEY_DOWNSTREAM_RATING to state.downstreamRating,
                KEY_FAULT to state.faultCurrent,
            ),
            results = buildMap {
                put(KEY_GRADE, stringResolver.get(result.grade.summaryRes()))
                put(KEY_RATIO, format(result.ratio))
                result.limitAmps?.let { put(KEY_LIMIT, format(it)) }
            },
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_RATING = 10_000.0
        const val MAX_FAULT = 200_000.0

        const val KEY_UPSTREAM_TYPE = "upstream_type"
        const val KEY_UPSTREAM_RATING = "upstream_rating"
        const val KEY_DOWNSTREAM_TYPE = "downstream_type"
        const val KEY_DOWNSTREAM_RATING = "downstream_rating"
        const val KEY_FAULT = "prospective_fault"

        const val KEY_GRADE = "grade"
        const val KEY_RATIO = "rating_ratio"
        const val KEY_LIMIT = "selectivity_limit"
    }
}
