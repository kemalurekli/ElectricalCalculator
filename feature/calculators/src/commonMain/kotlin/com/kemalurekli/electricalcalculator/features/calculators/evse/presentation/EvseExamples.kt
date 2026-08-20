package com.kemalurekli.electricalcalculator.features.calculators.evse.presentation

import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.DcFaultDetection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseConnection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_example_car_park
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_example_home
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_example_three_phase

/**
 * Three installations, and the two mistakes they make visible.
 *
 * The first pair differs only in the phases: the same 32 A is 7.4 kW or 22 kW,
 * which is the misunderstanding that turns up on domestic jobs. The third is
 * the one that matters more — a bank of points with no diversity assumed,
 * because charging is continuous and a car park is not a row of sockets.
 */
internal val evseExamples: ImmutableList<WorkedExample<EvseUiState>> = persistentListOf(
    WorkedExample(
        key = "home_single_phase",
        title = Res.string.ev_example_home,
        fill = { state ->
            state.copy(
                pointCount = "1",
                ratedCurrent = "32",
                connection = EvseConnection.SINGLE_PHASE,
                supplyVoltage = "230",
                simultaneity = "1",
                dcFaultDetection = DcFaultDetection.BUILT_IN_6MA,
            )
        },
    ),
    WorkedExample(
        key = "home_three_phase",
        title = Res.string.ev_example_three_phase,
        // Same current, three times the power.
        fill = { state ->
            state.copy(
                pointCount = "1",
                ratedCurrent = "32",
                connection = EvseConnection.THREE_PHASE,
                supplyVoltage = "400",
                simultaneity = "1",
                dcFaultDetection = DcFaultDetection.BUILT_IN_6MA,
            )
        },
    ),
    WorkedExample(
        key = "car_park",
        title = Res.string.ev_example_car_park,
        // Eight points, no diversity, and a charger nobody has checked the
        // plate of — so the RCD answer is Type B.
        fill = { state ->
            state.copy(
                pointCount = "8",
                ratedCurrent = "32",
                connection = EvseConnection.THREE_PHASE,
                supplyVoltage = "400",
                simultaneity = "1",
                dcFaultDetection = DcFaultDetection.NONE,
            )
        },
    ),
)
