package com.kemalurekli.electricalcalculator

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UserPreferences
import com.kemalurekli.electricalcalculator.core.designsystem.component.DisclaimerDialog
import com.kemalurekli.electricalcalculator.shell.ElecAppShell

/**
 * Extends [AppCompatActivity] rather than `ComponentActivity` solely for the
 * per-app language backport: below Android 13 the selected locale is applied in
 * AppCompat's `attachBaseContext`, which a plain `ComponentActivity` never runs.
 * Compose, Hilt and edge-to-edge are unaffected — `AppCompatActivity` is a
 * `ComponentActivity` subclass.
 */
class MainActivity : AppCompatActivity() {

    // Koin's, not the platform default: with Hilt gone there is no generated
    // factory, and the default one cannot build a constructor that takes
    // anything.
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Hold the splash only until the stored theme is known, so the first
        // composed frame already uses the correct colour scheme instead of
        // flashing light and snapping to dark.
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is MainUiState.Loading
        }

        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val preferences = when (val state = uiState) {
                MainUiState.Loading -> UserPreferences.Default
                is MainUiState.Ready -> state.preferences
            }
            val darkTheme = preferences.themeMode.resolveIsDark()

            ApplySystemBarStyle(darkTheme)

            ElecToolkitTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ElecAppShell(navController = rememberNavController())

                    // Shown over the app rather than before it: the reader can
                    // see what they are agreeing to use. Held until accepted,
                    // and only ever shown once — an acknowledgement that
                    // reappears every launch is one nobody reads.
                    if (uiState is MainUiState.Ready && !preferences.disclaimerAccepted) {
                        DisclaimerDialog(onAccept = viewModel::onAcceptDisclaimer)
                    }
                }
            }
        }
    }

    /**
     * Keeps the system bar icons legible as the theme changes.
     *
     * Edge-to-edge draws content behind transparent bars, so the icon tint has
     * to follow the app's own theme rather than the system's.
     */
    @Composable
    private fun ApplySystemBarStyle(darkTheme: Boolean) {
        SideEffect {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ) { darkTheme },
                navigationBarStyle = SystemBarStyle.auto(
                    lightScrim = LIGHT_NAV_BAR_SCRIM,
                    darkScrim = DARK_NAV_BAR_SCRIM,
                ) { darkTheme },
            )
        }
    }

    private companion object {
        /**
         * Scrims drawn behind three-button navigation on API levels that do not
         * enforce bar contrast themselves. Matches the AndroidX edge-to-edge
         * defaults.
         */
        const val LIGHT_NAV_BAR_SCRIM = 0xE6FFFFFF.toInt()
        const val DARK_NAV_BAR_SCRIM = 0x801B1B1B.toInt()
    }
}

/**
 * Resolves the stored preference into a concrete light/dark decision.
 *
 * Composable because [ThemeMode.SYSTEM] subscribes to the device setting, which
 * is what lets the app follow a system theme change without a restart.
 */
@Composable
private fun ThemeMode.resolveIsDark(): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}
