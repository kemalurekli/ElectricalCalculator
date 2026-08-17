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
    onNavigateBack: () -> Unit,
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
    onNavigateBack: () -> Unit,
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
                        ElecListItem(
                            title = category.title,
                            description = category.description,
                            icon = ElecIcons.Forum,
                            onClick = { onCategoryClick(category) },
                        )
                    }
                }
            }
        }
    }
}
