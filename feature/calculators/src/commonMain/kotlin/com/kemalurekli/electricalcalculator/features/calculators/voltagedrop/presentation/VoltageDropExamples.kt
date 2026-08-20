package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation

import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_example_lighting
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_example_riser
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_example_workshop

/**
 * Three runs a voltage drop calculation actually gets asked about.
 *
 * The middle one fails its limit on purpose. A long lighting circuit on a
 * small conductor is the classic case where the cable is comfortably within
 * its current rating and still unusable, and seeing that once is worth more
 * than three examples that all pass.
 */
internal val voltageDropExamples: ImmutableList<WorkedExample<VoltageDropUiState>> = persistentListOf(
    WorkedExample(
        key = "workshop_feed",
        title = Res.string.vd_example_workshop,
        // A 63 A workshop submain: comfortably inside the 5 % limit.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            voltage = "400",
            voltageEdited = true,
            current = "63",
            length = "45",
            crossSection = "25",
            material = ConductorMaterial.COPPER,
            powerFactor = "0.9",
            temperature = "70",
            parallelConductors = "1",
            )
        },
    ),
    WorkedExample(
        key = "long_lighting",
        title = Res.string.vd_example_lighting,
        // 90 m of 2,5 mm² for garden lighting. Well inside its current
        // rating and far outside the 3 % a lighting circuit is held to.
        fill = { state ->
            state.copy(
            system = SupplySystem.SINGLE_PHASE_AC,
            voltage = "230",
            voltageEdited = true,
            current = "10",
            length = "90",
            crossSection = "2.5",
            material = ConductorMaterial.COPPER,
            powerFactor = "1",
            temperature = "70",
            parallelConductors = "1",
            )
        },
    ),
    WorkedExample(
        key = "aluminium_riser",
        title = Res.string.vd_example_riser,
        // An aluminium riser: the material change is what the reader is here to see.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            voltage = "400",
            voltageEdited = true,
            current = "200",
            length = "60",
            crossSection = "95",
            material = ConductorMaterial.ALUMINIUM,
            powerFactor = "0.9",
            temperature = "70",
            parallelConductors = "1",
            )
        },
    ),
)
