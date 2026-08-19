package com.kemalurekli.electricalcalculator.features.calculators.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme

/**
 * A single calculator.
 *
 * The chrome — title, description, favourite toggle, back navigation — is
 * wired to the catalog and favourites repository. The input form, formula,
 * engineering notes and result actions are added per calculator in the
 * development phase that owns it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorDetailRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalculatorDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = ElecTheme.spacing

    ElecScreenScaffold(
        title = uiState.title.ifEmpty { stringResource(R.string.destination_calculators) },
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        actions = {
            if (uiState.id != null) {
                IconButton(onClick = viewModel::onToggleFavorite) {
                    Icon(
                        imageVector = if (uiState.isFavorite) {
                            ElecIcons.FavoriteOn
                        } else {
                            ElecIcons.FavoriteOff
                        },
                        contentDescription = stringResource(
                            if (uiState.isFavorite) {
                                R.string.action_favorite_remove
                            } else {
                                R.string.action_favorite_add
                            },
                        ),
                        tint = if (uiState.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            if (uiState.id == null) {
                ElecEmptyState(
                    title = stringResource(R.string.state_error_title),
                    message = stringResource(R.string.state_empty_calculators_message),
                    icon = ElecIcons.Calculators,
                    modifier = Modifier.fillMaxWidth(),
                )
                return@Column
            }

            Text(
                text = uiState.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
            )

            ElecEmptyState(
                title = stringResource(R.string.state_in_development_title),
                message = stringResource(R.string.state_in_development_message),
                icon = ElecIcons.forCalculator(uiState.icon),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
