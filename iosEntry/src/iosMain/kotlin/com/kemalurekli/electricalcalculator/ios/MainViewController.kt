package com.kemalurekli.electricalcalculator.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.kemalurekli.electricalcalculator.core.common.di.coreCommonModule
import com.kemalurekli.electricalcalculator.core.data.di.coreDataModule
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
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
import com.kemalurekli.electricalcalculator.features.theory.theoryModule
import com.kemalurekli.electricalcalculator.shell.ElecAppShell
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
    startKoin {
        modules(
            coreCommonModule,
            coreDataModule,
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
        )
    }
    return ComposeUIViewController {
        ElecToolkitTheme {
            // No `hasSettings`: that screen is the last one still in `:app`,
            // because it drives AppCompatDelegate for the per-app language and
            // reads BuildConfig. The shell leaves the gear off rather than
            // offering one that opens a route with no destination.
            //
            // The forum is here, and signs in with Apple. The two platforms use
            // different providers — App Store guideline 4.8 requires an
            // equivalent to any third-party sign-in, and Apple is both the
            // smaller build and the one that cannot be refused.
            ElecAppShell()
        }
    }
}
