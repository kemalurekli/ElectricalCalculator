package com.kemalurekli.electricalcalculator.core.ui.layout

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/**
 * How much room the current window has, expressed as layout decisions rather
 * than raw breakpoints.
 *
 * Screens read [dashboardColumns] and [contentMaxWidth] instead of testing
 * breakpoints themselves, so the phone/tablet/foldable rules are defined once
 * and every screen adapts consistently.
 *
 * @param dashboardColumns grid columns for large dashboard cards.
 * @param contentMaxWidth cap on content width. On a very wide window, letting
 *   a list run edge to edge produces unreadably long lines; the content is
 *   centred within this instead.
 */
@Immutable
data class WindowLayout(
    val dashboardColumns: Int,
    val contentMaxWidth: Dp,
    val isCompact: Boolean,
)

/**
 * Resolves the layout for the current window.
 *
 * Backed by [currentWindowAdaptiveInfoV2], so callers recompose automatically
 * when the window is resized, the device folds, or it enters multi-window mode.
 * The V2 variant is used because it reports the large and extra-large width
 * classes that desktop-sized windows fall into.
 */
@Composable
fun currentWindowLayout(): WindowLayout {
    val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val isExpanded = sizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
    )
    val isMedium = sizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
    )

    return when {
        isExpanded -> WindowLayout(
            dashboardColumns = 4,
            contentMaxWidth = 1240.dp,
            isCompact = false,
        )

        isMedium -> WindowLayout(
            dashboardColumns = 3,
            contentMaxWidth = 840.dp,
            isCompact = false,
        )

        else -> WindowLayout(
            dashboardColumns = 2,
            contentMaxWidth = Dp.Infinity,
            isCompact = true,
        )
    }
}
