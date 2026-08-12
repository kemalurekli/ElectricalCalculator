package com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation

import androidx.compose.runtime.Immutable
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.ui.model.SystemVoltageDefaults
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.CapacitorConnection
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.CalculatePowerFactorCorrectionUseCase
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.PowerFactorInput
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.PowerFactorResult
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
enum class PowerFactorField {
    ACTIVE_POWER,
    EXISTING_FACTOR,
    TARGET_FACTOR,
    VOLTAGE,
    FREQUENCY,
}

@Immutable
data class PowerFactorUiState(
    val system: SupplySystem = SupplySystem.THREE_PHASE_AC,
    val connection: CapacitorConnection = CapacitorConnection.DELTA,
    val activePowerKw: String = "",
    val existingFactor: String = "",
    val targetFactor: String = DEFAULT_TARGET,
    val voltage: String = SystemVoltageDefaults.forSystem(system).orEmpty(),
    /** True once the user has typed a voltage, after which it is never moved. */
    val voltageEdited: Boolean = false,
    val frequency: String = DEFAULT_FREQUENCY,
    val errors: Map<PowerFactorField, ValidationError> = emptyMap(),
    val result: PowerFactorResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    /** Star versus delta only exists on a three-phase bank. */
    val showConnection: Boolean get() = system == SupplySystem.THREE_PHASE_AC

    companion object {
        /** A common tariff threshold, and a sensible engineering target. */
        const val DEFAULT_TARGET = "0.95"

        const val DEFAULT_FREQUENCY = "50"
    }
}

