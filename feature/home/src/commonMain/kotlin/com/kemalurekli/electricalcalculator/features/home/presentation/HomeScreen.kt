package com.kemalurekli.electricalcalculator.features.home.presentation

import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecAccent
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.asRelativeTime
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecRecentRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSearchBar
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorCatalog
import com.kemalurekli.electricalcalculator.features.home.domain.SearchKind
import com.kemalurekli.electricalcalculator.features.home.domain.SearchableItem
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.core.navigation.TopLevelDestination
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.app_name
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.destination_favorites
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.home_badge_pinned
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.home_badge_saved
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.home_browse
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.home_recent
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.search_group_calculators
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.search_group_converter
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.search_group_field_notes
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.search_group_glossary
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.search_group_references
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.search_group_symbols
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.search_group_theory
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.state_empty_calculators_message
import com.kemalurekli.electricalcalculator.feature.home.generated.resources.state_empty_calculators_title

@Composable
fun HomeRoute(
    onNavigate: (Route) -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onOpenRecord: (CalculationRecord) -> Unit,
    onOpenSearchHit: (SearchableItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onToggleFavorite = viewModel::onToggleFavorite,
        onNavigate = onNavigate,
        onCalculatorClick = onCalculatorClick,
        onOpenRecord = onOpenRecord,
        onOpenSearchHit = onOpenSearchHit,
        modifier = modifier,
    )
}

/**
 * The dashboard.
 *
 * Everything lives in a single [LazyVerticalGrid] rather than a Column of
 * separate lists. Full-width rows (search field, section headings, favourites)
 * span every column, while dashboard cards occupy one cell each. One scrolling
 * container means one recycling pass and no nested scrolling, which is what
 * keeps the screen smooth as sections are added.
 *
 * ### Why the sections are grouped rather than loose
 *
 * Pinned and recent rows are drawn inside one [ElecCard] per section instead of
 * sitting directly on the page. Loose rows above a field of cards put two
 * different visual languages on one screen, and the top of the dashboard —
 * where the eye lands first — got the weaker one. Each group is a single grid
 * item holding its rows, which is affordable precisely because both sections
 * are bounded: favourites are what the user chose to pin, and recents are
 * capped at [HomeViewModel.RECENT_LIMIT].
 *
 * ### Why the top bar is compact
 *
 * A collapsing large bar spends about a fifth of the first screen restating the
 * app's own name, which the user just tapped to get here. The dashboard's job
 * is to put tools within reach, so the space goes to the tools.
 *
 * ### What the grid stopped being
 *
 * It used to hold every section the app had, because it was the only way to
 * reach any of them. The tab bar now carries the three that are opened
 * repeatedly and the More tab carries the rest, so a card here for Calculators
 * would be a second door into the room the reader is already standing next to.
 * What is left is the material that is genuinely looked up rather than lived
 * in — reference tables, the glossary, theory, field notes — plus the two
 * shelves built from the reader's own activity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onToggleFavorite: (CalculatorId) -> Unit,
    onNavigate: (Route) -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onOpenRecord: (CalculationRecord) -> Unit,
    onOpenSearchHit: (SearchableItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = rememberElecScrollBehavior()

    // The width cap on a wide window, the insets and the title bar all live in
    // ElecScreenScaffold now; this screen used to argue for them here, back
    // when it was one of the two that had got them right.
    ElecScreenScaffold(
        title = stringResource(Res.string.app_name),
        modifier = modifier,
        actions = {
            IconButton(onClick = { onNavigate(TopLevelDestination.SETTINGS.route) }) {
                Icon(
                    imageVector = TopLevelDestination.SETTINGS.icon,
                    contentDescription = stringResource(
                        TopLevelDestination.SETTINGS.title,
                    ),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(layout.dashboardColumns),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                bottom = spacing.xxl,
            ),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            fullWidthItem(key = "search") {
                ElecSearchBar(
                    query = uiState.query,
                    onQueryChange = onQueryChange,
                    modifier = Modifier.padding(bottom = spacing.xs),
                )
            }

            if (uiState.isSearching) {
                searchResults(uiState, onOpenSearchHit)
            } else {
                if (uiState.hasFavorites) {
                    fullWidthItem(key = "favorites") {
                        SectionGroup(
                            title = stringResource(Res.string.destination_favorites),
                            items = uiState.favorites,
                        ) { item ->
                            CalculatorRow(item, onCalculatorClick, onToggleFavorite)
                        }
                    }
                }

                if (uiState.hasRecent) {
                    fullWidthItem(key = "recent") {
                        SectionGroup(
                            title = stringResource(Res.string.home_recent),
                            items = uiState.recent,
                        ) { entry ->
                            val record = entry.record
                            ElecRecentRow(
                                title = record.title,
                                summary = record.summary,
                                timestamp = record.createdAt.asRelativeTime(),
                                // The calculator's own glyph, not a clock.
                                // Every row here is a past run, so a clock on
                                // each one is an indent rather than an icon —
                                // and which calculator it was is the one thing
                                // that tells two rows apart at a glance.
                                icon = ElecIcons.forCalculator(entry.icon),
                                // The whole record, not just which calculator
                                // it was. `onCalculatorClick` cannot carry the
                                // row's id, and opening a past calculation on
                                // an empty form is the one thing this row is
                                // not for.
                                onClick = { onOpenRecord(record) },
                            )
                        }
                    }
                }

                fullWidthItem(key = "index") {
                    IndexList(
                        favoriteCount = uiState.favorites.size,
                        savedCount = uiState.savedCount,
                        onNavigate = onNavigate,
                    )
                }
            }
        }
    }
}

/**
 * A titled group of rows, drawn as one card.
 *
 * The heading sits outside the card and the rows inside it, so the card's edge
 * encloses exactly the thing the heading names. Rows are separated by a hairline
 * rather than by gaps: inside a shared container the divider is what says "these
 * are peers in one list", where whitespace alone would just look like the rows
 * had drifted apart.
 */
