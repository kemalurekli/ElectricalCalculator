package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumCategory

@Composable
fun ForumCategoriesRoute(
    onCategoryClick: (ForumCategory) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: ForumCategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForumCategoriesScreen(
        uiState = uiState,
        onCategoryClick = onCategoryClick,
        onRetry = viewModel::onRefresh,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * The forum's sections.
 *
 * Reads as the glossary and the reference shelf do — one row per section, the
 * same list item, the same rhythm. The forum is another shelf in this app, not
 * a different application wearing its theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumCategoriesScreen(
    uiState: ForumScreenState<List<ForumCategory>>,
    onCategoryClick: (ForumCategory) -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ElecTopAppBar(
                title = stringResource(R.string.forum_categories_title),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        ForumStateHost(
            state = uiState,
            onRetry = onRetry,
            modifier = Modifier.padding(innerPadding),
        ) { categories ->
            if (categories.isEmpty()) {
                ElecEmptyState(
                    title = stringResource(R.string.forum_categories_empty_title),
                    message = stringResource(R.string.forum_categories_empty_message),
                    icon = ElecIcons.Forum,
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(categories, key = { it.id }) { category ->
                        ForumCategoryCard(
                            category = category,
                            onClick = { onCategoryClick(category) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * One section of the forum.
 *
 * Every row used to carry the same forum icon, which is six identical glyphs
 * telling the reader nothing. The icon now says what the section is about, and
 * the count says whether there is anything in it — which is the question
 * somebody is actually asking when they look at a list of empty-looking
 * categories.
 */
@Composable
private fun ForumCategoryCard(category: ForumCategory, onClick: () -> Unit) {
    val spacing = ElecTheme.spacing

    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = category.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(text = category.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.forum_category_threads,
                        category.threadCount,
                        category.threadCount,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * The section's own icon, chosen by its key rather than its title.
 *
 * Keys are stable and the same in both languages; titles are neither.
 */
private fun ForumCategory.icon() = when (key) {
    "installations" -> ElecIcons.ForumInstallations
    "protection" -> ElecIcons.ForumProtection
    "troubleshooting" -> ElecIcons.ForumTroubleshooting
    "design" -> ElecIcons.ForumDesign
    "standards" -> ElecIcons.ForumStandards
    "learning" -> ElecIcons.ForumLearning
    // A category added in the dashboard that this build has never heard of
    // still gets a row, just a generic one.
    else -> ElecIcons.Forum
}
