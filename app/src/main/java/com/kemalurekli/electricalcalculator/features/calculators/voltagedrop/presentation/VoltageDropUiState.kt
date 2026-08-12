package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation

import androidx.compose.runtime.Immutable
import com.kemalurekli.electricalcalculator.core.ui.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropResult

/** Identifies a form field, so errors can be routed back to the right input. */
enum class VoltageDropField {
    VOLTAGE,
    CURRENT,
    LENGTH,
    CROSS_SECTION,
    POWER_FACTOR,
    TEMPERATURE,
    PARALLEL_CONDUCTORS,
}

/**
 * State of the voltage drop calculator.
 *
 * Field values are kept as the raw strings the user typed rather than parsed
 * numbers. A calculator that reformats input mid-edit — turning "0." into "0"
 * as someone types "0.5" — is unusable, so parsing happens only at calculation
 * time.
 *
 * @param errors validation failures keyed by field. Populated on calculate, not
 *   on every keystroke, so the form does not turn red while it is being filled.
 * @param result the last successful calculation, or null if none has run or the
 *   inputs have changed since.
 */
@Immutable
data class VoltageDropUiState(
    val system: SupplySystem = SupplySystem.SINGLE_PHASE_AC,
    val material: ConductorMaterial = ConductorMaterial.COPPER,
    val voltage: String = SystemVoltageDefaults.forSystem(system).orEmpty(),
    /** True once the user has typed a voltage, after which it is never moved. */
    val voltageEdited: Boolean = false,
    val current: String = "",
    val length: String = "",
    val crossSection: String = "",
    val powerFactor: String = DEFAULT_POWER_FACTOR,
    val temperature: String = DEFAULT_TEMPERATURE,
    val parallelConductors: String = DEFAULT_PARALLEL,
    val errors: Map<VoltageDropField, ValidationError> = emptyMap(),
    val result: VoltageDropResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    /** Power factor is meaningless on DC, so the field is hidden there. */
    val showPowerFactor: Boolean get() = system.isAc

    companion object {
        /**
         * Unity by default. With reactance neglected a lower power factor
         * reduces the computed drop, so unity is the conservative starting
         * point — a user who does not set it cannot be handed an optimistic
         * result by accident.
         */
        const val DEFAULT_POWER_FACTOR = "1"

        /** The conductor rating for PVC insulation at full load. */
        const val DEFAULT_TEMPERATURE = "70"

        const val DEFAULT_PARALLEL = "1"
    }
}
