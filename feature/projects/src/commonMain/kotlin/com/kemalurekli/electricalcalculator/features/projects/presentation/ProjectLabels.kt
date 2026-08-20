package com.kemalurekli.electricalcalculator.features.projects.presentation

import com.kemalurekli.electricalcalculator.core.common.util.currentNumberSymbols
import com.kemalurekli.electricalcalculator.core.common.util.NumberSymbols
import org.jetbrains.compose.resources.StringResource
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.design.domain.BindingConstraint
import com.kemalurekli.electricalcalculator.features.design.domain.DesignFailure
import com.kemalurekli.electricalcalculator.features.inspection.domain.RcdType
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestKind
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestVerdict
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_binding_capacity
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_binding_drop
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_binding_loop
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_binding_none
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_binding_protective
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_failure_device_range
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_failure_no_size
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_load_kind_current
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_load_kind_power
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.common_material_aluminium
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.common_material_copper
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.common_system_dc
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.common_system_single_phase
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.common_system_three_phase
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_insulation_pvc
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_insulation_xlpe
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_method_b1
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_method_b1_full
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_method_b2
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_method_b2_full
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_method_c
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_method_c_full
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_method_e
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.cs_method_e_full
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.ef_device_b
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.ef_device_c
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.ef_device_custom
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.ef_device_d
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.ef_device_rcd
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_kind_continuity
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_kind_insulation
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_kind_loop
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_kind_polarity
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_kind_rcd_five
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_kind_rcd_rated
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_rcd_general
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_rcd_selective
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_verdict_fail
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_verdict_pass
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.tests_verdict_recorded

/**
 * The labels the project screens share.
 *
 * Deliberately the *same* string resources the calculators use. A cross-section
 * chosen here and the same choice made in the cable-size calculator must read
 * identically, or the two screens look like they are talking about different
 * things.
 */

internal fun SupplySystem.label(): StringResource = when (this) {
    SupplySystem.DC -> Res.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> Res.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> Res.string.common_system_three_phase
}

internal fun ConductorMaterial.label(): StringResource = when (this) {
    ConductorMaterial.COPPER -> Res.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> Res.string.common_material_aluminium
}

internal fun CableInsulation.label(): StringResource = when (this) {
    CableInsulation.PVC -> Res.string.cs_insulation_pvc
    CableInsulation.XLPE -> Res.string.cs_insulation_xlpe
}

internal fun InstallationMethod.label(): StringResource = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> Res.string.cs_method_b1
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> Res.string.cs_method_b2
    InstallationMethod.C_CLIPPED_DIRECT -> Res.string.cs_method_c
    InstallationMethod.E_FREE_AIR -> Res.string.cs_method_e
}

/**
 * The method spelled out.
 *
 * The segmented buttons only have room for the letter code, and "B1" tells a
 * reader who does not already know the table nothing at all. The same strings
 * the cable-size calculator uses, so the two screens describe a method
 * identically.
 */
internal fun InstallationMethod.fullLabel(): StringResource = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> Res.string.cs_method_b1_full
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> Res.string.cs_method_b2_full
    InstallationMethod.C_CLIPPED_DIRECT -> Res.string.cs_method_c_full
    InstallationMethod.E_FREE_AIR -> Res.string.cs_method_e_full
}

internal fun CircuitLoadKind.label(): StringResource = when (this) {
    CircuitLoadKind.CURRENT -> Res.string.circuit_load_kind_current
    CircuitLoadKind.POWER -> Res.string.circuit_load_kind_power
}

internal fun BindingConstraint.label(): StringResource = when (this) {
    BindingConstraint.CURRENT_CAPACITY -> Res.string.circuit_binding_capacity
    BindingConstraint.VOLTAGE_DROP -> Res.string.circuit_binding_drop
    BindingConstraint.EARTH_FAULT_LOOP -> Res.string.circuit_binding_loop
    BindingConstraint.PROTECTIVE_CONDUCTOR -> Res.string.circuit_binding_protective
    BindingConstraint.NONE -> Res.string.circuit_binding_none
}

internal fun DesignFailure.message(): StringResource = when (this) {
    DesignFailure.LOAD_BEYOND_DEVICE_RANGE -> Res.string.circuit_failure_device_range
    DesignFailure.NO_TABULATED_SIZE -> Res.string.circuit_failure_no_size
}

/**
 * Device types read as their own names.
 *
 * The earth-fault calculator already labels these; reusing its strings keeps a
 * Type C breaker called the same thing in both places.
 */
internal fun ProtectiveDeviceType.label(): StringResource = when (this) {
    ProtectiveDeviceType.MCB_TYPE_B -> Res.string.ef_device_b
    ProtectiveDeviceType.MCB_TYPE_C -> Res.string.ef_device_c
    ProtectiveDeviceType.MCB_TYPE_D -> Res.string.ef_device_d
    ProtectiveDeviceType.CUSTOM -> Res.string.ef_device_custom
    ProtectiveDeviceType.RCD -> Res.string.ef_device_rcd
}

/**
 * A figure for a schedule row.
 *
 * Significant digits rather than fixed decimals, because the same column holds
 * a 1.5 mm² cross-section and a 240 mm² one, and a fixed rule makes one of them
 * read wrongly.
 */
internal fun Double?.format(): String =
    this?.let { NumberFormatter.formatSignificant(it, symbols = currentNumberSymbols()) }.orEmpty()

internal fun TestKind.label(): StringResource = when (this) {
    TestKind.CONTINUITY -> Res.string.tests_kind_continuity
    TestKind.INSULATION -> Res.string.tests_kind_insulation
    TestKind.POLARITY -> Res.string.tests_kind_polarity
    TestKind.LOOP_IMPEDANCE -> Res.string.tests_kind_loop
    TestKind.RCD_AT_RATED -> Res.string.tests_kind_rcd_rated
    TestKind.RCD_AT_FIVE_TIMES -> Res.string.tests_kind_rcd_five
}

internal fun TestVerdict.label(): StringResource = when (this) {
    TestVerdict.PASS -> Res.string.tests_verdict_pass
    TestVerdict.FAIL -> Res.string.tests_verdict_fail
    TestVerdict.RECORDED -> Res.string.tests_verdict_recorded
}

internal fun RcdType.label(): StringResource = when (this) {
    RcdType.GENERAL -> Res.string.tests_rcd_general
    RcdType.SELECTIVE_S -> Res.string.tests_rcd_selective
}

/** The tester's own unit for each reading. */
internal fun TestKind.unit(): String? = when (this) {
    TestKind.CONTINUITY, TestKind.LOOP_IMPEDANCE -> "\u03a9"
    TestKind.INSULATION -> "M\u03a9"
    TestKind.RCD_AT_RATED, TestKind.RCD_AT_FIVE_TIMES -> "ms"
    TestKind.POLARITY -> null
}
