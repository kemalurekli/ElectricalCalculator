package com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation

import com.kemalurekli.electricalcalculator.core.common.util.seededDecimal
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayArrangement
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tf_example_control
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tf_example_spaced
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tf_example_touching

/**
 * The same tray asked three different questions.
 *
 * The first two carry an identical bundle in one layer, touching and then spaced
 * a diameter apart, so the width the spacing costs is visible as a number rather
 * than as advice. The third stacks control cables, where the binding constraint
 * stops being width and becomes area — which is why the calculator treats them
 * as two calculations rather than two views of one.
 */
internal val trayFillExamples: ImmutableList<WorkedExample<TrayFillUiState>> = persistentListOf(
    WorkedExample(
        key = "single_layer_touching",
        title = Res.string.tf_example_touching,
        // Six power cables laid touching across a 300 mm tray.
        fill = { state ->
            state.copy(
                trayWidth = "300",
                trayDepth = "100",
                arrangement = TrayArrangement.SINGLE_LAYER,
                spacing = "0",
                cables = persistentListOf(
                    TrayCableRowState(id = 0, diameter = "20.5".seededDecimal(), quantity = "6"),
                ),
            )
        },
    ),
    WorkedExample(
        key = "single_layer_spaced",
        title = Res.string.tf_example_spaced,
        // The same six, spaced one diameter apart to recover ampacity.
        fill = { state ->
            state.copy(
                trayWidth = "300",
                trayDepth = "100",
                arrangement = TrayArrangement.SINGLE_LAYER,
                spacing = "20.5".seededDecimal(),
                cables = persistentListOf(
                    TrayCableRowState(id = 0, diameter = "20.5".seededDecimal(), quantity = "6"),
                ),
            )
        },
    ),
    WorkedExample(
        key = "multi_layer_control",
        title = Res.string.tf_example_control,
        // Control cables stacked: now the tray's cross-section is what fills up.
        fill = { state ->
            state.copy(
                trayWidth = "200",
                trayDepth = "60",
                arrangement = TrayArrangement.MULTI_LAYER,
                limit = "40",
                cables = persistentListOf(
                    TrayCableRowState(id = 0, diameter = "9.5".seededDecimal(), quantity = "24"),
                ),
            )
        },
    ),
)
