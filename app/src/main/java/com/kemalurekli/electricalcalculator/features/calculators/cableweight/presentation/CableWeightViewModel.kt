package com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightInput
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightResult
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CalculateCableWeightUseCase
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
import kotlin.math.PI
import kotlin.math.sqrt

/** Identifies a form field so validation errors can be routed back to it. */
enum class CableWeightField {
    CROSS_SECTION,
    CONDUCTOR_COUNT,
    LENGTH,
    DIAMETER,
}

@Immutable
data class CableWeightUiState(
    val material: ConductorMaterial = ConductorMaterial.COPPER,
    val insulation: CableInsulation = CableInsulation.PVC,
    val crossSection: String = "",
    val conductorCount: String = DEFAULT_CONDUCTOR_COUNT,
    val length: String = "",
    val diameter: String = "",
    val errors: Map<CableWeightField, ValidationError> = emptyMap(),
    val diameterTooSmallFor: Double? = null,
    val result: CableWeightResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    /**
     * The sheath material only affects the estimate, so the selector is hidden
     * until a diameter makes that estimate possible.
     */
    val showInsulation: Boolean get() = diameter.isNotBlank()

    companion object {
        /** Three lines, a neutral and a protective conductor. */
        const val DEFAULT_CONDUCTOR_COUNT = "5"
    }
}

@HiltViewModel
class CableWeightViewModel @Inject constructor(
    private val calculateWeight: CalculateCableWeightUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CableWeightUiState())
    val uiState: StateFlow<CableWeightUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository
                .observeIsFavorite(CalculatorId.CABLE_WEIGHT)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing --------------------------------------------------------------

    fun onMaterialChange(material: ConductorMaterial) = update { it.copy(material = material) }

    fun onInsulationChange(insulation: CableInsulation) =
        update { it.copy(insulation = insulation) }

    fun onCrossSectionChange(value: String) =
        update(CableWeightField.CROSS_SECTION) { it.copy(crossSection = value) }

    /**
     * Digits only. A cable has a whole number of cores, and filtering the
     * separator out as it is typed beats truncating "3.7" to 3 behind the
     * user's back.
     */
    fun onConductorCountChange(value: String) =
        update(CableWeightField.CONDUCTOR_COUNT) {
            it.copy(conductorCount = value.filter(Char::isDigit))
        }

    fun onLengthChange(value: String) =
        update(CableWeightField.LENGTH) { it.copy(length = value) }

    fun onDiameterChange(value: String) =
        update(CableWeightField.DIAMETER) { it.copy(diameter = value) }

    private fun update(
        field: CableWeightField? = null,
        transform: (CableWeightUiState) -> CableWeightUiState,
    ) {
        _uiState.update { current ->
            transform(current).copy(
                result = null,
                steps = persistentListOf(),
                diameterTooSmallFor = null,
                errors = if (field == null) current.errors else current.errors - field,
            )
        }
    }

    // -- Calculation ------------------------------------------------------------------

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<CableWeightField, ValidationError>()

        fun validate(
            field: CableWeightField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
        ): Double? = NumericInput
            .validate(raw, min, max)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val area = validate(CableWeightField.CROSS_SECTION, state.crossSection, max = MAX_AREA_MM2)
        val count = validate(
            CableWeightField.CONDUCTOR_COUNT,
            state.conductorCount,
            min = MIN_CONDUCTORS,
            max = MAX_CONDUCTORS,
        )
        val length = validate(CableWeightField.LENGTH, state.length, max = MAX_LENGTH_M)

        // The diameter is optional: without it the conductor mass is still
        // exact, and the sheath estimate is simply not offered.
        val diameter = if (state.diameter.isBlank()) {
            null
        } else {
            validate(CableWeightField.DIAMETER, state.diameter, max = MAX_DIAMETER_MM)
        }

        // A cable cannot be thinner than the metal inside it. Caught here so the
        // user gets the minimum diameter rather than a mass with no sheath.
        var minimumDiameter: Double? = null
        if (area != null && count != null && diameter != null) {
            val required = sqrt(4.0 * area * count / PI)
            if (diameter < required) {
                minimumDiameter = required
                errors[CableWeightField.DIAMETER] = ValidationError.BelowMinimum(required)
            }
        }

        if (errors.isNotEmpty() || area == null || count == null || length == null) {
            _uiState.update {
                it.copy(
                    errors = errors,
                    diameterTooSmallFor = minimumDiameter,
                    result = null,
                    steps = persistentListOf(),
                )
            }
            return
        }

        val input = CableWeightInput(
            crossSectionMm2 = area,
            conductorCount = count.toInt(),
            lengthMeters = length,
            material = state.material,
            overallDiameterMm = diameter,
            insulation = state.insulation,
        )

        val result = calculateWeight(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                diameterTooSmallFor = null,
                result = result,
                steps = explainCableWeight(input, result),
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
    fun onApplyExample(example: WorkedExample<CableWeightUiState>) {
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update {
            CableWeightUiState(
                material = it.material,
                insulation = it.insulation,
                isFavorite = it.isFavorite,
            )
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.CABLE_WEIGHT) }
    }

    private fun saveToHistory(state: CableWeightUiState, result: CableWeightResult) {
        // The complete-cable mass is the headline when it exists; otherwise the
        // conductor mass is all that was asked for.
        val headline = result.totalMassKg ?: result.conductorMassKg

        val record = CalculationRecord(
            calculatorId = CalculatorId.CABLE_WEIGHT,
            title = stringResolver.get(R.string.cw_history_title)
                .format(state.conductorCount, state.crossSection, state.length),
            summary = stringResolver.get(R.string.cw_history_summary).format(format(headline)),
            inputs = buildMap {
                put(KEY_MATERIAL, state.material.name)
                put(KEY_AREA, state.crossSection)
                put(KEY_COUNT, state.conductorCount)
                put(KEY_LENGTH, state.length)
                if (state.diameter.isNotBlank()) {
                    put(KEY_DIAMETER, state.diameter)
                    put(KEY_INSULATION, state.insulation.name)
                }
            },
            results = buildMap {
                put(KEY_CONDUCTOR_MASS, format(result.conductorMassKg))
                put(KEY_CONDUCTOR_MASS_PER_M, format(result.conductorMassPerMeterKg))
                result.nonConductorMassKg?.let { put(KEY_SHEATH_MASS, format(it)) }
                result.totalMassKg?.let { put(KEY_TOTAL_MASS, format(it)) }
                result.totalMassPerMeterKg?.let { put(KEY_TOTAL_MASS_PER_M, format(it)) }
            },
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double) = NumberFormatter.format(value, decimals = 2)

    private companion object {
        const val MAX_AREA_MM2 = 5_000.0
        const val MIN_CONDUCTORS = 1.0
        const val MAX_CONDUCTORS = 100.0
        const val MAX_LENGTH_M = 100_000.0
        const val MAX_DIAMETER_MM = 500.0

        const val KEY_MATERIAL = "material"
        const val KEY_AREA = "cross_section_mm2"
        const val KEY_COUNT = "conductor_count"
        const val KEY_LENGTH = "length_m"
        const val KEY_DIAMETER = "overall_diameter_mm"
        const val KEY_INSULATION = "insulation"

        const val KEY_CONDUCTOR_MASS = "conductor_mass_kg"
        const val KEY_CONDUCTOR_MASS_PER_M = "conductor_mass_kg_per_m"
        const val KEY_SHEATH_MASS = "sheath_mass_kg"
        const val KEY_TOTAL_MASS = "total_mass_kg"
        const val KEY_TOTAL_MASS_PER_M = "total_mass_kg_per_m"
    }
}
