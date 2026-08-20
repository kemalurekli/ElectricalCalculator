package com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation

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
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.CalculateMotorStartingUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingInput
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingResult
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.StartingMethod
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
enum class MotorStartingField {
    FULL_LOAD_CURRENT,
    LOCKED_ROTOR_MULTIPLE,
    SUPPLY_VOLTAGE,
    TRANSFORMER_KVA,
    TRANSFORMER_IMPEDANCE,
}

@Immutable
data class MotorStartingUiState(
    val fullLoadCurrent: String = "",
    val lockedRotorMultiple: String = DEFAULT_LOCKED_ROTOR,
    val method: StartingMethod = StartingMethod.DIRECT_ON_LINE,
    val supplyVoltage: String = "",
    val voltageEdited: Boolean = false,
    val transformerKva: String = "",
    val transformerImpedance: String = DEFAULT_IMPEDANCE,
    val errors: Map<MotorStartingField, ValidationError> = emptyMap(),
    val result: MotorStartingResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    companion object {
        /**
         * Six times full load, which is what a plate usually says.
         *
         * Seven and eight are not unusual, and the figure drives the whole
         * answer — so it is offered as a starting point and asked for rather
         * than assumed.
         */
        const val DEFAULT_LOCKED_ROTOR = "6"

        /** Four per cent is the usual uk for distribution transformers up to about 630 kVA. */
        const val DEFAULT_IMPEDANCE = "4"
    }
}

