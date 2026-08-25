package com.kemalurekli.electricalcalculator.features.calculators.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
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
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorUiModel
import kotlinx.collections.immutable.persistentListOf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.category_cable_and_conduit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.destination_calculators
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.state_empty_calculators_message
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.state_empty_calculators_title

/**
 * Stateful entry point: binds the ViewModel and delegates rendering.
 *
 * Splitting the screen this way keeps the stateless composable previewable and
 * testable with plain data, with no Hilt graph required.
 */
@Composable
fun CalculatorsRoute(
    onCalculatorClick: (CalculatorId) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CalculatorsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalculatorsScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onToggleFavorite = viewModel::onToggleFavorite,
        onCalculatorClick = onCalculatorClick,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    uiState: CalculatorsUiState,
    onQueryChange: (String) -> Unit,
    onToggleFavorite: (CalculatorId) -> Unit,
    onCalculatorClick: (CalculatorId) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.destination_calculators),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = spacing.xl),
        ) {
            item(key = "search") {
                ElecSearchBar(
                    query = uiState.query,
                    onQueryChange = onQueryChange,
                    modifier = Modifier.padding(
                        horizontal = spacing.screenHorizontal,
                        vertical = spacing.sm,
                    ),
                )
            }

            if (uiState.isEmpty) {
                item(key = "empty") {
                    ElecEmptyState(
                        title = stringResource(Res.string.state_empty_calculators_title),
                        message = stringResource(Res.string.state_empty_calculators_message),
                        icon = ElecIcons.Search,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else if (uiState.isSearching) {
                itemsIndexed(
                    items = uiState.searchResults,
                    key = { _, item -> item.id.key },
                ) { index, item ->
                    CalculatorRow(item, onCalculatorClick, onToggleFavorite)
                    if (index < uiState.searchResults.lastIndex) ElecListDivider()
                }
            } else {
                uiState.sections.forEach { section ->
                    item(key = "header-${section.category.name}") {
                        ElecSectionHeader(title = stringResource(section.title))
                    }
                    itemsIndexed(
                        items = section.items,
                        key = { _, item -> item.id.key },
                    ) { index, item ->
                        CalculatorRow(item, onCalculatorClick, onToggleFavorite)
                        if (index < section.items.lastIndex) ElecListDivider()
                    }
                }
            }
        }
    }
}

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
    )
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun CalculatorsScreenPreview() {
    ElecToolkitTheme {
        Box {
            CalculatorsScreen(
                uiState = CalculatorsUiState(
                    sections = persistentListOf(
                        CalculatorSection(
                            category = CalculatorCategory.CABLE_AND_CONDUIT,
                            title = Res.string.category_cable_and_conduit,
                            items = persistentListOf(
                                CalculatorUiModel(
                                    id = CalculatorId.VOLTAGE_DROP,
                                    title = "Voltage Drop",
                                    description = "Volt drop and percentage over a cable run",
                                    icon = CalculatorIcon.VOLTAGE_DROP,
                                    category = CalculatorCategory.CABLE_AND_CONDUIT,
                                    isFavorite = true,
                                ),
                            ),
                        ),
                    ),
                ),
                onQueryChange = {},
                onToggleFavorite = {},
                onCalculatorClick = {},
                onNavigateBack = {},
            )
        }
    }
}
