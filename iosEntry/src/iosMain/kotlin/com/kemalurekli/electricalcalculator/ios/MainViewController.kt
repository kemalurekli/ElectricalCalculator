package com.kemalurekli.electricalcalculator.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.kemalurekli.electricalcalculator.core.common.di.coreCommonModule
import com.kemalurekli.electricalcalculator.core.data.di.coreDataModule
import com.kemalurekli.electricalcalculator.features.pro.proModule
import com.kemalurekli.electricalcalculator.core.backend.backendModule
import com.kemalurekli.electricalcalculator.core.feedback.feedbackModule
import com.kemalurekli.electricalcalculator.core.billing.di.billingModule
import com.kemalurekli.electricalcalculator.features.calculators.calculatorsModule
import com.kemalurekli.electricalcalculator.features.converter.converterModule
import com.kemalurekli.electricalcalculator.features.favorites.favoritesModule
import com.kemalurekli.electricalcalculator.features.fieldnotes.fieldNotesModule
import com.kemalurekli.electricalcalculator.features.glossary.glossaryModule
import com.kemalurekli.electricalcalculator.features.history.historyModule
import com.kemalurekli.electricalcalculator.features.home.homeModule
import com.kemalurekli.electricalcalculator.features.forum.forumModule
import com.kemalurekli.electricalcalculator.features.projects.projectsModule
import com.kemalurekli.electricalcalculator.features.references.referencesModule
import com.kemalurekli.electricalcalculator.features.settings.settingsModule
import com.kemalurekli.electricalcalculator.features.theory.theoryModule
import com.kemalurekli.electricalcalculator.shell.ElecToolkitApp
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

/**
 * The single function Xcode's Swift shell calls.
 *
 * Everything above this line is shared Kotlin; everything below it is UIKit.
 * Keeping the boundary to one function is deliberate — the more of the app that
 * lives in Swift, the more there is to write twice.
 *
 * There used to be a hand-built tab bar here, listing whichever screens had
 * been ported. It was a stand-in for the navigation graph and it did what
 * stand-ins do: it fell behind. The calculators built for iOS for four commits
 * before anything on this side opened them, because a module compiling for a
 * target is not the same as being reachable on it, and only the shell says
 * which. It is gone. `ElecAppShell` is the same code Android runs.
 *
 * Koin is started here rather than in Swift because this is where the object
 * graph is known. On Android the equivalent call is in the Application class.
 */
fun MainViewController(): UIViewController {
    startGraphOnce()
    return ComposeUIViewController {
        // The whole app, and the same composable Android sets as its content:
        // the theme the reader chose, the disclaimer over it, the shell under.
        ElecToolkitApp()
    }
}

/**
 * Builds the object graph the first time, and only the first time.
 *
 * `MainViewController()` is called from SwiftUI's `makeUIViewController`, and
 * SwiftUI is free to call that more than once — a scene reconnecting, the view's
 * identity changing, a state restoration. `startKoin` is not idempotent: the
 * second call throws `KoinAppAlreadyStartedException`, which crosses back into
 * Swift as a fatal error, or takes the composition down and leaves a live
 * process showing nothing.
 *
 * Android has no equivalent problem — `Application.onCreate` runs once per
 * process by construction — which is exactly why this was easy to miss.
 */
private var graphStarted = false

private fun startGraphOnce() {
    if (graphStarted) return
    graphStarted = true
    startKoin {
        modules(
            coreCommonModule,
            coreDataModule,
            billingModule,
            backendModule,
            feedbackModule,
            converterModule,
            historyModule,
            glossaryModule,
            fieldNotesModule,
            referencesModule,
            calculatorsModule,
            theoryModule,
            favoritesModule,
            homeModule,
            projectsModule,
            forumModule,
            proModule,
            settingsModule,
        )
    }
}
