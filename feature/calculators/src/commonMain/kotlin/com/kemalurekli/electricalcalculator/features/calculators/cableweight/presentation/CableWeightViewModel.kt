package com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightInput
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightResult
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CalculateCableWeightUseCase
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sqrt
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_history_summary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_history_title

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

class CableWeightViewModel(
    private val calculateWeight: CalculateCableWeightUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CableWeightUiState())
    val uiState: StateFlow<CableWeightUiState> = _uiState.asStateFlow()

    /**
     * The user's settings, read once when the calculator opens.
     *
     * Held rather than observed on purpose: a form must not be rewritten
     * underneath the user because Settings changed while this screen sat on the
     * back stack.
     */
    private var defaults = EngineeringDefaults.Default

    /**
     * Whether anything has yet written to the form.
     *
     * The defaults are read asynchronously, so they can in principle land after
     * the first edit, after an example has been applied, or after a saved
     * calculation has been reopened. In each of those cases the form already
     * says something more specific than a default and must be left alone. The
     * flag is set at the *call*, ahead of any suspension, so ordering between
     * the two coroutines cannot decide the outcome.
     */
    private var formTouched = false

    init {
        viewModelScope.launch {
            defaults = userPreferences.engineeringDefaults()
            if (!formTouched) _uiState.update { it.withDefaults(defaults) }
        }
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
        formTouched = true
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
        formTouched = true
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

    /**
     * Reloads a saved calculation into the form.
     *
     * The record stores the text the reader typed rather than the parsed number,
     * so the form comes back exactly as it was left — see [CalculationRecord].
     * A key the record does not carry keeps the form's current value, which is
     * what lets a record written before a field existed still open.
     */
    fun onRestore(recordId: Long) {
        formTouched = true
        viewModelScope.launch {
            val record = historyRepository.findById(recordId) ?: return@launch
            if (record.calculatorId != CalculatorId.CABLE_WEIGHT) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    material = inputs.enumOrNull(KEY_MATERIAL) ?: it.material,
                    crossSection = inputs.pick(KEY_AREA, it.crossSection),
                    conductorCount = inputs.pick(KEY_COUNT, it.conductorCount),
                    length = inputs.pick(KEY_LENGTH, it.length),
                    diameter = inputs.pick(KEY_DIAMETER, it.diameter),
                    insulation = inputs.enumOrNull(KEY_INSULATION) ?: it.insulation,
                )
            }
            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: CableWeightUiState, result: CableWeightResult) {
        // The complete-cable mass is the headline when it exists; otherwise the
        // conductor mass is all that was asked for.
        val headline = result.totalMassKg ?: result.conductorMassKg

        val record = CalculationRecord(
            calculatorId = CalculatorId.CABLE_WEIGHT,
            title = stringResolver.get(Res.string.cw_history_title, state.conductorCount, state.crossSection, state.length),
            summary = stringResolver.get(Res.string.cw_history_summary, format(headline)),
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

/**
 * The user's engineering defaults, applied to a form that is still untouched.
 *
 * Only the fields this calculator shares with Settings move.
 */
private fun CableWeightUiState.withDefaults(defaults: EngineeringDefaults) = copy(
    material = defaults.material,
    insulation = defaults.insulation,
)
