package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout

/**
 * The frame a screen is drawn in: title bar, insets, width cap, snackbars.
 *
 * Every screen in the app repeated this by hand, and twenty-four of the
 * forty-two got it right. The other eighteen — Settings, all five forum
 * screens, Projects, History, Favourites, the calculator index and four
 * calculators — were missing the width cap, so on a tablet or an unfolded
 * foldable their content ran the full width of the window: list rows a hand's
 * width apart from the control at their far end, and paragraphs too long to
 * find the start of the next line. It was never visible on a phone, which is
 * why it survived so long, and iPad is a first-class target for the iOS build.
 *
 * Doing it here means the rules are stated once. A screen that wants different
 * behaviour has to say so.
 *
 * ### Why the cap goes on the Scaffold and not on the content
 *
 * `widthIn` must precede `fillMaxSize`: constraints flow from the outside in,
 * so filling first pins the minimum width to the whole window and leaves the
 * cap nothing to reduce. And it wraps the Scaffold rather than just its body,
 * because a capped list under a full-bleed title bar puts the heading and the
 * rows it names on two different grids.
 *
 * @param scrollBehavior pass the same instance the content's `nestedScroll`
 *   uses; it is what tells the bar to draw its separator once something has
 *   scrolled underneath.
 * @param snackbarHostState omit unless the screen shows snackbars; the default
 *   is a host nothing ever posts to, which costs one object and saves every
 *   caller from declaring one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElecScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    /** Replaces [title] in the bar — see [ElecTopAppBar]. */
    titleContent: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val layout = currentWindowLayout()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier
                .widthIn(max = layout.contentMaxWidth)
                .fillMaxSize()
                .then(
                    scrollBehavior
                        ?.let { Modifier.nestedScroll(it.nestedScrollConnection) }
                        ?: Modifier,
                ),
            topBar = {
                ElecTopAppBar(
                    title = title,
                    onNavigateBack = onNavigateBack,
                    scrollBehavior = scrollBehavior,
                    actions = actions,
                    titleContent = titleContent,
                )
            },
            bottomBar = bottomBar,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = floatingActionButton,
            content = content,
        )
    }
}

/** The scroll behaviour every screen uses, named so no call site picks another. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberElecScrollBehavior(): TopAppBarScrollBehavior =
    TopAppBarDefaults.pinnedScrollBehavior()
