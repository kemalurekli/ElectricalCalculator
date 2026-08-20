package com.kemalurekli.electricalcalculator.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.features.converter.converterModule
import com.kemalurekli.electricalcalculator.features.converter.presentation.ConverterRoute
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

/**
 * The single function Xcode's Swift shell calls.
 *
 * Everything above this line is shared Kotlin; everything below it is UIKit.
 * Keeping the boundary to one function is deliberate — the more of the app that
 * lives in Swift, the more there is to write twice.
 *
 * `ProofScreen` used to be here: five lines showing that shared Kotlin could
 * run on iOS at all. It has done its job and been replaced by the real
 * converter, which is the same code Android renders.
 *
 * Koin is started here rather than in Swift because this is where the object
 * graph is known. On Android the equivalent call is in the Application class.
 */
fun MainViewController(): UIViewController {
    startKoin {
        modules(converterModule)
    }
    return ComposeUIViewController {
        ElecToolkitTheme {
            // Nowhere to go back to yet — this screen is the whole app on iOS
            // until navigation is ported.
            ConverterRoute(onNavigateBack = {})
        }
    }
}
