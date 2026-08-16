package com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecStepsCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicsResult

@Composable
fun HarmonicsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    recordId: Long? = null,
    viewModel: HarmonicsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }

    HarmonicsScreen(
        uiState = uiState,
        onFundamentalChange = viewModel::onFundamentalChange,
        onMagnitudeChange = viewModel::onMagnitudeChange,
        onBalancedChange = viewModel::onBalancedChange,
        onCalculate = viewModel::onCalculate,
        onReset = viewModel::onReset,
        onApplyExample = viewModel::onApplyExample,
        onToggleFavorite = viewModel::onToggleFavorite,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarmonicsScreen(
    uiState: HarmonicsUiState,
    onFundamentalChange: (String) -> Unit,
    onMagnitudeChange: (Int, String) -> Unit,
    onBalancedChange: (Boolean) -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onApplyExample: (WorkedExample<HarmonicsUiState>) -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ElecTopAppBar(
                title = stringResource(R.string.calculator_harmonics_title),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onToggleFavorite) {
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
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = spacing.xl),
        ) {
            ElecExamplesCard(
                examples = harmonicsExamples,
                onSelect = onApplyExample,
                modifier = Modifier.padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.xs,
                ),
            )

            ElecSectionHeader(title = stringResource(R.string.calculator_inputs))

            ElecCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
            ) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    ElecNumericField(
                        value = uiState.fundamental,
                        onValueChange = onFundamentalChange,
                        label = stringResource(R.string.hm_fundamental),
                        unit = "A",
                        error = uiState.fundamentalError,
                    )

                    Text(
                        text = stringResource(R.string.hm_spectrum),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    // One field per order rather than an add-a-row list: a
                    // spectrum comes off an analyser with these exact
                    // headings, and typing it in should feel like copying.
                    HARMONIC_ORDERS.forEach { order ->
                        ElecNumericField(
                            value = uiState.magnitudes[order].orEmpty(),
                            onValueChange = { onMagnitudeChange(order, it) },
                            label = stringResource(R.string.hm_order, order),
                            unit = "%",
                            error = uiState.errors[order],
                            imeAction = if (order == HARMONIC_ORDERS.last()) {
                                ImeAction.Done
                            } else {
                                ImeAction.Next
                            },
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = uiState.balanced,
                                role = Role.Switch,
                                onValueChange = onBalancedChange,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.hm_balanced),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = stringResource(R.string.hm_balanced_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = uiState.balanced, onCheckedChange = null)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                OutlinedButton(onClick = onReset) {
                    Text(stringResource(R.string.action_reset))
                }
                Button(onClick = onCalculate, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_calculate))
                }
            }

            uiState.result?.let { result ->
                ResultCard(result, uiState.balanced)
                ElecStepsCard(
                    steps = uiState.steps,
                    modifier = Modifier.padding(
                        horizontal = spacing.screenHorizontal,
                        vertical = spacing.xs,
                    ),
                )
                NoteCard()
            }
        }
    }
}

@Composable
private fun ResultCard(result: HarmonicsResult, balanced: Boolean) {
    val spacing = ElecTheme.spacing

    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.hm_result_thd),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${result.thdPercent.f()} %",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Line(stringResource(R.string.hm_result_rms), "${result.rmsAmps.f()} A")
            Line(
                stringResource(R.string.hm_result_neutral),
                if (balanced) {
                    "${result.neutralAmps.f()} A"
                } else {
                    stringResource(R.string.hm_result_neutral_unknown)
                },
            )
            Line(stringResource(R.string.hm_result_k), result.kFactor.f())
            result.dominantOrder?.let {
                Line(stringResource(R.string.hm_result_dominant), stringResource(R.string.hm_order, it))
            }

            if (result.neutralExceedsLines) {
                Text(
                    text = stringResource(R.string.hm_warn_neutral),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun Line(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

/** What the four figures do not cover, said where they are read. */
@Composable
private fun NoteCard() {
    val spacing = ElecTheme.spacing
    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.hm_note_scope),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(spacing.lg),
        )
    }
}

private fun Double.f() = NumberFormatter.format(this, decimals = 2)
