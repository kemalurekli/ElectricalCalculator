package com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.StartingMethod
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The same 30 kW machine, three times.
 *
 * Nothing about the motor changes across the set. What changes is the supply it
 * is started against and the method used, which is the whole argument: a motor
 * is not "too big" on its own, it is too big for a particular transformer.
 */
internal val motorStartingExamples: ImmutableList<WorkedExample<MotorStartingUiState>> =
    persistentListOf(
        WorkedExample(
            key = "stiff_supply",
            titleRes = R.string.ms_example_stiff,
            // 400 kVA behind it: the start is a flicker and nothing more.
            fill = { state ->
                state.copy(
                    fullLoadCurrent = "55",
                    lockedRotorMultiple = "6",
                    method = StartingMethod.DIRECT_ON_LINE,
                    supplyVoltage = "400",
                    transformerKva = "400",
                    transformerImpedance = "4",
                )
            },
        ),
        WorkedExample(
            key = "small_site",
            titleRes = R.string.ms_example_small,
            // The same motor on 50 kVA. Over 20 %, which is where contactors
            // start letting go and the start takes the board with it.
            fill = { state ->
                state.copy(
                    fullLoadCurrent = "55",
                    lockedRotorMultiple = "6",
                    method = StartingMethod.DIRECT_ON_LINE,
                    supplyVoltage = "400",
                    transformerKva = "50",
                    transformerImpedance = "6",
                )
            },
        ),
        WorkedExample(
            key = "star_delta_rescue",
            titleRes = R.string.ms_example_star_delta,
            // The same impossible start, made possible — and the torque line
            // shows what it cost. A loaded machine may still not turn.
            fill = { state ->
                state.copy(
                    fullLoadCurrent = "55",
                    lockedRotorMultiple = "6",
                    method = StartingMethod.STAR_DELTA,
                    supplyVoltage = "400",
                    transformerKva = "50",
                    transformerImpedance = "6",
                )
            },
        ),
    )
