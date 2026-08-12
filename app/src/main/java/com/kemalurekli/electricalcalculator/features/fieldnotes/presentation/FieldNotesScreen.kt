package com.kemalurekli.electricalcalculator.features.fieldnotes.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSearchBar
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.features.fieldnotes.domain.FieldNoteCategory
import kotlinx.collections.immutable.persistentListOf

@Composable
fun FieldNotesRoute(
    openNoteKey: String?,
    onCalculatorClick: (CalculatorId) -> Unit,
    onReferenceClick: (String) -> Unit,
    onGlossaryClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FieldNotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Keyed on the argument rather than run once: navigating from one search hit
    // to another without leaving the screen changes the key and nothing else.
    LaunchedEffect(openNoteKey) {
        openNoteKey?.let(viewModel::onOpenNote)
    }

    // Scrolling is a separate effect from opening, because the list it has to
    // scroll only exists after the state rebuild the open triggers.
    LaunchedEffect(uiState.expandedKey, uiState.sections) {
        val key = uiState.expandedKey ?: return@LaunchedEffect
        val index = uiState.sections
            .flatMap { section -> listOf(null) + section.notes.map { it.key } }
            .indexOf(key)
        if (index >= 0) listState.animateScrollToItem(index)
    }

    FieldNotesScreen(
        uiState = uiState,
        listState = listState,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onToggleExpanded = viewModel::onToggleExpanded,
        onCalculatorClick = onCalculatorClick,
        onReferenceClick = onReferenceClick,
        onGlossaryClick = onGlossaryClick,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * The field notes.
 *
 * A flat list of cards under category headings, filtered by a chip row and a
 * query. Each card shows its claim closed and its reasoning open, which is what
 * makes the list skimmable: the titles are written as sentences, so a reader can
 * run down them and stop only where something surprises them.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FieldNotesScreen(
    uiState: FieldNotesUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (FieldNoteCategory?) -> Unit,
    onToggleExpanded: (String) -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onReferenceClick: (String) -> Unit,
    onGlossaryClick: (String) -> Unit,
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
                    title = stringResource(R.string.destination_field_notes),
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
                    placeholder = stringResource(R.string.fn_search_hint),
                    modifier = Modifier.padding(
                        start = spacing.screenHorizontal,
                        end = spacing.screenHorizontal,
                        bottom = spacing.sm,
                    ),
                )

                CategoryFilter(
                    categories = uiState.categories,
                    selected = uiState.filter,
                    onFilterChange = onFilterChange,
                )

                if (uiState.hasNoResults) {
                    ElecEmptyState(
                        title = stringResource(R.string.fn_empty_title),
                        message = stringResource(R.string.fn_empty_message),
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
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    uiState.sections.forEach { section ->
                        item(key = "header-${section.category.name}") {
                            ElecSectionHeader(
                                title = stringResource(section.titleRes),
                                modifier = Modifier.padding(horizontal = 0.dp),
                            )
                        }
                        items(
                            items = section.notes,
                            key = { "note-${it.key}" },
                        ) { note ->
                            NoteCard(
                                note = note,
                                isExpanded = note.key == uiState.expandedKey,
                                onToggle = { onToggleExpanded(note.key) },
                                onCalculatorClick = onCalculatorClick,
                                onReferenceClick = onReferenceClick,
                                onGlossaryClick = onGlossaryClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The category chips.
 *
 * Scrolls horizontally rather than wrapping: a wrapping row of eight chips takes
 * three lines on a phone and pushes the first note off the screen, which costs
 * more than the chips are worth.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilter(
    categories: List<FieldNoteCategory>,
    selected: FieldNoteCategory?,
    onFilterChange: (FieldNoteCategory?) -> Unit,
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
            label = { Text(stringResource(R.string.fn_filter_all)) },
        )
        categories.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onFilterChange(category) },
                label = { Text(stringResource(category.titleRes())) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteCard(
    note: FieldNoteUiModel,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onReferenceClick: (String) -> Unit,
    onGlossaryClick: (String) -> Unit,
) {
    val spacing = ElecTheme.spacing

    ElecCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            // The claim, stated as a sentence. Closed, this is the whole card —
            // a reader who already knows it can move on without opening it.
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    HorizontalDivider()

                    Text(
                        text = note.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (note.terms.isNotEmpty() ||
                        note.calculator != null ||
                        note.referenceTopic != null
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            // Acting on the note comes before reading around it.
                            note.calculator?.let { id ->
                                AssistChip(
                                    onClick = { onCalculatorClick(id) },
                                    label = { Text(stringResource(R.string.fn_related_calculator)) },
                                )
                            }
                            note.referenceTopic?.let { topicKey ->
                                AssistChip(
                                    onClick = { onReferenceClick(topicKey) },
                                    label = { Text(stringResource(R.string.fn_related_reference)) },
                                )
                            }
                            note.terms.forEach { link ->
                                AssistChip(
                                    onClick = { onGlossaryClick(link.key) },
                                    label = { Text(link.name) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun FieldNotesScreenPreview() {
    ElecToolkitTheme {
        FieldNotesScreen(
            uiState = FieldNotesUiState(
                categories = persistentListOf(FieldNoteCategory.SAFETY_AND_PRACTICE),
                sections = persistentListOf(
                    FieldNoteSection(
                        category = FieldNoteCategory.SAFETY_AND_PRACTICE,
                        titleRes = R.string.fn_cat_safety_and_practice,
                        notes = persistentListOf(
                            FieldNoteUiModel(
                                key = "neutral_is_a_live_conductor",
                                category = FieldNoteCategory.SAFETY_AND_PRACTICE,
                                title = "A neutral is a live conductor, not a safe one",
                                body = "It sits near earth potential only while the circuit is " +
                                    "intact and reasonably balanced.",
                                terms = persistentListOf(
                                    FieldNoteLink("neutral_conductor", "Neutral conductor"),
                                ),
                                calculator = CalculatorId.NEUTRAL_CURRENT,
                                referenceTopic = null,
                            ),
                        ),
                    ),
                ),
                expandedKey = "neutral_is_a_live_conductor",
            ),
            onQueryChange = {},
            onFilterChange = {},
            onToggleExpanded = {},
            onCalculatorClick = {},
            onReferenceClick = {},
            onGlossaryClick = {},
            onNavigateBack = {},
        )
    }
}
