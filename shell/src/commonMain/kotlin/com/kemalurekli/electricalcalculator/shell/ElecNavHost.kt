package com.kemalurekli.electricalcalculator.shell

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecMotion
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteKind
import com.kemalurekli.electricalcalculator.core.navigation.ElecTab
import com.kemalurekli.electricalcalculator.core.navigation.PaywallReason
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.core.navigation.TopLevelDestination
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorDestination
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorDetailRoute
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorsRoute
import com.kemalurekli.electricalcalculator.features.converter.presentation.ConverterRoute
import com.kemalurekli.electricalcalculator.features.favorites.presentation.FavoritesRoute
import com.kemalurekli.electricalcalculator.features.fieldnotes.presentation.FieldNotesRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumAccountRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumCategoriesRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumComposeThreadRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumProfileRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadRoute
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadsRoute
import com.kemalurekli.electricalcalculator.features.glossary.presentation.GlossaryRoute
import com.kemalurekli.electricalcalculator.features.history.presentation.HistoryRoute
import com.kemalurekli.electricalcalculator.features.home.domain.SearchKind
import com.kemalurekli.electricalcalculator.features.home.domain.SearchableItem
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeRoute
import com.kemalurekli.electricalcalculator.features.more.presentation.MoreRoute
import com.kemalurekli.electricalcalculator.features.projects.presentation.CircuitRoute
import com.kemalurekli.electricalcalculator.features.pro.presentation.PaywallRoute
import com.kemalurekli.electricalcalculator.features.projects.presentation.ProjectRoute
import com.kemalurekli.electricalcalculator.features.projects.presentation.ProjectsRoute
import com.kemalurekli.electricalcalculator.features.references.presentation.ReferenceDetailRoute
import com.kemalurekli.electricalcalculator.features.references.presentation.ReferencesRoute
import com.kemalurekli.electricalcalculator.features.settings.presentation.LanguageRoute
import com.kemalurekli.electricalcalculator.features.settings.presentation.SettingsRoute
import com.kemalurekli.electricalcalculator.features.theory.presentation.TheoryRoute
import com.kemalurekli.electricalcalculator.features.theory.presentation.TheoryTopicRoute

/**
 * The application's navigation graph.
 *
 * Destinations are declared with the type-safe `composable<Route>` overload, so
 * each screen's arguments are derived from its `@Serializable` route class and
 * checked at compile time.
 *
 * Screens receive plain lambdas rather than the [NavHostController] itself.
 * That keeps every feature independent of Navigation, so a screen can be
 * previewed, tested and later extracted into its own Gradle module untouched.
 */
