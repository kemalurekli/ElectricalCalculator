package com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation

import androidx.compose.runtime.Immutable
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
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.CalculateSolarStringUseCase
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.SolarStringInput
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.SolarStringResult
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
enum class SolarStringField {
    VOC,
    VMP,
    COEFFICIENT,
    MIN_TEMPERATURE,
    MAX_TEMPERATURE,
    INVERTER_MAX,
    MPPT_MIN,
}

@Immutable
data class SolarStringUiState(
    val voc: String = "",
    val vmp: String = "",
    val coefficient: String = "",
    val minTemperature: String = DEFAULT_MIN_TEMPERATURE,
    val maxTemperature: String = DEFAULT_MAX_TEMPERATURE,
    val inverterMax: String = DEFAULT_INVERTER_MAX,
    val mpptMin: String = "",
    val errors: Map<SolarStringField, ValidationError> = emptyMap(),
    val result: SolarStringResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    companion object {
        /**
         * A cold morning, not an average winter.
         *
         * The maximum-modules limit is a destruction limit, so the figure that
         * belongs here is the site's record low — and offering a mild default
         * would be offering the wrong kind of help. −10 °C is a starting point
         * a temperate-climate designer will raise or lower knowingly.
         */
        const val DEFAULT_MIN_TEMPERATURE = "-10"

        /** Cell temperature runs well above air temperature in sun. */
        const val DEFAULT_MAX_TEMPERATURE = "70"

        /** The commonest string-inverter DC rating. */
        const val DEFAULT_INVERTER_MAX = "1000"
    }
}

@HiltViewModel
class SolarStringViewModel @Inject constructor(
    private val calculateString: CalculateSolarStringUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolarStringUiState())
    val uiState: StateFlow<SolarStringUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.SOLAR_STRING)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing -----------------------------------------------------------

    fun onVocChange(value: String) = update(SolarStringField.VOC) { it.copy(voc = value) }

    fun onVmpChange(value: String) = update(SolarStringField.VMP) { it.copy(vmp = value) }

    fun onCoefficientChange(value: String) =
        update(SolarStringField.COEFFICIENT) { it.copy(coefficient = value) }

    fun onMinTemperatureChange(value: String) =
        update(SolarStringField.MIN_TEMPERATURE) { it.copy(minTemperature = value) }

    fun onMaxTemperatureChange(value: String) =
        update(SolarStringField.MAX_TEMPERATURE) { it.copy(maxTemperature = value) }

    fun onInverterMaxChange(value: String) =
        update(SolarStringField.INVERTER_MAX) { it.copy(inverterMax = value) }

    fun onMpptMinChange(value: String) =
        update(SolarStringField.MPPT_MIN) { it.copy(mpptMin = value) }

    private fun update(
        field: SolarStringField? = null,
        transform: (SolarStringUiState) -> SolarStringUiState,
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
        val errors = mutableMapOf<SolarStringField, ValidationError>()

        fun validate(
            field: SolarStringField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
            allowZero: Boolean = false,
            allowNegative: Boolean = false,
        ): Double? = NumericInput
            .validate(raw, min, max, allowZero, allowNegative)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val voc = validate(SolarStringField.VOC, state.voc, max = MAX_MODULE_VOLTS)
        val vmp = validate(SolarStringField.VMP, state.vmp, max = MAX_MODULE_VOLTS)

        // β is negative on every silicon module; a positive entry is a sign
        // error, and accepting it would size the string the wrong way.
        val beta = validate(
            SolarStringField.COEFFICIENT,
            state.coefficient,
            min = MIN_COEFFICIENT,
            max = MAX_COEFFICIENT,
            allowNegative = true,
        )

        val minTemp = validate(
            SolarStringField.MIN_TEMPERATURE,
            state.minTemperature,
            min = MIN_TEMPERATURE,
            max = MAX_TEMPERATURE,
            allowZero = true,
            allowNegative = true,
        )
        val maxTemp = validate(
            SolarStringField.MAX_TEMPERATURE,
            state.maxTemperature,
            min = MIN_TEMPERATURE,
            max = MAX_TEMPERATURE,
            allowZero = true,
            allowNegative = true,
        )
        val inverterMax = validate(SolarStringField.INVERTER_MAX, state.inverterMax, max = MAX_DC)
        val mpptMin = validate(SolarStringField.MPPT_MIN, state.mpptMin, max = MAX_DC)

        if (errors.isNotEmpty() || voc == null || vmp == null || beta == null ||
            minTemp == null || maxTemp == null || inverterMax == null || mpptMin == null
        ) {
            _uiState.update {
                it.copy(errors = errors, result = null, steps = persistentListOf())
            }
            return
        }

        val input = SolarStringInput(
            moduleVocVolts = voc,
            moduleVmpVolts = vmp,
            voltageCoefficientPercentPerK = beta,
            minimumCellTemperatureC = minTemp,
            maximumCellTemperatureC = maxTemp,
            inverterMaxDcVolts = inverterMax,
            inverterMpptMinVolts = mpptMin,
        )
        val result = calculateString(input)

        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainSolarString(input, result),
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
    fun onApplyExample(example: WorkedExample<SolarStringUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update { SolarStringUiState(isFavorite = it.isFavorite) }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.SOLAR_STRING) }
    }

    private fun saveToHistory(state: SolarStringUiState, result: SolarStringResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.SOLAR_STRING,
            title = stringResolver.get(R.string.ss_history_title)
                .format(state.voc, state.inverterMax),
            summary = stringResolver.get(R.string.ss_history_summary)
                .format(result.minimumModules.toString(), result.maximumModules.toString()),
            inputs = mapOf(
                KEY_VOC to state.voc,
                KEY_VMP to state.vmp,
                KEY_BETA to state.coefficient,
                KEY_MIN_TEMP to state.minTemperature,
                KEY_MAX_TEMP to state.maxTemperature,
                KEY_INVERTER_MAX to state.inverterMax,
                KEY_MPPT_MIN to state.mpptMin,
            ),
            results = mapOf(
                KEY_MAX_MODULES to result.maximumModules.toString(),
                KEY_MIN_MODULES to result.minimumModules.toString(),
                KEY_VOC_COLD to format(result.vocAtMinimumTemperature),
                KEY_VMP_HOT to format(result.vmpAtMaximumTemperature),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_MODULE_VOLTS = 500.0
        const val MIN_COEFFICIENT = -2.0
        const val MAX_COEFFICIENT = -0.01
        const val MIN_TEMPERATURE = -60.0
        const val MAX_TEMPERATURE = 120.0
        const val MAX_DC = 2_000.0

        const val KEY_VOC = "module_voc"
        const val KEY_VMP = "module_vmp"
        const val KEY_BETA = "voltage_coefficient"
        const val KEY_MIN_TEMP = "min_cell_temperature"
        const val KEY_MAX_TEMP = "max_cell_temperature"
        const val KEY_INVERTER_MAX = "inverter_max_dc"
        const val KEY_MPPT_MIN = "mppt_min"

        const val KEY_MAX_MODULES = "max_modules"
        const val KEY_MIN_MODULES = "min_modules"
        const val KEY_VOC_COLD = "voc_cold"
        const val KEY_VMP_HOT = "vmp_hot"
    }
}
