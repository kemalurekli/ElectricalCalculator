package com.kemalurekli.electricalcalculator.features.calculators.battery.presentation

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
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.BatteryInput
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.BatteryResult
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.CalculateBatteryRuntimeUseCase
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
enum class BatteryField {
    CAPACITY,
    RATED_HOURS,
    VOLTAGE,
    LOAD_POWER,
    EFFICIENCY,
    DEPTH_OF_DISCHARGE,
    PEUKERT,
}

@Immutable
data class BatteryUiState(
    val capacityAh: String = "",
    val ratedHours: String = DEFAULT_RATED_HOURS,
    val voltage: String = "",
    val loadWatts: String = "",
    val efficiency: String = DEFAULT_EFFICIENCY,
    val depthOfDischarge: String = DEFAULT_DEPTH_OF_DISCHARGE,
    val peukert: String = DEFAULT_PEUKERT,
    val errors: Map<BatteryField, ValidationError> = emptyMap(),
    val result: BatteryResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    companion object {
        /** Lead-acid is conventionally rated over a twenty-hour discharge. */
        const val DEFAULT_RATED_HOURS = "20"

        /** A realistic inverter-plus-wiring figure, as a percentage. */
        const val DEFAULT_EFFICIENCY = "90"

        /** The customary lead-acid limit, as a percentage. */
        const val DEFAULT_DEPTH_OF_DISCHARGE = "50"

        /** Mid-range for a lead-acid bank. */
        const val DEFAULT_PEUKERT = "1.15"
    }
}

