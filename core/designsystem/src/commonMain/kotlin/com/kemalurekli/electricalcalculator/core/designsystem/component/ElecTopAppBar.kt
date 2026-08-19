package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_back

/**
 * The app's top bar. Every screen uses this one.
 *
 * ### The line underneath
 *
 * The bar separated itself from the content by a tonal step —  `surface` at
 * rest, `surfaceContainer` once something had scrolled under it. In the brand
 * palette those are #FBFCFD and #EFF2F6, about three per cent apart, which is
 * not a boundary anyone can see. Content slid under the bar and simply
 * vanished, with nothing to say it had gone somewhere rather than stopped.
 *
 * A hairline draws that boundary at any tone, and it is what both platforms do:
 * an iOS navigation bar grows a separator on scroll for the same reason. It
 * appears only when the content is actually scrolled, so a screen that fits has
 * no line dividing nothing from nothing.
 *
 * @param onNavigateBack when non-null, a back affordance is shown. Screens that
 *   are a navigation root — every tab root — pass null rather than rendering a
 *   button whose destination they cannot name.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElecTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = { BackButton(onNavigateBack) },
            actions = actions,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        )
        // `overlappedFraction` is 0 while the content sits below the bar and
        // rises as it scrolls under. Fading the line in with it means the
        // separator arrives with the thing it separates.
        val overlap = scrollBehavior?.state?.overlappedFraction ?: 0f
        if (overlap > 0f) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = overlap.coerceIn(0f, 1f),
                ),
            )
        }
    }
}

// `ElecLargeTopAppBar` used to live here: a collapsing bar whose title started
// large and shrank as content scrolled under it. Forty-two screens were written
// and none of them ever called it. It is deleted rather than kept "in case",
// because a shared design language has to be one decision per question — and on
// iOS the large-title bar is the platform default for a root screen, so an
// unused component claiming the app supports both would have been read as
// permission to use it there and not here.
//
// The app's answer is the compact bar above, on every screen, both platforms.

@Composable
private fun BackButton(onNavigateBack: (() -> Unit)?) {
    if (onNavigateBack == null) return
    IconButton(onClick = onNavigateBack) {
        Icon(
            imageVector = ElecIcons.Back,
            contentDescription = stringResource(Res.string.action_back),
        )
    }
}
