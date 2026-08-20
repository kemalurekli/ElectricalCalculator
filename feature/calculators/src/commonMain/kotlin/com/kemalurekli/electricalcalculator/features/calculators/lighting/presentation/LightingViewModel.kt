package com.kemalurekli.electricalcalculator.features.calculators.lighting.presentation

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
import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.CalculateLightingUseCase
import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.LightingInput
import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.LightingResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.lt_history_summary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.lt_history_title

/** Identifies a form field so validation errors can be routed back to it. */
enum class LightingField {
    ILLUMINANCE,
    LENGTH,
    WIDTH,
    MOUNTING_HEIGHT,
    FLUX,
    UTILISATION,
    MAINTENANCE,
}

@Immutable
data class LightingUiState(
    val illuminance: String = DEFAULT_ILLUMINANCE,
    val length: String = "",
    val width: String = "",
    val mountingHeight: String = DEFAULT_MOUNTING_HEIGHT,
    val flux: String = "",
    val utilisation: String = DEFAULT_UTILISATION,
    val maintenance: String = DEFAULT_MAINTENANCE,
    val errors: Map<LightingField, ValidationError> = emptyMap(),
    val result: LightingResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    companion object {
        /** EN 12464-1 for general office work — the most common design figure. */
        const val DEFAULT_ILLUMINANCE = "500"

        /** A 3 m ceiling over a 0,8 m desk, which is the ordinary office. */
        const val DEFAULT_MOUNTING_HEIGHT = "2.2"

        /**
         * A mid-range figure for a light room of ordinary proportions.
         *
         * Offered because a form that opens blank here invites a guess, and a
         * guess is worse than a stated middle the reader can correct from their
         * own luminaire's table. The notes say where to get the real one.
         */
        const val DEFAULT_UTILISATION = "0.6"

        /** A clean interior on a normal cleaning cycle. */
        const val DEFAULT_MAINTENANCE = "0.8"
    }
}

class LightingViewModel(
    private val calculateLighting: CalculateLightingUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LightingUiState())
    val uiState: StateFlow<LightingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.LIGHTING_LUMEN)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing -----------------------------------------------------------

    fun onIlluminanceChange(value: String) =
        update(LightingField.ILLUMINANCE) { it.copy(illuminance = value) }

    fun onLengthChange(value: String) = update(LightingField.LENGTH) { it.copy(length = value) }

    fun onWidthChange(value: String) = update(LightingField.WIDTH) { it.copy(width = value) }

    fun onMountingHeightChange(value: String) =
        update(LightingField.MOUNTING_HEIGHT) { it.copy(mountingHeight = value) }

    fun onFluxChange(value: String) = update(LightingField.FLUX) { it.copy(flux = value) }

    fun onUtilisationChange(value: String) =
        update(LightingField.UTILISATION) { it.copy(utilisation = value) }

    fun onMaintenanceChange(value: String) =
        update(LightingField.MAINTENANCE) { it.copy(maintenance = value) }

    private fun update(
        field: LightingField? = null,
        transform: (LightingUiState) -> LightingUiState,
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
        val errors = mutableMapOf<LightingField, ValidationError>()

        fun validate(
            field: LightingField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
        ): Double? = NumericInput
            .validate(raw, min, max)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val lux = validate(LightingField.ILLUMINANCE, state.illuminance, max = MAX_LUX)
        val length = validate(LightingField.LENGTH, state.length, max = MAX_DIMENSION)
        val width = validate(LightingField.WIDTH, state.width, max = MAX_DIMENSION)
        val height = validate(
            LightingField.MOUNTING_HEIGHT,
            state.mountingHeight,
            max = MAX_DIMENSION,
        )
        val flux = validate(LightingField.FLUX, state.flux, max = MAX_FLUX)
        val uf = validate(LightingField.UTILISATION, state.utilisation, min = MIN_FACTOR, max = 1.0)
        val mf = validate(LightingField.MAINTENANCE, state.maintenance, min = MIN_FACTOR, max = 1.0)

        if (errors.isNotEmpty() || lux == null || length == null || width == null ||
            height == null || flux == null || uf == null || mf == null
        ) {
            _uiState.update {
                it.copy(errors = errors, result = null, steps = persistentListOf())
            }
            return
        }

        val input = LightingInput(
            targetIlluminanceLux = lux,
            roomLengthMetres = length,
            roomWidthMetres = width,
            mountingHeightMetres = height,
            luminousFluxPerLuminaireLumens = flux,
            utilisationFactor = uf,
            maintenanceFactor = mf,
        )
        val result = calculateLighting(input)

        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainLighting(input, result),
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
    fun onApplyExample(example: WorkedExample<LightingUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update { LightingUiState(isFavorite = it.isFavorite) }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.LIGHTING_LUMEN) }
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
            if (record.calculatorId != CalculatorId.LIGHTING_LUMEN) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    illuminance = inputs.pick(KEY_LUX, it.illuminance),
                    length = inputs.pick(KEY_LENGTH, it.length),
                    width = inputs.pick(KEY_WIDTH, it.width),
                    mountingHeight = inputs.pick(KEY_HEIGHT, it.mountingHeight),
                    flux = inputs.pick(KEY_FLUX, it.flux),
                    utilisation = inputs.pick(KEY_UF, it.utilisation),
                    maintenance = inputs.pick(KEY_MF, it.maintenance),
                )
            }
            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: LightingUiState, result: LightingResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.LIGHTING_LUMEN,
            title = stringResolver.get(Res.string.lt_history_title, state.length, state.width, state.illuminance),
            summary = stringResolver.get(Res.string.lt_history_summary, result.luminaireCount.toString()),
            inputs = mapOf(
                KEY_LUX to state.illuminance,
                KEY_LENGTH to state.length,
                KEY_WIDTH to state.width,
                KEY_HEIGHT to state.mountingHeight,
                KEY_FLUX to state.flux,
                KEY_UF to state.utilisation,
                KEY_MF to state.maintenance,
            ),
            results = mapOf(
                KEY_COUNT to result.luminaireCount.toString(),
                KEY_ACHIEVED to format(result.achievedIlluminanceLux),
                KEY_ROOM_INDEX to format(result.roomIndex),
                KEY_AREA to format(result.areaSquareMetres),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_LUX = 20_000.0
        const val MAX_DIMENSION = 500.0
        const val MAX_FLUX = 500_000.0
        const val MIN_FACTOR = 0.05

        const val KEY_LUX = "target_lux"
        const val KEY_LENGTH = "room_length"
        const val KEY_WIDTH = "room_width"
        const val KEY_HEIGHT = "mounting_height"
        const val KEY_FLUX = "luminous_flux"
        const val KEY_UF = "utilisation_factor"
        const val KEY_MF = "maintenance_factor"

        const val KEY_COUNT = "luminaire_count"
        const val KEY_ACHIEVED = "achieved_lux"
        const val KEY_ROOM_INDEX = "room_index"
        const val KEY_AREA = "area"
    }
}
