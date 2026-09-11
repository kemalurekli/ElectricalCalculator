package com.kemalurekli.electricalcalculator.shell

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.kemalurekli.electricalcalculator.core.navigation.Route
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kemalurekli.electricalcalculator.core.common.util.RegionProvider
import com.kemalurekli.electricalcalculator.core.designsystem.component.DisclaimerDialog
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.domain.repository.AppLanguageRepository
import com.kemalurekli.electricalcalculator.core.domain.repository.UserPreferencesRepository
import com.kemalurekli.electricalcalculator.core.navigation.ElecTab
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

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

    // The paywall takes the window as well, for a different reason than the
    // keyboard does. It is a question rather than a place, and a screen asking
    // to be paid with the app's own tabs running along the bottom reads as a
    // settings page. The way out is the back arrow it draws itself — this hides
    // the bar, not the exit.
    val onPaywall = current?.hasRoute(Route.Paywall::class) == true

    // Which tab the reader is *inside*, which is not the same question as which
    // tab a destination belongs to.
    //
    // The reference library is on the More shelf and is also linked from the
    // dashboard. Asked the second way, the bar lit More while the Home screen
    // sat underneath it in the back stack — so the bar was telling the reader
    // they were somewhere they were not, and Home, the tab they were already
    // in, looked like a button that did nothing.
    //
    // The back stack knows. The nearest tab root below the current screen is
    // the tab they are in, whatever route they took to get here.
    val backStack by navController.currentBackStack.collectAsState()
    val activeTab = remember(backStack) {
        backStack.asReversed().firstNotNullOfOrNull { entry ->
            ElecTab.entries.firstOrNull { tab -> entry.destination.hasRoute(tab.route::class) }
        } ?: ElecTab.HOME
    }

    val fullWindow = keyboardVisible || onPaywall
    val suiteType = if (fullWindow) {
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
                    selected = activeTab == tab,
                    // A second press on the tab you are already in goes back to
                    // where that tab starts. It is what every reader has been
                    // taught to expect by every other app, and it is the way
                    // out of a screen opened from the dashboard without
                    // reaching for the back arrow.
                    onClick = {
                        if (activeTab == tab) {
                            actions.resetTab(tab)
                        } else {
                            actions.navigateToTab(tab)
                        }
                    },
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
                    if (fullWindow) {
                        Modifier
                    } else {
                        Modifier.consumeWindowInsets(WindowInsets.navigationBars)
                    },
                ),
        )
    }
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

/**
 * The app, themed from what the reader chose, with the disclaimer over it.
 *
 * Both of these used to be in `MainActivity`, which meant iOS had neither:
 * choosing Dark in settings did nothing there, and the disclaimer — which the
 * app is not supposed to be used without — never appeared at all.
 *
 * The splash hold is still Android's. `MainUiState.Loading` exists so the first
 * composed frame already has the right colour scheme instead of flashing light
 * and snapping to dark; iOS has no splash API to hold, so it renders the stored
 * theme as soon as the preference arrives, which is a frame later.
 */
@Composable
fun ElecToolkitApp(
    modifier: Modifier = Modifier,
    preferencesRepository: UserPreferencesRepository = koinInject(),
    languageRepository: AppLanguageRepository = koinInject(),
) {
    val preferences by preferencesRepository.preferences
        .collectAsState(initial = null)
    val language by languageRepository.language.collectAsState()

    val resolved = preferences ?: UserPreferences.Default
    val darkTheme = when (resolved.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Outside the `key` below, so changing the language does not also throw
    // away where the reader was. Android would have restored the back stack
    // through saved state after the activity recreation; keeping the same
    // controller is how iOS gets the same outcome.
    val navController = rememberNavController()

    ElecToolkitTheme(darkTheme = darkTheme) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // Restarts the whole tree when the language changes.
            //
            // Compose Resources picks a `values-*` folder from `Locale.current`,
            // read inside each `stringResource` call and cached there. Nothing
            // invalidates that cache on its own: the strings are not state, so
            // a screen that does not otherwise recompose keeps the language it
            // was composed in. Android never needed this because it recreates
            // the activity; this is the same restart, done in one place for
            // both.
            key(language) {
                ElecAppShell(navController = navController)
            }

            // Over the app rather than before it: the reader can see what they
            // are agreeing to use. Held until accepted, and only ever shown
            // once — an acknowledgement that reappears every launch is one
            // nobody reads.
            if (preferences != null && !resolved.disclaimerAccepted) {
                val scope = rememberCoroutineScope()
                DisclaimerDialog(
                    onAccept = {
                        scope.launch { preferencesRepository.setDisclaimerAccepted(true) }
                    },
                )
            }
        }
    }
}