@HiltViewModel
class BatteryViewModel @Inject constructor(
    private val calculateRuntime: CalculateBatteryRuntimeUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.BATTERY_RUNTIME)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing --------------------------------------------------------------

    fun onCapacityChange(value: String) =
        update(BatteryField.CAPACITY) { it.copy(capacityAh = value) }

    fun onRatedHoursChange(value: String) =
        update(BatteryField.RATED_HOURS) { it.copy(ratedHours = value) }

    fun onVoltageChange(value: String) =
        update(BatteryField.VOLTAGE) { it.copy(voltage = value) }

    fun onLoadPowerChange(value: String) =
        update(BatteryField.LOAD_POWER) { it.copy(loadWatts = value) }

    fun onEfficiencyChange(value: String) =
        update(BatteryField.EFFICIENCY) { it.copy(efficiency = value) }

    fun onDepthOfDischargeChange(value: String) =
        update(BatteryField.DEPTH_OF_DISCHARGE) { it.copy(depthOfDischarge = value) }

    fun onPeukertChange(value: String) =
        update(BatteryField.PEUKERT) { it.copy(peukert = value) }

    private fun update(
        field: BatteryField? = null,
        transform: (BatteryUiState) -> BatteryUiState,
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
        val errors = mutableMapOf<BatteryField, ValidationError>()

        fun validate(
            field: BatteryField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
        ): Double? = NumericInput
            .validate(raw, min, max)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val capacity = validate(BatteryField.CAPACITY, state.capacityAh, max = MAX_CAPACITY_AH)
        val ratedHours = validate(
            BatteryField.RATED_HOURS,
            state.ratedHours,
            min = MIN_RATED_HOURS,
            max = MAX_RATED_HOURS,
        )
        val voltage = validate(BatteryField.VOLTAGE, state.voltage, max = MAX_VOLTAGE)
        val loadWatts = validate(BatteryField.LOAD_POWER, state.loadWatts, max = MAX_LOAD_WATTS)
        val efficiency = validate(
            BatteryField.EFFICIENCY,
            state.efficiency,
            min = MIN_PERCENT,
            max = MAX_PERCENT,
        )
        val depthOfDischarge = validate(
            BatteryField.DEPTH_OF_DISCHARGE,
            state.depthOfDischarge,
            min = MIN_PERCENT,
            max = MAX_PERCENT,
        )
        val peukert = validate(
            BatteryField.PEUKERT,
            state.peukert,
            min = MIN_PEUKERT,
            max = MAX_PEUKERT,
        )

        if (errors.isNotEmpty() ||
            capacity == null || ratedHours == null || voltage == null || loadWatts == null ||
            efficiency == null || depthOfDischarge == null || peukert == null
        ) {
            _uiState.update { it.copy(errors = errors, result = null, steps = persistentListOf()) }
            return
        }

        val input = BatteryInput(
            capacityAh = capacity,
            ratedDischargeHours = ratedHours,
            bankVoltage = voltage,
            loadPowerWatts = loadWatts,
            systemEfficiency = efficiency / PERCENT,
            depthOfDischarge = depthOfDischarge / PERCENT,
            peukertExponent = peukert,
        )

        val result = calculateRuntime(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainBatteryRuntime(input, result),
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
    fun onApplyExample(example: WorkedExample<BatteryUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update { BatteryUiState(isFavorite = it.isFavorite) }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.BATTERY_RUNTIME) }
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
            if (record.calculatorId != CalculatorId.BATTERY_RUNTIME) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    capacityAh = inputs.pick(KEY_CAPACITY, it.capacityAh),
                    ratedHours = inputs.pick(KEY_RATED_HOURS, it.ratedHours),
                    voltage = inputs.pick(KEY_VOLTAGE, it.voltage),
                    loadWatts = inputs.pick(KEY_LOAD, it.loadWatts),
                    efficiency = inputs.pick(KEY_EFFICIENCY, it.efficiency),
                    depthOfDischarge = inputs.pick(KEY_DEPTH_OF_DISCHARGE, it.depthOfDischarge),
                    peukert = inputs.pick(KEY_PEUKERT, it.peukert),
                )
            }
            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: BatteryUiState, result: BatteryResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.BATTERY_RUNTIME,
            title = stringResolver.get(R.string.bt_history_title)
                .format(state.capacityAh, state.loadWatts),
            summary = stringResolver.get(R.string.bt_history_summary)
                .format(format(result.runtimeHours)),
            inputs = mapOf(
                KEY_CAPACITY to state.capacityAh,
                KEY_RATED_HOURS to state.ratedHours,
                KEY_VOLTAGE to state.voltage,
                KEY_LOAD to state.loadWatts,
                KEY_EFFICIENCY to state.efficiency,
                KEY_DEPTH_OF_DISCHARGE to state.depthOfDischarge,
                KEY_PEUKERT to state.peukert,
            ),
            results = mapOf(
                KEY_RUNTIME to format(result.runtimeHours),
                KEY_IDEAL_RUNTIME to format(result.idealRuntimeHours),
                KEY_CURRENT to format(result.dischargeCurrentAmps),
                KEY_C_RATE to format(result.cRate),
                KEY_USABLE_CAPACITY to format(result.usableCapacityAh),
                KEY_ENERGY to format(result.energyDeliveredWh),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val PERCENT = 100.0

        const val MAX_CAPACITY_AH = 1_000_000.0
        const val MIN_RATED_HOURS = 0.1
        const val MAX_RATED_HOURS = 1_000.0
        const val MAX_VOLTAGE = 100_000.0
        const val MAX_LOAD_WATTS = 100_000_000.0
        const val MIN_PERCENT = 1.0
        const val MAX_PERCENT = 100.0

        // Below 1.0 a battery would gain capacity as it is discharged harder,
        // which no chemistry does; above 2.0 is beyond any real cell.
        const val MIN_PEUKERT = 1.0
        const val MAX_PEUKERT = 2.0

        const val KEY_CAPACITY = "capacity_ah"
        const val KEY_RATED_HOURS = "rated_discharge_hours"
        const val KEY_VOLTAGE = "bank_voltage"
        const val KEY_LOAD = "load_watts"
        const val KEY_EFFICIENCY = "system_efficiency_percent"
        const val KEY_DEPTH_OF_DISCHARGE = "depth_of_discharge_percent"
        const val KEY_PEUKERT = "peukert_exponent"

        const val KEY_RUNTIME = "runtime_hours"
        const val KEY_IDEAL_RUNTIME = "ideal_runtime_hours"
        const val KEY_CURRENT = "discharge_current"
        const val KEY_C_RATE = "c_rate"
        const val KEY_USABLE_CAPACITY = "usable_capacity_ah"
        const val KEY_ENERGY = "energy_wh"
    }
}
