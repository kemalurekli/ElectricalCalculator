package com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation

import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_example_derated
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_example_lighting
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_example_sockets

/**
 * Three sizing jobs, chosen so a different constraint wins each time.
 *
 * The socket circuit is decided by current, the lighting run by voltage drop,
 * and the hot grouped motor feed by derating — which is the one that surprises
 * people, because nothing about the load changed and the cable still grew.
 */
internal val cableSizeExamples: ImmutableList<WorkedExample<CableSizeUiState>> = persistentListOf(
    WorkedExample(
        key = "socket_circuit",
        title = Res.string.cs_example_sockets,
        // An ordinary domestic socket circuit; capacity decides it.
        fill = { state ->
            state.copy(
            system = SupplySystem.SINGLE_PHASE_AC,
            voltage = "230",
            voltageEdited = true,
            current = "20",
            length = "25",
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.PVC,
            method = InstallationMethod.C_CLIPPED_DIRECT,
            powerFactor = "1",
            maxDropPercent = "5",
            ambientTemperature = "30",
            groupedCircuits = "1",
            parallelConductors = "1",
            )
        },
    ),
    WorkedExample(
        key = "hot_grouped_motor",
        title = Res.string.cs_example_derated,
        // 45 °C ambient and four circuits bunched together. Ca · Cg does the
        // damage here, not the load.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            voltage = "400",
            voltageEdited = true,
            current = "40",
            length = "60",
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.XLPE,
            method = InstallationMethod.C_CLIPPED_DIRECT,
            powerFactor = "0.85",
            maxDropPercent = "5",
            ambientTemperature = "45",
            groupedCircuits = "4",
            parallelConductors = "1",
            )
        },
    ),
    WorkedExample(
        key = "lighting_three_percent",
        title = Res.string.cs_example_lighting,
        // A 70 m lighting run held to 3 %: voltage drop wins by a wide margin.
        fill = { state ->
            state.copy(
            system = SupplySystem.SINGLE_PHASE_AC,
            voltage = "230",
            voltageEdited = true,
            current = "12",
            length = "70",
            material = ConductorMaterial.COPPER,
            insulation = CableInsulation.PVC,
            method = InstallationMethod.B1_CONDUIT_ON_WALL,
            powerFactor = "1",
            maxDropPercent = "3",
            ambientTemperature = "30",
            groupedCircuits = "1",
            parallelConductors = "1",
            )
        },
    ),
)
