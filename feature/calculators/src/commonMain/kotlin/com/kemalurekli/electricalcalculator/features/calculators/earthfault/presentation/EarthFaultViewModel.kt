package com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation

import com.kemalurekli.electricalcalculator.core.common.util.seededDecimal
import org.jetbrains.compose.resources.StringResource
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
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.CalculateEarthFaultUseCase
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultInput
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultResult
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_b
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_c
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_custom
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_d
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_rcd
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_history_summary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_history_title

/** Identifies a form field so validation errors can be routed back to it. */
enum class EarthFaultField {
    EXTERNAL_IMPEDANCE,
    VOLTAGE,
    LENGTH,
    LINE_SECTION,
    PROTECTIVE_SECTION,
    PARALLEL,
    DEVICE_RATING,
    CLEARING_TIME,
}

@Immutable
data class EarthFaultUiState(
    val deviceType: ProtectiveDeviceType = ProtectiveDeviceType.MCB_TYPE_B,
    val material: ConductorMaterial = ConductorMaterial.COPPER,
    val insulation: CableInsulation = CableInsulation.PVC,
    val externalImpedance: String = DEFAULT_EXTERNAL_IMPEDANCE,
    val voltage: String = DEFAULT_VOLTAGE,
    val length: String = "",
    val lineSection: String = "",
    val protectiveSection: String = "",
    val parallelConductors: String = DEFAULT_PARALLEL,
    val deviceRating: String = "",
    val clearingTime: String = DEFAULT_CLEARING_TIME,
    val errors: Map<EarthFaultField, ValidationError> = emptyMap(),
    val result: EarthFaultResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    companion object {
        /** Line-to-earth on a 230/400 V system. */
        const val DEFAULT_VOLTAGE = "230"

        /**
         * The distributor's typical maximum for TN-C-S, the most common
         * arrangement. Offered rather than left blank because a designer
         * checking a circuit usually has this figure as an assumption long
         * before they have a measurement — but it is an assumption, and the
         * field's hint says which system each typical value belongs to.
         */
        val DEFAULT_EXTERNAL_IMPEDANCE get() = "0.35".seededDecimal()

        const val DEFAULT_PARALLEL = "1"

        /** An MCB in its magnetic range; the middle of the usual 0.01–0.1 s. */
        val DEFAULT_CLEARING_TIME get() = "0.1".seededDecimal()

        /** The customary residual rating for additional protection. */
        val DEFAULT_RESIDUAL_RATING get() = "0.03".seededDecimal()
    }
}

