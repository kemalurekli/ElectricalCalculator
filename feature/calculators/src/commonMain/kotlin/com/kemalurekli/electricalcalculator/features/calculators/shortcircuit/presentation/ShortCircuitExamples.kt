package com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation

import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.FaultType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_example_board
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_example_far
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_example_neutral

/**
 * Three points in one installation, walking away from the origin.
 *
 * At the board the supply dominates and the fault current is enormous; 120 m
 * down a small cable the run has taken over and it has collapsed. That is the
 * whole reason a minimum fault current is calculated separately.
 */
internal val shortCircuitExamples: ImmutableList<WorkedExample<ShortCircuitUiState>> = persistentListOf(
    WorkedExample(
        key = "at_the_board",
        title = Res.string.sc_example_board,
        // At a board fed by a 20 kA supply: breaking capacity is the question.
        fill = { state ->
            state.copy(
            faultType = FaultType.THREE_PHASE,
            voltage = "400",
            supplyCurrent = "20000",
            length = "10",
            crossSection = "95",
            neutralSection = "95",
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.PVC,
            parallelConductors = "1",
            reactance = "0.08",
            )
        },
    ),
    WorkedExample(
        key = "end_of_run",
        title = Res.string.sc_example_far,
        // 120 m of 6 mm²: the cable now sets the current, not the supply.
        fill = { state ->
            state.copy(
            faultType = FaultType.THREE_PHASE,
            voltage = "400",
            supplyCurrent = "20000",
            length = "120",
            crossSection = "6",
            neutralSection = "6",
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.PVC,
            parallelConductors = "1",
            reactance = "0.08",
            )
        },
    ),
    WorkedExample(
        key = "reduced_neutral",
        title = Res.string.sc_example_neutral,
        // A line-to-neutral fault with a reduced neutral — the case most likely
        // to leave a device without enough current to trip.
        fill = { state ->
            state.copy(
            faultType = FaultType.LINE_TO_NEUTRAL,
            voltage = "230",
            supplyCurrent = "10000",
            length = "60",
            crossSection = "16",
            neutralSection = "10",
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.PVC,
            parallelConductors = "1",
            reactance = "0.08",
            )
        },
    ),
)
