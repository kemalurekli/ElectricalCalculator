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
        accent = ElecAccent.TERTIARY,
    ),
    REFERENCES(
        route = Route.References,
        labelRes = R.string.destination_references,
        titleRes = R.string.dashboard_references_title,
        subtitleRes = R.string.dashboard_references_subtitle,
        icon = ElecIcons.References,
        accent = ElecAccent.SECONDARY,
    ),
    GLOSSARY(
        route = Route.Glossary(),
        labelRes = R.string.destination_glossary,
        titleRes = R.string.dashboard_glossary_title,
        subtitleRes = R.string.dashboard_glossary_subtitle,
        icon = ElecIcons.Glossary,
        accent = ElecAccent.TERTIARY,
    ),
    FAVORITES(
        route = Route.Favorites,
        labelRes = R.string.destination_favorites,
        titleRes = R.string.dashboard_favorites_title,
        subtitleRes = R.string.dashboard_favorites_subtitle,
        icon = ElecIcons.FavoriteOff,
        accent = ElecAccent.PRIMARY,
    ),
    HISTORY(
        route = Route.History,
        labelRes = R.string.destination_history,
        titleRes = R.string.dashboard_history_title,
        subtitleRes = R.string.dashboard_history_subtitle,
        icon = ElecIcons.History,
        accent = ElecAccent.SECONDARY,
    ),
    SETTINGS(
        route = Route.Settings,
        labelRes = R.string.destination_settings,
        titleRes = R.string.dashboard_settings_title,
        subtitleRes = R.string.dashboard_settings_subtitle,
        icon = ElecIcons.Settings,
        accent = ElecAccent.NEUTRAL,
    ),
}