@Composable
private fun <T> SectionGroup(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        ElecSectionHeader(
            title = title,
            modifier = Modifier.padding(horizontal = 0.dp),
        )
        ElecCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = spacing.lg)) {
                items.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    itemContent(item)
                }
            }
        }
    }
}

/**
 * The app's other shelves, as an index.
 *
 * ### Why this stopped being a grid of cards
 *
 * It was six cards at 150dp each — an icon in a tinted square, a title and a
 * two-line subtitle — taking about 470dp, half the dashboard, below two
 * sections that were already lists inside cards. Two problems with that.
 *
 * One: it is the More tab. `moreDestinations` is literally `dashboardCards +
 * SETTINGS`, so half the home screen was the contents of another tab drawn as
 * posters. A poster is a claim that something is the main event; these are
 * shelves you look something up on.
 *
 * Two: the screen spoke two languages at once — titled lists in cards at the
 * top, a poster wall underneath — and the half that got the visual weight was
 * the half that was not the reader's own work.
 *
 * As rows it costs about 290dp, the dashboard has one object language, and
 * what the reader has actually been doing keeps the top of the screen. The
 * destinations are still one tap away; they are simply no longer shouting.
 *
 * The subtitles go with the cards. In a list of eight words — Referanslar,
 * Sözlük, Teori — a sentence under each is describing what the noun already
 * says. This is the one place in the app where that is true; every other list
 * explains its rows, and still does.
 *
 * ### Where the accent went
 *
 * The cards were tinted by territory: the shelves the app ships in one colour,
 * the two built from the reader's own activity in another. That distinction is
 * worth keeping and a hue is not the only way to say it — in a list, order
 * says it, and says it without adding a second colour to a screen that has
 * enough. The app's own material comes first and the reader's own comes last,
 * which is also the order the More tab groups them in.
 */
@Composable
private fun IndexList(
    favoriteCount: Int,
    savedCount: Int,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionGroup(
        title = stringResource(Res.string.home_browse),
        items = TopLevelDestination.dashboardCards
            .sortedBy { it.accent == ElecAccent.TERTIARY },
        modifier = modifier,
    ) { destination ->
        ElecListItem(
            title = stringResource(destination.title),
            onClick = { onNavigate(destination.route) },
            icon = destination.icon,
            // Only the two sections that accumulate content carry a count; a
            // badge reading "0" on an empty section is noise, not information.
            badge = when (destination) {
                TopLevelDestination.FAVORITES -> favoriteCount
                    .takeIf { it > 0 }
                    ?.let { stringResource(Res.string.home_badge_pinned, it) }

                TopLevelDestination.HISTORY -> savedCount
                    .takeIf { it > 0 }
                    ?.let { stringResource(Res.string.home_badge_saved, it) }

                else -> null
            },
            contentPadding = PaddingValues(vertical = 12.dp),
        )
    }
}


