package com.kemalurekli.electricalcalculator.features.calculators.battery.presentation

import com.kemalurekli.electricalcalculator.core.common.util.seededDecimal
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_example_caravan
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_example_lithium
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_example_ups

/**
 * Three banks whose Peukert exponents differ enough to matter.
 *
 * Lead-acid at 1,15 against lithium at 1,05 is the comparison worth making:
 * the same nameplate capacity delivers materially different runtime, and the
 * naive figure shown beside it is the one most people would have used.
 */
internal val batteryExamples: ImmutableList<WorkedExample<BatteryUiState>> = persistentListOf(
    WorkedExample(
        key = "ups_48v",
        title = Res.string.bt_example_ups,
        // A 48 V UPS bank carrying 500 W through the inverter.
        fill = { state ->
            state.copy(
            capacityAh = "100",
            ratedHours = "20",
            voltage = "48",
            loadWatts = "500",
            efficiency = "90",
            depthOfDischarge = "50",
            peukert = "1.15".seededDecimal(),
            )
        },
    ),
    WorkedExample(
        key = "caravan_12v",
        title = Res.string.bt_example_caravan,
        // 12 V leisure battery, hard discharge: Peukert bites hardest here.
        fill = { state ->
            state.copy(
            capacityAh = "100",
            ratedHours = "20",
            voltage = "12",
            loadWatts = "200",
            efficiency = "85",
            depthOfDischarge = "50",
            peukert = "1.25".seededDecimal(),
            )
        },
    ),
    WorkedExample(
        key = "lithium_bank",
        title = Res.string.bt_example_lithium,
        // Lithium: nearly ideal, and usable far deeper than lead-acid.
        fill = { state ->
            state.copy(
            capacityAh = "100",
            ratedHours = "5",
            voltage = "48",
            loadWatts = "500",
            efficiency = "95",
            depthOfDischarge = "80",
            peukert = "1.05".seededDecimal(),
            )
        },
    ),
)
