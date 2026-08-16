package com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.pick
import com.kemalurekli.electricalcalculator.core.common.util.enumOrNull
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.CalculateNeutralCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.NeutralCurrentInput
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.NeutralCurrentResult
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
enum class NeutralCurrentField {
    LINE_1,
    LINE_2,
    LINE_3,
    THIRD_HARMONIC,
}

@Immutable
data class NeutralCurrentUiState(
    val line1: String = "",
    val line2: String = "",
    val line3: String = "",
    val thirdHarmonic: String = DEFAULT_THIRD_HARMONIC,
    val errors: Map<NeutralCurrentField, ValidationError> = emptyMap(),
    val result: NeutralCurrentResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    companion object {
        /**
         * Zero, so the calculator opens on the textbook case.
         *
         * Unlike efficiency or ambient temperature there is no "usual" harmonic
         * content — it is a property of the load, not of the installation — so
         * the default is the one that assumes nothing.
         */
        const val DEFAULT_THIRD_HARMONIC = "0"
    }
}

@HiltViewModel
class NeutralCurrentViewModel @Inject constructor(
    private val calculateNeutral: CalculateNeutralCurrentUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NeutralCurrentUiState())
    val uiState: StateFlow<NeutralCurrentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.NEUTRAL_CURRENT)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing -----------------------------------------------------------

    fun onLine1Change(value: String) =
        update(NeutralCurrentField.LINE_1) { it.copy(line1 = value) }

    fun onLine2Change(value: String) =
        update(NeutralCurrentField.LINE_2) { it.copy(line2 = value) }

    fun onLine3Change(value: String) =
        update(NeutralCurrentField.LINE_3) { it.copy(line3 = value) }

    fun onThirdHarmonicChange(value: String) =
        update(NeutralCurrentField.THIRD_HARMONIC) { it.copy(thirdHarmonic = value) }

    private fun update(
        field: NeutralCurrentField? = null,
        transform: (NeutralCurrentUiState) -> NeutralCurrentUiState,
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
        val errors = mutableMapOf<NeutralCurrentField, ValidationError>()

        fun validate(
            field: NeutralCurrentField,
            raw: String,
            max: Double,
        ): Double? = NumericInput
            // A phase carrying nothing is an ordinary state of affairs on an
            // unbalanced board, so zero is accepted here rather than rejected.
            .validate(raw, min = 0.0, max = max, allowZero = true)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val i1 = validate(NeutralCurrentField.LINE_1, state.line1, MAX_CURRENT)
        val i2 = validate(NeutralCurrentField.LINE_2, state.line2, MAX_CURRENT)
        val i3 = validate(NeutralCurrentField.LINE_3, state.line3, MAX_CURRENT)
        val harmonic = validate(
            NeutralCurrentField.THIRD_HARMONIC,
            state.thirdHarmonic,
            MAX_HARMONIC_PERCENT,
        )

        if (errors.isNotEmpty() || i1 == null || i2 == null || i3 == null || harmonic == null) {
            _uiState.update {
                it.copy(errors = errors, result = null, steps = persistentListOf())
            }
            return
        }

        val input = NeutralCurrentInput(
            lineCurrents = Triple(i1, i2, i3),
            thirdHarmonicPercent = harmonic,
        )
        val result = calculateNeutral(input)

        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainNeutralCurrent(input, result),
            )
        }

        saveToHistory(state, result)
    }

    /**
     * Loads a worked example and runs it.
     *
     * Calculating immediately is the point: one tap produces a filled form, a
     * result and the arithmetic between them, which is a complete worked example
     * rather than a form the reader still has to submit.
     */
    fun onApplyExample(example: WorkedExample<NeutralCurrentUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update { NeutralCurrentUiState(isFavorite = it.isFavorite) }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.NEUTRAL_CURRENT) }
    }

    /**
     * Reloads a saved calculation into the form.
     *
     * The record stores the text the reader typed rather than the parsed number,
     * so the form comes back exactly as it was left — see [CalculationRecord].
     * A key the record does not carry keeps the form's current value, which is
     * what lets a record written before a field existed still open.
     */
    fun onRestore(recordId: Long) {
        viewModelScope.launch {
            val record = historyRepository.findById(recordId) ?: return@launch
            if (record.calculatorId != CalculatorId.NEUTRAL_CURRENT) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    line1 = inputs.pick(KEY_L1, it.line1),
                    line2 = inputs.pick(KEY_L2, it.line2),
                    line3 = inputs.pick(KEY_L3, it.line3),
                    thirdHarmonic = inputs.pick(KEY_HARMONIC, it.thirdHarmonic),
                )
            }
            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: NeutralCurrentUiState, result: NeutralCurrentResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.NEUTRAL_CURRENT,
            title = stringResolver.get(R.string.nc_history_title)
                .format(state.line1, state.line2, state.line3),
            summary = stringResolver.get(R.string.nc_history_summary)
                .format(format(result.neutralCurrentAmps)),
            inputs = mapOf(
                KEY_L1 to state.line1,
                KEY_L2 to state.line2,
                KEY_L3 to state.line3,
                KEY_HARMONIC to state.thirdHarmonic,
            ),
            results = mapOf(
                KEY_NEUTRAL to format(result.neutralCurrentAmps),
                KEY_FUNDAMENTAL to format(result.fundamentalNeutralAmps),
                KEY_TRIPLEN to format(result.triplenNeutralAmps),
                KEY_RATIO to format(result.neutralToHighestLineRatio),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_CURRENT = 100_000.0
        const val MAX_HARMONIC_PERCENT = 200.0

        const val KEY_L1 = "line_1"
        const val KEY_L2 = "line_2"
        const val KEY_L3 = "line_3"
        const val KEY_HARMONIC = "third_harmonic_percent"

        const val KEY_NEUTRAL = "neutral_current"
        const val KEY_FUNDAMENTAL = "fundamental_component"
        const val KEY_TRIPLEN = "triplen_component"
        const val KEY_RATIO = "neutral_to_line_ratio"
    }
}
