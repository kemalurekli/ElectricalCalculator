package com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.presentation

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Three boards, and the third is the one worth running.
 *
 * A balanced linear board puts nothing in the neutral. An unbalanced one puts a
 * little. A perfectly balanced board full of electronics puts more in the
 * neutral than any line carries — which is the case that surprises people, and
 * the case that derates the cable.
 */
internal val neutralCurrentExamples: ImmutableList<WorkedExample<NeutralCurrentUiState>> =
    persistentListOf(
        WorkedExample(
            key = "balanced_linear",
            titleRes = R.string.nc_example_balanced,
            // Motors and heaters, evenly spread: the neutral carries nothing.
            fill = { state ->
                state.copy(line1 = "100", line2 = "100", line3 = "100", thirdHarmonic = "0")
            },
        ),
        WorkedExample(
            key = "unbalanced",
            titleRes = R.string.nc_example_unbalanced,
            // Single-phase loads spread unevenly across a distribution board.
            fill = { state ->
                state.copy(line1 = "100", line2 = "80", line3 = "60", thirdHarmonic = "0")
            },
        ),
        WorkedExample(
            key = "electronic_load",
            titleRes = R.string.nc_example_electronic,
            // Balanced, and the neutral is still the busiest conductor.
            fill = { state ->
                state.copy(line1 = "100", line2 = "100", line3 = "100", thirdHarmonic = "40")
            },
        ),
    )
