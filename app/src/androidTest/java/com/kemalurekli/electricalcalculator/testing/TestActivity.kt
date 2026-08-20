package com.kemalurekli.electricalcalculator.testing

import androidx.activity.ComponentActivity

/**
 * A bare activity for `createAndroidComposeRule`.
 *
 * `MainActivity` would work and would drag the whole navigation graph in with
 * it. This is the smallest thing that can host a composable, which is all a
 * screen test wants. It was `HiltTestActivity` and needed `@AndroidEntryPoint`;
 * with Koin there is nothing to annotate.
 */
class TestActivity : ComponentActivity()
