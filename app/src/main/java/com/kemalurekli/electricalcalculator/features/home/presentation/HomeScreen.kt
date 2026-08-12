package com.kemalurekli.electricalcalculator.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecDashboardCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecRecentRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSearchBar
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorCatalog
import com.kemalurekli.electricalcalculator.core.domain.search.SearchKind
import com.kemalurekli.electricalcalculator.core.domain.search.SearchableItem
import com.kemalurekli.electricalcalculator.core.navigation.Route
import com.kemalurekli.electricalcalculator.core.navigation.TopLevelDestination
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.CalculatorUiModel
import kotlinx.collections.immutable.persistentListOf

@Composable
fun HomeRoute(
    onNavigate: (Route) -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onOpenSearchHit: (SearchableItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onToggleFavorite = viewModel::onToggleFavorite,
        onNavigate = onNavigate,
        onCalculatorClick = onCalculatorClick,
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onToggleFavorite: (CalculatorId) -> Unit,
    onNavigate: (Route) -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onOpenSearchHit: (SearchableItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // On a wide window the whole screen — title included — is capped and
    // centred rather than stretched edge to edge. Full-bleed rows on a large
    // tablet strand the trailing control an uncomfortable reach from the label
    // it belongs to. Capping the Scaffold rather than just its content is what
    // keeps the heading aligned with the cards beneath it.
    //
    // `widthIn` must precede `fillMaxSize`: constraints flow outside in, so
    // filling first would pin the minimum width to the full window and leave
    // nothing for the cap to reduce.
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier
                .widthIn(max = layout.contentMaxWidth)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                ElecTopAppBar(
                    title = stringResource(R.string.app_name),
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = { onNavigate(TopLevelDestination.SETTINGS.route) }) {
                            Icon(
                                imageVector = TopLevelDestination.SETTINGS.icon,
                                contentDescription = stringResource(
                                    TopLevelDestination.SETTINGS.titleRes,
                                ),
                            )
                        }
                    },
                )
            },
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
                                title = stringResource(R.string.destination_favorites),
                                items = uiState.favorites,
                            ) { item ->
                                CalculatorRow(item, onCalculatorClick, onToggleFavorite)
                            }
                        }
                    }

                    if (uiState.hasRecent) {
                        fullWidthItem(key = "recent") {
                            SectionGroup(
                                title = stringResource(R.string.home_recent),
                                items = uiState.recent,
                            ) { record ->
                                ElecRecentRow(
                                    title = record.title,
                                    summary = record.summary,
                                    timestamp = record.createdAt.toRelativeTime(),
                                    icon = ElecIcons.History,
                                    onClick = { onCalculatorClick(record.calculatorId) },
                                )
                            }
                        }
                    }

                    fullWidthItem(key = "browse-header") {
                        ElecSectionHeader(
                            title = stringResource(R.string.home_browse),
                            modifier = Modifier.padding(horizontal = 0.dp),
                        )
                    }
                    fullWidthItem(key = "dashboard") {
                        DashboardGrid(
                            columns = layout.dashboardColumns,
                            favoriteCount = uiState.favorites.size,
                            savedCount = uiState.savedCount,
                            onNavigate = onNavigate,
                        )
                    }
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
 * The dashboard cards, laid out in rows of [columns].
 *
 * Deliberately *not* a lazy grid. `LazyVerticalGrid` measures every cell
 * independently, so a card cannot stretch to match its neighbour and rows come
 * out ragged the moment one translation runs longer than another — which is
 * exactly what happens once the app is localised.
 *
 * `IntrinsicSize.Min` on the row makes it as tall as its tallest card, and
 * `fillMaxHeight` then squares up the rest. That costs a second measure pass,
 * which is irrelevant for a fixed set of six cards but would not be for a long
 * list.
 *
 * Equal heights *within* a row were never the whole problem, though: rows still
 * differed from each other, so a three-line subtitle in the first row left the
 * grid visibly stepped. [CARD_MIN_HEIGHT] sets a floor every card clears, which
 * collapses that variation to at most the one line a long translation adds,
 * without capping the text and truncating the languages that need the room.
 */
@Composable
private fun DashboardGrid(
    columns: Int,
    favoriteCount: Int,
    savedCount: Int,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        TopLevelDestination.dashboardCards.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                row.forEach { destination ->
                    ElecDashboardCard(
                        title = stringResource(destination.titleRes),
                        subtitle = stringResource(destination.subtitleRes),
                        icon = destination.icon,
                        accent = destination.accent,
                        // Only the two sections that accumulate content carry a
                        // count; a badge reading "0" on an empty section is
                        // noise rather than information.
                        badge = when (destination) {
                            TopLevelDestination.FAVORITES -> favoriteCount
                                .takeIf { it > 0 }
                                ?.let { stringResource(R.string.home_badge_pinned, it) }

                            TopLevelDestination.HISTORY -> savedCount
                                .takeIf { it > 0 }
                                ?.let { stringResource(R.string.home_badge_saved, it) }

                            else -> null
                        },
                        onClick = { onNavigate(destination.route) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .heightIn(min = CARD_MIN_HEIGHT),
                    )
                }
                // Keeps a short final row aligned with the columns above it
                // instead of stretching its cards across the full width.
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Floor for a dashboard card, chosen to fit an icon, a two-line title and a
 * two-line subtitle at the default font scale — the shape most of the cards
 * take — so the shorter ones stop reading as unfinished next to the longer.
 */
private val CARD_MIN_HEIGHT = 150.dp

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
                title = stringResource(R.string.state_empty_calculators_title),
                message = stringResource(R.string.state_empty_calculators_message),
                icon = ElecIcons.Search,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        return
    }

    uiState.searchResults.forEach { section ->
        fullWidthItem(key = "search-header-${section.kind.name}") {
            ElecSectionHeader(title = stringResource(section.kind.titleRes()))
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
private fun SearchKind.titleRes(): Int = when (this) {
    SearchKind.CALCULATOR -> R.string.search_group_calculators
    SearchKind.CONVERTER -> R.string.search_group_converter
    SearchKind.REFERENCE -> R.string.search_group_references
    SearchKind.GLOSSARY -> R.string.search_group_glossary
    SearchKind.SYMBOL -> R.string.search_group_symbols
}

private fun SearchKind.icon() = when (this) {
    SearchKind.CALCULATOR -> ElecIcons.Calculators
    SearchKind.CONVERTER -> ElecIcons.Converter
    SearchKind.REFERENCE -> ElecIcons.References
    SearchKind.GLOSSARY -> ElecIcons.Glossary
    SearchKind.SYMBOL -> ElecIcons.References
}

/**
 * Formats a timestamp as "2 minutes ago".
 *
 * Uses the platform formatter so the phrasing follows the device language
 * without the app shipping its own plural rules for every locale.
 */
@Composable
private fun java.time.Instant.toRelativeTime(): String {
    val elapsed = System.currentTimeMillis() - toEpochMilli()
    // The platform formatter renders anything under a minute as "0 minutes
    // ago", which reads as broken for a calculation just run.
    if (elapsed < android.text.format.DateUtils.MINUTE_IN_MILLIS) {
        return stringResource(R.string.home_just_now)
    }
    return android.text.format.DateUtils.getRelativeTimeSpanString(
        toEpochMilli(),
        System.currentTimeMillis(),
        android.text.format.DateUtils.MINUTE_IN_MILLIS,
    ).toString()
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
            onOpenSearchHit = {},
        )
    }
}