@Composable
fun ElecNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    // Remembered so the lambdas passed to screens keep a stable identity and do
    // not invalidate every destination on each recomposition of the host.
    val actions = remember(navController) { NavActions(navController) }

    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier,
        // Spelled out rather than left to the library. The defaults are not the
        // same on both platforms, and "the same app on both" has to include how
        // it moves — a reader switching between an iPhone and an Android tablet
        // should not have to relearn what a transition means.
        enterTransition = { forward(isTabSwitch()) },
        exitTransition = { forwardOut(isTabSwitch()) },
        popEnterTransition = { back(isTabSwitch()) },
        popExitTransition = { backOut(isTabSwitch()) },
    ) {
        composable<Route.Home> {
            HomeRoute(
                onNavigate = actions::navigateTo,
                onCalculatorClick = actions::navigateToCalculator,
                // The same call the History screen makes. The dashboard's
                // recent rows used to drop the record id here and open a blank
                // form instead of the calculation they were showing.
                onOpenRecord = { record ->
                    actions.navigateToCalculator(record.calculatorId, record.id)
                },
                onOpenSearchHit = actions::navigateToSearchHit,
            )
        }

        // The four tab roots pass no back handler. A back arrow on a tab root
        // has nowhere honest to point: the reader did not arrive from
        // somewhere, they switched tabs, and the bar below is what takes them
        // back. ElecTopAppBar draws no affordance when the handler is null.
        composable<Route.Calculators> {
            CalculatorsRoute(onCalculatorClick = actions::navigateToCalculator)
        }

        composable<Route.More> {
            MoreRoute(onNavigate = actions::navigateTo)
        }

        composable<Route.Forum> {
            ForumCategoriesRoute(
                onCategoryClick = { actions.navigateTo(Route.ForumCategory(it.id, it.title, it.key, it.threadCount)) },
            )
        }

        composable<Route.ForumCategory> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.ForumCategory>()
            ForumThreadsRoute(
                categoryId = route.categoryId,
                categoryTitle = route.title,
                threadCount = route.threadCount,
                onThreadClick = { actions.navigateTo(Route.ForumThread(it.id, it.title, it.isLocked, it.authorId, route.title, it.replyCount)) },
                onNewThread = {
                    actions.navigateTo(
                        Route.ForumComposeThread(route.categoryId, it, route.title, route.key),
                    )
                },
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
                replyCount = route.replyCount,
                onOpenProfile = { actions.navigateTo(Route.ForumProfile(it)) },
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.Settings> {
            SettingsRoute(
                onNavigateBack = actions::navigateBack,
                onOpenForumAccount = { actions.navigateTo(Route.ForumAccount) },
                onOpenLanguage = { actions.navigateTo(Route.Language) },
                // The one entrance to the paywall that is not a refusal. Every
                // other one is reached by walking into something locked.
                onOpenPaywall = { actions.navigateTo(Route.Paywall()) },
            )
        }

        composable<Route.Language> {
            LanguageRoute(onNavigateBack = actions::navigateBack)
        }

        composable<Route.ForumAccount> {
            ForumAccountRoute(
                onNavigateBack = actions::navigateBack,
                onOpenProfile = { actions.navigateTo(Route.ForumProfile(it)) },
            )
        }

        composable<Route.Calculator> { backStackEntry ->
            // Each calculator gets its own screen as its phase lands. Until
            // then the shared detail screen renders the catalog entry with an
            // in-development notice, so the destination is never a dead end.
            val route = backStackEntry.toRoute<Route.Calculator>()
            val calculatorId = CalculatorId.fromKeyOrNull(route.calculatorKey)
            val recordId = route.recordId
            CalculatorDestination(
                id = calculatorId,
                recordId = recordId,
                onReferenceClick = actions::navigateToReference,
                onNavigateBack = actions::navigateBack,
                onShowPaywall = { actions.navigateTo(Route.Paywall(PaywallReason.CALCULATION_EXPORT)) },
            )
        }

        composable<Route.Converter> {
            ConverterRoute(onNavigateBack = actions::navigateBack)
        }

        composable<Route.FieldNotes> { backStackEntry ->
            FieldNotesRoute(
                openNoteKey = backStackEntry.toRoute<Route.FieldNotes>().noteKey,
                onCalculatorClick = actions::navigateToCalculator,
                onReferenceClick = actions::navigateToReference,
                onGlossaryClick = { actions.navigateTo(Route.Glossary(it)) },
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.Theory> {
            TheoryRoute(
                onTopicClick = actions::navigateToTheoryTopic,
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.TheoryTopic> { backStackEntry ->
            TheoryTopicRoute(
                topicKey = backStackEntry.toRoute<Route.TheoryTopic>().topicKey,
                onCalculatorClick = actions::navigateToCalculator,
                onReferenceClick = actions::navigateToReference,
                onGlossaryClick = { actions.navigateTo(Route.Glossary(it)) },
                onShowPaywall = { actions.navigateTo(Route.Paywall(PaywallReason.THEORY)) },
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.References> {
            ReferencesRoute(
                onTopicClick = actions::navigateToReference,
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.Glossary> { backStackEntry ->
            GlossaryRoute(
                openTermKey = backStackEntry.toRoute<Route.Glossary>().termKey,
                onCalculatorClick = actions::navigateToCalculator,
                onReferenceClick = actions::navigateToReference,
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.Reference> { backStackEntry ->
            ReferenceDetailRoute(
                topicKey = backStackEntry.toRoute<Route.Reference>().topicKey,
                onNavigateBack = actions::navigateBack,
            )
        }






        composable<Route.Projects> {
            ProjectsRoute(onOpenProject = { actions.navigateTo(Route.Project(it)) })
        }

        composable<Route.Project> { backStackEntry ->
            ProjectRoute(
                projectId = backStackEntry.toRoute<Route.Project>().projectId,
                onOpenCircuit = { projectId, circuitId ->
                    actions.navigateTo(Route.Circuit(projectId, circuitId))
                },
                onNavigateBack = actions::navigateBack,
                onShowPaywall = { actions.navigateTo(Route.Paywall(it)) },
            )
        }

        composable<Route.Paywall> { backStackEntry ->
            PaywallRoute(
                reason = backStackEntry.toRoute<Route.Paywall>().reason,
                // One way out for both endings. Buying and backing out leave
                // the reader where they were, on the screen they were trying
                // to use — which after a purchase now does what they wanted.
                onDone = actions::navigateBack,
            )
        }

        composable<Route.Circuit> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Circuit>()
            CircuitRoute(
                projectId = route.projectId,
                circuitId = route.circuitId,
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.Favorites> {
            FavoritesRoute(
                onOpenFavorite = actions::navigateToFavorite,
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.History> {
            HistoryRoute(
                onOpenRecord = { record ->
                    actions.navigateToCalculator(record.calculatorId, record.id)
                },
                onNavigateBack = actions::navigateBack,
            )
        }

    }
}

/**
 * Navigation commands, kept in one place so back-stack policy is defined once
 * rather than repeated at each call site.
 */
class NavActions(private val navController: NavHostController) {

    fun navigateTo(route: Route) {
        // `launchSingleTop` prevents a second copy of a destination when a card
        // is double-tapped before the transition finishes.
        navController.navigate(route) { launchSingleTop = true }
    }

    /**
     * Switches to a tab, keeping where the reader had got to inside it.
     *
     * Three flags, each answering a question the reader would otherwise ask.
     * `popUpTo(start) { saveState }` means the back stack does not accumulate
     * one entry per tab press — after tapping around the bar for a minute, one
     * back gesture still leaves the app rather than replaying the tour.
     * `restoreState` puts the tab back where it was, so returning to a
     * half-written forum thread finds it half-written. `launchSingleTop` keeps
     * a second tap on the current tab from stacking a copy of it.
     */
    fun navigateToTab(tab: ElecTab) {
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    /**
     * Back to where a tab begins, for a second press on the tab already showing.
     *
     * `popBackStack` rather than `navigate`: the tab's root is already in the
     * stack — that is what made this tab the active one — so the work is
     * removing what is on top of it, not putting another copy underneath.
     * Pressing it at the root pops nothing and correctly does nothing.
     */
    fun resetTab(tab: ElecTab) {
        navController.popBackStack(tab.route, inclusive = false)
    }

    /**
     * Goes to [route] and drops the screen that asked for it.
     *
     * For a form whose job is finished once it succeeds: backing out of the
     * thread somebody just opened should land in the category it now appears
     * in, not in the empty form that created it.
     */
    fun navigateReplacing(route: Route) {
        navController.navigate(route) {
            launchSingleTop = true
            navController.currentBackStackEntry?.destination?.route?.let { current ->
                popUpTo(current) { inclusive = true }
            }
        }
    }

    fun navigateToCalculator(id: CalculatorId, recordId: Long? = null) {
        navigateTo(Route.Calculator.of(id, recordId))
    }

    fun navigateToReference(topicKey: String) {
        navigateTo(Route.Reference(topicKey))
    }

    fun navigateToTheoryTopic(topicKey: String) {
        navigateTo(Route.TheoryTopic(topicKey))
    }

    /**
     * Opens whatever a favourite points at.
     *
     * The `when` is exhaustive, so a shelf that becomes pinnable without a way
     * to open it is a build error rather than a row that does nothing.
     */
    fun navigateToFavorite(item: FavoriteItem) {
        when (item.kind) {
            FavoriteKind.CALCULATOR -> CalculatorId.fromKeyOrNull(item.key)
                ?.let(::navigateToCalculator)

            FavoriteKind.REFERENCE -> navigateToReference(item.key)
            FavoriteKind.GLOSSARY -> navigateTo(Route.Glossary(item.key))
            FavoriteKind.FIELD_NOTE -> navigateTo(Route.FieldNotes(item.key))
            FavoriteKind.THEORY -> navigateToTheoryTopic(item.key)
        }
    }

    /**
     * Opens whatever a search hit points at.
     *
     * A symbol has no screen of its own — its key is the topic it lives on, so
     * it lands beside its neighbours, which is where a reader comparing two
     * symbols needs to be.
     */
    fun navigateToSearchHit(hit: SearchableItem) {
        when (hit.kind) {
            SearchKind.CALCULATOR -> CalculatorId.fromKeyOrNull(hit.key)
                ?.let(::navigateToCalculator)

            SearchKind.CONVERTER -> navigateTo(Route.Converter)
            SearchKind.THEORY -> navigateToTheoryTopic(hit.key)
            SearchKind.REFERENCE, SearchKind.SYMBOL -> navigateToReference(hit.key)
            SearchKind.GLOSSARY -> navigateTo(Route.Glossary(hit.key))
            SearchKind.FIELD_NOTE -> navigateTo(Route.FieldNotes(hit.key))
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}

/**
 * Whether this transition is between two tabs rather than into a screen.
 *
 * A tab switch is a lateral move — nothing is on top of anything — so it fades.
 * Opening a calculator or a thread is a push, and slides, because the back
 * gesture has to feel like undoing a direction.
 *
 * The test is whether both ends are tab *roots*, not whether they belong to
 * different tabs. Settings belongs to the More tab but is also reachable from
 * the home top bar, and that is a push however far across the bar it lands.
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    initialState.destination.isTabRoot() && targetState.destination.isTabRoot()

private fun NavDestination.isTabRoot(): Boolean =
    ElecTab.entries.any { tab -> hasRoute(tab.route::class) }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.forward(tabSwitch: Boolean) =
    if (tabSwitch) {
        fadeIn(tween(TAB_MILLIS))
    } else {
        slideIntoContainer(SlideDirection.Start, tween(PUSH_MILLIS)) + fadeIn(tween(PUSH_MILLIS))
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.forwardOut(tabSwitch: Boolean) =
    if (tabSwitch) {
        fadeOut(tween(TAB_MILLIS))
    } else {
        slideOutOfContainer(SlideDirection.Start, tween(PUSH_MILLIS)) + fadeOut(tween(PUSH_MILLIS))
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.back(tabSwitch: Boolean) =
    if (tabSwitch) {
        fadeIn(tween(TAB_MILLIS))
    } else {
        slideIntoContainer(SlideDirection.End, tween(PUSH_MILLIS)) + fadeIn(tween(PUSH_MILLIS))
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.backOut(tabSwitch: Boolean) =
    if (tabSwitch) {
        fadeOut(tween(TAB_MILLIS))
    } else {
        slideOutOfContainer(SlideDirection.End, tween(PUSH_MILLIS)) + fadeOut(tween(PUSH_MILLIS))
    }

private const val TAB_MILLIS = ElecMotion.TAB_MILLIS
private const val PUSH_MILLIS = ElecMotion.PUSH_MILLIS
