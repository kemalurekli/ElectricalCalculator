package com.kemalurekli.electricalcalculator.core.navigation

import org.jetbrains.compose.resources.StringResource
import androidx.compose.ui.graphics.vector.ImageVector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecAccent
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import kotlin.reflect.KClass
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_calculators_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_calculators_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_converter_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_converter_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_favorites_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_favorites_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_field_notes_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_field_notes_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_forum_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_forum_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_glossary_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_glossary_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_history_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_history_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_projects_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_projects_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_references_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_references_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_settings_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_settings_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_theory_subtitle
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.dashboard_theory_title
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_calculators
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_converter
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_favorites
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_field_notes
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_forum
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_glossary
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_history
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_home
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_more
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_projects
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_references
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_settings
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.destination_theory
import com.kemalurekli.electricalcalculator.core.navigation.generated.resources.tab_calculators

/**
 * The destinations reachable from the home dashboard.
 *
 * Declaring them once drives both the dashboard cards and the navigation bar on
 * larger layouts, so the two can never present a different set of sections.
 *
 * ### On the accents
 *
 * [ElecAccent] says which territory a section belongs to, and one distinction
 * is drawn consistently: the six shelves the app ships — tools, reference
 * material, theory and working knowledge — are primary, and the two built from
 * the user's own activity are tertiary. Assigned ad hoc it would mean nothing,
 * and alternating hues at random is what makes a set of sections look
 * assembled rather than designed.
 *
 * The dashboard no longer paints it. It orders by it instead — the app's own
 * shelves first, the reader's own last — which carries the same distinction
 * without putting a second hue on the screen.
 *
 * Three roles are in play, which is one more than the old palette could carry:
 * its `secondaryContainer` (#DAE2F9) and `primaryContainer` (#D8E2FF) were
 * within a couple of steps of each other, so the primary/secondary split
 * encoded a difference the eye could not resolve. The brand palette separates
 * them properly — `primaryContainer` is now a steel blue and `secondaryContainer`
 * a near-neutral slate — so [PROJECTS] and [FORUM], the two places where the
 * user's own work meets other people's, read as their own group rather than as
 * two more blue tiles.
 *
 * [SETTINGS] carries no accent role of its own because it is not a dashboard
 * card — it lives in the home top bar. It stays in this enum so that its route,
 * label and icon are still declared once alongside its peers.
 */
enum class TopLevelDestination(
    val route: Route,
    val label: StringResource,
    val title: StringResource,
    val subtitle: StringResource,
    val icon: ImageVector,
    val accent: ElecAccent,
) {
    PROJECTS(
        route = Route.Projects,
        label = Res.string.destination_projects,
        title = Res.string.dashboard_projects_title,
        subtitle = Res.string.dashboard_projects_subtitle,
        icon = ElecIcons.Projects,
        accent = ElecAccent.SECONDARY,
    ),
    FORUM(
        route = Route.Forum,
        label = Res.string.destination_forum,
        title = Res.string.dashboard_forum_title,
        subtitle = Res.string.dashboard_forum_subtitle,
        icon = ElecIcons.Forum,
        accent = ElecAccent.SECONDARY,
    ),
    CALCULATORS(
        route = Route.Calculators,
        label = Res.string.destination_calculators,
        title = Res.string.dashboard_calculators_title,
        subtitle = Res.string.dashboard_calculators_subtitle,
        icon = ElecIcons.Calculators,
        accent = ElecAccent.PRIMARY,
    ),
    CONVERTER(
        route = Route.Converter,
        label = Res.string.destination_converter,
        title = Res.string.dashboard_converter_title,
        subtitle = Res.string.dashboard_converter_subtitle,
        icon = ElecIcons.Converter,
        accent = ElecAccent.PRIMARY,
    ),
    REFERENCES(
        route = Route.References,
        label = Res.string.destination_references,
        title = Res.string.dashboard_references_title,
        subtitle = Res.string.dashboard_references_subtitle,
        icon = ElecIcons.References,
        accent = ElecAccent.PRIMARY,
    ),
    GLOSSARY(
        route = Route.Glossary(),
        label = Res.string.destination_glossary,
        title = Res.string.dashboard_glossary_title,
        subtitle = Res.string.dashboard_glossary_subtitle,
        icon = ElecIcons.Glossary,
        accent = ElecAccent.PRIMARY,
    ),
    FAVORITES(
        route = Route.Favorites,
        label = Res.string.destination_favorites,
        title = Res.string.dashboard_favorites_title,
        subtitle = Res.string.dashboard_favorites_subtitle,
        icon = ElecIcons.FavoriteOff,
        accent = ElecAccent.TERTIARY,
    ),
    HISTORY(
        route = Route.History,
        label = Res.string.destination_history,
        title = Res.string.dashboard_history_title,
        subtitle = Res.string.dashboard_history_subtitle,
        icon = ElecIcons.History,
        accent = ElecAccent.TERTIARY,
    ),
    THEORY(
        route = Route.Theory,
        label = Res.string.destination_theory,
        title = Res.string.dashboard_theory_title,
        subtitle = Res.string.dashboard_theory_subtitle,
        icon = ElecIcons.Theory,
        accent = ElecAccent.PRIMARY,
    ),
    FIELD_NOTES(
        route = Route.FieldNotes(),
        label = Res.string.destination_field_notes,
        title = Res.string.dashboard_field_notes_title,
        subtitle = Res.string.dashboard_field_notes_subtitle,
        icon = ElecIcons.FieldNotes,
        accent = ElecAccent.PRIMARY,
    ),
    SETTINGS(
        route = Route.Settings,
        label = Res.string.destination_settings,
        title = Res.string.dashboard_settings_title,
        subtitle = Res.string.dashboard_settings_subtitle,
        icon = ElecIcons.Settings,
        accent = ElecAccent.NEUTRAL,
    ),
    ;

    companion object {
        /**
         * The destinations that appear as cards on the dashboard.
         *
         * Everything except the three that are now a tab in their own right —
         * a card that duplicates the tab directly beneath it is a second door
         * into the same room — and except [SETTINGS], which is reached from the
         * home top bar and from More: configuration is not something a user
         * browses to alongside the tools.
         *
         * The count moves as sections are added, and a card stranded alone on a
         * final row is what an earlier version of this dashboard looked like
         * and was rebuilt to avoid. The dashboard solves it by giving the
         * *first* card the full width whenever the count is odd, which is a
         * rule rather than a special case: these are declared in order of how
         * central they are, so the one that gets the extra room is the one that
         * has earned it.
         */
        val dashboardCards: List<TopLevelDestination> =
            entries - SETTINGS - tabDestinations.toSet()

        /**
         * Everything reachable from the More tab.
         *
         * The complement of the tab bar, in the same declared order, with
         * [SETTINGS] last because it is the one entry that configures the app
         * rather than doing work in it.
         */
        val moreDestinations: List<TopLevelDestination> = dashboardCards + SETTINGS
    }
}