/**
 * Search results, grouped by which shelf they came from.
 *
 * The heading is what makes a mixed list usable: "Voltage Drop" as a calculator
 * and "Voltage drop" as a glossary term are different answers to the same query,
 * and a flat list would leave the reader guessing which one they were about to
 * open.
 */
private fun LazyGridScope.searchResults(
    uiState: HomeUiState,
    onOpenSearchHit: (SearchableItem) -> Unit,
) {
    if (uiState.hasNoResults) {
        fullWidthItem(key = "no-results") {
            ElecEmptyState(
                title = stringResource(Res.string.state_empty_calculators_title),
                message = stringResource(Res.string.state_empty_calculators_message),
                icon = ElecIcons.Search,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        return
    }

    uiState.searchResults.forEach { section ->
        fullWidthItem(key = "search-header-${section.kind.name}") {
            ElecSectionHeader(title = stringResource(section.kind.title()))
        }
        items(
            items = section.hits,
            key = { "hit-${section.kind.name}-${it.key}-${it.title}" },
            span = { GridItemSpan(maxLineSpan) },
        ) { hit ->
            ElecListItem(
                title = hit.title,
                description = hit.subtitle,
                icon = section.kind.icon(),
                onClick = { onOpenSearchHit(hit) },
            )
        }
    }
}

/** The heading a group of hits renders under. */
private fun SearchKind.title(): StringResource = when (this) {
    SearchKind.CALCULATOR -> Res.string.search_group_calculators
    SearchKind.CONVERTER -> Res.string.search_group_converter
    SearchKind.THEORY -> Res.string.search_group_theory
    SearchKind.REFERENCE -> Res.string.search_group_references
    SearchKind.GLOSSARY -> Res.string.search_group_glossary
    SearchKind.SYMBOL -> Res.string.search_group_symbols
    SearchKind.FIELD_NOTE -> Res.string.search_group_field_notes
}

private fun SearchKind.icon() = when (this) {
    SearchKind.CALCULATOR -> ElecIcons.Calculators
    SearchKind.CONVERTER -> ElecIcons.Converter
    SearchKind.THEORY -> ElecIcons.Theory
    SearchKind.REFERENCE -> ElecIcons.References
    SearchKind.GLOSSARY -> ElecIcons.Glossary
    SearchKind.SYMBOL -> ElecIcons.References
    SearchKind.FIELD_NOTE -> ElecIcons.FieldNotes
}

/** A row that spans the full grid width regardless of the column count. */
private fun LazyGridScope.fullWidthItem(
    key: String,
    content: @Composable () -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }

@Composable
private fun CalculatorRow(
    item: CalculatorUiModel,
    onCalculatorClick: (CalculatorId) -> Unit,
    onToggleFavorite: (CalculatorId) -> Unit,
) {
    ElecListItem(
        title = item.title,
        description = item.description,
        icon = ElecIcons.forCalculator(item.icon),
        onClick = { onCalculatorClick(item.id) },
        isFavorite = item.isFavorite,
        onToggleFavorite = { onToggleFavorite(item.id) },
        // The grid already applies the screen inset, so the row supplies none.
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
    )
}

@Preview(name = "Home", showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    ElecToolkitTheme {
        HomeScreen(
            uiState = HomeUiState(
                favorites = persistentListOf(
                    CalculatorUiModel(
                        id = CalculatorId.VOLTAGE_DROP,
                        title = "Voltage Drop",
                        description = "Volt drop and percentage over a cable run",
                        icon = CalculatorIcon.VOLTAGE_DROP,
                        category = CalculatorCategory.CABLE_AND_CONDUIT,
                        isFavorite = true,
                    ),
                ),
                isLoading = false,
            ),
            onQueryChange = {},
            onToggleFavorite = {},
            onNavigate = {},
            onCalculatorClick = {},
            onOpenRecord = {},
            onOpenSearchHit = {},
        )
    }
}

@Preview(name = "Home – tablet", showBackground = true, widthDp = 900, heightDp = 800)
@Composable
private fun HomeScreenTabletPreview() {
    ElecToolkitTheme {
        HomeScreen(
            uiState = HomeUiState(isLoading = false),
            onQueryChange = {},
            onToggleFavorite = {},
            onNavigate = {},
            onCalculatorClick = {},
            onOpenRecord = {},
            onOpenSearchHit = {},
        )
    }
}

