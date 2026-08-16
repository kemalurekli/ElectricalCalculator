package com.kemalurekli.electricalcalculator.features.theory.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSearchBar
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryLevel
import kotlinx.collections.immutable.persistentListOf

@Composable
fun TheoryRoute(
    onTopicClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TheoryListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TheoryScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onTopicClick = onTopicClick,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * The theory shelf.
 *
 * ### Two patterns, on purpose
 *
 * The *frame* is the general-info screen's: a pinned search bar, a horizontally
 * scrolling row of filter chips, and sections below. The *row* is the reference
 * screen's: a title, a line of description, and a tap that navigates.
 *
 * Both halves are deliberate. The chips are what let a reader who is past Ohm's
 * law jump to Advanced without scrolling through what they already know, and the
 * sections in [TheoryLevel] declaration order are what make scrolling down the
 * curriculum. But a topic opens a form the reader is going to work in, and a form
 * that expands inside a list would put a keyboard, a result and a derivation into
 * a row — so it gets a page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryScreen(
    uiState: TheoryListUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (TheoryLevel?) -> Unit,
    onTopicClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
                    title = stringResource(R.string.destination_theory),
                    onNavigateBack = onNavigateBack,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                ElecSearchBar(
                    query = uiState.query,
                    onQueryChange = onQueryChange,
                    placeholder = stringResource(R.string.th_search_hint),
                    modifier = Modifier.padding(
                        start = spacing.screenHorizontal,
                        end = spacing.screenHorizontal,
                        bottom = spacing.sm,
                    ),
                )

                LevelFilter(
                    levels = uiState.levels,
                    selected = uiState.filter,
                    onFilterChange = onFilterChange,
                )

                if (uiState.hasNoResults) {
                    ElecEmptyState(
                        title = stringResource(R.string.th_empty_title),
                        message = stringResource(R.string.th_empty_message),
                        icon = ElecIcons.Search,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.lg),
                    )
                    return@Column
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = spacing.screenHorizontal,
                        end = spacing.screenHorizontal,
                        top = spacing.sm,
                        bottom = spacing.xxl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    uiState.sections.forEach { section ->
                        item(key = "header-${section.level.name}") {
                            ElecSectionHeader(
                                title = stringResource(section.titleRes),
                                modifier = Modifier.padding(horizontal = 0.dp),
                            )
                        }
                        items(
                            items = section.topics,
                            key = { "topic-${it.key}" },
                        ) { topic ->
                            ElecListItem(
                                title = topic.title,
                                description = topic.summary,
                                icon = ElecIcons.Theory,
                                onClick = { onTopicClick(topic.key) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The level chips.
 *
 * Scrolls horizontally rather than wrapping, matching the general-info screen —
 * four chips fit on a phone today, and a row that reflows once a fifth is added
 * would push the first topic off the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelFilter(
    levels: List<TheoryLevel>,
    selected: TheoryLevel?,
    onFilterChange: (TheoryLevel?) -> Unit,
) {
    val spacing = ElecTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onFilterChange(null) },
            label = { Text(stringResource(R.string.th_filter_all)) },
        )
        levels.forEach { level ->
            FilterChip(
                selected = selected == level,
                onClick = { onFilterChange(level) },
                label = { Text(stringResource(level.titleRes())) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TheoryScreenPreview() {
    ElecToolkitTheme {
        TheoryScreen(
            uiState = TheoryListUiState(
                levels = persistentListOf(TheoryLevel.FOUNDATION),
                sections = persistentListOf(
                    TheorySection(
                        level = TheoryLevel.FOUNDATION,
                        titleRes = R.string.th_level_foundation,
                        topics = persistentListOf(
                            TheoryTopicRow(
                                key = "ohm_law",
                                level = TheoryLevel.FOUNDATION,
                                title = "Ohm's law",
                                summary = "Voltage, current and resistance",
                            ),
                        ),
                    ),
                ),
            ),
            onQueryChange = {},
            onFilterChange = {},
            onTopicClick = {},
            onNavigateBack = {},
        )
    }
}