/**
 * The tabs along the bottom of the app.
 *
 * Four sections plus a way to reach the rest. Until now the app's ten shelves
 * were reachable only by returning to the dashboard and tapping a card, which
 * left it with no persistent sense of place: nothing on screen said where you
 * were or what else there was. On iOS, where this same code will run, an app
 * with sections and no tab bar reads as unfinished.
 *
 * Which four is a question about what people open repeatedly, not about what
 * the app contains. [TopLevelDestination.PROJECTS] is the job in progress,
 * [TopLevelDestination.CALCULATORS] is the work, and
 * [TopLevelDestination.FORUM] is the one part that changes while you are not
 * looking. Everything else — reference tables, the glossary, theory, saved
 * work — is looked up rather than lived in, and belongs behind [MORE].
 *
 * Five is also the ceiling both platforms set: a sixth tab does not fit a
 * phone's width at an accessible label size.
 */
enum class ElecTab(
    val destination: TopLevelDestination?,
    val label: StringResource,
    val icon: ImageVector,
) {
    HOME(null, Res.string.destination_home, ElecIcons.Home),
    CALCULATORS(
        TopLevelDestination.CALCULATORS,
        // Not `destination_calculators`. A tab is about 72dp wide and the
        // Turkish "Hesaplayıcılar" wraps to two lines in it, which throws the
        // whole bar out of alignment. The screen it opens keeps the full name.
        Res.string.tab_calculators,
        ElecIcons.Calculators,
    ),
    PROJECTS(TopLevelDestination.PROJECTS, Res.string.destination_projects, ElecIcons.Projects),
    FORUM(TopLevelDestination.FORUM, Res.string.destination_forum, ElecIcons.Forum),
    MORE(null, Res.string.destination_more, ElecIcons.MoreTab),
    ;

    /** Where selecting this tab navigates. */
    val route: Route
        get() = when (this) {
            HOME -> Route.Home
            MORE -> Route.More
            else -> requireNotNull(destination).route
        }

    /**
     * Every destination this tab owns.
     *
     * Listed rather than derived, because "which tab is this screen in" has no
     * answer the navigation graph can give: the graph is flat, so a thread and
     * the forum it belongs to are siblings as far as it knows. The bar reads
     * this to decide what to highlight, which is why a detail screen has to
     * name its tab — otherwise opening a thread would appear to leave the forum.
     */
    val routes: List<KClass<out Route>>
        get() = when (this) {
            HOME -> listOf(Route.Home::class)

            CALCULATORS -> listOf(Route.Calculators::class, Route.Calculator::class)

            PROJECTS -> listOf(
                Route.Projects::class,
                Route.Project::class,
                Route.Circuit::class,
            )

            FORUM -> listOf(
                Route.Forum::class,
                Route.ForumCategory::class,
                Route.ForumThread::class,
                Route.ForumComposeThread::class,
                Route.ForumProfile::class,
            )

            // Everything with no tab of its own. Settings is here as well as in
            // the home top bar: two doors into a room nobody visits often is
            // better than one nobody can find.
            MORE -> listOf(
                Route.More::class,
                Route.Converter::class,
                Route.References::class,
                Route.Reference::class,
                Route.Glossary::class,
                Route.Theory::class,
                Route.TheoryTopic::class,
                Route.FieldNotes::class,
                Route.Favorites::class,
                Route.History::class,
                Route.Settings::class,
                Route.ForumAccount::class,
            )
        }
}

/** The tab destinations, for the dashboard to exclude. */
private val tabDestinations: List<TopLevelDestination>
    get() = listOf(
        TopLevelDestination.CALCULATORS,
        TopLevelDestination.PROJECTS,
        TopLevelDestination.FORUM,
    )
