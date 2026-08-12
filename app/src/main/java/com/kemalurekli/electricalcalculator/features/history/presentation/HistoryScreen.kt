package com.kemalurekli.electricalcalculator.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDateTime
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecLoadingState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSearchBar
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle
import com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord

@Composable
fun HistoryRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    HistoryScreen(
        uiState = uiState,
        // The field renders the raw query so typing stays responsive; the
        // debounced value in `uiState` only drives the results.
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ElecTopAppBar(
                title = stringResource(R.string.destination_history),
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
                query = query,
                onQueryChange = onQueryChange,
                // This field searches saved calculations, not the catalog, so
                // it must not borrow the home screen's placeholder.
                placeholder = stringResource(R.string.search_history_hint),
                modifier = Modifier.padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.sm,
                ),
            )

            when {
                uiState.isLoading -> ElecLoadingState()

                uiState.records.isEmpty() -> ElecEmptyState(
                    title = stringResource(R.string.state_empty_history_title),
                    message = stringResource(R.string.state_empty_history_message),
                    icon = ElecIcons.History,
                    modifier = Modifier.fillMaxWidth(),
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = spacing.screenHorizontal,
                        end = spacing.screenHorizontal,
                        bottom = spacing.xl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(uiState.records, key = { it.id }) { record ->
                        HistoryCard(record = record)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(record: CalculationRecord) {
    val spacing = ElecTheme.spacing
    ElecCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = record.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = record.summary,
                style = NumericCompactTextStyle,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = record.createdAt.formatAsDateTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
