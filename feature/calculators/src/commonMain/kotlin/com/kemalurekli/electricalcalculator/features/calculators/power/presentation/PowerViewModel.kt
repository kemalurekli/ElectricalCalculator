package com.kemalurekli.electricalcalculator.features.calculators.power.presentation

import androidx.compose.runtime.Immutable
import com.kemalurekli.electricalcalculator.core.domain.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.kemalurekli.electricalcalculator.core.domain.model.PowerFactorType
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.CalculatePowerUseCase
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.PowerInput
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.PowerResult
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_history_summary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_history_title

/** Identifies a form field so validation errors can be routed back to it. */
enum class PowerField {
    VOLTAGE,
    CURRENT,
    POWER_FACTOR,
}

@Immutable
data class PowerUiState(
    val system: SupplySystem = SupplySystem.THREE_PHASE_AC,
    val powerFactorType: PowerFactorType = PowerFactorType.LAGGING,
    val voltage: String = SystemVoltageDefaults.forSystem(system).orEmpty(),
    /** True once the user has typed a voltage, after which it is never moved. */
    val voltageEdited: Boolean = false,
    val current: String = "",
    val powerFactor: String = DEFAULT_POWER_FACTOR,
    val errors: Map<PowerField, ValidationError> = emptyMap(),
    val result: PowerResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    val showPowerFactor: Boolean get() = system.isAc

    companion object {
        /** Typical mixed industrial load. */
        const val DEFAULT_POWER_FACTOR = "0.85"
    }
}

class PowerViewModel(
    private val calculatePower: CalculatePowerUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PowerUiState())
    val uiState: StateFlow<PowerUiState> = _uiState.asStateFlow()

    /**
     * The user's settings, read once when the calculator opens.
     *
     * Held rather than observed on purpose: a form must not be rewritten
     * underneath the user because Settings changed while this screen sat on the
     * back stack.
     */
    private var defaults = EngineeringDefaults.Default

    /**
     * Whether anything has yet written to the form.
     *
     * The defaults are read asynchronously, so they can in principle land after
     * the first edit, after an example has been applied, or after a saved
     * calculation has been reopened. In each of those cases the form already
     * says something more specific than a default and must be left alone. The
     * flag is set at the *call*, ahead of any suspension, so ordering between
     * the two coroutines cannot decide the outcome.
     */
    private var formTouched = false

    init {
        viewModelScope.launch {
            defaults = userPreferences.engineeringDefaults()
            if (!formTouched) _uiState.update { it.withDefaults(defaults) }
        }
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.POWER)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing ------------------------------------------------------------

    fun onSystemChange(system: SupplySystem) = update {
        it.copy(
            system = system,
            voltage = SystemVoltageDefaults.follow(it.voltage, system, it.voltageEdited, defaults),
        )
    }

    fun onPowerFactorTypeChange(type: PowerFactorType) = update { it.copy(powerFactorType = type) }

    fun onVoltageChange(value: String) =
        update(PowerField.VOLTAGE) { it.copy(voltage = value, voltageEdited = true) }

    fun onCurrentChange(value: String) = update(PowerField.CURRENT) { it.copy(current = value) }

    fun onPowerFactorChange(value: String) =
        update(PowerField.POWER_FACTOR) { it.copy(powerFactor = value) }

    private fun update(
        field: PowerField? = null,
        transform: (PowerUiState) -> PowerUiState,
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

    // -- Calculation ---------------------------------------------------------------

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<PowerField, ValidationError>()

        fun validate(
            field: PowerField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
            allowZero: Boolean = false,
        ): Double? = NumericInput
            .validate(raw, min, max, allowZero)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val voltage = validate(PowerField.VOLTAGE, state.voltage, max = MAX_VOLTAGE)
        val current = validate(PowerField.CURRENT, state.current, max = MAX_CURRENT)

        val powerFactor = if (state.system.isAc) {
            // Zero is admitted: a purely reactive load is a real, if unusual,
            // case and the triangle handles it.
            validate(PowerField.POWER_FACTOR, state.powerFactor, min = 0.0, max = 1.0, allowZero = true)
        } else {
            1.0
        }

        if (errors.isNotEmpty() || voltage == null || current == null || powerFactor == null) {
            _uiState.update {
                it.copy(errors = errors, result = null, steps = persistentListOf())
            }
            return
        }

        val input = PowerInput(
            voltage = voltage,
            current = current,
            powerFactor = powerFactor,
            powerFactorType = state.powerFactorType,
            system = state.system,
        )

        val result = calculatePower(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainPower(input, result),
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
    fun onApplyExample(example: WorkedExample<PowerUiState>) {
        formTouched = true
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update {
            PowerUiState(
                system = it.system,
                voltage = SystemVoltageDefaults.forSystem(it.system, defaults)
                    ?: it.voltage,
                voltageEdited = false,
                powerFactorType = it.powerFactorType,
                isFavorite = it.isFavorite,
            )
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.POWER) }
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
        formTouched = true
        viewModelScope.launch {
            val record = historyRepository.findById(recordId) ?: return@launch
            if (record.calculatorId != CalculatorId.POWER) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    system = inputs.enumOrNull(KEY_SYSTEM) ?: it.system,
                    powerFactorType = inputs.enumOrNull(KEY_PF_TYPE) ?: it.powerFactorType,
                    voltage = inputs.pick(KEY_VOLTAGE, it.voltage),
                    current = inputs.pick(KEY_CURRENT, it.current),
                    powerFactor = inputs.pick(KEY_POWER_FACTOR, it.powerFactor),
                    voltageEdited = true,
                )
            }
            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: PowerUiState, result: PowerResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.POWER,
            title = stringResolver.get(Res.string.pw_history_title, state.voltage, state.current),
            summary = stringResolver.get(Res.string.pw_history_summary, format(result.activePowerWatts / WATTS_PER_KW)),
            inputs = mapOf(
                KEY_SYSTEM to state.system.name,
                KEY_PF_TYPE to state.powerFactorType.name,
                KEY_VOLTAGE to state.voltage,
                KEY_CURRENT to state.current,
                KEY_POWER_FACTOR to state.powerFactor,
            ),
            results = mapOf(
                KEY_ACTIVE to format(result.activePowerWatts / WATTS_PER_KW),
                KEY_REACTIVE to format(result.reactivePowerVar / WATTS_PER_KW),
                KEY_APPARENT to format(result.apparentPowerVa / WATTS_PER_KW),
                KEY_ANGLE to format(result.phaseAngleDegrees),
                KEY_TAN_PHI to NumberFormatter.formatSignificant(result.tangentPhi),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val WATTS_PER_KW = 1_000.0
        const val MAX_VOLTAGE = 1_000_000.0
        const val MAX_CURRENT = 1_000_000.0

        const val KEY_SYSTEM = "system"
        const val KEY_PF_TYPE = "power_factor_type"
        const val KEY_VOLTAGE = "voltage"
        const val KEY_CURRENT = "current"
        const val KEY_POWER_FACTOR = "power_factor"

        const val KEY_ACTIVE = "active_power_kw"
        const val KEY_REACTIVE = "reactive_power_kvar"
        const val KEY_APPARENT = "apparent_power_kva"
        const val KEY_ANGLE = "phase_angle_deg"
        const val KEY_TAN_PHI = "tan_phi"
    }
}

/**
 * The user's engineering defaults, applied to a form that is still untouched.
 *
 * Only the fields this calculator shares with Settings move.
 */
private fun PowerUiState.withDefaults(defaults: EngineeringDefaults) = copy(
    voltage = defaults.voltageFor(system) ?: voltage,
)
