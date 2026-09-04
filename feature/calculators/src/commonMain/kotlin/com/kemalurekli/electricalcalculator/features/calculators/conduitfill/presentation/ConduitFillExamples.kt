package com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation

import com.kemalurekli.electricalcalculator.core.common.util.seededDecimal
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.FillRule
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cf_example_mixed
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cf_example_three_singles
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cf_example_two_cables

/**
 * Three conduits, chosen to walk across the step in NEC Chapter 9, Table 1.
 *
 * One cable is allowed 53 % of the bore, two only 31 %, three or more 40 %. The
 * middle example sits on that step deliberately: adding a *third* cable to a
 * two-cable conduit can leave it more compliant than it was, which reads as a
 * mistake until you have seen where the number comes from.
 *
 * The rows carry explicit ids because the screen keys its list on them; ids are
 * assigned from zero so an example never collides with a row the user added.
 */
internal val conduitFillExamples: ImmutableList<WorkedExample<ConduitFillUiState>> =
    persistentListOf(
        WorkedExample(
            key = "three_singles",
            title = Res.string.cf_example_three_singles,
            // Three singles in 25 mm: the ordinary case, held to 40 %.
            fill = { state ->
                state.copy(
                    conduitDiameter = "25",
                    rule = FillRule.NEC_TABLE_1,
                    cables = persistentListOf(
                        CableRowState(id = 0, diameter = "8.5".seededDecimal(), quantity = "3"),
                    ),
                )
            },
        ),
        WorkedExample(
            key = "two_cables_step",
            title = Res.string.cf_example_two_cables,
            // Two cables are held to 31 %, which is the row people misread.
            fill = { state ->
                state.copy(
                    conduitDiameter = "20",
                    rule = FillRule.NEC_TABLE_1,
                    cables = persistentListOf(
                        CableRowState(id = 0, diameter = "10.5".seededDecimal(), quantity = "2"),
                    ),
                )
            },
        ),
        WorkedExample(
            key = "mixed_bundle",
            title = Res.string.cf_example_mixed,
            // A mixed bundle against a house 40 % rule rather than a code table.
            fill = { state ->
                state.copy(
                    conduitDiameter = "32",
                    rule = FillRule.CUSTOM,
                    customLimit = "40",
                    cables = persistentListOf(
                        CableRowState(id = 0, diameter = "12", quantity = "3"),
                        CableRowState(id = 1, diameter = "7.5".seededDecimal(), quantity = "4"),
                    ),
                )
            },
        ),
    )
