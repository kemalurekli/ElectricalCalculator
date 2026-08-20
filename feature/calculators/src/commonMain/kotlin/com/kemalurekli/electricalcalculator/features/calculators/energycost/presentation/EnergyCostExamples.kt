package com.kemalurekli.electricalcalculator.features.calculators.energycost.presentation

import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_example_lighting
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_example_motor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_example_standby

/**
 * Three cases a customer actually asks about.
 *
 * The standby load is there because it is the one people dismiss. Ten watts is
 * nothing; ten watts for 8 760 hours is not, and the arithmetic is the only way
 * to settle that argument.
 */
internal val energyCostExamples: ImmutableList<WorkedExample<EnergyCostUiState>> = persistentListOf(
    WorkedExample(
        key = "lighting_upgrade",
        title = Res.string.ec_example_lighting,
        // Replacing fluorescent with LED across a shop floor.
        fill = { state ->
            state.copy(
                power = "6000",
                hoursPerDay = "12",
                daysPerYear = "300",
                tariff = "3",
                replacementPower = "2400",
                replacementCost = "45000",
            )
        },
    ),
    WorkedExample(
        key = "motor_running_cost",
        title = Res.string.ec_example_motor,
        // What a 15 kW motor costs to run on a two-shift day.
        fill = { state ->
            state.copy(
                power = "15000",
                hoursPerDay = "16",
                daysPerYear = "250",
                tariff = "3",
                replacementPower = "",
                replacementCost = "",
            )
        },
    ),
    WorkedExample(
        key = "standby_load",
        title = Res.string.ec_example_standby,
        // Ten watts, always on. 8 760 hours is the number that settles it.
        fill = { state ->
            state.copy(
                power = "10",
                hoursPerDay = "24",
                daysPerYear = "365",
                tariff = "3",
                replacementPower = "",
                replacementCost = "",
            )
        },
    ),
)
