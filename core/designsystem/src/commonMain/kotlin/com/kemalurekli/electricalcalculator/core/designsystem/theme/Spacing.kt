package com.kemalurekli.electricalcalculator.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale on a 4dp grid.
 *
 * Every layout in the app measures its padding and gaps from these tokens so
 * that rhythm stays consistent across screens and densities.
 */
@Immutable
data class ElecSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    /** Horizontal inset applied to the content of every full-width screen. */
    val screenHorizontal: Dp = 16.dp,
    /** Vertical gap between major sections on a screen. */
    val sectionGap: Dp = 24.dp,
    /**
     * Bottom padding a scrolling list needs when a floating action button sits
     * over it.
     *
     * The button floats above the content rather than beside it, so the last row
     * of a list that ends at the window's edge is permanently underneath it —
     * readable in a screenshot, unreachable with a thumb. Three screens had this
     * and none of them had noticed, because the bug only shows once the list is
     * long enough to reach the bottom.
     *
     * A 56dp button, its own 16dp inset, and one more gap so the last row is not
     * touching it.
     */
    val fabClearance: Dp = 88.dp,
)

val LocalElecSpacing = staticCompositionLocalOf { ElecSpacing() }

/**
 * Semantic colours that Material 3 does not define, used to communicate the
 * engineering status of a result (within limits, marginal, informational).
 */
@Immutable
data class ElecSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

internal val LightSemanticColors = ElecSemanticColors(
    success = SuccessLight,
    onSuccess = OnSuccessLight,
    successContainer = SuccessContainerLight,
    onSuccessContainer = OnSuccessContainerLight,
    warning = WarningLight,
    onWarning = OnWarningLight,
    warningContainer = WarningContainerLight,
    onWarningContainer = OnWarningContainerLight,
    info = InfoLight,
    onInfo = OnInfoLight,
    infoContainer = InfoContainerLight,
    onInfoContainer = OnInfoContainerLight,
)

internal val DarkSemanticColors = ElecSemanticColors(
    success = SuccessDark,
    onSuccess = OnSuccessDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = OnSuccessContainerDark,
    warning = WarningDark,
    onWarning = OnWarningDark,
    warningContainer = WarningContainerDark,
    onWarningContainer = OnWarningContainerDark,
    info = InfoDark,
    onInfo = OnInfoDark,
    infoContainer = InfoContainerDark,
    onInfoContainer = OnInfoContainerDark,
)

val LocalElecSemanticColors = staticCompositionLocalOf { LightSemanticColors }
