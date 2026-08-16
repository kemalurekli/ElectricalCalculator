package com.kemalurekli.electricalcalculator.features.projects.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.design.domain.BindingConstraint
import com.kemalurekli.electricalcalculator.features.design.domain.DesignFailure
import java.util.Locale

/**
 * The labels the project screens share.
 *
 * Deliberately the *same* string resources the calculators use. A cross-section
 * chosen here and the same choice made in the cable-size calculator must read
 * identically, or the two screens look like they are talking about different
 * things.
 */

internal fun SupplySystem.labelRes(): Int = when (this) {
    SupplySystem.DC -> R.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> R.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> R.string.common_system_three_phase
}

internal fun ConductorMaterial.labelRes(): Int = when (this) {
    ConductorMaterial.COPPER -> R.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> R.string.common_material_aluminium
}

internal fun CableInsulation.labelRes(): Int = when (this) {
    CableInsulation.PVC -> R.string.cs_insulation_pvc
    CableInsulation.XLPE -> R.string.cs_insulation_xlpe
}

internal fun InstallationMethod.labelRes(): Int = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> R.string.cs_method_b1
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> R.string.cs_method_b2
    InstallationMethod.C_CLIPPED_DIRECT -> R.string.cs_method_c
    InstallationMethod.E_FREE_AIR -> R.string.cs_method_e
}

internal fun CircuitLoadKind.labelRes(): Int = when (this) {
    CircuitLoadKind.CURRENT -> R.string.circuit_load_kind_current
    CircuitLoadKind.POWER -> R.string.circuit_load_kind_power
}

internal fun BindingConstraint.labelRes(): Int = when (this) {
    BindingConstraint.CURRENT_CAPACITY -> R.string.circuit_binding_capacity
    BindingConstraint.VOLTAGE_DROP -> R.string.circuit_binding_drop
    BindingConstraint.EARTH_FAULT_LOOP -> R.string.circuit_binding_loop
    BindingConstraint.PROTECTIVE_CONDUCTOR -> R.string.circuit_binding_protective
    BindingConstraint.NONE -> R.string.circuit_binding_none
}

internal fun DesignFailure.messageRes(): Int = when (this) {
    DesignFailure.LOAD_BEYOND_DEVICE_RANGE -> R.string.circuit_failure_device_range
    DesignFailure.NO_TABULATED_SIZE -> R.string.circuit_failure_no_size
}

/**
 * Device types read as their own names.
 *
 * The earth-fault calculator already labels these; reusing its strings keeps a
 * Type C breaker called the same thing in both places.
 */
internal fun ProtectiveDeviceType.labelRes(): Int = when (this) {
    ProtectiveDeviceType.MCB_TYPE_B -> R.string.ef_device_b
    ProtectiveDeviceType.MCB_TYPE_C -> R.string.ef_device_c
    ProtectiveDeviceType.MCB_TYPE_D -> R.string.ef_device_d
    ProtectiveDeviceType.CUSTOM -> R.string.ef_device_custom
    ProtectiveDeviceType.RCD -> R.string.ef_device_rcd
}

/**
 * A figure for a schedule row.
 *
 * Significant digits rather than fixed decimals, because the same column holds
 * a 1.5 mm² cross-section and a 240 mm² one, and a fixed rule makes one of them
 * read wrongly.
 */
internal fun Double?.format(): String =
    this?.let { NumberFormatter.formatSignificant(it, locale = Locale.getDefault()) }.orEmpty()
