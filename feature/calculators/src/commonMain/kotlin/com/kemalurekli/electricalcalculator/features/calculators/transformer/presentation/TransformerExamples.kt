package com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation

import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_example_1000
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_example_630
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_example_single

/**
 * Two distribution transformers an engineer meets constantly, and one small
 * single-phase unit.
 *
 * The 1000 kVA case is the one worth knowing by heart: 1443 A on the
 * secondary, which is the figure a substation engineer checks everything else
 * against.
 */
internal val transformerExamples: ImmutableList<WorkedExample<TransformerUiState>> = persistentListOf(
    WorkedExample(
        key = "dist_630",
        title = Res.string.tx_example_630,
        // 630 kVA at 4 % — the common size below the 6 % threshold.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            ratingKva = "630",
            primaryVoltage = "34500",
            secondaryVoltage = "400",
            impedancePercent = "4",
            )
        },
    ),
    WorkedExample(
        key = "dist_1000",
        title = Res.string.tx_example_1000,
        // 1000 kVA at 400 V is the canonical 1443 A.
        fill = { state ->
            state.copy(
            system = SupplySystem.THREE_PHASE_AC,
            ratingKva = "1000",
            primaryVoltage = "34500",
            secondaryVoltage = "400",
            impedancePercent = "6",
            )
        },
    ),
    WorkedExample(
        key = "single_phase_25",
        title = Res.string.tx_example_single,
        // A small single-phase unit: no √3, and no factor of two either.
        fill = { state ->
            state.copy(
            system = SupplySystem.SINGLE_PHASE_AC,
            ratingKva = "25",
            primaryVoltage = "11000",
            secondaryVoltage = "230",
            impedancePercent = "4",
            )
        },
    ),
)
