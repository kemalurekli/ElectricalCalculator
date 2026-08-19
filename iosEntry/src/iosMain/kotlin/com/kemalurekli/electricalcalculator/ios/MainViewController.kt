package com.kemalurekli.electricalcalculator.ios

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * The single function Xcode's Swift shell calls.
 *
 * Everything above this line is shared Kotlin; everything below it is UIKit.
 * Keeping the boundary to one function is deliberate — the more of the app that
 * lives in Swift, the more there is to write twice.
 */
fun MainViewController(): UIViewController = ComposeUIViewController { ProofScreen() }
