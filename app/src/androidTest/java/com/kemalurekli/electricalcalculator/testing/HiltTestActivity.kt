package com.kemalurekli.electricalcalculator.testing

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Empty host activity for Compose tests that need the Hilt graph.
 *
 * `hiltViewModel()` resolves its ViewModel from the host activity, so the
 * activity must be a Hilt entry point. `MainActivity` cannot be used because it
 * installs its own navigation graph, which would put the screen under test
 * behind a real navigation flow.
 *
 * Declared in `src/debug/AndroidManifest.xml`, which merges into the test APK.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
