package com.kemalurekli.electricalcalculator.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumCategoriesRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumComposeThreadRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumProfileRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadsRoute
import com.kemalurekli.electricalcalculator.features.settings.presentation.SettingsRoute
import com.kemalurekli.electricalcalculator.shell.NavActions

/**
 * The destinations that have not left `:app`.
 *
 * `:shell` holds the graph and every other screen in it. These two are passed
 * in because they are the last Android-only features: the forum holds the only
 * platform-specific authentication — Credential Manager has no iOS counterpart,
 * and App Store guideline 4.8 wants Sign in with Apple beside it — and the
 * settings screen renders two of the forum's sections.
 *
 * iOS passes nothing, and leaves the Forum tab out of its bar rather than
 * offering one that leads nowhere.
 */
fun NavGraphBuilder.androidDestinations(actions: NavActions) {
    composable<Route.Forum> {
        ForumCategoriesRoute(
            onCategoryClick = { actions.navigateTo(Route.ForumCategory(it.id, it.title)) },
        )
    }

    composable<Route.ForumCategory> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.ForumCategory>()
        ForumThreadsRoute(
            categoryId = route.categoryId,
            categoryTitle = route.title,
            onThreadClick = { actions.navigateTo(Route.ForumThread(it.id, it.title, it.isLocked, it.authorId, route.title)) },
            onNewThread = { actions.navigateTo(Route.ForumComposeThread(route.categoryId, it)) },
            onNavigateBack = actions::navigateBack,
        )
    }

    composable<Route.ForumComposeThread> {
        ForumComposeThreadRoute(
            onThreadCreated = { id, title ->
                // Replaces the compose screen rather than stacking on it:
                // backing out of the new thread should land in the category
                // it now appears in, not in the form that created it.
                actions.navigateReplacing(Route.ForumThread(id, title))
            },
            onNavigateBack = actions::navigateBack,
        )
    }

    composable<Route.ForumProfile> {
        ForumProfileRoute(onNavigateBack = actions::navigateBack)
    }

    composable<Route.ForumThread> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.ForumThread>()
        ForumThreadRoute(
            threadId = route.threadId,
            threadTitle = route.title,
            isLocked = route.isLocked,
            threadAuthorId = route.authorId,
            categoryTitle = route.categoryTitle,
            onOpenProfile = { actions.navigateTo(Route.ForumProfile(it)) },
            onNavigateBack = actions::navigateBack,
        )
    }

    composable<Route.Settings> {
        SettingsRoute(
            onNavigateBack = actions::navigateBack,
            onOpenForumProfile = { actions.navigateTo(Route.ForumProfile(it)) },
        )
    }
}