@HiltViewModel
class MotorStartingViewModel @Inject constructor(
    private val calculateStarting: CalculateMotorStartingUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: ResourceIdResolver,
    private val timeProvider: TimeProvider,
    private val userPreferences: com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotorStartingUiState())
    val uiState: StateFlow<MotorStartingUiState> = _uiState.asStateFlow()

    private var formTouched = false

    init {
        viewModelScope.launch {
            val defaults = userPreferences.engineeringDefaults()
            if (!formTouched) {
                _uiState.update { it.copy(supplyVoltage = defaults.threePhaseVoltage) }
            }
        }
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.MOTOR_STARTING)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing -----------------------------------------------------------

    fun onFullLoadCurrentChange(value: String) =
        update(MotorStartingField.FULL_LOAD_CURRENT) { it.copy(fullLoadCurrent = value) }

    fun onLockedRotorChange(value: String) =
        update(MotorStartingField.LOCKED_ROTOR_MULTIPLE) { it.copy(lockedRotorMultiple = value) }

    fun onMethodChange(method: StartingMethod) = update { it.copy(method = method) }

    fun onSupplyVoltageChange(value: String) =
        update(MotorStartingField.SUPPLY_VOLTAGE) {
            it.copy(supplyVoltage = value, voltageEdited = true)
        }

    fun onTransformerKvaChange(value: String) =
        update(MotorStartingField.TRANSFORMER_KVA) { it.copy(transformerKva = value) }

    fun onTransformerImpedanceChange(value: String) =
        update(MotorStartingField.TRANSFORMER_IMPEDANCE) { it.copy(transformerImpedance = value) }

    private fun update(
        field: MotorStartingField? = null,
        transform: (MotorStartingUiState) -> MotorStartingUiState,
    ) {
        formTouched = true
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
        val errors = mutableMapOf<MotorStartingField, ValidationError>()

        fun validate(
            field: MotorStartingField,
            raw: String,
            max: Double,
            allowZero: Boolean = false,
        ): Double? = NumericInput
            .validate(raw, min = 0.0, max = max, allowZero = allowZero)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val current = validate(MotorStartingField.FULL_LOAD_CURRENT, state.fullLoadCurrent, MAX_CURRENT)
        val multiple =
            validate(MotorStartingField.LOCKED_ROTOR_MULTIPLE, state.lockedRotorMultiple, MAX_MULTIPLE)
        val voltage = validate(MotorStartingField.SUPPLY_VOLTAGE, state.supplyVoltage, MAX_VOLTAGE)
        val kva = validate(MotorStartingField.TRANSFORMER_KVA, state.transformerKva, MAX_KVA)
        // Zero is allowed: it is how a reader says "assume an infinite bus",
        // and the use case answers that question rather than dividing by it.
        val impedance = validate(
            MotorStartingField.TRANSFORMER_IMPEDANCE,
            state.transformerImpedance,
            MAX_IMPEDANCE,
            allowZero = true,
        )

        if (errors.isNotEmpty() || current == null || multiple == null || voltage == null ||
            kva == null || impedance == null
        ) {
            _uiState.update { it.copy(errors = errors, result = null, steps = persistentListOf()) }
            return
        }

        val input = MotorStartingInput(
            fullLoadCurrentAmps = current,
            lockedRotorMultiple = multiple,
            method = state.method,
            supplyVoltage = voltage,
            transformerKva = kva,
            transformerImpedancePercent = impedance,
        )
        val result = calculateStarting(input)

        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainMotorStarting(input, result),
            )
        }

        saveToHistory(state, result)
    }

    fun onApplyExample(example: WorkedExample<MotorStartingUiState>) {
        formTouched = true
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update {
            MotorStartingUiState(isFavorite = it.isFavorite, supplyVoltage = it.supplyVoltage)
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.MOTOR_STARTING) }
    }

    fun onRestore(recordId: Long) {
        formTouched = true
        viewModelScope.launch {
            val record = historyRepository.findById(recordId) ?: return@launch
            if (record.calculatorId != CalculatorId.MOTOR_STARTING) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    fullLoadCurrent = inputs.pick(KEY_CURRENT, it.fullLoadCurrent),
                    lockedRotorMultiple = inputs.pick(KEY_MULTIPLE, it.lockedRotorMultiple),
                    method = inputs.enumOrNull<StartingMethod>(KEY_METHOD) ?: it.method,
                    supplyVoltage = inputs.pick(KEY_VOLTAGE, it.supplyVoltage),
                    voltageEdited = true,
                    transformerKva = inputs.pick(KEY_KVA, it.transformerKva),
                    transformerImpedance = inputs.pick(KEY_IMPEDANCE, it.transformerImpedance),
                )
            }
            onCalculate()
        }
    }

    private fun saveToHistory(state: MotorStartingUiState, result: MotorStartingResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.MOTOR_STARTING,
            title = stringResolver.get(R.string.ms_history_title)
                .format(state.fullLoadCurrent, state.transformerKva),
            summary = stringResolver.get(R.string.ms_history_summary)
                .format(format(result.dipPercent)),
            inputs = mapOf(
                KEY_CURRENT to state.fullLoadCurrent,
                KEY_MULTIPLE to state.lockedRotorMultiple,
                KEY_METHOD to state.method.name,
                KEY_VOLTAGE to state.supplyVoltage,
                KEY_KVA to state.transformerKva,
                KEY_IMPEDANCE to state.transformerImpedance,
            ),
            results = mapOf(
                KEY_DIP to format(result.dipPercent),
                KEY_RESIDUAL to format(result.residualVoltage),
                KEY_STARTING_CURRENT to format(result.startingCurrentAmps),
                KEY_TORQUE to format(result.startingTorquePercent),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_CURRENT = 10_000.0
        const val MAX_MULTIPLE = 20.0
        const val MAX_VOLTAGE = 100_000.0
        const val MAX_KVA = 100_000.0
        const val MAX_IMPEDANCE = 50.0

        const val KEY_CURRENT = "full_load_current"
        const val KEY_MULTIPLE = "locked_rotor_multiple"
        const val KEY_METHOD = "starting_method"
        const val KEY_VOLTAGE = "supply_voltage"
        const val KEY_KVA = "transformer_kva"
        const val KEY_IMPEDANCE = "transformer_impedance"

        const val KEY_DIP = "dip_percent"
        const val KEY_RESIDUAL = "residual_voltage"
        const val KEY_STARTING_CURRENT = "starting_current"
        const val KEY_TORQUE = "starting_torque"
    }
}
