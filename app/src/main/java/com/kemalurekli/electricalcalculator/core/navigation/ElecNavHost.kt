package com.kemalurekli.electricalcalculator.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteKind
import com.kemalurekli.electricalcalculator.core.domain.search.SearchKind
import com.kemalurekli.electricalcalculator.core.domain.search.SearchableItem
import com.kemalurekli.electricalcalculator.features.calculators.energycost.presentation.EnergyCostRoute
import com.kemalurekli.electricalcalculator.features.calculators.lighting.presentation.LightingRoute
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.presentation.NeutralCurrentRoute
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorDetailRoute
import com.kemalurekli.electricalcalculator.features.calculators.battery.presentation.BatteryRoute
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation.CableSizeRoute
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation.CableWeightRoute
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation.ConduitFillRoute
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation.EarthFaultRoute
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation.SolarStringRoute
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation.TrayFillRoute
import com.kemalurekli.electricalcalculator.features.calculators.presentation.CalculatorsRoute
import com.kemalurekli.electricalcalculator.features.calculators.motor.presentation.MotorRoute
import com.kemalurekli.electricalcalculator.features.calculators.power.presentation.PowerRoute
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation.PowerFactorRoute
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation.ShortCircuitRoute
import com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation.TransformerRoute
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropRoute
import com.kemalurekli.electricalcalculator.features.converter.presentation.ConverterRoute
import com.kemalurekli.electricalcalculator.features.favorites.presentation.FavoritesRoute
import com.kemalurekli.electricalcalculator.features.projects.presentation.CircuitRoute
import com.kemalurekli.electricalcalculator.features.projects.presentation.ProjectRoute
import com.kemalurekli.electricalcalculator.features.projects.presentation.ProjectsRoute
import com.kemalurekli.electricalcalculator.features.fieldnotes.presentation.FieldNotesRoute
import com.kemalurekli.electricalcalculator.features.theory.presentation.TheoryRoute
import com.kemalurekli.electricalcalculator.features.theory.presentation.TheoryTopicRoute
import com.kemalurekli.electricalcalculator.features.glossary.presentation.GlossaryRoute
import com.kemalurekli.electricalcalculator.features.history.presentation.HistoryRoute
import com.kemalurekli.electricalcalculator.features.home.presentation.HomeRoute
import com.kemalurekli.electricalcalculator.features.references.presentation.ReferenceDetailRoute
import com.kemalurekli.electricalcalculator.features.references.presentation.ReferencesRoute
import com.kemalurekli.electricalcalculator.features.settings.presentation.SettingsRoute

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
    ) {
        composable<Route.Home> {
            HomeRoute(
                onNavigate = actions::navigateTo,
                onCalculatorClick = actions::navigateToCalculator,
                onOpenSearchHit = actions::navigateToSearchHit,
            )
        }

        composable<Route.Calculators> {
            CalculatorsRoute(
                onCalculatorClick = actions::navigateToCalculator,
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.Calculator> { backStackEntry ->
            // Each calculator gets its own screen as its phase lands. Until
            // then the shared detail screen renders the catalog entry with an
            // in-development notice, so the destination is never a dead end.
            val route = backStackEntry.toRoute<Route.Calculator>()
            val calculatorId = CalculatorId.fromKeyOrNull(route.calculatorKey)
            val recordId = route.recordId
            when (calculatorId) {
                CalculatorId.VOLTAGE_DROP ->
                    VoltageDropRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.CABLE_SIZE ->
                    CableSizeRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.TRANSFORMER_CURRENT ->
                    TransformerRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.MOTOR_CURRENT ->
                    MotorRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.POWER ->
                    PowerRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.POWER_FACTOR_CORRECTION ->
                    PowerFactorRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.BATTERY_RUNTIME ->
                    BatteryRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.CABLE_WEIGHT ->
                    CableWeightRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.CONDUIT_FILL ->
                    ConduitFillRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.CABLE_TRAY_FILL ->
                    TrayFillRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.SHORT_CIRCUIT ->
                    ShortCircuitRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.EARTH_FAULT_LOOP ->
                    EarthFaultRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.LIGHTING_LUMEN ->
                    LightingRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.SOLAR_STRING ->
                    SolarStringRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.NEUTRAL_CURRENT ->
                    NeutralCurrentRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                CalculatorId.ENERGY_COST ->
                    EnergyCostRoute(
                        onReferenceClick = actions::navigateToReference,
                        onNavigateBack = actions::navigateBack,
                        recordId = recordId,
                    )

                else -> CalculatorDetailRoute(onNavigateBack = actions::navigateBack)
            }
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
            ProjectsRoute(
                onOpenProject = { actions.navigateTo(Route.Project(it)) },
                onNavigateBack = actions::navigateBack,
            )
        }

        composable<Route.Project> { backStackEntry ->
            ProjectRoute(
                projectId = backStackEntry.toRoute<Route.Project>().projectId,
                onOpenCircuit = { projectId, circuitId ->
                    actions.navigateTo(Route.Circuit(projectId, circuitId))
                },
                onNavigateBack = actions::navigateBack,
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

        composable<Route.Settings> {
            SettingsRoute(onNavigateBack = actions::navigateBack)
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
