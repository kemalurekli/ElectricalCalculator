package com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation

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
import com.kemalurekli.electricalcalculator.core.common.util.ResourceIdResolver
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.domain.model.CableBundleEntry
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.CalculateConduitFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.ConduitFillInput
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.ConduitFillResult
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.FillRule
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Identifies a form-level field so validation errors can be routed back to it. */
enum class ConduitFillField {
    CONDUIT_DIAMETER,
    CUSTOM_LIMIT,
}

/**
 * One editable row of the cable list.
 *
 * @param id stable across edits and reorderings, so a row keeps its identity in
 *   the list and its errors cannot land on the wrong cable after a removal.
 */
@Immutable
data class CableRowState(
    val id: Int,
    val diameter: String = "",
    val quantity: String = DEFAULT_QUANTITY,
    val diameterError: ValidationError? = null,
    val quantityError: ValidationError? = null,
) {
    companion object {
        const val DEFAULT_QUANTITY = "1"
    }
}

@Immutable
data class ConduitFillUiState(
    val conduitDiameter: String = "",
    val rule: FillRule = FillRule.NEC_TABLE_1,
    val customLimit: String = DEFAULT_CUSTOM_LIMIT,
    val cables: ImmutableList<CableRowState> = persistentListOf(CableRowState(id = 0)),
    val errors: Map<ConduitFillField, ValidationError> = emptyMap(),
    val result: ConduitFillResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    val showCustomLimit: Boolean get() = rule == FillRule.CUSTOM

    /** The last row stays: an empty list has nothing to calculate. */
    val canRemoveCable: Boolean get() = cables.size > 1

    val canAddCable: Boolean get() = cables.size < MAX_CABLE_ROWS

    companion object {
        /** The most common European figure, and a sane starting point. */
        const val DEFAULT_CUSTOM_LIMIT = "40"

        /**
         * A form limit, not an engineering one. Beyond a dozen distinct cable
         * types the form stops being usable; identical cables go in one row
         * with a quantity anyway.
         */
        const val MAX_CABLE_ROWS = 12
    }
}

