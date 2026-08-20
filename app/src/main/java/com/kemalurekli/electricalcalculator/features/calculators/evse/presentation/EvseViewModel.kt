package com.kemalurekli.electricalcalculator.features.calculators.evse.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.CalculateEvseUseCase
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.DcFaultDetection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseConnection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseInput
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class EvseField {
    POINT_COUNT,
    RATED_CURRENT,
    SUPPLY_VOLTAGE,
    SIMULTANEITY,
}

@Immutable
data class EvseUiState(
    val pointCount: String = "1",
    val ratedCurrent: String = DEFAULT_RATED_CURRENT,
    val connection: EvseConnection = EvseConnection.SINGLE_PHASE,
    val supplyVoltage: String = "",
    val simultaneity: String = DEFAULT_SIMULTANEITY,
    val dcFaultDetection: DcFaultDetection = DcFaultDetection.BUILT_IN_6MA,
    val errors: Map<EvseField, ValidationError> = emptyMap(),
    val result: EvseResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    companion object {
        /** 32 A is the commonest domestic and light-commercial point. */
        const val DEFAULT_RATED_CURRENT = "32"

        /**
         * No diversity.
         *
         * Charging is a continuous load, so the app declines to assume any
         * sharing. Lowering it is a decision about a particular site, which is
         * why the field is here and why it starts at one.
         */
        const val DEFAULT_SIMULTANEITY = "1"
    }
}

@HiltViewModel
class EvseViewModel @Inject constructor(
    private val calculateEvse: CalculateEvseUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: ResourceIdResolver,
    private val timeProvider: TimeProvider,
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EvseUiState())
    val uiState: StateFlow<EvseUiState> = _uiState.asStateFlow()

    private var formTouched = false

    init {
        viewModelScope.launch {
            val defaults = userPreferences.engineeringDefaults()
            if (!formTouched) {
                _uiState.update { it.copy(supplyVoltage = defaults.singlePhaseVoltage) }
            }
        }
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.EVSE)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    fun onPointCountChange(value: String) =
        update(EvseField.POINT_COUNT) { it.copy(pointCount = value) }

    fun onRatedCurrentChange(value: String) =
        update(EvseField.RATED_CURRENT) { it.copy(ratedCurrent = value) }

    /**
     * Switching to three phase moves an untouched voltage to the line value.
     *
     * A charging point is described by its current, so the voltage is the field
     * a reader is least likely to remember to change — and the one that turns
     * 7.4 kW into 22 kW.
     */
    fun onConnectionChange(connection: EvseConnection) = update {
        it.copy(connection = connection)
    }

    fun onSupplyVoltageChange(value: String) =
        update(EvseField.SUPPLY_VOLTAGE) { it.copy(supplyVoltage = value) }

    fun onSimultaneityChange(value: String) =
        update(EvseField.SIMULTANEITY) { it.copy(simultaneity = value) }

    fun onDcDetectionChange(detection: DcFaultDetection) =
        update { it.copy(dcFaultDetection = detection) }

    private fun update(
        field: EvseField? = null,
        transform: (EvseUiState) -> EvseUiState,
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

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<EvseField, ValidationError>()

        fun validate(field: EvseField, raw: String, max: Double): Double? = NumericInput
            .validate(raw, min = 0.0, max = max)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val count = validate(EvseField.POINT_COUNT, state.pointCount, MAX_POINTS)
        val rated = validate(EvseField.RATED_CURRENT, state.ratedCurrent, MAX_CURRENT)
        val voltage = validate(EvseField.SUPPLY_VOLTAGE, state.supplyVoltage, MAX_VOLTAGE)
        val simultaneity = validate(EvseField.SIMULTANEITY, state.simultaneity, 1.0)

        if (errors.isNotEmpty() || count == null || rated == null || voltage == null ||
            simultaneity == null
        ) {
            _uiState.update { it.copy(errors = errors, result = null, steps = persistentListOf()) }
            return
        }

        val input = EvseInput(
            pointCount = count.toInt(),
            ratedCurrentPerPoint = rated,
            connection = state.connection,
            supplyVoltage = voltage,
            simultaneityFactor = simultaneity,
            dcFaultDetection = state.dcFaultDetection,
        )
        val result = calculateEvse(input)

        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainEvse(input, result),
            )
        }

        saveToHistory(state, result)
    }

    fun onApplyExample(example: WorkedExample<EvseUiState>) {
        formTouched = true
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update { EvseUiState(isFavorite = it.isFavorite, supplyVoltage = it.supplyVoltage) }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.EVSE) }
    }

    fun onRestore(recordId: Long) {
        formTouched = true
        viewModelScope.launch {
            val record = historyRepository.findById(recordId) ?: return@launch
            if (record.calculatorId != CalculatorId.EVSE) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    pointCount = inputs.pick(KEY_POINTS, it.pointCount),
                    ratedCurrent = inputs.pick(KEY_RATED, it.ratedCurrent),
                    connection = inputs.enumOrNull<EvseConnection>(KEY_CONNECTION) ?: it.connection,
                    supplyVoltage = inputs.pick(KEY_VOLTAGE, it.supplyVoltage),
                    simultaneity = inputs.pick(KEY_SIMULTANEITY, it.simultaneity),
                    dcFaultDetection = inputs.enumOrNull<DcFaultDetection>(KEY_DC)
                        ?: it.dcFaultDetection,
                )
            }
            onCalculate()
        }
    }

    private fun saveToHistory(state: EvseUiState, result: EvseResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.EVSE,
            title = stringResolver.get(R.string.ev_history_title)
                .format(state.pointCount, state.ratedCurrent),
            summary = stringResolver.get(R.string.ev_history_summary)
                .format(format(result.designCurrentAmps)),
            inputs = mapOf(
                KEY_POINTS to state.pointCount,
                KEY_RATED to state.ratedCurrent,
                KEY_CONNECTION to state.connection.name,
                KEY_VOLTAGE to state.supplyVoltage,
                KEY_SIMULTANEITY to state.simultaneity,
                KEY_DC to state.dcFaultDetection.name,
            ),
            results = buildMap {
                put(KEY_DESIGN, format(result.designCurrentAmps))
                put(KEY_TOTAL_KW, format(result.totalConnectedKw))
                result.deviceRatingAmps?.let { put(KEY_DEVICE, format(it)) }
            },
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_POINTS = 200.0
        const val MAX_CURRENT = 1_000.0
        const val MAX_VOLTAGE = 1_000.0

        const val KEY_POINTS = "point_count"
        const val KEY_RATED = "rated_current"
        const val KEY_CONNECTION = "connection"
        const val KEY_VOLTAGE = "supply_voltage"
        const val KEY_SIMULTANEITY = "simultaneity"
        const val KEY_DC = "dc_detection"

        const val KEY_DESIGN = "design_current"
        const val KEY_TOTAL_KW = "total_kw"
        const val KEY_DEVICE = "device_rating"
    }
}
