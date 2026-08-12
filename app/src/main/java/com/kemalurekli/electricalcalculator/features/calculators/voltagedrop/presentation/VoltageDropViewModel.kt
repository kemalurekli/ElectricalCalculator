package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.CalculateVoltageDropUseCase
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropInput
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropResult
import com.kemalurekli.electricalcalculator.core.ui.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoltageDropViewModel @Inject constructor(
    private val calculateVoltageDrop: CalculateVoltageDropUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoltageDropUiState())
    val uiState: StateFlow<VoltageDropUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.observeIsFavorite(CalculatorId.VOLTAGE_DROP).collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }

    // -- Field editing ------------------------------------------------------

    fun onSystemChange(system: SupplySystem) = update {
        it.copy(
            system = system,
            voltage = SystemVoltageDefaults.follow(it.voltage, system, it.voltageEdited),
        )
    }

    fun onMaterialChange(material: ConductorMaterial) = update { it.copy(material = material) }

    fun onVoltageChange(value: String) =
        update(VoltageDropField.VOLTAGE) { it.copy(voltage = value, voltageEdited = true) }

    fun onCurrentChange(value: String) = update(VoltageDropField.CURRENT) { it.copy(current = value) }

    fun onLengthChange(value: String) = update(VoltageDropField.LENGTH) { it.copy(length = value) }

    fun onCrossSectionChange(value: String) =
        update(VoltageDropField.CROSS_SECTION) { it.copy(crossSection = value) }

    fun onPowerFactorChange(value: String) =
        update(VoltageDropField.POWER_FACTOR) { it.copy(powerFactor = value) }

    fun onTemperatureChange(value: String) =
        update(VoltageDropField.TEMPERATURE) { it.copy(temperature = value) }

    fun onParallelConductorsChange(value: String) =
        update(VoltageDropField.PARALLEL_CONDUCTORS) { it.copy(parallelConductors = value) }

    /**
     * Applies an edit and clears the stale result.
     *
     * Leaving the previous result on screen after an input changes is the
     * classic calculator bug: the figure no longer corresponds to the form
     * above it, and there is nothing to tell the user which inputs produced it.
     */
    private fun update(
        field: VoltageDropField? = null,
        transform: (VoltageDropUiState) -> VoltageDropUiState,
    ) {
        _uiState.update { current ->
            transform(current).copy(
                result = null,
                steps = persistentListOf(),
                errors = if (field == null) current.errors else current.errors - field,
            )
        }
    }

    // -- Calculation ---------------------------------------------------------

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<VoltageDropField, ValidationError>()

        fun validate(
            field: VoltageDropField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
            allowZero: Boolean = false,
            allowNegative: Boolean = false,
        ): Double? = NumericInput
            .validate(raw, min, max, allowZero, allowNegative)
            .also { outcome -> if (outcome is Outcome.Failure) errors[field] = outcome.error }
            .let { (it as? Outcome.Success)?.value }

        val voltage = validate(VoltageDropField.VOLTAGE, state.voltage, max = MAX_VOLTAGE)
        val current = validate(VoltageDropField.CURRENT, state.current, max = MAX_CURRENT)
        val length = validate(VoltageDropField.LENGTH, state.length, max = MAX_LENGTH)
        val area = validate(VoltageDropField.CROSS_SECTION, state.crossSection, max = MAX_AREA)

        // Power factor is only read on AC; validating it on DC would block a
        // calculation on a field the user cannot even see.
        val powerFactor = if (state.system.isAc) {
            validate(VoltageDropField.POWER_FACTOR, state.powerFactor, min = MIN_POWER_FACTOR, max = 1.0)
        } else {
            1.0
        }

        val temperature = validate(
            VoltageDropField.TEMPERATURE,
            state.temperature,
            min = MIN_TEMPERATURE,
            max = MAX_TEMPERATURE,
            allowZero = true,
            allowNegative = true,
        )
        val parallel = validate(
            VoltageDropField.PARALLEL_CONDUCTORS,
            state.parallelConductors,
            min = 1.0,
            max = MAX_PARALLEL,
        )

        if (errors.isNotEmpty() ||
            voltage == null || current == null || length == null ||
            area == null || powerFactor == null || temperature == null || parallel == null
        ) {
            _uiState.update {
                it.copy(errors = errors, result = null, steps = persistentListOf())
            }
            return
        }

        val input = VoltageDropInput(
            systemVoltage = voltage,
            loadCurrent = current,
            lengthMetres = length,
            crossSectionMm2 = area,
            material = state.material,
            system = state.system,
            powerFactor = powerFactor,
            conductorTemperatureC = temperature,
            // Fractional conductors are meaningless; the field is validated as
            // a number so it can report a range, then rounded here.
            parallelConductors = parallel.toInt(),
        )

        val result = calculateVoltageDrop(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainVoltageDrop(input, result),
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
    fun onApplyExample(example: WorkedExample<VoltageDropUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update {
            VoltageDropUiState(
                system = it.system,
                voltage = SystemVoltageDefaults.forSystem(it.system)
                    ?: it.voltage,
                voltageEdited = false,
                material = it.material,
                isFavorite = it.isFavorite,
            )
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.VOLTAGE_DROP) }
    }

    // -- History -------------------------------------------------------------

    /**
     * Persists a completed calculation.
     *
     * Inputs are stored as the raw field strings so the record can be loaded
     * straight back into this form by "recalculate" without a reverse-parse.
     */
    private fun saveToHistory(
        state: VoltageDropUiState,
        input: VoltageDropInput,
        result: VoltageDropResult,
    ) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.VOLTAGE_DROP,
            title = stringResolver.get(R.string.vd_history_title)
                .format(format(input.crossSectionMm2), format(input.lengthMetres)),
            summary = stringResolver.get(R.string.vd_history_summary)
                .format(format(result.voltageDrop), format(result.dropPercentage)),
            inputs = mapOf(
                KEY_SYSTEM to state.system.name,
                KEY_MATERIAL to state.material.name,
                KEY_VOLTAGE to state.voltage,
                KEY_CURRENT to state.current,
                KEY_LENGTH to state.length,
                KEY_CROSS_SECTION to state.crossSection,
                KEY_POWER_FACTOR to state.powerFactor,
                KEY_TEMPERATURE to state.temperature,
                KEY_PARALLEL to state.parallelConductors,
            ),
            results = mapOf(
                KEY_DROP to format(result.voltageDrop),
                KEY_DROP_PERCENT to format(result.dropPercentage),
                KEY_VOLTAGE_AT_LOAD to format(result.voltageAtLoad),
                KEY_RESISTANCE to NumberFormatter.formatSignificant(result.conductorResistance),
                KEY_POWER_LOSS to format(result.powerLossWatts),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_VOLTAGE = 500_000.0
        const val MAX_CURRENT = 100_000.0
        const val MAX_LENGTH = 100_000.0
        const val MAX_AREA = 5_000.0
        const val MIN_POWER_FACTOR = 0.01
        const val MIN_TEMPERATURE = -50.0
        const val MAX_TEMPERATURE = 250.0
        const val MAX_PARALLEL = 20.0

        const val KEY_SYSTEM = "system"
        const val KEY_MATERIAL = "material"
        const val KEY_VOLTAGE = "voltage"
        const val KEY_CURRENT = "current"
        const val KEY_LENGTH = "length"
        const val KEY_CROSS_SECTION = "cross_section"
        const val KEY_POWER_FACTOR = "power_factor"
        const val KEY_TEMPERATURE = "temperature"
        const val KEY_PARALLEL = "parallel_conductors"

        const val KEY_DROP = "voltage_drop"
        const val KEY_DROP_PERCENT = "drop_percent"
        const val KEY_VOLTAGE_AT_LOAD = "voltage_at_load"
        const val KEY_RESISTANCE = "resistance"
        const val KEY_POWER_LOSS = "power_loss"
    }
}
