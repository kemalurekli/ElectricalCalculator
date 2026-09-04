package com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation

import com.kemalurekli.electricalcalculator.core.common.util.seededDecimal
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.CapacitorConnection
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pf_example_factory
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pf_example_shop

/**
 * Two correction jobs at opposite ends of the scale.
 *
 * Both make the same point in different sizes: the released capacity is
 * usually the number that pays for the work, because it is transformer and
 * cable capacity recovered without touching either.
 */
internal val powerFactorExamples: ImmutableList<WorkedExample<PowerFactorUiState>> = persistentListOf(
    WorkedExample(
        key = "factory_100kw",
        title = Res.string.pf_example_factory,
        // 100 kW from 0,75 to 0,95 — the reference case.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            connection = CapacitorConnection.DELTA,
            activePowerKw = "100",
            existingFactor = "0.75".seededDecimal(),
            targetFactor = "0.95".seededDecimal(),
            voltage = "400",
            voltageEdited = true,
            frequency = "50",
            )
        },
    ),
    WorkedExample(
        key = "small_shop_30kw",
        title = Res.string.pf_example_shop,
        // A 30 kW shop already at 0,85, pushed just past a tariff threshold.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            connection = CapacitorConnection.DELTA,
            activePowerKw = "30",
            existingFactor = "0.85".seededDecimal(),
            targetFactor = "0.95".seededDecimal(),
            voltage = "400",
            voltageEdited = true,
            frequency = "50",
            )
        },
    ),
)
