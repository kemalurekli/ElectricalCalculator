package com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.CableBundleEntry
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.CalculateTrayFillUseCase
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayArrangement
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillInput
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillResult
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cf_export_cable_line
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tf_history_summary_area
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tf_history_summary_width
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tf_history_title

/** Identifies a form-level field so validation errors can be routed back to it. */
enum class TrayFillField {
    TRAY_WIDTH,
    TRAY_DEPTH,
    SPACING,
    LIMIT,
}

/**
 * One editable row of the cable list.
 *
 * @param id stable across edits, so a row keeps its identity and its errors
 *   cannot land on the wrong cable after a removal.
 */
@Immutable
data class TrayCableRowState(
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
data class TrayFillUiState(
    val trayWidth: String = "",
    val trayDepth: String = "",
    val arrangement: TrayArrangement = TrayArrangement.SINGLE_LAYER,
    val spacing: String = DEFAULT_SPACING,
    val limit: String = DEFAULT_LIMIT,
    val cables: ImmutableList<TrayCableRowState> = persistentListOf(TrayCableRowState(id = 0)),
    val errors: Map<TrayFillField, ValidationError> = emptyMap(),
    val result: TrayFillResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    /** Depth and an area limit only mean something once cables are stacked. */
    val showDepthAndLimit: Boolean get() = arrangement == TrayArrangement.MULTI_LAYER

    /** Spacing is a single-layer concern: stacked cables are not spread out. */
    val showSpacing: Boolean get() = arrangement == TrayArrangement.SINGLE_LAYER

    val canRemoveCable: Boolean get() = cables.size > 1

    val canAddCable: Boolean get() = cables.size < MAX_CABLE_ROWS

    companion object {
        /** Touching, which is how cables land unless the design says otherwise. */
        const val DEFAULT_SPACING = "0"

        const val DEFAULT_LIMIT = "40"

        /** A form limit, not an engineering one — identical cables share a row. */
        const val MAX_CABLE_ROWS = 12
    }
}

class TrayFillViewModel(
    private val calculateFill: CalculateTrayFillUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrayFillUiState())
    val uiState: StateFlow<TrayFillUiState> = _uiState.asStateFlow()

    /** Only ever increments, so a removed row's id is never reused. */
    private var nextCableId = 1

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.CABLE_TRAY_FILL)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing --------------------------------------------------------------

    fun onTrayWidthChange(value: String) =
        update(TrayFillField.TRAY_WIDTH) { it.copy(trayWidth = value) }

    fun onTrayDepthChange(value: String) =
        update(TrayFillField.TRAY_DEPTH) { it.copy(trayDepth = value) }

    fun onArrangementChange(arrangement: TrayArrangement) =
        update { it.copy(arrangement = arrangement) }

    fun onSpacingChange(value: String) =
        update(TrayFillField.SPACING) { it.copy(spacing = value) }

    fun onLimitChange(value: String) = update(TrayFillField.LIMIT) { it.copy(limit = value) }

    fun onCableDiameterChange(id: Int, value: String) = update {
        it.mapRow(id) { row -> row.copy(diameter = value, diameterError = null) }
    }

    /** Digits only: a tray carries a whole number of cables. */
    fun onCableQuantityChange(id: Int, value: String) = update {
        it.mapRow(id) { row ->
            row.copy(quantity = value.filter(Char::isDigit), quantityError = null)
        }
    }

    fun onAddCable() = update {
        if (!it.canAddCable) {
            it
        } else {
            it.copy(cables = (it.cables + TrayCableRowState(id = nextCableId++)).toImmutableList())
        }
    }

    fun onRemoveCable(id: Int) = update {
        if (!it.canRemoveCable) {
            it
        } else {
            it.copy(cables = it.cables.filterNot { row -> row.id == id }.toImmutableList())
        }
    }

    private fun TrayFillUiState.mapRow(
        id: Int,
        transform: (TrayCableRowState) -> TrayCableRowState,
    ): TrayFillUiState = copy(
        cables = cables.map { if (it.id == id) transform(it) else it }.toImmutableList(),
    )

    private fun update(
        field: TrayFillField? = null,
        transform: (TrayFillUiState) -> TrayFillUiState,
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
        val errors = mutableMapOf<TrayFillField, ValidationError>()

        fun validate(
            field: TrayFillField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
            allowZero: Boolean = false,
        ): Double? = NumericInput
            .validate(raw, min, max, allowZero = allowZero)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val width = validate(TrayFillField.TRAY_WIDTH, state.trayWidth, max = MAX_TRAY_MM)

        // Only the fields the chosen arrangement actually shows are validated:
        // rejecting a form over a hidden field leaves the user nothing to fix.
        val depth = if (state.showDepthAndLimit) {
            validate(TrayFillField.TRAY_DEPTH, state.trayDepth, max = MAX_TRAY_MM)
        } else {
            null
        }
        val limit = if (state.showDepthAndLimit) {
            validate(TrayFillField.LIMIT, state.limit, min = MIN_LIMIT_PERCENT, max = MAX_LIMIT_PERCENT)
        } else {
            null
        }
        // Zero is the normal answer here, not a mistake: it means touching.
        val spacing = if (state.showSpacing) {
            validate(TrayFillField.SPACING, state.spacing, max = MAX_SPACING_MM, allowZero = true)
        } else {
            null
        }

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

        val missingRequired = width == null ||
            (state.showDepthAndLimit && (depth == null || limit == null)) ||
            (state.showSpacing && spacing == null)

        if (errors.isNotEmpty() || missingRequired || !rowsAreValid) {
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

        val input = TrayFillInput(
            trayWidthMm = requireNotNull(width),
            trayDepthMm = depth ?: 0.0,
            cables = entries,
            arrangement = state.arrangement,
            clearSpacingMm = spacing ?: 0.0,
            permittedFillFraction = (limit ?: DEFAULT_LIMIT_PERCENT) / PERCENT,
        )

        val result = calculateFill(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                cables = rowStates,
                result = result,
                steps = explainTrayFill(input, result),
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
    fun onApplyExample(example: WorkedExample<TrayFillUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        nextCableId = 1
        _uiState.update {
            TrayFillUiState(arrangement = it.arrangement, isFavorite = it.isFavorite)
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.CABLE_TRAY_FILL) }
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
            if (record.calculatorId != CalculatorId.CABLE_TRAY_FILL) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    trayWidth = inputs.pick(KEY_WIDTH, it.trayWidth),
                    arrangement = inputs.enumOrNull(KEY_ARRANGEMENT) ?: it.arrangement,
                    trayDepth = inputs.pick(KEY_DEPTH, it.trayDepth),
                    limit = inputs.pick(KEY_LIMIT, it.limit),
                    spacing = inputs.pick(KEY_SPACING, it.spacing),
                )
            }
            inputs[KEY_CABLE_DATA]
                ?.split(ROW_SEPARATOR)
                ?.mapIndexedNotNull { index, encoded ->
                    val parts = encoded.split(FIELD_SEPARATOR)
                    if (parts.size != 2) null
                    else TrayCableRowState(id = index, quantity = parts[0], diameter = parts[1])
                }
                ?.takeIf { it.isNotEmpty() }
                ?.let { rows -> _uiState.update { it.copy(cables = rows.toImmutableList()) } }

            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: TrayFillUiState, result: TrayFillResult) {
        // The headline differs by arrangement because the binding constraint
        // does: a single layer is a width, a stack is a percentage.
        val summary = when (result) {
            is TrayFillResult.SingleLayer -> stringResolver
                .get(Res.string.tf_history_summary_width, format(result.requiredWidthMm))

            is TrayFillResult.MultiLayer -> stringResolver
                .get(Res.string.tf_history_summary_area, format(result.fillFraction * PERCENT))
        }

        val record = CalculationRecord(
            calculatorId = CalculatorId.CABLE_TRAY_FILL,
            title = stringResolver.get(Res.string.tf_history_title, state.trayWidth, result.cableCount.toString()),
            summary = summary,
            inputs = buildMap {
                put(KEY_WIDTH, state.trayWidth)
                put(KEY_ARRANGEMENT, state.arrangement.name)
                if (state.showDepthAndLimit) {
                    put(KEY_DEPTH, state.trayDepth)
                    put(KEY_LIMIT, state.limit)
                }
                if (state.showSpacing) put(KEY_SPACING, state.spacing)
                state.cables.forEachIndexed { index, row ->
                    put(
                        KEY_CABLE_PREFIX + (index + 1),
                        stringResolver.get(Res.string.cf_export_cable_line, row.quantity, row.diameter),
                    )
                }
                // The lines above are for a reader: they are localised and carry
                // a unit, so they cannot be parsed back. Reopening the record
                // needs the raw text the user typed, which is what this is.
                put(KEY_CABLE_DATA, state.cables.joinToString(ROW_SEPARATOR) {
                    it.quantity + FIELD_SEPARATOR + it.diameter
                })
            },
            results = buildMap {
                put(KEY_CABLE_AREA, format(result.cableAreaMm2))
                put(KEY_WITHIN_LIMIT, result.isWithinLimit.toString())
                when (result) {
                    is TrayFillResult.SingleLayer -> {
                        put(KEY_REQUIRED_WIDTH, format(result.requiredWidthMm))
                        put(KEY_SPARE_WIDTH, format(result.spareWidthMm))
                        put(KEY_WIDTH_USED_PERCENT, format(result.widthUsedFraction * PERCENT))
                    }

                    is TrayFillResult.MultiLayer -> {
                        put(KEY_FILL_PERCENT, format(result.fillFraction * PERCENT))
                        put(KEY_PERMITTED_PERCENT, format(result.permittedFraction * PERCENT))
                        put(KEY_SPARE_AREA, format(result.spareAreaMm2))
                        put(KEY_LAYERS, result.estimatedLayers.toString())
                    }
                }
            },
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val PERCENT = 100.0
        const val DEFAULT_LIMIT_PERCENT = 40.0
        const val MAX_TRAY_MM = 5_000.0
        const val MAX_SPACING_MM = 1_000.0
        const val MAX_CABLE_MM = 500.0
        const val MIN_QUANTITY = 1.0
        const val MAX_QUANTITY = 500.0
        const val MIN_LIMIT_PERCENT = 1.0
        const val MAX_LIMIT_PERCENT = 100.0

        const val KEY_WIDTH = "tray_width_mm"
        const val KEY_DEPTH = "tray_depth_mm"
        const val KEY_ARRANGEMENT = "arrangement"
        const val KEY_SPACING = "clear_spacing_mm"
        const val KEY_LIMIT = "permitted_fill_percent"
        const val KEY_CABLE_PREFIX = "cable_"
        const val KEY_CABLE_DATA = "cable_rows"

        /** Neither can appear in a number, in any locale. */
        const val ROW_SEPARATOR = ";"
        const val FIELD_SEPARATOR = "|"

        const val KEY_CABLE_AREA = "cable_area_mm2"
        const val KEY_WITHIN_LIMIT = "within_limit"
        const val KEY_REQUIRED_WIDTH = "required_width_mm"
        const val KEY_SPARE_WIDTH = "spare_width_mm"
        const val KEY_WIDTH_USED_PERCENT = "width_used_percent"
        const val KEY_FILL_PERCENT = "fill_percent"
        const val KEY_PERMITTED_PERCENT = "permitted_percent"
        const val KEY_SPARE_AREA = "spare_area_mm2"
        const val KEY_LAYERS = "estimated_layers"
    }
}
