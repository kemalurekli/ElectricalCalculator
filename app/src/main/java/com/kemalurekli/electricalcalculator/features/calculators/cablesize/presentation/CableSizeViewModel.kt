package com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CableSizeInput
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CableSizeResult
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CalculateCableSizeUseCase
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CorrectionFactors
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
class CableSizeViewModel @Inject constructor(
    private val calculateCableSize: CalculateCableSizeUseCase,
    private val correctionFactors: CorrectionFactors,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CableSizeUiState())
    val uiState: StateFlow<CableSizeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.observeIsFavorite(CalculatorId.CABLE_SIZE).collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }

    // -- Field editing --------------------------------------------------------

    fun onSystemChange(system: SupplySystem) = update {
        it.copy(
            system = system,
            voltage = SystemVoltageDefaults.follow(it.voltage, system, it.voltageEdited),
        )
    }

    fun onMaterialChange(material: ConductorMaterial) = update { it.copy(material = material) }

    fun onInsulationChange(insulation: CableInsulation) = update { it.copy(insulation = insulation) }

    fun onMethodChange(method: InstallationMethod) = update { it.copy(method = method) }

    fun onVoltageChange(value: String) =
        update(CableSizeField.VOLTAGE) { it.copy(voltage = value, voltageEdited = true) }

    fun onCurrentChange(value: String) = update(CableSizeField.CURRENT) { it.copy(current = value) }

    fun onLengthChange(value: String) = update(CableSizeField.LENGTH) { it.copy(length = value) }

    fun onPowerFactorChange(value: String) =
        update(CableSizeField.POWER_FACTOR) { it.copy(powerFactor = value) }

    fun onMaxDropChange(value: String) =
        update(CableSizeField.MAX_DROP) { it.copy(maxDropPercent = value) }

    fun onAmbientChange(value: String) =
        update(CableSizeField.AMBIENT) { it.copy(ambientTemperature = value) }

    fun onCircuitsChange(value: String) =
        update(CableSizeField.CIRCUITS) { it.copy(groupedCircuits = value) }

    fun onParallelConductorsChange(value: String) =
        update(CableSizeField.PARALLEL_CONDUCTORS) { it.copy(parallelConductors = value) }

    /** Applies an edit and drops the now-stale result. */
    private fun update(
        field: CableSizeField? = null,
        transform: (CableSizeUiState) -> CableSizeUiState,
    ) {
        _uiState.update { current ->
            transform(current).copy(
                result = null,
                steps = persistentListOf(),
                errors = if (field == null) current.errors else current.errors - field,
            )
        }
    }

    // -- Calculation -----------------------------------------------------------

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<CableSizeField, ValidationError>()

        fun validate(
            field: CableSizeField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
            allowZero: Boolean = false,
            allowNegative: Boolean = false,
        ): Double? = NumericInput
            .validate(raw, min, max, allowZero, allowNegative)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val voltage = validate(CableSizeField.VOLTAGE, state.voltage, max = MAX_VOLTAGE)
        val current = validate(CableSizeField.CURRENT, state.current, max = MAX_CURRENT)
        val length = validate(CableSizeField.LENGTH, state.length, max = MAX_LENGTH)

        val powerFactor = if (state.system.isAc) {
            validate(CableSizeField.POWER_FACTOR, state.powerFactor, min = MIN_POWER_FACTOR, max = 1.0)
        } else {
            1.0
        }

        val maxDrop = validate(
            CableSizeField.MAX_DROP,
            state.maxDropPercent,
            min = MIN_MAX_DROP,
            max = MAX_MAX_DROP,
        )

        // The correction tables stop at a defined ceiling; beyond it the cable
        // has no rating at all, so the range is bounded rather than clamped.
        val ambient = validate(
            CableSizeField.AMBIENT,
            state.ambientTemperature,
            min = MIN_AMBIENT,
            max = correctionFactors.maxAmbientC(state.insulation),
            allowZero = true,
            allowNegative = true,
        )

        val circuits = validate(CableSizeField.CIRCUITS, state.groupedCircuits, min = 1.0, max = MAX_CIRCUITS)
        val parallel = validate(
            CableSizeField.PARALLEL_CONDUCTORS,
            state.parallelConductors,
            min = 1.0,
            max = MAX_PARALLEL,
        )

        if (errors.isNotEmpty() ||
            voltage == null || current == null || length == null || powerFactor == null ||
            maxDrop == null || ambient == null || circuits == null || parallel == null
        ) {
            _uiState.update {
                it.copy(errors = errors, result = null, steps = persistentListOf())
            }
            return
        }

        val input = CableSizeInput(
            systemVoltage = voltage,
            designCurrent = current,
            lengthMetres = length,
            material = state.material,
            insulation = state.insulation,
            method = state.method,
            system = state.system,
            powerFactor = powerFactor,
            maxVoltageDropPercent = maxDrop,
            ambientTemperatureC = ambient,
            groupedCircuits = circuits.toInt(),
            parallelConductors = parallel.toInt(),
        )

        val result = calculateCableSize(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainCableSize(input, result),
            )
        }

        // Only a run that produced a size is worth keeping; "no size fits" is
        // not a result the user would want to reopen from history.
        if (result.hasSolution) {
            saveToHistory(state, input, result)
        }
    }

    /**
     * Loads a worked example and runs it.
     *
     * Calculating immediately is the point: one tap produces a filled form, a
     * result and the arithmetic between them, which is a complete worked example
     * rather than a form the reader still has to submit.
     */
    fun onApplyExample(example: WorkedExample<CableSizeUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update {
            CableSizeUiState(
                system = it.system,
                voltage = SystemVoltageDefaults.forSystem(it.system)
                    ?: it.voltage,
                voltageEdited = false,
                material = it.material,
                insulation = it.insulation,
                method = it.method,
                isFavorite = it.isFavorite,
            )
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.CABLE_SIZE) }
    }

    private fun saveToHistory(
        state: CableSizeUiState,
        input: CableSizeInput,
        result: CableSizeResult,
    ) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.CABLE_SIZE,
            title = stringResolver.get(R.string.cs_history_title)
                .format(format(input.designCurrent), format(input.lengthMetres)),
            summary = stringResolver.get(R.string.cs_history_summary)
                .format(format(result.recommendedAreaMm2 ?: 0.0)),
            inputs = mapOf(
                KEY_SYSTEM to state.system.name,
                KEY_MATERIAL to state.material.name,
                KEY_INSULATION to state.insulation.name,
                KEY_METHOD to state.method.name,
                KEY_VOLTAGE to state.voltage,
                KEY_CURRENT to state.current,
                KEY_LENGTH to state.length,
                KEY_POWER_FACTOR to state.powerFactor,
                KEY_MAX_DROP to state.maxDropPercent,
                KEY_AMBIENT to state.ambientTemperature,
                KEY_CIRCUITS to state.groupedCircuits,
                KEY_PARALLEL to state.parallelConductors,
            ),
            results = mapOf(
                KEY_AREA to format(result.recommendedAreaMm2 ?: 0.0),
                KEY_AREA_BY_CAPACITY to format(result.currentCapacityAreaMm2 ?: 0.0),
                KEY_AREA_BY_DROP to format(result.voltageDropAreaMm2 ?: 0.0),
                KEY_REQUIRED_CAPACITY to format(result.requiredCapacityAmps),
                KEY_DROP_PERCENT to format(result.voltageDropPercent),
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
        const val MIN_POWER_FACTOR = 0.01
        const val MIN_MAX_DROP = 0.1
        const val MAX_MAX_DROP = 25.0
        const val MIN_AMBIENT = -20.0
        const val MAX_CIRCUITS = 20.0
        const val MAX_PARALLEL = 20.0

        const val KEY_SYSTEM = "system"
        const val KEY_MATERIAL = "material"
        const val KEY_INSULATION = "insulation"
        const val KEY_METHOD = "method"
        const val KEY_VOLTAGE = "voltage"
        const val KEY_CURRENT = "current"
        const val KEY_LENGTH = "length"
        const val KEY_POWER_FACTOR = "power_factor"
        const val KEY_MAX_DROP = "max_drop_percent"
        const val KEY_AMBIENT = "ambient_temperature"
        const val KEY_CIRCUITS = "grouped_circuits"
        const val KEY_PARALLEL = "parallel_conductors"

        const val KEY_AREA = "cross_section"
        const val KEY_AREA_BY_CAPACITY = "area_by_capacity"
        const val KEY_AREA_BY_DROP = "area_by_drop"
        const val KEY_REQUIRED_CAPACITY = "required_capacity"
        const val KEY_DROP_PERCENT = "drop_percent"
    }
}
