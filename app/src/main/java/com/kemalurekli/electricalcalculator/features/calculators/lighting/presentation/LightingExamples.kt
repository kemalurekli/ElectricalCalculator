package com.kemalurekli.electricalcalculator.features.calculators.lighting.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Three rooms at the three illuminances EN 12464-1 is most often quoted for.
 *
 * The warehouse is deliberately a tall space with a low target. It gives a small
 * room index, which is where a reader has to go and find a different column of
 * their luminaire's utilisation table — and the reason the room index is
 * reported at all.
 */
internal val lightingExamples: ImmutableList<WorkedExample<LightingUiState>> = persistentListOf(
    WorkedExample(
        key = "office",
        titleRes = R.string.lt_example_office,
        // A 12 × 8 m office at 500 lx: the reference case, and it lands exactly.
        fill = { state ->
            state.copy(
                illuminance = "500",
                length = "12",
                width = "8",
                mountingHeight = "2.2",
                flux = "4000",
                utilisation = "0.6",
                maintenance = "0.8",
            )
        },
    ),
    WorkedExample(
        key = "warehouse",
        titleRes = R.string.lt_example_warehouse,
        // Tall and dim: a low room index, high-bay fittings, 150 lx.
        fill = { state ->
            state.copy(
                illuminance = "150",
                length = "40",
                width = "20",
                mountingHeight = "8",
                flux = "22000",
                utilisation = "0.5",
                maintenance = "0.7",
            )
        },
    ),
    WorkedExample(
        key = "drawing_office",
        titleRes = R.string.lt_example_drawing,
        // 750 lx for technical drawing — half again the office figure.
        fill = { state ->
            state.copy(
                illuminance = "750",
                length = "9",
                width = "6",
                mountingHeight = "2.2",
                flux = "4000",
                utilisation = "0.65",
                maintenance = "0.8",
            )
        },
    ),
)
