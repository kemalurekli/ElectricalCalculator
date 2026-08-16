package com.kemalurekli.electricalcalculator.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecAccent
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons

/**
 * The destinations reachable from the home dashboard.
 *
 * Declaring them once drives both the dashboard cards and the navigation bar on
 * larger layouts, so the two can never present a different set of sections.
 *
 * ### On the accents
 *
 * [ElecAccent] is documented as tinting a section by what it is *for*, so the
 * dashboard draws one distinction and draws it consistently: the six shelves
 * the app ships — tools, reference material, theory and working knowledge — are
 * primary, and the two built from the user's own activity are tertiary. Read
 * down the grid, the colour now means something. Assigned ad hoc it means
 * nothing, and alternating hues at random is what makes a set of cards look
 * assembled rather than designed.
 *
 * Only two of the four roles are in play deliberately. In this palette
 * `secondaryContainer` (#DAE2F9) and `primaryContainer` (#D8E2FF) are within a
 * couple of steps of each other, so a primary/secondary split encodes a
 * difference the eye cannot resolve — it reads as four identical blue cards
 * while the code claims three groups.
 *
 * [SETTINGS] carries no accent role of its own because it is not a dashboard
 * card — it lives in the home top bar. It stays in this enum so that its route,
 * label and icon are still declared once alongside its peers.
 */
enum class TopLevelDestination(
    val route: Route,
    val labelRes: Int,
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val accent: ElecAccent,
) {
    CALCULATORS(
        route = Route.Calculators,
        labelRes = R.string.destination_calculators,
        titleRes = R.string.dashboard_calculators_title,
        subtitleRes = R.string.dashboard_calculators_subtitle,
        icon = ElecIcons.Calculators,
        accent = ElecAccent.PRIMARY,
    ),
    CONVERTER(
        route = Route.Converter,
        labelRes = R.string.destination_converter,
        titleRes = R.string.dashboard_converter_title,
        subtitleRes = R.string.dashboard_converter_subtitle,
        icon = ElecIcons.Converter,
        accent = ElecAccent.PRIMARY,
    ),
    REFERENCES(
        route = Route.References,
        labelRes = R.string.destination_references,
        titleRes = R.string.dashboard_references_title,
        subtitleRes = R.string.dashboard_references_subtitle,
        icon = ElecIcons.References,
        accent = ElecAccent.PRIMARY,
    ),
    GLOSSARY(
        route = Route.Glossary(),
        labelRes = R.string.destination_glossary,
        titleRes = R.string.dashboard_glossary_title,
        subtitleRes = R.string.dashboard_glossary_subtitle,
        icon = ElecIcons.Glossary,
        accent = ElecAccent.PRIMARY,
    ),
    FAVORITES(
        route = Route.Favorites,
        labelRes = R.string.destination_favorites,
        titleRes = R.string.dashboard_favorites_title,
        subtitleRes = R.string.dashboard_favorites_subtitle,
        icon = ElecIcons.FavoriteOff,
        accent = ElecAccent.TERTIARY,
    ),
    HISTORY(
        route = Route.History,
        labelRes = R.string.destination_history,
        titleRes = R.string.dashboard_history_title,
        subtitleRes = R.string.dashboard_history_subtitle,
        icon = ElecIcons.History,
        accent = ElecAccent.TERTIARY,
    ),
    THEORY(
        route = Route.Theory,
        labelRes = R.string.destination_theory,
        titleRes = R.string.dashboard_theory_title,
        subtitleRes = R.string.dashboard_theory_subtitle,
        icon = ElecIcons.Theory,
        accent = ElecAccent.PRIMARY,
    ),
    FIELD_NOTES(
        route = Route.FieldNotes(),
        labelRes = R.string.destination_field_notes,
        titleRes = R.string.dashboard_field_notes_title,
        subtitleRes = R.string.dashboard_field_notes_subtitle,
        icon = ElecIcons.FieldNotes,
        accent = ElecAccent.PRIMARY,
    ),
    SETTINGS(
        route = Route.Settings,
        labelRes = R.string.destination_settings,
        titleRes = R.string.dashboard_settings_title,
        subtitleRes = R.string.dashboard_settings_subtitle,
        icon = ElecIcons.Settings,
        accent = ElecAccent.NEUTRAL,
    ),
    ;

    companion object {
        /**
         * The destinations that appear as cards on the dashboard.
         *
         * Everything except [SETTINGS], which is reached from the home top bar:
         * configuration is not something a user browses to alongside the tools.
         *
         * That leaves eight, which fills four rows of two exactly. Earlier there
         * were seven and the last one was stranded alone on a final row, so it
         * was drawn as a full-width band underneath instead; [THEORY] made the
         * count even again and the band is gone. Adding a ninth destination
         * brings the problem back — solve it then, rather than keeping a special
         * case against the possibility.
         */
        val dashboardCards: List<TopLevelDestination> = entries - SETTINGS
    }
}
