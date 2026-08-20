package com.kemalurekli.electricalcalculator.features.calculators.energycost.presentation

import androidx.compose.runtime.Immutable
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
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.CalculateEnergyCostUseCase
import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.EnergyCostInput
import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.EnergyCostResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_history_summary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_history_title

/** Identifies a form field so validation errors can be routed back to it. */
enum class EnergyCostField {
    POWER,
    HOURS_PER_DAY,
    DAYS_PER_YEAR,
    TARIFF,
    REPLACEMENT_POWER,
    REPLACEMENT_COST,
}

@Immutable
data class EnergyCostUiState(
    val power: String = "",
    val hoursPerDay: String = "",
    val daysPerYear: String = DEFAULT_DAYS_PER_YEAR,
    val tariff: String = "",
    val replacementPower: String = "",
    val replacementCost: String = "",
    val errors: Map<EnergyCostField, ValidationError> = emptyMap(),
    val result: EnergyCostResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    /** True once the reader has started describing an alternative. */
    val isComparing: Boolean get() = replacementPower.isNotBlank()

    companion object {
        /**
         * A working year rather than a calendar one.
         *
         * Most things this calculator is pointed at are in a workplace and run
         * on weekdays. Someone costing an always-on load types 365, which is one
         * edit; someone costing a machine would otherwise overstate by 46 %.
         */
        const val DEFAULT_DAYS_PER_YEAR = "250"
    }
}

class EnergyCostViewModel(
    private val calculateCost: CalculateEnergyCostUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnergyCostUiState())
    val uiState: StateFlow<EnergyCostUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.ENERGY_COST)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing -----------------------------------------------------------

    fun onPowerChange(value: String) = update(EnergyCostField.POWER) { it.copy(power = value) }

    fun onHoursChange(value: String) =
        update(EnergyCostField.HOURS_PER_DAY) { it.copy(hoursPerDay = value) }

    fun onDaysChange(value: String) =
        update(EnergyCostField.DAYS_PER_YEAR) { it.copy(daysPerYear = value) }

    fun onTariffChange(value: String) = update(EnergyCostField.TARIFF) { it.copy(tariff = value) }

    fun onReplacementPowerChange(value: String) =
        update(EnergyCostField.REPLACEMENT_POWER) { it.copy(replacementPower = value) }

    fun onReplacementCostChange(value: String) =
        update(EnergyCostField.REPLACEMENT_COST) { it.copy(replacementCost = value) }

    private fun update(
        field: EnergyCostField? = null,
        transform: (EnergyCostUiState) -> EnergyCostUiState,
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
        val errors = mutableMapOf<EnergyCostField, ValidationError>()

        fun validate(
            field: EnergyCostField,
            raw: String,
            max: Double,
            allowZero: Boolean = false,
        ): Double? = NumericInput
            .validate(raw, min = 0.0, max = max, allowZero = allowZero)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val power = validate(EnergyCostField.POWER, state.power, MAX_POWER)
        val hours = validate(EnergyCostField.HOURS_PER_DAY, state.hoursPerDay, HOURS_IN_A_DAY)
        val days = validate(EnergyCostField.DAYS_PER_YEAR, state.daysPerYear, DAYS_IN_A_YEAR)
        val tariff = validate(EnergyCostField.TARIFF, state.tariff, MAX_TARIFF)

        // The comparison is optional, so a blank field is not an error — but a
        // filled one still has to be a number.
        val replacementPower = state.replacementPower
            .takeIf { it.isNotBlank() }
            ?.let { validate(EnergyCostField.REPLACEMENT_POWER, it, MAX_POWER, allowZero = true) }
        val replacementCost = state.replacementCost
            .takeIf { it.isNotBlank() }
            ?.let { validate(EnergyCostField.REPLACEMENT_COST, it, MAX_CAPITAL, allowZero = true) }

        if (errors.isNotEmpty() || power == null || hours == null || days == null || tariff == null) {
            _uiState.update {
                it.copy(errors = errors, result = null, steps = persistentListOf())
            }
            return
        }

        val input = EnergyCostInput(
            powerWatts = power,
            hoursPerDay = hours,
            daysPerYear = days,
            tariffPerKwh = tariff,
            replacementPowerWatts = replacementPower,
            replacementCostToBuy = replacementCost,
        )
        val result = calculateCost(input)

        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainEnergyCost(input, result),
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
    fun onApplyExample(example: WorkedExample<EnergyCostUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update { EnergyCostUiState(isFavorite = it.isFavorite) }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.ENERGY_COST) }
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
            if (record.calculatorId != CalculatorId.ENERGY_COST) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    power = inputs.pick(KEY_POWER, it.power),
                    hoursPerDay = inputs.pick(KEY_HOURS, it.hoursPerDay),
                    daysPerYear = inputs.pick(KEY_DAYS, it.daysPerYear),
                    tariff = inputs.pick(KEY_TARIFF, it.tariff),
                    replacementPower = inputs.pick(KEY_REPLACEMENT_POWER, it.replacementPower),
                    replacementCost = inputs.pick(KEY_REPLACEMENT_COST, it.replacementCost),
                )
            }
            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: EnergyCostUiState, result: EnergyCostResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.ENERGY_COST,
            title = stringResolver.get(Res.string.ec_history_title, state.power, state.hoursPerDay),
            summary = stringResolver.get(Res.string.ec_history_summary, format(result.annualCost)),
            inputs = mapOf(
                KEY_POWER to state.power,
                KEY_HOURS to state.hoursPerDay,
                KEY_DAYS to state.daysPerYear,
                KEY_TARIFF to state.tariff,
                KEY_REPLACEMENT_POWER to state.replacementPower,
                KEY_REPLACEMENT_COST to state.replacementCost,
            ),
            results = buildMap {
                put(KEY_ANNUAL_KWH, format(result.annualEnergyKwh))
                put(KEY_ANNUAL_COST, format(result.annualCost))
                result.annualSaving?.let { put(KEY_SAVING, format(it)) }
                result.paybackYears?.let { put(KEY_PAYBACK, format(it)) }
            },
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_POWER = 10_000_000.0
        const val HOURS_IN_A_DAY = 24.0
        const val DAYS_IN_A_YEAR = 366.0
        const val MAX_TARIFF = 1_000.0
        const val MAX_CAPITAL = 100_000_000.0

        const val KEY_POWER = "power_watts"
        const val KEY_HOURS = "hours_per_day"
        const val KEY_DAYS = "days_per_year"
        const val KEY_TARIFF = "tariff_per_kwh"
        const val KEY_REPLACEMENT_POWER = "replacement_watts"
        const val KEY_REPLACEMENT_COST = "replacement_capital"

        const val KEY_ANNUAL_KWH = "annual_kwh"
        const val KEY_ANNUAL_COST = "annual_cost"
        const val KEY_SAVING = "annual_saving"
        const val KEY_PAYBACK = "payback_years"
    }
}
