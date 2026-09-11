package com.kemalurekli.electricalcalculator.features.theory.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListDivider
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSearchBar
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.features.theory.domain.TheoryLevel
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_has_calculator
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_pro_badge
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.destination_theory
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_empty_message
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_empty_title
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_level_foundation
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_search_hint

@Composable
fun TheoryRoute(
    onTopicClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: TheoryListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TheoryScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
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
 * The *frame* is a pinned search bar and sections below. The *row* is the
 * reference screen's: a title, a line of description, and a tap that navigates.
 *
 * There was a row of filter chips — All, Foundation, Intermediate, Advanced —
 * above the list, and it is gone. It named the three things the list already
 * says in its own headings, one screen apart, and a control that repeats a
 * label is a control the reader has to work out is a control. The headings do
 * the job and are set heavier for it.
 *
 * A topic opens a form the reader is going to work in, and a form that expands
 * inside a list would put a keyboard, a result and a derivation into a row — so
 * it gets a page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryScreen(
    uiState: TheoryListUiState,
    onQueryChange: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.destination_theory),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ElecSearchBar(
                query = uiState.query,
                onQueryChange = onQueryChange,
                placeholder = stringResource(Res.string.th_search_hint),
                modifier = Modifier.padding(
                    start = spacing.screenHorizontal,
                    end = spacing.screenHorizontal,
                    bottom = spacing.sm,
                ),
            )

            if (uiState.hasNoResults) {
                ElecEmptyState(
                    title = stringResource(Res.string.th_empty_title),
                    message = stringResource(Res.string.th_empty_message),
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
                            title = stringResource(section.title),
                            modifier = Modifier.padding(horizontal = 0.dp),
                        )
                    }
                    itemsIndexed(
                        items = section.topics,
                        key = { _, topic -> "topic-${topic.key}" },
                    ) { index, topic ->
                        ElecListItem(
                            title = topic.title,
                            description = topic.summary,
                            // No leading glyph. Every topic on this shelf is a
                            // topic, so a sigma on each of twenty rows was an
                            // indent with an opinion. The level a reader might
                            // have wanted from it is the heading above.
                            // "Pro" outranks "has a calculator" on a row the
                            // reader cannot open yet: one sets an expectation
                            // before the tap, the other describes something
                            // behind a door that is shut. Once Pro is owned
                            // the calculator badge comes back.
                            badge = if (uiState.isLocked(topic.level)) {
                                stringResource(Res.string.th_pro_badge)
                            } else {
                                stringResource(Res.string.th_has_calculator)
                                    .takeIf { topic.hasCalculator }
                            },
                            onClick = { onTopicClick(topic.key) },
                        )
                        if (index < section.topics.lastIndex) ElecListDivider()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TheoryScreenPreview() {
    ElecToolkitTheme {
        TheoryScreen(
            uiState = TheoryListUiState(
                sections = persistentListOf(
                    TheorySection(
                        level = TheoryLevel.FOUNDATION,
                        title = Res.string.th_level_foundation,
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
            onTopicClick = {},
            onNavigateBack = {},
        )
    }
}
