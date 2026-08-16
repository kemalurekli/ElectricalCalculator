package com.kemalurekli.electricalcalculator.features.glossary.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
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

/**
 * The glossary: an A–Z of the vocabulary, searchable in either language.
 *
 * ### Why the definitions expand in place rather than opening a screen
 *
 * A reference topic is read; a glossary is consulted. Someone here has one word
 * in mind and wants it answered without losing their place, so a term opens
 * where it sits and closes again. One at a time — opening a second closes the
 * first — which keeps the list navigable instead of turning it into a wall.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryRoute(
    openTermKey: String?,
    onCalculatorClick: (CalculatorId) -> Unit,
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GlossaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()

    // Keyed on the term so arriving from a second search hit re-opens; the
    // ViewModel ignores a repeat of one it has already honoured.
    LaunchedEffect(openTermKey) {
        openTermKey?.let(viewModel::onOpenTerm)
    }

    GlossaryScreen(
        uiState = uiState,
        pinned = pinned,
        onToggleFavorite = viewModel::onToggleFavorite,
        onQueryChange = viewModel::onQueryChange,
        onToggleExpanded = viewModel::onToggleExpanded,
        onFollowLink = viewModel::onFollowLink,
        onCalculatorClick = onCalculatorClick,
        onReferenceClick = onReferenceClick,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlossaryScreen(
    uiState: GlossaryUiState,
    onQueryChange: (String) -> Unit,
    onToggleExpanded: (String) -> Unit,
    onFollowLink: (String) -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    pinned: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    // Where the open term sits in the flat list, so it can be scrolled to.
    // Setting `expandedKey` alone leaves it expanded somewhere below 117
    // entries, which for a term arrived at from search is the same as not
    // opening it at all.
    val openIndex = remember(uiState.sections, uiState.expandedKey) {
        val key = uiState.expandedKey ?: return@remember null
        var index = SEARCH_BAR_ITEMS
        uiState.sections.forEach { section ->
            if (section.letter.isNotEmpty()) index++
            section.terms.forEach { term ->
                if (term.key == key) return@remember index
                index++
            }
        }
        null
    }

    LaunchedEffect(openIndex) {
        openIndex?.let { listState.animateScrollToItem(it) }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier
                .widthIn(max = layout.contentMaxWidth)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                ElecTopAppBar(
                    title = stringResource(R.string.dashboard_glossary_title),
                    onNavigateBack = onNavigateBack,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = spacing.lg,
                    end = spacing.lg,
                    bottom = spacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                item(key = "search") {
                    ElecSearchBar(
                        query = uiState.query,
                        onQueryChange = onQueryChange,
                        placeholder = stringResource(R.string.glossary_search_hint),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.sm),
                    )
                }

                if (uiState.hasNoResults) {
                    item(key = "empty") {
                        ElecEmptyState(
                            title = stringResource(R.string.glossary_empty_title),
                            message = stringResource(R.string.glossary_empty_message),
                            icon = ElecIcons.Search,
                            modifier = Modifier.padding(top = spacing.xxl),
                        )
                    }
                }

                uiState.sections.forEach { section ->
                    if (section.letter.isNotEmpty()) {
                        item(key = "letter-${section.letter}") {
                            ElecSectionHeader(title = section.letter)
                        }
                    }
                    items(
                        count = section.terms.size,
                        key = { index -> section.terms[index].key },
                    ) { index ->
                        val term = section.terms[index]
                        TermCard(
                            term = term,
                            isExpanded = term.key == uiState.expandedKey,
                            isFavorite = term.key in pinned,
                            onToggleFavorite = { onToggleFavorite(term.key) },
                            onToggle = { onToggleExpanded(term.key) },
                            onFollowLink = onFollowLink,
                            onCalculatorClick = onCalculatorClick,
                            onReferenceClick = onReferenceClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TermCard(
    term: GlossaryTermUiModel,
    isExpanded: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onToggle: () -> Unit,
    onFollowLink: (String) -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onReferenceClick: (String) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = term.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                )
                // The symbol is how the quantity appears in a formula, so it
                // sits where the eye lands when scanning for one.
                term.symbol?.let { symbol ->
                    Text(
                        text = if (term.unit != null) "$symbol · ${term.unit}" else symbol,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = spacing.sm),
                    )
                }
                    // Shown only on an open card: a star on every row of a
                    // 117-term list is clutter, and pinning is a decision a
                    // reader makes after reading, not while scanning.
                    if (isExpanded) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(FAVORITE_TOUCH_TARGET),
                        ) {
                            Icon(
                                imageVector = if (isFavorite) ElecIcons.FavoriteOn
                                else ElecIcons.FavoriteOff,
                                contentDescription = stringResource(
                                    if (isFavorite) R.string.action_favorite_remove
                                    else R.string.action_favorite_add,
                                ),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
            }

            // Present only when it is saying something the title does not — in
            // an English build the two are the same words.
            term.englishName?.let { english ->
                Text(
                    text = english,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    HorizontalDivider()

                    Text(
                        text = term.definition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (term.seeAlso.isNotEmpty() ||
                        term.calculator != null ||
                        term.referenceTopic != null
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            // The two jumps out of the glossary come first:
                            // reading what a thing is, then going and doing it.
                            term.calculator?.let { id ->
                                AssistChip(
                                    onClick = { onCalculatorClick(id) },
                                    label = {
                                        Text(stringResource(R.string.glossary_open_calculator))
                                    },
                                )
                            }
                            term.referenceTopic?.let { topicKey ->
                                AssistChip(
                                    onClick = { onReferenceClick(topicKey) },
                                    label = {
                                        Text(stringResource(R.string.glossary_open_reference))
                                    },
                                )
                            }
                            term.seeAlso.forEach { link ->
                                AssistChip(
                                    onClick = { onFollowLink(link.key) },
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
private fun GlossaryScreenPreview() {
    ElecToolkitTheme {
        GlossaryRoute(
            openTermKey = null,
            onCalculatorClick = {},
            onReferenceClick = {},
            onNavigateBack = {},
        )
    }
}

/** The search bar occupies the list's first slot. */
private const val SEARCH_BAR_ITEMS = 1

/** Keeps the star's tap area clear of the card's own expand gesture. */
private val FAVORITE_TOUCH_TARGET = 40.dp
