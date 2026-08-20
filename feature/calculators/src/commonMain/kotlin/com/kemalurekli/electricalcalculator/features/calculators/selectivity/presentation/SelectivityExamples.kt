package com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation

import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_example_at_board
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_example_close
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_example_holds

/**
 * Three boards, and three different reasons discrimination fails or holds.
 *
 * The pair chosen for the second one is not a strawman. 63 A above 40 A is what
 * a great many distribution boards actually look like, and its ratio is 1.575 —
 * just under the margin conventionally asked for, which is the kind of near
 * miss that only shows up when someone works it out.
 */
internal val selectivityExamples: ImmutableList<WorkedExample<SelectivityUiState>> =
    persistentListOf(
        WorkedExample(
            key = "final_circuit_far_from_the_board",
            title = Res.string.sel_example_holds,
            // A long final circuit: the fault current at its far end is modest,
            // and the incomer never sees enough to open.
            fill = { state ->
                state.copy(
                    upstreamType = ProtectiveDeviceType.MCB_TYPE_C,
                    upstreamRating = "63",
                    downstreamType = ProtectiveDeviceType.MCB_TYPE_B,
                    downstreamRating = "16",
                    faultCurrent = "480",
                )
            },
        ),
        WorkedExample(
            key = "close_ratings",
            title = Res.string.sel_example_close,
            // 63 A over 40 A is 1.575 — the overload end fails by a whisker.
            fill = { state ->
                state.copy(
                    upstreamType = ProtectiveDeviceType.MCB_TYPE_C,
                    upstreamRating = "63",
                    downstreamType = ProtectiveDeviceType.MCB_TYPE_C,
                    downstreamRating = "40",
                    faultCurrent = "500",
                )
            },
        ),
        WorkedExample(
            key = "fault_at_the_board",
            title = Res.string.sel_example_at_board,
            // The same pair as the first example, at a fault close to the board
            // rather than at the end of a long run. Nothing about the devices
            // changed; the answer did.
            fill = { state ->
                state.copy(
                    upstreamType = ProtectiveDeviceType.MCB_TYPE_C,
                    upstreamRating = "63",
                    downstreamType = ProtectiveDeviceType.MCB_TYPE_B,
                    downstreamRating = "16",
                    faultCurrent = "1800",
                )
            },
        ),
    )
