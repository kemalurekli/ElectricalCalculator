package com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.common.util.pick
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.CalculateHarmonicsUseCase
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicComponent
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicsInput
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The orders the form asks for.
 *
 * Odd, and up to the thirteenth — which is where a spectrum from ordinary
 * equipment has run out of anything worth designing against. Even orders are
 * left out because a symmetrical waveform has none, and the ones a meter does
 * show usually mean a failing device rather than a load to size for.
 */
internal val HARMONIC_ORDERS = listOf(3, 5, 7, 9, 11, 13)

@Immutable
data class HarmonicsUiState(
    val fundamental: String = "",
    /** Percentage of fundamental, keyed by harmonic order. Blank means absent. */
    val magnitudes: Map<Int, String> = HARMONIC_ORDERS.associateWith { "" },
    val balanced: Boolean = true,
    val errors: Map<Int, ValidationError> = emptyMap(),
    val fundamentalError: ValidationError? = null,
    val result: HarmonicsResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
)

@HiltViewModel
class HarmonicsViewModel @Inject constructor(
    private val calculateHarmonics: CalculateHarmonicsUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HarmonicsUiState())
    val uiState: StateFlow<HarmonicsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.HARMONICS)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    fun onFundamentalChange(value: String) = update {
        it.copy(fundamental = value, fundamentalError = null)
    }

    fun onMagnitudeChange(order: Int, value: String) = update {
        it.copy(magnitudes = it.magnitudes + (order to value), errors = it.errors - order)
    }

    fun onBalancedChange(balanced: Boolean) = update { it.copy(balanced = balanced) }

    private fun update(transform: (HarmonicsUiState) -> HarmonicsUiState) {
        _uiState.update { transform(it).copy(result = null, steps = persistentListOf()) }
    }

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<Int, ValidationError>()

        val fundamental = NumericInput.validate(state.fundamental, min = 0.0, max = MAX_CURRENT)
        val fundamentalValue = (fundamental as? Outcome.Success)?.value

        val components = mutableListOf<HarmonicComponent>()
        HARMONIC_ORDERS.forEach { order ->
            val raw = state.magnitudes[order].orEmpty()
            // A blank order is one the meter did not report, not a zero the
            // user asserted. Both give the same arithmetic; only one of them
            // should be an error when left empty.
            if (raw.isBlank()) return@forEach
            when (val outcome = NumericInput.validate(raw, min = 0.0, max = MAX_PERCENT, allowZero = true)) {
                is Outcome.Success -> components += HarmonicComponent(order, outcome.value)
                is Outcome.Failure -> errors[order] = outcome.error
            }
        }

        if (errors.isNotEmpty() || fundamentalValue == null) {
            _uiState.update {
                it.copy(
                    errors = errors,
                    fundamentalError = (fundamental as? Outcome.Failure)?.error,
                    result = null,
                    steps = persistentListOf(),
                )
            }
            return
        }

        val input = HarmonicsInput(
            fundamentalAmps = fundamentalValue,
            components = components,
            balanced = state.balanced,
        )
        val result = calculateHarmonics(input)

        _uiState.update {
            it.copy(
                errors = emptyMap(),
                fundamentalError = null,
                result = result,
                steps = explainHarmonics(input, result),
            )
        }

        saveToHistory(state, result)
    }

    fun onApplyExample(example: WorkedExample<HarmonicsUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update { HarmonicsUiState(isFavorite = it.isFavorite) }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.HARMONICS) }
    }

    fun onRestore(recordId: Long) {
        viewModelScope.launch {
            val record = historyRepository.findById(recordId) ?: return@launch
            if (record.calculatorId != CalculatorId.HARMONICS) return@launch
            val inputs = record.inputs
            _uiState.update { state ->
                state.copy(
                    fundamental = inputs.pick(KEY_FUNDAMENTAL, state.fundamental),
                    magnitudes = HARMONIC_ORDERS.associateWith { order ->
                        inputs.pick("h$order", state.magnitudes[order].orEmpty())
                    },
                    balanced = inputs.pick(KEY_BALANCED, state.balanced.toString()).toBoolean(),
                )
            }
            onCalculate()
        }
    }

    private fun saveToHistory(state: HarmonicsUiState, result: HarmonicsResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.HARMONICS,
            title = stringResolver.get(R.string.hm_history_title).format(state.fundamental),
            summary = stringResolver.get(R.string.hm_history_summary)
                .format(format(result.thdPercent)),
            inputs = buildMap {
                put(KEY_FUNDAMENTAL, state.fundamental)
                put(KEY_BALANCED, state.balanced.toString())
                HARMONIC_ORDERS.forEach { put("h$it", state.magnitudes[it].orEmpty()) }
            },
            results = mapOf(
                KEY_THD to format(result.thdPercent),
                KEY_RMS to format(result.rmsAmps),
                KEY_NEUTRAL to format(result.neutralAmps),
                KEY_K to format(result.kFactor),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_CURRENT = 100_000.0
        const val MAX_PERCENT = 200.0

        const val KEY_FUNDAMENTAL = "fundamental"
        const val KEY_BALANCED = "balanced"
        const val KEY_THD = "thd_percent"
        const val KEY_RMS = "rms_amps"
        const val KEY_NEUTRAL = "neutral_amps"
        const val KEY_K = "k_factor"
    }
}
