package com.kemalurekli.electricalcalculator.features.calculators.motor.presentation

import androidx.compose.runtime.Immutable
import com.kemalurekli.electricalcalculator.core.ui.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
import com.kemalurekli.electricalcalculator.core.domain.model.PowerUnit
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.CalculateMotorCurrentUseCase
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.MotorInput
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.MotorResult
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Identifies a form field so validation errors can be routed back to it. */
enum class MotorField {
    POWER,
    VOLTAGE,
    EFFICIENCY,
    POWER_FACTOR,
    STARTING_RATIO,
}

@Immutable
data class MotorUiState(
    val system: SupplySystem = SupplySystem.THREE_PHASE_AC,
    val powerUnit: PowerUnit = PowerUnit.KILOWATT,
    val ratedPower: String = "",
    val voltage: String = SystemVoltageDefaults.forSystem(system).orEmpty(),
    /** True once the user has typed a voltage, after which it is never moved. */
    val voltageEdited: Boolean = false,
    val efficiency: String = DEFAULT_EFFICIENCY,
    val powerFactor: String = DEFAULT_POWER_FACTOR,
    val startingRatio: String = DEFAULT_STARTING_RATIO,
    val errors: Map<MotorField, ValidationError> = emptyMap(),
    val result: MotorResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    val showPowerFactor: Boolean get() = system.isAc

    companion object {
        /** Mid-range for an IE3 induction motor. */
        const val DEFAULT_EFFICIENCY = "90"

        /** Typical full-load cos φ for a squirrel cage motor. */
        const val DEFAULT_POWER_FACTOR = "0.85"

        /** Direct-on-line starting. */
        const val DEFAULT_STARTING_RATIO = "6"
    }
}

@HiltViewModel
class MotorViewModel @Inject constructor(
    private val calculateMotorCurrent: CalculateMotorCurrentUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotorUiState())
    val uiState: StateFlow<MotorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.MOTOR_CURRENT)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing -----------------------------------------------------------

    fun onSystemChange(system: SupplySystem) = update {
        it.copy(
            system = system,
            voltage = SystemVoltageDefaults.follow(it.voltage, system, it.voltageEdited),
        )
    }

    fun onPowerUnitChange(unit: PowerUnit) = update { it.copy(powerUnit = unit) }

    fun onPowerChange(value: String) = update(MotorField.POWER) { it.copy(ratedPower = value) }

    fun onVoltageChange(value: String) =
        update(MotorField.VOLTAGE) { it.copy(voltage = value, voltageEdited = true) }

    fun onEfficiencyChange(value: String) =
        update(MotorField.EFFICIENCY) { it.copy(efficiency = value) }

    fun onPowerFactorChange(value: String) =
        update(MotorField.POWER_FACTOR) { it.copy(powerFactor = value) }

    fun onStartingRatioChange(value: String) =
        update(MotorField.STARTING_RATIO) { it.copy(startingRatio = value) }

    private fun update(
        field: MotorField? = null,
        transform: (MotorUiState) -> MotorUiState,
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
        val errors = mutableMapOf<MotorField, ValidationError>()

        fun validate(
            field: MotorField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
        ): Double? = NumericInput
            .validate(raw, min, max)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val power = validate(MotorField.POWER, state.ratedPower, max = MAX_POWER)
        val voltage = validate(MotorField.VOLTAGE, state.voltage, max = MAX_VOLTAGE)
        val efficiency = validate(
            MotorField.EFFICIENCY,
            state.efficiency,
            min = MIN_EFFICIENCY_PERCENT,
            max = MAX_EFFICIENCY_PERCENT,
        )

        val powerFactor = if (state.system.isAc) {
            validate(MotorField.POWER_FACTOR, state.powerFactor, min = MIN_POWER_FACTOR, max = 1.0)
        } else {
            1.0
        }

        val startingRatio = validate(
            MotorField.STARTING_RATIO,
            state.startingRatio,
            min = 1.0,
            max = MAX_STARTING_RATIO,
        )

        if (errors.isNotEmpty() ||
            power == null || voltage == null || efficiency == null ||
            powerFactor == null || startingRatio == null
        ) {
            _uiState.update {
                it.copy(errors = errors, result = null, steps = persistentListOf())
            }
            return
        }

        val input = MotorInput(
            ratedPower = power,
            powerUnit = state.powerUnit,
            voltage = voltage,
            // Entered as a percentage, used as a fraction.
            efficiency = efficiency / PERCENT,
            powerFactor = powerFactor,
            startingCurrentRatio = startingRatio,
            system = state.system,
        )

        val result = calculateMotorCurrent(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainMotorCurrent(input, result),
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
    fun onApplyExample(example: WorkedExample<MotorUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update {
            MotorUiState(
                system = it.system,
                voltage = SystemVoltageDefaults.forSystem(it.system)
                    ?: it.voltage,
                voltageEdited = false,
                powerUnit = it.powerUnit,
                isFavorite = it.isFavorite,
            )
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.MOTOR_CURRENT) }
    }

    private fun saveToHistory(state: MotorUiState, result: MotorResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.MOTOR_CURRENT,
            title = stringResolver.get(R.string.mt_history_title)
                .format("${state.ratedPower} ${state.powerUnit.name}", state.voltage),
            summary = stringResolver.get(R.string.mt_history_summary)
                .format(format(result.fullLoadCurrent)),
            inputs = mapOf(
                KEY_SYSTEM to state.system.name,
                KEY_POWER_UNIT to state.powerUnit.name,
                KEY_POWER to state.ratedPower,
                KEY_VOLTAGE to state.voltage,
                KEY_EFFICIENCY to state.efficiency,
                KEY_POWER_FACTOR to state.powerFactor,
                KEY_STARTING_RATIO to state.startingRatio,
            ),
            results = mapOf(
                KEY_CURRENT to format(result.fullLoadCurrent),
                KEY_STARTING_CURRENT to format(result.startingCurrent),
                KEY_INPUT_POWER to format(result.inputPowerWatts / WATTS_PER_KW),
                KEY_APPARENT_POWER to format(result.apparentPowerVa / WATTS_PER_KW),
                KEY_LOSSES to format(result.lossesWatts / WATTS_PER_KW),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val PERCENT = 100.0
        const val WATTS_PER_KW = 1_000.0
        const val MAX_POWER = 100_000.0
        const val MAX_VOLTAGE = 100_000.0
        const val MIN_EFFICIENCY_PERCENT = 1.0
        const val MAX_EFFICIENCY_PERCENT = 100.0
        const val MIN_POWER_FACTOR = 0.01
        const val MAX_STARTING_RATIO = 20.0

        const val KEY_SYSTEM = "system"
        const val KEY_POWER_UNIT = "power_unit"
        const val KEY_POWER = "rated_power"
        const val KEY_VOLTAGE = "voltage"
        const val KEY_EFFICIENCY = "efficiency_percent"
        const val KEY_POWER_FACTOR = "power_factor"
        const val KEY_STARTING_RATIO = "starting_ratio"

        const val KEY_CURRENT = "full_load_current"
        const val KEY_STARTING_CURRENT = "starting_current"
        const val KEY_INPUT_POWER = "input_power_kw"
        const val KEY_APPARENT_POWER = "apparent_power_kva"
        const val KEY_LOSSES = "losses_kw"
    }
}
