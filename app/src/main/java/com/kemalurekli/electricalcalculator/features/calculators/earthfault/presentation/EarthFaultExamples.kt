package com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Three circuits, and the middle one does not disconnect.
 *
 * That is the example this calculator exists for. A 60 m run on a Type C
 * breaker looks entirely ordinary and fails the check, and the only way to
 * know is to have run it.
 */
internal val earthFaultExamples: ImmutableList<WorkedExample<EarthFaultUiState>> = persistentListOf(
    WorkedExample(
        key = "tncs_type_b",
        titleRes = R.string.ef_example_tncs,
        // A 32 A Type B circuit on a TN-C-S supply: comfortably compliant.
        fill = { state ->
            state.copy(
            deviceType = ProtectiveDeviceType.MCB_TYPE_B,
            externalImpedance = "0.35",
            voltage = "230",
            length = "30",
            lineSection = "4",
            protectiveSection = "2.5",
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.PVC,
            deviceRating = "32",
            clearingTime = "0.1",
            parallelConductors = "1",
            )
        },
    ),
    WorkedExample(
        key = "too_long_type_c",
        titleRes = R.string.ef_example_fails,
        // The same circuit on a Type C, run out to 60 m. It will not disconnect
        // in time, and nothing about it looks wrong on a drawing.
        fill = { state ->
            state.copy(
            deviceType = ProtectiveDeviceType.MCB_TYPE_C,
            externalImpedance = "0.35",
            voltage = "230",
            length = "60",
            lineSection = "4",
            protectiveSection = "2.5",
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.PVC,
            deviceRating = "32",
            clearingTime = "0.1",
            parallelConductors = "1",
            )
        },
    ),
    WorkedExample(
        key = "tt_with_rcd",
        titleRes = R.string.ef_example_tt,
        // A TT installation with a 30 mA RCD: judged on touch voltage, which is
        // why a 21 Ω loop is acceptable here and hopeless on a breaker.
        fill = { state ->
            state.copy(
            deviceType = ProtectiveDeviceType.RCD,
            externalImpedance = "21",
            voltage = "230",
            length = "25",
            lineSection = "2.5",
            protectiveSection = "2.5",
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.PVC,
            deviceRating = "0.03",
            clearingTime = "0.04",
            parallelConductors = "1",
            )
        },
    ),
)
