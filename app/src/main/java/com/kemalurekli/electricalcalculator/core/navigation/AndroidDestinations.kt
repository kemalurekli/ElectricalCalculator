package com.kemalurekli.electricalcalculator.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kemalurekli.electricalcalculator.features.settings.presentation.SettingsRoute
import com.kemalurekli.electricalcalculator.shell.NavActions

/**
 * The one destination that has not left `:app`.
 *
 * `:shell` holds the graph and every other screen in it. Settings is passed in
 * because it drives `AppCompatDelegate` for the per-app language and reads
 * `BuildConfig.VERSION_NAME` — neither of which means anything off Android.
 *
 * The forum was here until Sign in with Apple gave iOS a way to sign in. It is
 * in `:shell` now.
 */
fun NavGraphBuilder.androidDestinations(actions: NavActions) {

    composable<Route.Settings> {
        SettingsRoute(
            onNavigateBack = actions::navigateBack,
            onOpenForumProfile = { actions.navigateTo(Route.ForumProfile(it)) },
        )
    }
}
