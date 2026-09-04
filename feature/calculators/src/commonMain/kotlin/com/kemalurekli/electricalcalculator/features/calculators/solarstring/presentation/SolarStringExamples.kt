package com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation

import com.kemalurekli.electricalcalculator.core.common.util.seededDecimal
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_example_continental
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_example_residential
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_example_temperate

/**
 * The same module on the same inverter, at three sites.
 *
 * Only the cold-morning temperature changes between the first two, and the
 * maximum string length changes with it. That is the entire lesson of this
 * calculator in one comparison: the module did not change, the inverter did not
 * change, and a string that is safe in one place damages equipment in another.
 */
internal val solarStringExamples: ImmutableList<WorkedExample<SolarStringUiState>> =
    persistentListOf(
        WorkedExample(
            key = "temperate",
            title = Res.string.ss_example_temperate,
            // A mild coastal site: −10 °C on the coldest clear morning.
            fill = { state ->
                state.copy(
                    voc = "49.5".seededDecimal(),
                    vmp = "41.5".seededDecimal(),
                    coefficient = "-0.27",
                    minTemperature = "-10",
                    maxTemperature = "70",
                    inverterMax = "1000",
                    mpptMin = "200",
                )
            },
        ),
        WorkedExample(
            key = "continental",
            title = Res.string.ss_example_continental,
            // Inland at −25 °C: the same array, and a shorter string.
            fill = { state ->
                state.copy(
                    voc = "49.5".seededDecimal(),
                    vmp = "41.5".seededDecimal(),
                    coefficient = "-0.27",
                    minTemperature = "-25",
                    maxTemperature = "70",
                    inverterMax = "1000",
                    mpptMin = "200",
                )
            },
        ),
        WorkedExample(
            key = "residential",
            title = Res.string.ss_example_residential,
            // A 600 V rooftop inverter, which is where strings get short.
            fill = { state ->
                state.copy(
                    voc = "41.0".seededDecimal(),
                    vmp = "34.2".seededDecimal(),
                    coefficient = "-0.29",
                    minTemperature = "-15",
                    maxTemperature = "75",
                    inverterMax = "600",
                    mpptMin = "125",
                )
            },
        ),
    )
