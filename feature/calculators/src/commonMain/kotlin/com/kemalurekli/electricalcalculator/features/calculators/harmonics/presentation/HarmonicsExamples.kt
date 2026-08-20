package com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation

import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_example_drive
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_example_lighting
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_example_office

/**
 * Three loads whose spectra look nothing like each other.
 *
 * The office floor is the one to read first: its neutral carries more than its
 * lines, which is the single fact about harmonics that changes how a board is
 * built.
 */
internal val harmonicsExamples: ImmutableList<WorkedExample<HarmonicsUiState>> = persistentListOf(
    WorkedExample(
        key = "office_floor",
        title = Res.string.hm_example_office,
        // Switch-mode supplies: heavy third, and enough ninth to matter.
        fill = { state ->
            state.copy(
                fundamental = "100",
                magnitudes = mapOf(3 to "70", 5 to "40", 7 to "15", 9 to "10", 11 to "6", 13 to "4"),
                balanced = true,
            )
        },
    ),
    WorkedExample(
        key = "six_pulse_drive",
        title = Res.string.hm_example_drive,
        // A six-pulse rectifier produces no triplen at all: the fifth and
        // seventh dominate, the neutral stays empty, and the transformer still
        // runs hot. The pairing that shows the two problems are not the same.
        fill = { state ->
            state.copy(
                fundamental = "150",
                magnitudes = mapOf(3 to "0", 5 to "35", 7 to "18", 9 to "0", 11 to "8", 13 to "5"),
                balanced = true,
            )
        },
    ),
    WorkedExample(
        key = "led_lighting",
        title = Res.string.hm_example_lighting,
        // Modest current, ugly spectrum. Small loads are where people assume
        // harmonics do not matter.
        fill = { state ->
            state.copy(
                fundamental = "25",
                magnitudes = mapOf(3 to "25", 5 to "12", 7 to "7", 9 to "5", 11 to "3", 13 to "2"),
                balanced = true,
            )
        },
    ),
)
