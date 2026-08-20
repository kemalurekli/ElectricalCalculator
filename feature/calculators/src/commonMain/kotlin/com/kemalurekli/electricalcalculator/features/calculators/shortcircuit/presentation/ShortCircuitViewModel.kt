package com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation

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
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.CalculateShortCircuitUseCase
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.FaultType
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.ShortCircuitInput
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.ShortCircuitResult
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_history_summary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_history_title

/** Identifies a form field so validation errors can be routed back to it. */
enum class ShortCircuitField {
    VOLTAGE,
    SUPPLY_CURRENT,
    LENGTH,
    CROSS_SECTION,
    NEUTRAL_SECTION,
    PARALLEL,
    REACTANCE,
}

@Immutable
data class ShortCircuitUiState(
    val faultType: FaultType = FaultType.THREE_PHASE,
    val material: ConductorMaterial = ConductorMaterial.COPPER,
    val insulation: CableInsulation = CableInsulation.PVC,
    val voltage: String = DEFAULT_VOLTAGE,
    val supplyCurrent: String = "",
    val length: String = "",
    val crossSection: String = "",
    val neutralSection: String = "",
    val parallelConductors: String = DEFAULT_PARALLEL,
    val reactance: String = DEFAULT_REACTANCE,
    val errors: Map<ShortCircuitField, ValidationError> = emptyMap(),
    val result: ShortCircuitResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    /** Only a line–neutral fault returns through the neutral. */
    val showNeutralSection: Boolean get() = faultType.usesNeutralReturn

    companion object {
        /** Line-to-line for the three-phase default. */
        const val DEFAULT_VOLTAGE = "400"

        const val DEFAULT_PARALLEL = "1"

        const val DEFAULT_REACTANCE = "0.08"

        /** The line-to-neutral counterpart of a 400 V system. */
        const val DEFAULT_PHASE_VOLTAGE = "230"
    }
}