@HiltViewModel
class PowerFactorViewModel @Inject constructor(
    private val calculateCorrection: CalculatePowerFactorCorrectionUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PowerFactorUiState())
    val uiState: StateFlow<PowerFactorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.POWER_FACTOR_CORRECTION)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing --------------------------------------------------------------

    fun onSystemChange(system: SupplySystem) = update {
        it.copy(
            system = system,
            voltage = SystemVoltageDefaults.follow(it.voltage, system, it.voltageEdited),
        )
    }

    fun onConnectionChange(connection: CapacitorConnection) =
        update { it.copy(connection = connection) }

    fun onActivePowerChange(value: String) =
        update(PowerFactorField.ACTIVE_POWER) { it.copy(activePowerKw = value) }

    fun onExistingFactorChange(value: String) =
        update(PowerFactorField.EXISTING_FACTOR) { it.copy(existingFactor = value) }

    fun onTargetFactorChange(value: String) =
        update(PowerFactorField.TARGET_FACTOR) { it.copy(targetFactor = value) }

    fun onVoltageChange(value: String) =
        update(PowerFactorField.VOLTAGE) { it.copy(voltage = value, voltageEdited = true) }

    fun onFrequencyChange(value: String) =
        update(PowerFactorField.FREQUENCY) { it.copy(frequency = value) }

    private fun update(
        field: PowerFactorField? = null,
        transform: (PowerFactorUiState) -> PowerFactorUiState,
    ) {
        _uiState.update { current ->
            transform(current).copy(
                result = null,
                steps = persistentListOf(),
                errors = if (field == null) current.errors else current.errors - field,
            )
        }
    }

    // -- Calculation ------------------------------------------------------------------

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<PowerFactorField, ValidationError>()

        fun validate(
            field: PowerFactorField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
        ): Double? = NumericInput
            .validate(raw, min, max)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val activePower = validate(PowerFactorField.ACTIVE_POWER, state.activePowerKw, max = MAX_POWER_KW)
        val existing = validate(
            PowerFactorField.EXISTING_FACTOR,
            state.existingFactor,
            min = MIN_FACTOR,
            max = 1.0,
        )
        val target = validate(PowerFactorField.TARGET_FACTOR, state.targetFactor, min = MIN_FACTOR, max = 1.0)
        val voltage = validate(PowerFactorField.VOLTAGE, state.voltage, max = MAX_VOLTAGE)
        val frequency = validate(PowerFactorField.FREQUENCY, state.frequency, min = MIN_FREQUENCY, max = MAX_FREQUENCY)

        // Correcting downward is not a thing: a target below the existing factor
        // would ask for a negative capacitor. Caught here rather than returning
        // a nonsensical result.
        if (existing != null && target != null && target < existing) {
            errors[PowerFactorField.TARGET_FACTOR] = ValidationError.BelowMinimum(existing)
        }

        if (errors.isNotEmpty() ||
            activePower == null || existing == null || target == null ||
            voltage == null || frequency == null
        ) {
            _uiState.update { it.copy(errors = errors, result = null, steps = persistentListOf()) }
            return
        }

        val input = PowerFactorInput(
            activePowerWatts = activePower * WATTS_PER_KW,
            existingPowerFactor = existing,
            targetPowerFactor = target,
            voltage = voltage,
            frequencyHz = frequency,
            connection = state.connection,
            system = state.system,
        )

        val result = calculateCorrection(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainPowerFactor(input, result),
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
    fun onApplyExample(example: WorkedExample<PowerFactorUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update {
            PowerFactorUiState(
                system = it.system,
                voltage = SystemVoltageDefaults.forSystem(it.system)
                    ?: it.voltage,
                voltageEdited = false,
                connection = it.connection,
                isFavorite = it.isFavorite,
            )
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.POWER_FACTOR_CORRECTION) }
    }

    private fun saveToHistory(state: PowerFactorUiState, result: PowerFactorResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.POWER_FACTOR_CORRECTION,
            title = stringResolver.get(R.string.pf_history_title)
                .format(state.existingFactor, state.targetFactor),
            summary = stringResolver.get(R.string.pf_history_summary)
                .format(format(result.requiredCapacitorVar / WATTS_PER_KW)),
            inputs = mapOf(
                KEY_SYSTEM to state.system.name,
                KEY_CONNECTION to state.connection.name,
                KEY_ACTIVE_POWER to state.activePowerKw,
                KEY_EXISTING to state.existingFactor,
                KEY_TARGET to state.targetFactor,
                KEY_VOLTAGE to state.voltage,
                KEY_FREQUENCY to state.frequency,
            ),
            results = mapOf(
                KEY_CAPACITOR_KVAR to format(result.requiredCapacitorVar / WATTS_PER_KW),
                KEY_CAPACITANCE_UF to format(result.capacitancePerPhaseFarads * MICRO),
                KEY_RELEASED_KVA to format(result.releasedCapacityVa / WATTS_PER_KW),
                KEY_CURRENT_BEFORE to format(result.currentBeforeAmps),
                KEY_CURRENT_AFTER to format(result.currentAfterAmps),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val WATTS_PER_KW = 1_000.0
        const val MICRO = 1_000_000.0
        const val MAX_POWER_KW = 1_000_000.0
        const val MAX_VOLTAGE = 1_000_000.0
        const val MIN_FACTOR = 0.05
        const val MIN_FREQUENCY = 1.0
        const val MAX_FREQUENCY = 1_000.0

        const val KEY_SYSTEM = "system"
        const val KEY_CONNECTION = "connection"
        const val KEY_ACTIVE_POWER = "active_power_kw"
        const val KEY_EXISTING = "existing_power_factor"
        const val KEY_TARGET = "target_power_factor"
        const val KEY_VOLTAGE = "voltage"
        const val KEY_FREQUENCY = "frequency_hz"

        const val KEY_CAPACITOR_KVAR = "capacitor_kvar"
        const val KEY_CAPACITANCE_UF = "capacitance_uf"
        const val KEY_RELEASED_KVA = "released_kva"
        const val KEY_CURRENT_BEFORE = "current_before"
        const val KEY_CURRENT_AFTER = "current_after"
    }
}
