package com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation

import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_example_aluminium
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_example_conductor_only
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_example_drum

/**
 * Three drums, one of which has no diameter to hand.
 *
 * That last case is the point: without a datasheet diameter the calculator
 * reports the conductor mass, which is exact, and refuses to estimate the
 * rest. Seeing it stop is more useful than seeing it guess.
 */
internal val cableWeightExamples: ImmutableList<WorkedExample<CableWeightUiState>> = persistentListOf(
    WorkedExample(
        key = "drum_4x25",
        title = Res.string.cw_example_drum,
        // A 1 km drum of 4×25 mm² PVC copper — about 1,44 kg/m complete.
        fill = { state ->
            state.copy(
            crossSection = "25",
            conductorCount = "4",
            length = "1000",
            material = ConductorMaterial.COPPER,
            diameter = "25",
            insulation = CableInsulation.PVC,
            )
        },
    ),
    WorkedExample(
        key = "aluminium_feeder",
        title = Res.string.cw_example_aluminium,
        // The same geometry in aluminium: a third of the metal mass.
        fill = { state ->
            state.copy(
            crossSection = "95",
            conductorCount = "4",
            length = "500",
            material = ConductorMaterial.ALUMINIUM,
            diameter = "48",
            insulation = CableInsulation.XLPE,
            )
        },
    ),
    WorkedExample(
        key = "no_datasheet",
        title = Res.string.cw_example_conductor_only,
        // No overall diameter: only the figure that can be derived is reported.
        fill = { state ->
            state.copy(
            crossSection = "1.5",
            conductorCount = "3",
            length = "100",
            material = ConductorMaterial.COPPER,
            diameter = "",
            insulation = CableInsulation.PVC,
            )
        },
    ),
)
