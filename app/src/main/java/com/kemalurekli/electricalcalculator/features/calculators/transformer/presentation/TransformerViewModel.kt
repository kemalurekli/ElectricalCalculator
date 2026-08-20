package com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation

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
import com.kemalurekli.electricalcalculator.core.common.util.ResourceIdResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.CalculateTransformerCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.TransformerInput
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.TransformerResult
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
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
enum class TransformerField {
    RATING,
    PRIMARY_VOLTAGE,
    SECONDARY_VOLTAGE,
    IMPEDANCE,
}

@Immutable
data class TransformerUiState(
    val system: SupplySystem = SupplySystem.THREE_PHASE_AC,
    val ratingKva: String = "",
    val primaryVoltage: String = "",
    val secondaryVoltage: String = "",
    val impedancePercent: String = DEFAULT_IMPEDANCE,
    val errors: Map<TransformerField, ValidationError> = emptyMap(),
    val result: TransformerResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    companion object {
        /** Typical for distribution transformers above 630 kVA. */
        const val DEFAULT_IMPEDANCE = "6"
    }
}

@HiltViewModel
class TransformerViewModel @Inject constructor(
    private val calculateTransformerCurrent: CalculateTransformerCurrentUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: ResourceIdResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransformerUiState())
    val uiState: StateFlow<TransformerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.TRANSFORMER_CURRENT)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing ---------------------------------------------------------

    fun onSystemChange(system: SupplySystem) = update { it.copy(system = system) }

    fun onRatingChange(value: String) = update(TransformerField.RATING) { it.copy(ratingKva = value) }

    fun onPrimaryVoltageChange(value: String) =
        update(TransformerField.PRIMARY_VOLTAGE) { it.copy(primaryVoltage = value) }

    fun onSecondaryVoltageChange(value: String) =
        update(TransformerField.SECONDARY_VOLTAGE) { it.copy(secondaryVoltage = value) }

    fun onImpedanceChange(value: String) =
        update(TransformerField.IMPEDANCE) { it.copy(impedancePercent = value) }

    private fun update(
        field: TransformerField? = null,
        transform: (TransformerUiState) -> TransformerUiState,
    ) {
        _uiState.update { current ->
            transform(current).copy(
                result = null,
                steps = persistentListOf(),
                errors = if (field == null) current.errors else current.errors - field,
            )
        }
    }

    // -- Calculation -------------------------------------------------------------

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<TransformerField, ValidationError>()

        fun validate(
            field: TransformerField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
        ): Double? = NumericInput
            .validate(raw, min, max)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val rating = validate(TransformerField.RATING, state.ratingKva, max = MAX_RATING_KVA)
        val primary = validate(TransformerField.PRIMARY_VOLTAGE, state.primaryVoltage, max = MAX_VOLTAGE)
        val secondary = validate(TransformerField.SECONDARY_VOLTAGE, state.secondaryVoltage, max = MAX_VOLTAGE)
        val impedance = validate(
            TransformerField.IMPEDANCE,
            state.impedancePercent,
            min = MIN_IMPEDANCE,
            max = MAX_IMPEDANCE,
        )

        if (errors.isNotEmpty() ||
            rating == null || primary == null || secondary == null || impedance == null
        ) {
            _uiState.update { it.copy(errors = errors, result = null, steps = persistentListOf()) }
            return
        }

        val input = TransformerInput(
            ratingKva = rating,
            primaryVoltage = primary,
            secondaryVoltage = secondary,
            impedanceVoltagePercent = impedance,
            system = state.system,
        )

        val result = calculateTransformerCurrent(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainTransformer(input, result),
            )
        }

        saveToHistory(state, input, result)
    }

    /**
     * Loads a worked example and runs it.
     *
     * Calculating immediately is the point: one tap produces a filled form, a
     * result and the arithmetic between them, which is a complete worked example
     * rather than a form the reader still has to submit.
     */
    fun onApplyExample(example: WorkedExample<TransformerUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update { TransformerUiState(system = it.system, isFavorite = it.isFavorite) }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.TRANSFORMER_CURRENT) }
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
            if (record.calculatorId != CalculatorId.TRANSFORMER_CURRENT) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    system = inputs.enumOrNull(KEY_SYSTEM) ?: it.system,
                    ratingKva = inputs.pick(KEY_RATING, it.ratingKva),
                    primaryVoltage = inputs.pick(KEY_PRIMARY, it.primaryVoltage),
                    secondaryVoltage = inputs.pick(KEY_SECONDARY, it.secondaryVoltage),
                    impedancePercent = inputs.pick(KEY_IMPEDANCE, it.impedancePercent),
                )
            }
            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(
        state: TransformerUiState,
        input: TransformerInput,
        result: TransformerResult,
    ) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.TRANSFORMER_CURRENT,
            title = stringResolver.get(R.string.tx_history_title)
                .format(
                    format(input.ratingKva),
                    format(input.primaryVoltage),
                    format(input.secondaryVoltage),
                ),
            summary = stringResolver.get(R.string.tx_history_summary)
                .format(format(result.secondaryCurrent)),
            inputs = mapOf(
                KEY_SYSTEM to state.system.name,
                KEY_RATING to state.ratingKva,
                KEY_PRIMARY to state.primaryVoltage,
                KEY_SECONDARY to state.secondaryVoltage,
                KEY_IMPEDANCE to state.impedancePercent,
            ),
            results = mapOf(
                KEY_SECONDARY_CURRENT to format(result.secondaryCurrent),
                KEY_PRIMARY_CURRENT to format(result.primaryCurrent),
                KEY_RATIO to format(result.voltageRatio),
                KEY_SHORT_CIRCUIT to format(result.secondaryShortCircuitCurrent),
                KEY_SHORT_CIRCUIT_POWER to format(result.shortCircuitPowerKva),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_RATING_KVA = 1_000_000.0
        const val MAX_VOLTAGE = 1_000_000.0
        const val MIN_IMPEDANCE = 0.1
        const val MAX_IMPEDANCE = 50.0

        const val KEY_SYSTEM = "system"
        const val KEY_RATING = "rating_kva"
        const val KEY_PRIMARY = "primary_voltage"
        const val KEY_SECONDARY = "secondary_voltage"
        const val KEY_IMPEDANCE = "impedance_percent"

        const val KEY_SECONDARY_CURRENT = "secondary_current"
        const val KEY_PRIMARY_CURRENT = "primary_current"
        const val KEY_RATIO = "voltage_ratio"
        const val KEY_SHORT_CIRCUIT = "short_circuit_current"
        const val KEY_SHORT_CIRCUIT_POWER = "short_circuit_power"
    }
}
