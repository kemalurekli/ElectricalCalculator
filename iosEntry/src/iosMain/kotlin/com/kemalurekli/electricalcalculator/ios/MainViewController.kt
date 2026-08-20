package com.kemalurekli.electricalcalculator.ios

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.kemalurekli.electricalcalculator.core.data.di.coreDataModule
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.features.converter.converterModule
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.dashboard_converter_title
import com.kemalurekli.electricalcalculator.features.history.historyModule
import com.kemalurekli.electricalcalculator.features.history.presentation.HistoryRoute
import com.kemalurekli.electricalcalculator.features.converter.presentation.ConverterRoute
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.destination_history
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.Res as ConverterRes
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.Res as HistoryRes

/**
 * The single function Xcode's Swift shell calls.
 *
 * Everything above this line is shared Kotlin; everything below it is UIKit.
 * Keeping the boundary to one function is deliberate — the more of the app that
 * lives in Swift, the more there is to write twice.
 *
 * `ProofScreen` used to be here: five lines showing that shared Kotlin could
 * run on iOS at all. It has done its job and been replaced by real screens.
 *
 * Koin is started here rather than in Swift because this is where the object
 * graph is known. On Android the equivalent call is in the Application class.
 */
fun MainViewController(): UIViewController {
    startKoin {
        modules(coreDataModule, converterModule, historyModule)
    }
    return ComposeUIViewController {
        ElecToolkitTheme {
            IosShell()
        }
    }
}

/** The two screens ported so far. */
private enum class IosTab { Converter, History }

/**
 * A stand-in for `ElecAppShell` until navigation itself is multiplatform.
 *
 * The Android shell drives the same `NavigationSuiteScaffold` from a
 * `NavHostController`; this one swaps on an enum, because the navigation graph
 * still lives in `:app` alongside forty-odd routes that have not moved. What is
 * being proven here is not navigation — it is that the tab bar, the theme and a
 * screen reading from the database all work on iOS at once.
 *
 * Both screens are given a null `onNavigateBack`: a tab is a destination, not
 * somewhere you arrived from, so the title bar shows no back arrow. The empty
 * `onOpenRecord` is the honest kind of stub — opening a saved record needs the
 * calculator screens, which come later.
 */
@Composable
private fun IosShell() {
    // The ordinal rather than the entry itself: `rememberSaveable` stores
    // primitives, and an enum needs a Saver spelled out to survive the trip.
    var selectedOrdinal by rememberSaveable { mutableStateOf(IosTab.Converter.ordinal) }
    val selected = IosTab.entries[selectedOrdinal]

    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        layoutType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo()),
        navigationSuiteItems = {
            item(
                selected = selected == IosTab.Converter,
                onClick = { selectedOrdinal = IosTab.Converter.ordinal },
                icon = { Icon(ElecIcons.Converter, contentDescription = null) },
                label = { Text(stringResource(ConverterRes.string.dashboard_converter_title)) },
            )
            item(
                selected = selected == IosTab.History,
                onClick = { selectedOrdinal = IosTab.History.ordinal },
                icon = { Icon(ElecIcons.History, contentDescription = null) },
                label = { Text(stringResource(HistoryRes.string.destination_history)) },
            )
        },
    ) {
        when (selected) {
            IosTab.Converter -> ConverterRoute(onNavigateBack = null)
            IosTab.History -> HistoryRoute(onOpenRecord = {}, onNavigateBack = null)
        }
    }
}
