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
import com.kemalurekli.electricalcalculator.core.common.di.coreCommonModule
import com.kemalurekli.electricalcalculator.core.data.di.coreDataModule
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.features.calculators.calculatorsModule
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorDestination
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorsRoute
import com.kemalurekli.electricalcalculator.features.converter.converterModule
import com.kemalurekli.electricalcalculator.features.fieldnotes.fieldNotesModule
import com.kemalurekli.electricalcalculator.features.fieldnotes.presentation.FieldNotesRoute
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.destination_field_notes
import com.kemalurekli.electricalcalculator.features.glossary.glossaryModule
import com.kemalurekli.electricalcalculator.features.glossary.presentation.GlossaryRoute
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.dashboard_glossary_title
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.tab_converter
import com.kemalurekli.electricalcalculator.features.history.historyModule
import com.kemalurekli.electricalcalculator.features.history.presentation.HistoryRoute
import com.kemalurekli.electricalcalculator.features.converter.presentation.ConverterRoute
import com.kemalurekli.electricalcalculator.feature.history.generated.resources.destination_history
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController
import com.kemalurekli.electricalcalculator.feature.converter.generated.resources.Res as ConverterRes
import com.kemalurekli.electricalcalculator.feature.fieldnotes.generated.resources.Res as FieldNotesRes
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.Res as NavigationRes
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.tab_calculators
import com.kemalurekli.electricalcalculator.feature.glossary.generated.resources.Res as GlossaryRes
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
        modules(
            coreCommonModule,
            coreDataModule,
            converterModule,
            historyModule,
            glossaryModule,
            fieldNotesModule,
            calculatorsModule,
        )
    }
    return ComposeUIViewController {
        ElecToolkitTheme {
            IosShell()
        }
    }
}

/** The screens ported so far. */
private enum class IosTab { Calculators, Converter, Glossary, FieldNotes, History }

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
    var selectedOrdinal by rememberSaveable { mutableStateOf(IosTab.Calculators.ordinal) }
    val selected = IosTab.entries[selectedOrdinal]

    // One level of stack, by hand. The calculators are a list that opens a
    // screen, and until `ElecNavHost` is multiplatform there is nothing here to
    // ask. Storing the key rather than the enum for the reason above.
    var openCalculatorKey by rememberSaveable { mutableStateOf<String?>(null) }

    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        layoutType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo()),
        navigationSuiteItems = {
            item(
                selected = selected == IosTab.Calculators,
                onClick = { selectedOrdinal = IosTab.Calculators.ordinal },
                icon = { Icon(ElecIcons.Calculators, contentDescription = null) },
                label = { Text(stringResource(NavigationRes.string.tab_calculators)) },
            )
            item(
                selected = selected == IosTab.Converter,
                onClick = { selectedOrdinal = IosTab.Converter.ordinal },
                icon = { Icon(ElecIcons.Converter, contentDescription = null) },
                label = { Text(stringResource(ConverterRes.string.tab_converter)) },
            )
            item(
                selected = selected == IosTab.Glossary,
                onClick = { selectedOrdinal = IosTab.Glossary.ordinal },
                icon = { Icon(ElecIcons.Glossary, contentDescription = null) },
                label = { Text(stringResource(GlossaryRes.string.dashboard_glossary_title)) },
            )
            item(
                selected = selected == IosTab.FieldNotes,
                onClick = { selectedOrdinal = IosTab.FieldNotes.ordinal },
                icon = { Icon(ElecIcons.FieldNotes, contentDescription = null) },
                label = { Text(stringResource(FieldNotesRes.string.destination_field_notes)) },
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
            IosTab.Calculators -> {
                val open = openCalculatorKey?.let(CalculatorId::fromKeyOrNull)
                if (open == null) {
                    CalculatorsRoute(onCalculatorClick = { openCalculatorKey = it.key })
                } else {
                    CalculatorDestination(
                        id = open,
                        recordId = null,
                        // The reference section is a tab of its own on Android
                        // and not here yet, so a "see the table" link has
                        // nowhere to go.
                        onReferenceClick = {},
                        onNavigateBack = { openCalculatorKey = null },
                    )
                }
            }

            IosTab.Converter -> ConverterRoute(onNavigateBack = null)
            // The glossary's outbound links go to screens that have not moved
            // yet, so they do nothing here rather than pretending to.
            IosTab.Glossary -> GlossaryRoute(
                openTermKey = null,
                onCalculatorClick = {},
                onReferenceClick = {},
                onNavigateBack = null,
            )
            IosTab.FieldNotes -> FieldNotesRoute(
                openNoteKey = null,
                onCalculatorClick = {},
                onReferenceClick = {},
                onGlossaryClick = {},
                onNavigateBack = null,
            )
            IosTab.History -> HistoryRoute(onOpenRecord = {}, onNavigateBack = null)
        }
    }
}
