package com.kemalurekli.electricalcalculator.features.calculators.motor.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.PowerUnit
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Three motors across the range the calculator is used on.
 *
 * The horsepower case is deliberate: a nameplate in hp is where the two
 * horsepowers — 745,7 W and 735,5 W — quietly differ, and the worked steps
 * make visible which one was used.
 */
internal val motorExamples: ImmutableList<WorkedExample<MotorUiState>> = persistentListOf(
    WorkedExample(
        key = "dol_5_5kw",
        titleRes = R.string.mt_example_dol,
        // The everyday 5,5 kW squirrel cage started direct-on-line.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            powerUnit = PowerUnit.KILOWATT,
            ratedPower = "5.5",
            voltage = "400",
            voltageEdited = true,
            efficiency = "90",
            powerFactor = "0.85",
            startingRatio = "6",
            )
        },
    ),
    WorkedExample(
        key = "large_75kw",
        titleRes = R.string.mt_example_large,
        // 75 kW: high efficiency, and a starting current that decides the switchgear.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            powerUnit = PowerUnit.KILOWATT,
            ratedPower = "75",
            voltage = "400",
            voltageEdited = true,
            efficiency = "95",
            powerFactor = "0.87",
            startingRatio = "7",
            )
        },
    ),
    WorkedExample(
        key = "nameplate_hp",
        titleRes = R.string.mt_example_hp,
        // A 10 hp nameplate — the conversion step earns its place here.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            powerUnit = PowerUnit.HORSEPOWER,
            ratedPower = "10",
            voltage = "400",
            voltageEdited = true,
            efficiency = "89",
            powerFactor = "0.86",
            startingRatio = "6",
            )
        },
    ),
)
