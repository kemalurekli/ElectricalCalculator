package com.kemalurekli.electricalcalculator.features.calculators.power.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Three loads that put the power triangle in three different shapes.
 *
 * The heater has unity power factor and no reactive component at all; the
 * DC bus has no phase angle to speak of. Between them they show what the
 * triangle degenerates to when there is nothing to lag.
 */
internal val powerExamples: ImmutableList<WorkedExample<PowerUiState>> = persistentListOf(
    WorkedExample(
        key = "workshop_load",
        titleRes = R.string.pw_example_workshop,
        // A mixed workshop load at a realistic lagging power factor.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            voltage = "400",
            voltageEdited = true,
            current = "25",
            powerFactor = "0.85",
            )
        },
    ),
    WorkedExample(
        key = "resistive_heater",
        titleRes = R.string.pw_example_heater,
        // Purely resistive: cos φ = 1, so S, P and the current all line up.
        fill = { state ->
            state.copy(
            system = SupplySystem.SINGLE_PHASE_AC,
            voltage = "230",
            voltageEdited = true,
            current = "16",
            powerFactor = "1",
            )
        },
    ),
    WorkedExample(
        key = "dc_bus",
        titleRes = R.string.pw_example_dc,
        // A 48 V DC bus: no phase angle, so no reactive step is shown at all.
        fill = { state ->
            state.copy(
            system = SupplySystem.DC,
            voltage = "48",
            voltageEdited = true,
            current = "20",
            powerFactor = "1",
            )
        },
    ),
)