@HiltViewModel
class ConduitFillViewModel @Inject constructor(
    private val calculateFill: CalculateConduitFillUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: ResourceIdResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConduitFillUiState())
    val uiState: StateFlow<ConduitFillUiState> = _uiState.asStateFlow()

    /** Only ever increments, so a removed row's id is never reused. */
    private var nextCableId = 1

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.CONDUIT_FILL)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing --------------------------------------------------------------

    fun onConduitDiameterChange(value: String) =
        update(ConduitFillField.CONDUIT_DIAMETER) { it.copy(conduitDiameter = value) }

    fun onRuleChange(rule: FillRule) = update { it.copy(rule = rule) }

    fun onCustomLimitChange(value: String) =
        update(ConduitFillField.CUSTOM_LIMIT) { it.copy(customLimit = value) }

    fun onCableDiameterChange(id: Int, value: String) = update {
        it.mapRow(id) { row -> row.copy(diameter = value, diameterError = null) }
    }

    /** Digits only: a conduit holds a whole number of cables. */
    fun onCableQuantityChange(id: Int, value: String) = update {
        it.mapRow(id) { row ->
            row.copy(quantity = value.filter(Char::isDigit), quantityError = null)
        }
    }

    fun onAddCable() = update {
        if (!it.canAddCable) {
            it
        } else {
            it.copy(cables = (it.cables + CableRowState(id = nextCableId++)).toImmutableList())
        }
    }

    fun onRemoveCable(id: Int) = update {
        if (!it.canRemoveCable) {
            it
        } else {
            it.copy(cables = it.cables.filterNot { row -> row.id == id }.toImmutableList())
        }
    }

    private fun ConduitFillUiState.mapRow(
        id: Int,
        transform: (CableRowState) -> CableRowState,
    ): ConduitFillUiState = copy(
        cables = cables.map { if (it.id == id) transform(it) else it }.toImmutableList(),
    )

    private fun update(
        field: ConduitFillField? = null,
        transform: (ConduitFillUiState) -> ConduitFillUiState,
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
        val errors = mutableMapOf<ConduitFillField, ValidationError>()

        fun validate(
            field: ConduitFillField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
        ): Double? = NumericInput
            .validate(raw, min, max)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val conduitDiameter = validate(
            ConduitFillField.CONDUIT_DIAMETER,
            state.conduitDiameter,
            max = MAX_CONDUIT_MM,
        )

        val customLimit = if (state.rule == FillRule.CUSTOM) {
            validate(
                ConduitFillField.CUSTOM_LIMIT,
                state.customLimit,
                min = MIN_LIMIT_PERCENT,
                max = MAX_LIMIT_PERCENT,
            )
        } else {
            // Not shown, so not validated — a stale entry must not block a
            // calculation the user cannot see the field for.
            null
        }

        // Every row is validated so the user sees all the bad ones at once,
        // rather than fixing them one calculate at a time.
        val validatedRows = state.cables.map { row ->
            val diameter = NumericInput.validate(row.diameter, max = MAX_CABLE_MM)
            val quantity = NumericInput.validate(row.quantity, min = MIN_QUANTITY, max = MAX_QUANTITY)
            row.copy(
                diameterError = (diameter as? Outcome.Failure)?.error,
                quantityError = (quantity as? Outcome.Failure)?.error,
            ) to (diameter as? Outcome.Success)?.value?.let { d ->
                (quantity as? Outcome.Success)?.value?.let { q ->
                    CableBundleEntry(diameterMm = d, quantity = q.toInt())
                }
            }
        }

        val rowStates = validatedRows.map { it.first }.toImmutableList()
        val entries = validatedRows.mapNotNull { it.second }
        val rowsAreValid = entries.size == state.cables.size

        if (errors.isNotEmpty() || conduitDiameter == null || !rowsAreValid ||
            (state.rule == FillRule.CUSTOM && customLimit == null)
        ) {
            _uiState.update {
                it.copy(
                    errors = errors,
                    cables = rowStates,
                    result = null,
                    steps = persistentListOf(),
                )
            }
            return
        }

        val input = ConduitFillInput(
            conduitInnerDiameterMm = conduitDiameter,
            cables = entries,
            rule = state.rule,
            customLimitFraction = (customLimit ?: MAX_LIMIT_PERCENT) / PERCENT,
        )

        val result = calculateFill(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                cables = rowStates,
                result = result,
                steps = explainConduitFill(input, result),
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
    fun onApplyExample(example: WorkedExample<ConduitFillUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        nextCableId = 1
        _uiState.update {
            ConduitFillUiState(rule = it.rule, isFavorite = it.isFavorite)
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.CONDUIT_FILL) }
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
            if (record.calculatorId != CalculatorId.CONDUIT_FILL) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    conduitDiameter = inputs.pick(KEY_CONDUIT_DIAMETER, it.conduitDiameter),
                    rule = inputs.enumOrNull(KEY_RULE) ?: it.rule,
                    customLimit = inputs.pick(KEY_CUSTOM_LIMIT, it.customLimit),
                )
            }
            inputs[KEY_CABLE_DATA]
                ?.split(ROW_SEPARATOR)
                ?.mapIndexedNotNull { index, encoded ->
                    val parts = encoded.split(FIELD_SEPARATOR)
                    if (parts.size != 2) null
                    else CableRowState(id = index, quantity = parts[0], diameter = parts[1])
                }
                ?.takeIf { it.isNotEmpty() }
                ?.let { rows -> _uiState.update { it.copy(cables = rows.toImmutableList()) } }

            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: ConduitFillUiState, result: ConduitFillResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.CONDUIT_FILL,
            title = stringResolver.get(R.string.cf_history_title)
                .format(state.conduitDiameter, result.cableCount.toString()),
            summary = stringResolver.get(R.string.cf_history_summary)
                .format(format(result.fillFraction * PERCENT)),
            inputs = buildMap {
                put(KEY_CONDUIT_DIAMETER, state.conduitDiameter)
                put(KEY_RULE, state.rule.name)
                if (state.rule == FillRule.CUSTOM) put(KEY_CUSTOM_LIMIT, state.customLimit)
                // Flattened rather than serialised: the history screen renders
                // plain key/value pairs, and "3 × Ø 8.5 mm" reads as a cable.
                state.cables.forEachIndexed { index, row ->
                    put(
                        KEY_CABLE_PREFIX + (index + 1),
                        stringResolver.get(R.string.cf_export_cable_line)
                            .format(row.quantity, row.diameter),
                    )
                }
                // The lines above are for a reader: they are localised and carry
                // a unit, so they cannot be parsed back. Reopening the record
                // needs the raw text the user typed, which is what this is.
                put(KEY_CABLE_DATA, state.cables.joinToString(ROW_SEPARATOR) {
                    it.quantity + FIELD_SEPARATOR + it.diameter
                })
            },
            results = mapOf(
                KEY_FILL_PERCENT to format(result.fillFraction * PERCENT),
                KEY_PERMITTED_PERCENT to format(result.permittedFraction * PERCENT),
                KEY_CONDUIT_AREA to format(result.conduitAreaMm2),
                KEY_CABLE_AREA to format(result.cableAreaMm2),
                KEY_SPARE_AREA to format(result.spareAreaMm2),
                KEY_WITHIN_LIMIT to result.isWithinLimit.toString(),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val PERCENT = 100.0
        const val MAX_CONDUIT_MM = 1_000.0
        const val MAX_CABLE_MM = 500.0
        const val MIN_QUANTITY = 1.0
        const val MAX_QUANTITY = 500.0
        const val MIN_LIMIT_PERCENT = 1.0
        const val MAX_LIMIT_PERCENT = 100.0

        const val KEY_CONDUIT_DIAMETER = "conduit_inner_diameter_mm"
        const val KEY_RULE = "fill_rule"
        const val KEY_CUSTOM_LIMIT = "custom_limit_percent"
        const val KEY_CABLE_PREFIX = "cable_"
        const val KEY_CABLE_DATA = "cable_rows"

        /** Neither can appear in a number, in any locale. */
        const val ROW_SEPARATOR = ";"
        const val FIELD_SEPARATOR = "|"

        const val KEY_FILL_PERCENT = "fill_percent"
        const val KEY_PERMITTED_PERCENT = "permitted_percent"
        const val KEY_CONDUIT_AREA = "conduit_area_mm2"
        const val KEY_CABLE_AREA = "cable_area_mm2"
        const val KEY_SPARE_AREA = "spare_area_mm2"
        const val KEY_WITHIN_LIMIT = "within_limit"
    }
}