class EarthFaultViewModel(
    private val calculateLoop: CalculateEarthFaultUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EarthFaultUiState())
    val uiState: StateFlow<EarthFaultUiState> = _uiState.asStateFlow()

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
                .observeIsFavorite(CalculatorId.EARTH_FAULT_LOOP)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing --------------------------------------------------------------

    /**
     * The rating field means three different quantities depending on the device
     * — In, Ia, or IΔn — and they differ by orders of magnitude. Carrying a
     * 32 A entry into an RCD would read as 32 A of residual current, so the
     * field is cleared to that device's sensible default instead.
     */
    fun onDeviceTypeChange(deviceType: ProtectiveDeviceType) = update {
        val ratingChangesMeaning = it.deviceType.isResidualCurrent != deviceType.isResidualCurrent
        it.copy(
            deviceType = deviceType,
            deviceRating = when {
                !ratingChangesMeaning -> it.deviceRating
                deviceType.isResidualCurrent -> EarthFaultUiState.DEFAULT_RESIDUAL_RATING
                else -> ""
            },
        )
    }

    fun onMaterialChange(material: ConductorMaterial) = update { it.copy(material = material) }

    fun onInsulationChange(insulation: CableInsulation) = update { it.copy(insulation = insulation) }

    fun onExternalImpedanceChange(value: String) =
        update(EarthFaultField.EXTERNAL_IMPEDANCE) { it.copy(externalImpedance = value) }

    fun onVoltageChange(value: String) =
        update(EarthFaultField.VOLTAGE) { it.copy(voltage = value) }

    fun onLengthChange(value: String) =
        update(EarthFaultField.LENGTH) { it.copy(length = value) }

    fun onLineSectionChange(value: String) =
        update(EarthFaultField.LINE_SECTION) { it.copy(lineSection = value) }

    fun onProtectiveSectionChange(value: String) =
        update(EarthFaultField.PROTECTIVE_SECTION) { it.copy(protectiveSection = value) }

    /** Digits only: a circuit has a whole number of conductors per phase. */
    fun onParallelChange(value: String) =
        update(EarthFaultField.PARALLEL) {
            it.copy(parallelConductors = value.filter(Char::isDigit))
        }

    fun onDeviceRatingChange(value: String) =
        update(EarthFaultField.DEVICE_RATING) { it.copy(deviceRating = value) }

    fun onClearingTimeChange(value: String) =
        update(EarthFaultField.CLEARING_TIME) { it.copy(clearingTime = value) }

    private fun update(
        field: EarthFaultField? = null,
        transform: (EarthFaultUiState) -> EarthFaultUiState,
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

    // -- Calculation ------------------------------------------------------------------

    fun onCalculate() {
        val state = _uiState.value
        val errors = mutableMapOf<EarthFaultField, ValidationError>()

        fun validate(
            field: EarthFaultField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
            allowZero: Boolean = false,
        ): Double? = NumericInput
            .validate(raw, min, max, allowZero = allowZero)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        // Zero is meaningful at the origin of an installation fed directly.
        val externalImpedance = validate(
            EarthFaultField.EXTERNAL_IMPEDANCE,
            state.externalImpedance,
            max = MAX_IMPEDANCE,
            allowZero = true,
        )
        val voltage = validate(EarthFaultField.VOLTAGE, state.voltage, max = MAX_VOLTAGE)
        val length = validate(EarthFaultField.LENGTH, state.length, max = MAX_LENGTH)
        val lineSection = validate(EarthFaultField.LINE_SECTION, state.lineSection, max = MAX_AREA)
        val protectiveSection = validate(
            EarthFaultField.PROTECTIVE_SECTION,
            state.protectiveSection,
            max = MAX_AREA,
        )
        val parallel = validate(
            EarthFaultField.PARALLEL,
            state.parallelConductors,
            min = MIN_PARALLEL,
            max = MAX_PARALLEL,
        )
        val rating = validate(
            EarthFaultField.DEVICE_RATING,
            state.deviceRating,
            max = if (state.deviceType.isResidualCurrent) MAX_RESIDUAL else MAX_DEVICE_RATING,
        )
        val clearingTime = validate(
            EarthFaultField.CLEARING_TIME,
            state.clearingTime,
            max = MAX_CLEARING_TIME,
        )

        if (errors.isNotEmpty() || externalImpedance == null || voltage == null ||
            length == null || lineSection == null || protectiveSection == null ||
            parallel == null || rating == null || clearingTime == null
        ) {
            _uiState.update { it.copy(errors = errors, result = null, steps = persistentListOf()) }
            return
        }

        val input = EarthFaultInput(
            externalImpedanceOhms = externalImpedance,
            phaseVoltage = voltage,
            lengthMetres = length,
            lineCrossSectionMm2 = lineSection,
            protectiveCrossSectionMm2 = protectiveSection,
            parallelConductors = parallel.toInt(),
            material = state.material,
            insulation = state.insulation,
            deviceType = state.deviceType,
            deviceRatingAmps = rating,
            clearingTimeSeconds = clearingTime,
        )

        val result = calculateLoop(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainEarthFault(input, result),
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
    fun onApplyExample(example: WorkedExample<EarthFaultUiState>) {
        formTouched = true
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update {
            EarthFaultUiState(
                deviceType = it.deviceType,
                material = it.material,
                insulation = it.insulation,
                voltage = defaults.singlePhaseVoltage,
                deviceRating = if (it.deviceType.isResidualCurrent) {
                    EarthFaultUiState.DEFAULT_RESIDUAL_RATING
                } else {
                    ""
                },
                isFavorite = it.isFavorite,
            )
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.EARTH_FAULT_LOOP) }
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
            if (record.calculatorId != CalculatorId.EARTH_FAULT_LOOP) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    externalImpedance = inputs.pick(KEY_ZE, it.externalImpedance),
                    voltage = inputs.pick(KEY_VOLTAGE, it.voltage),
                    length = inputs.pick(KEY_LENGTH, it.length),
                    lineSection = inputs.pick(KEY_LINE, it.lineSection),
                    protectiveSection = inputs.pick(KEY_PROTECTIVE, it.protectiveSection),
                    parallelConductors = inputs.pick(KEY_PARALLEL, it.parallelConductors),
                    material = inputs.enumOrNull(KEY_MATERIAL) ?: it.material,
                    insulation = inputs.enumOrNull(KEY_INSULATION) ?: it.insulation,
                    deviceType = inputs.enumOrNull(KEY_DEVICE) ?: it.deviceType,
                    deviceRating = inputs.pick(KEY_RATING, it.deviceRating),
                    clearingTime = inputs.pick(KEY_CLEARING_TIME, it.clearingTime),
                )
            }
            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: EarthFaultUiState, result: EarthFaultResult) {
        val record = CalculationRecord(
            calculatorId = CalculatorId.EARTH_FAULT_LOOP,
            title = stringResolver.get(Res.string.ef_history_title, stringResolver.get(state.deviceType.label()), state.length),
            summary = stringResolver.get(Res.string.ef_history_summary, format(result.loopImpedanceOhms, IMPEDANCE_DECIMALS)),
            inputs = mapOf(
                KEY_ZE to state.externalImpedance,
                KEY_VOLTAGE to state.voltage,
                KEY_LENGTH to state.length,
                KEY_LINE to state.lineSection,
                KEY_PROTECTIVE to state.protectiveSection,
                KEY_PARALLEL to state.parallelConductors,
                KEY_MATERIAL to state.material.name,
                KEY_INSULATION to state.insulation.name,
                KEY_DEVICE to state.deviceType.name,
                KEY_RATING to state.deviceRating,
                KEY_CLEARING_TIME to state.clearingTime,
            ),
            results = buildMap {
                put(KEY_ZS, format(result.loopImpedanceOhms, IMPEDANCE_DECIMALS))
                put(KEY_ZS_MAX, format(result.maximumPermittedOhms, IMPEDANCE_DECIMALS))
                put(KEY_FAULT_CURRENT, format(result.faultCurrentAmps))
                result.operatingCurrentAmps?.let { put(KEY_OPERATING_CURRENT, format(it)) }
                put(KEY_ADIABATIC, format(result.adiabaticMinimumMm2))
                // Both verdicts are recorded: a run that disconnected but did
                // not withstand must not read as a pass in the history list.
                put(KEY_DISCONNECTS, result.disconnectsInTime.toString())
                put(KEY_WITHSTANDS, result.protectiveConductorWithstands.toString())
            },
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double, decimals: Int = DISPLAY_DECIMALS) =
        NumberFormatter.format(value, decimals)

    private fun ProtectiveDeviceType.label(): StringResource = when (this) {
        ProtectiveDeviceType.MCB_TYPE_B -> Res.string.ef_device_b
        ProtectiveDeviceType.MCB_TYPE_C -> Res.string.ef_device_c
        ProtectiveDeviceType.MCB_TYPE_D -> Res.string.ef_device_d
        ProtectiveDeviceType.CUSTOM -> Res.string.ef_device_custom
        ProtectiveDeviceType.RCD -> Res.string.ef_device_rcd
    }

    private companion object {
        const val DISPLAY_DECIMALS = 2
        const val IMPEDANCE_DECIMALS = 3

        const val MAX_IMPEDANCE = 1_000.0
        const val MAX_VOLTAGE = 100_000.0
        const val MAX_LENGTH = 100_000.0
        const val MAX_AREA = 5_000.0
        const val MIN_PARALLEL = 1.0
        const val MAX_PARALLEL = 100.0
        const val MAX_DEVICE_RATING = 100_000.0
        const val MAX_RESIDUAL = 10.0
        const val MAX_CLEARING_TIME = 600.0

        const val KEY_ZE = "external_impedance"
        const val KEY_VOLTAGE = "phase_voltage"
        const val KEY_LENGTH = "length_m"
        const val KEY_LINE = "line_cross_section_mm2"
        const val KEY_PROTECTIVE = "protective_cross_section_mm2"
        const val KEY_PARALLEL = "parallel_conductors"
        const val KEY_MATERIAL = "material"
        const val KEY_INSULATION = "insulation"
        const val KEY_DEVICE = "device_type"
        const val KEY_RATING = "device_rating"
        const val KEY_CLEARING_TIME = "clearing_time_s"

        const val KEY_ZS = "loop_impedance"
        const val KEY_ZS_MAX = "maximum_permitted_impedance"
        const val KEY_FAULT_CURRENT = "fault_current"
        const val KEY_OPERATING_CURRENT = "operating_current"
        const val KEY_ADIABATIC = "adiabatic_minimum_mm2"
        const val KEY_DISCONNECTS = "disconnects_in_time"
        const val KEY_WITHSTANDS = "protective_conductor_withstands"
    }
}

/**
 * The user's engineering defaults, applied to a form that is still untouched.
 *
 * Only the fields this calculator shares with Settings move.
 */
private fun EarthFaultUiState.withDefaults(defaults: EngineeringDefaults) = copy(
    material = defaults.material,
    insulation = defaults.insulation,
    // An earth fault loop is a line-to-earth circuit, so the line-to-neutral
    // voltage is the one that drives it.
    voltage = defaults.singlePhaseVoltage,
)
