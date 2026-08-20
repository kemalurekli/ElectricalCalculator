package com.kemalurekli.electricalcalculator.shell

import org.koin.compose.koinInject
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import androidx.compose.runtime.LaunchedEffect
import com.kemalurekli.electricalcalculator.core.navigation.ElecTab
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * The frame every screen is drawn inside.
 *
 * Until this existed the app's shelves were reachable only by returning to the
 * dashboard and tapping a card. Nothing on screen said where you were or what
 * else there was, and on a tablet — where there is room for permanent
 * navigation — there was still none.
 *
 * ### Why NavigationSuiteScaffold rather than a NavigationBar
 *
 * One declaration renders as a bottom bar on a phone and as a navigation rail
 * on a tablet or an unfolded foldable, decided from the window. Writing the bar
 * directly would mean writing the rail too and keeping the two in step. It is
 * also available on Compose Multiplatform, so the shell moves with the rest of
 * the UI rather than being rebuilt against a UITabBarController.
 *
 * ### The insets, and why the bar goes away with the keyboard
 *
 * Every screen sets `contentWindowInsets = WindowInsets.safeDrawing` on its own
 * Scaffold, and `safeDrawing` is measured against the *window* — it has no idea
 * it is being read inside a box that the tab bar has already shortened. With
 * the keyboard open that produced a gap exactly one tab bar tall between the
 * content and the keyboard, because the screen padded for an IME inset measured
 * from the bottom of a window it no longer reached.
 *
 * Hiding the suite while the keyboard is up fixes it at the source rather than
 * by arithmetic: the content is the whole window again, and `safeDrawing` is
 * once more the truth. It is also the better behaviour — a tab bar underneath a
 * keyboard is a row of controls nobody can reach, and both platforms hide it.
 *
 * [consumeWindowInsets] then handles the ordinary case, where the bar is
 * showing and has already paid for the navigation bar inset. Only
 * [WindowInsets.navigationBars] is consumed: the status bar at the top is
 * untouched by the shell and the screens still need to pad for it.
 *
 * This app has shipped two separate bugs from double-counted insets, and this
 * was very nearly the third. That is why it is spelled out rather than left as
 * a modifier nobody questions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ElecAppShell(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    SeedEngineeringDefaults()

    val actions = remember(navController) { NavActions(navController) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination

    // `WindowInsets.isImeVisible` is Android-only. The inset itself is not, and
    // a keyboard that occupies no space is not showing — which is the question
    // being asked.
    val density = LocalDensity.current
    val keyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val suiteType = if (keyboardVisible) {
        NavigationSuiteType.None
    } else {
        NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo())
    }

    NavigationSuiteScaffold(
        modifier = modifier.fillMaxSize(),
        layoutType = suiteType,
        navigationSuiteItems = {
            ElecTab.entries.forEach { tab ->
                item(
                    selected = current.isIn(tab),
                    onClick = { actions.navigateToTab(tab) },
                    // The label names it; announcing the icon as well would
                    // read every tab twice.
                    icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                    // Resolved inside the slot, not above the loop: this
                    // builder is a plain lambda, not a composable one.
                    label = { Text(text = stringResource(tab.label)) },
                )
            }
        },
    ) {
        ElecNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                // Nothing to consume when the suite is not there: the screens
                // own the whole window, keyboard inset included.
                .then(
                    if (keyboardVisible) {
                        Modifier
                    } else {
                        Modifier.consumeWindowInsets(WindowInsets.navigationBars)
                    },
                ),
        )
    }
}

/**
 * Whether what is on screen belongs to [tab], so the bar can highlight it.
 *
 * Matched against every route the tab owns, not only its root. A reader three
 * screens into the forum — a category, a thread, an author's profile — is still
 * in the Forum tab, and a bar that drops its highlight the moment they open
 * something has stopped telling them where they are.
 */
private fun NavDestination?.isIn(tab: ElecTab): Boolean {
    val destination = this ?: return false
    return tab.routes.any { route -> destination.hasRoute(route) }
}

/**
 * The first-run guess at supply voltage and frequency.
 *
 * Runs once per launch and does nothing after the first, which is what makes it
 * safe to sit here rather than behind a first-run check: a language change on
 * Android recreates the activity and lands here again, finds the values already
 * owned by the user, and leaves them.
 *
 * It used to be in `MainViewModel`, which only Android has — so an iPhone
 * opened for the first time in Texas got the 230 V defaults the rest of the
 * world uses, and no screen said why.
 */
@Composable
private fun SeedEngineeringDefaults(
    preferences: UserPreferencesRepository = koinInject(),
    regionProvider: RegionProvider = koinInject(),
) {
    LaunchedEffect(Unit) {
        preferences.seedEngineeringDefaults(regionProvider.currentRegion())
    }
}