class ShortCircuitViewModel(
    private val calculateFault: CalculateShortCircuitUseCase,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringResolver: StringResolver,
    private val timeProvider: TimeProvider,
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShortCircuitUiState())
    val uiState: StateFlow<ShortCircuitUiState> = _uiState.asStateFlow()

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
                .observeIsFavorite(CalculatorId.SHORT_CIRCUIT)
                .collect { isFavorite -> _uiState.update { it.copy(isFavorite = isFavorite) } }
        }
    }

    // -- Field editing --------------------------------------------------------------

    /**
     * Switching fault type also moves the voltage, because the two faults are
     * driven by different voltages — line-to-line and line-to-neutral. Leaving
     * 400 V in place for a line–neutral fault would overstate the current by √3,
     * so the default follows the choice unless the user has edited it.
     */
    fun onFaultTypeChange(faultType: FaultType) = update {
        val wasDefaultVoltage = it.voltage == defaults.threePhaseVoltage ||
            it.voltage == defaults.singlePhaseVoltage
        it.copy(
            faultType = faultType,
            voltage = if (wasDefaultVoltage) defaults.voltageForFault(faultType) else it.voltage,
        )
    }

    fun onMaterialChange(material: ConductorMaterial) = update { it.copy(material = material) }

    fun onInsulationChange(insulation: CableInsulation) = update { it.copy(insulation = insulation) }

    fun onVoltageChange(value: String) =
        update(ShortCircuitField.VOLTAGE) { it.copy(voltage = value) }

    fun onSupplyCurrentChange(value: String) =
        update(ShortCircuitField.SUPPLY_CURRENT) { it.copy(supplyCurrent = value) }

    fun onLengthChange(value: String) =
        update(ShortCircuitField.LENGTH) { it.copy(length = value) }

    fun onCrossSectionChange(value: String) =
        update(ShortCircuitField.CROSS_SECTION) { it.copy(crossSection = value) }

    fun onNeutralSectionChange(value: String) =
        update(ShortCircuitField.NEUTRAL_SECTION) { it.copy(neutralSection = value) }

    /** Digits only: a circuit has a whole number of conductors per phase. */
    fun onParallelChange(value: String) =
        update(ShortCircuitField.PARALLEL) {
            it.copy(parallelConductors = value.filter(Char::isDigit))
        }

    fun onReactanceChange(value: String) =
        update(ShortCircuitField.REACTANCE) { it.copy(reactance = value) }

    private fun update(
        field: ShortCircuitField? = null,
        transform: (ShortCircuitUiState) -> ShortCircuitUiState,
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
        val errors = mutableMapOf<ShortCircuitField, ValidationError>()

        fun validate(
            field: ShortCircuitField,
            raw: String,
            min: Double? = null,
            max: Double? = null,
            allowZero: Boolean = false,
        ): Double? = NumericInput
            .validate(raw, min, max, allowZero = allowZero)
            .also { if (it is Outcome.Failure) errors[field] = it.error }
            .let { (it as? Outcome.Success)?.value }

        val voltage = validate(ShortCircuitField.VOLTAGE, state.voltage, max = MAX_VOLTAGE)
        val supplyCurrent = validate(
            ShortCircuitField.SUPPLY_CURRENT,
            state.supplyCurrent,
            max = MAX_SUPPLY_CURRENT,
        )
        // Zero length is meaningful: it asks for the current at the origin.
        val length = validate(ShortCircuitField.LENGTH, state.length, max = MAX_LENGTH, allowZero = true)
        val area = validate(ShortCircuitField.CROSS_SECTION, state.crossSection, max = MAX_AREA)
        val parallel = validate(
            ShortCircuitField.PARALLEL,
            state.parallelConductors,
            min = MIN_PARALLEL,
            max = MAX_PARALLEL,
        )
        // Zero reactance is a legitimate simplification, not a mistake.
        val reactance = validate(
            ShortCircuitField.REACTANCE,
            state.reactance,
            max = MAX_REACTANCE,
            allowZero = true,
        )

        // Only asked for when the neutral is actually in the loop; blank falls
        // back to the line section, which is the usual construction.
        val neutralArea = if (!state.showNeutralSection || state.neutralSection.isBlank()) {
            area
        } else {
            validate(ShortCircuitField.NEUTRAL_SECTION, state.neutralSection, max = MAX_AREA)
        }

        if (errors.isNotEmpty() || voltage == null || supplyCurrent == null || length == null ||
            area == null || parallel == null || reactance == null || neutralArea == null
        ) {
            _uiState.update { it.copy(errors = errors, result = null, steps = persistentListOf()) }
            return
        }

        val input = ShortCircuitInput(
            faultType = state.faultType,
            nominalVoltage = voltage,
            supplyFaultCurrentAmps = supplyCurrent,
            lengthMetres = length,
            crossSectionMm2 = area,
            neutralCrossSectionMm2 = neutralArea,
            parallelConductors = parallel.toInt(),
            material = state.material,
            insulation = state.insulation,
            reactancePerKmOhms = reactance,
        )

        val result = calculateFault(input)
        _uiState.update {
            it.copy(
                errors = emptyMap(),
                result = result,
                steps = explainShortCircuit(input, result),
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
    fun onApplyExample(example: WorkedExample<ShortCircuitUiState>) {
        formTouched = true
        _uiState.update { example.fill(it) }
        onCalculate()
    }

    fun onReset() {
        _uiState.update {
            ShortCircuitUiState(
                faultType = it.faultType,
                material = it.material,
                insulation = it.insulation,
                voltage = defaults.voltageForFault(it.faultType),
                isFavorite = it.isFavorite,
            )
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(CalculatorId.SHORT_CIRCUIT) }
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
            if (record.calculatorId != CalculatorId.SHORT_CIRCUIT) return@launch
            val inputs = record.inputs
            _uiState.update {
                it.copy(
                    faultType = inputs.enumOrNull(KEY_FAULT_TYPE) ?: it.faultType,
                    voltage = inputs.pick(KEY_VOLTAGE, it.voltage),
                    supplyCurrent = inputs.pick(KEY_SUPPLY_CURRENT, it.supplyCurrent),
                    length = inputs.pick(KEY_LENGTH, it.length),
                    crossSection = inputs.pick(KEY_AREA, it.crossSection),
                    neutralSection = inputs.pick(KEY_NEUTRAL_AREA, it.neutralSection),
                    parallelConductors = inputs.pick(KEY_PARALLEL, it.parallelConductors),
                    material = inputs.enumOrNull(KEY_MATERIAL) ?: it.material,
                    insulation = inputs.enumOrNull(KEY_INSULATION) ?: it.insulation,
                    reactance = inputs.pick(KEY_REACTANCE, it.reactance),
                )
            }
            // The reader tapped a result, so show one rather than an empty form.
            onCalculate()
        }
    }

    private fun saveToHistory(state: ShortCircuitUiState, result: ShortCircuitResult) {
        // The minimum leads the summary: it is the figure that decides whether
        // the circuit is protected, and the one worth seeing in a list.
        val record = CalculationRecord(
            calculatorId = CalculatorId.SHORT_CIRCUIT,
            title = stringResolver.get(Res.string.sc_history_title, state.crossSection, state.length),
            summary = stringResolver.get(Res.string.sc_history_summary, format(result.minimumFaultCurrentAmps / AMPS_PER_KILOAMP)),
            inputs = buildMap {
                put(KEY_FAULT_TYPE, state.faultType.name)
                put(KEY_VOLTAGE, state.voltage)
                put(KEY_SUPPLY_CURRENT, state.supplyCurrent)
                put(KEY_LENGTH, state.length)
                put(KEY_AREA, state.crossSection)
                if (state.showNeutralSection && state.neutralSection.isNotBlank()) {
                    put(KEY_NEUTRAL_AREA, state.neutralSection)
                }
                put(KEY_PARALLEL, state.parallelConductors)
                put(KEY_MATERIAL, state.material.name)
                put(KEY_INSULATION, state.insulation.name)
                put(KEY_REACTANCE, state.reactance)
            },
            results = mapOf(
                KEY_MINIMUM to format(result.minimumFaultCurrentAmps),
                KEY_MAXIMUM to format(result.maximumFaultCurrentAmps),
                KEY_SUPPLY_IMPEDANCE to format(result.supplyImpedanceOhms, IMPEDANCE_DECIMALS),
                KEY_LOOP_HOT to format(result.loopImpedanceHotOhms, IMPEDANCE_DECIMALS),
            ),
            createdAt = timeProvider.now(),
        )

        viewModelScope.launch { historyRepository.save(record) }
    }

    private fun format(value: Double, decimals: Int = DISPLAY_DECIMALS) =
        NumberFormatter.format(value, decimals)

    private companion object {
        const val DISPLAY_DECIMALS = 2
        const val IMPEDANCE_DECIMALS = 4
        const val AMPS_PER_KILOAMP = 1_000.0

        const val MAX_VOLTAGE = 100_000.0
        const val MAX_SUPPLY_CURRENT = 1_000_000.0
        const val MAX_LENGTH = 100_000.0
        const val MAX_AREA = 5_000.0
        const val MIN_PARALLEL = 1.0
        const val MAX_PARALLEL = 100.0
        const val MAX_REACTANCE = 10.0

        const val KEY_FAULT_TYPE = "fault_type"
        const val KEY_VOLTAGE = "nominal_voltage"
        const val KEY_SUPPLY_CURRENT = "supply_fault_current"
        const val KEY_LENGTH = "length_m"
        const val KEY_AREA = "cross_section_mm2"
        const val KEY_NEUTRAL_AREA = "neutral_cross_section_mm2"
        const val KEY_PARALLEL = "parallel_conductors"
        const val KEY_MATERIAL = "material"
        const val KEY_INSULATION = "insulation"
        const val KEY_REACTANCE = "reactance_per_km"

        const val KEY_MINIMUM = "minimum_fault_current"
        const val KEY_MAXIMUM = "maximum_fault_current"
        const val KEY_SUPPLY_IMPEDANCE = "supply_impedance"
        const val KEY_LOOP_HOT = "loop_impedance_hot"
    }
}

/**
 * The supply voltage that drives [faultType].
 *
 * A line-to-line fault is driven by the full three-phase voltage; a fault that
 * returns through the neutral sees only line-to-neutral. Leaving 400 V in place
 * for a line–neutral fault would overstate the current by √3.
 */
private fun EngineeringDefaults.voltageForFault(faultType: FaultType): String =
    if (faultType.usesNeutralReturn) singlePhaseVoltage else threePhaseVoltage

/**
 * The user's engineering defaults, applied to a form that is still untouched.
 *
 * Only the fields this calculator shares with Settings move.
 */
private fun ShortCircuitUiState.withDefaults(defaults: EngineeringDefaults) = copy(
    material = defaults.material,
    insulation = defaults.insulation,
    voltage = defaults.voltageForFault(faultType),
)
