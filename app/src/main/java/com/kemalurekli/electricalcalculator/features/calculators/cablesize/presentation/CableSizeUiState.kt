package com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation

import androidx.compose.runtime.Immutable
import com.kemalurekli.electricalcalculator.core.ui.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.ui.model.SystemVoltageDefaults
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CableSizeResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Identifies a form field so validation errors can be routed back to it. */
enum class CableSizeField {
    VOLTAGE,
    CURRENT,
    LENGTH,
    POWER_FACTOR,
    MAX_DROP,
    AMBIENT,
    CIRCUITS,
    PARALLEL_CONDUCTORS,
}

@Immutable
data class CableSizeUiState(
    val system: SupplySystem = SupplySystem.SINGLE_PHASE_AC,
    val material: ConductorMaterial = ConductorMaterial.COPPER,
    val insulation: CableInsulation = CableInsulation.PVC,
    val method: InstallationMethod = InstallationMethod.C_CLIPPED_DIRECT,
    val voltage: String = SystemVoltageDefaults.forSystem(system).orEmpty(),
    /** True once the user has typed a voltage, after which it is never moved. */
    val voltageEdited: Boolean = false,
    val current: String = "",
    val length: String = "",
    val powerFactor: String = DEFAULT_POWER_FACTOR,
    val maxDropPercent: String = DEFAULT_MAX_DROP,
    val ambientTemperature: String = DEFAULT_AMBIENT,
    val groupedCircuits: String = DEFAULT_CIRCUITS,
    val parallelConductors: String = DEFAULT_PARALLEL,
    val errors: Map<CableSizeField, ValidationError> = emptyMap(),
    val result: CableSizeResult? = null,
    val isFavorite: Boolean = false,
    val steps: ImmutableList<CalculationStep> = persistentListOf(),
) {
    val showPowerFactor: Boolean get() = system.isAc

    companion object {
        const val DEFAULT_POWER_FACTOR = "1"

        /** IEC 60364-5-52 Annex G general limit; 3 % applies to lighting. */
        const val DEFAULT_MAX_DROP = "5"

        /** The ambient the capacity tables are referenced to. */
        const val DEFAULT_AMBIENT = "30"

        const val DEFAULT_CIRCUITS = "1"
        const val DEFAULT_PARALLEL = "1"
    }
}
