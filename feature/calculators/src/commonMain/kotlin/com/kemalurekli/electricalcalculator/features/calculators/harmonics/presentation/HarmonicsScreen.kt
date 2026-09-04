package com.kemalurekli.electricalcalculator.features.calculators.harmonics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecStepsCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.harmonics.domain.HarmonicsResult
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_harmonics_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_balanced
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_balanced_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_fundamental
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_note_scope
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_order
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_result_dominant
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_result_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_result_neutral
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_result_neutral_unknown
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_result_rms
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_result_thd
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_spectrum
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.hm_warn_neutral
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove

@Composable
fun HarmonicsRoute(
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    recordId: Long? = null,
    viewModel: HarmonicsViewModel = koinViewModel(),
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
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.calculator_harmonics_title),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
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
                            DesignSystemRes.string.action_favorite_remove
                        } else {
                            DesignSystemRes.string.action_favorite_add
                        },
                    ),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = spacing.xl),
        ) {
            // The result sits above the form once it exists: after pressing
            // Calculate the user is looking for the number, not the fields
            // they just finished filling in. The workings and the notes stay
            // below, where they are read second.
            uiState.result?.let { result -> ResultCard(result, uiState.balanced) }

            ElecExamplesCard(
                examples = harmonicsExamples,
                onSelect = onApplyExample,
                hasResult = uiState.result != null,
                modifier = Modifier.padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.xs,
                ),
            )

            ElecSectionHeader(title = stringResource(Res.string.calculator_inputs))

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
                        label = stringResource(Res.string.hm_fundamental),
                        unit = "A",
                        error = uiState.fundamentalError,
                    )

                    Text(
                        text = stringResource(Res.string.hm_spectrum),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    // One field per order rather than an add-a-row list: a
                    // spectrum comes off an analyser with these exact
                    // headings, and typing it in should feel like copying.
                    HARMONIC_ORDERS.forEach { order ->
                        ElecNumericField(
                            value = uiState.magnitudes[order].orEmpty(),
                            onValueChange = { onMagnitudeChange(order, it) },
                            label = stringResource(Res.string.hm_order, order),
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
                                text = stringResource(Res.string.hm_balanced),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = stringResource(Res.string.hm_balanced_hint),
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
                    Text(stringResource(Res.string.action_reset))
                }
                Button(onClick = onCalculate, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.action_calculate))
                }
            }

            if (uiState.result != null) {
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
                    text = stringResource(Res.string.hm_result_thd),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${result.thdPercent.f()} %",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Line(stringResource(Res.string.hm_result_rms), "${result.rmsAmps.f()} A")
            Line(
                stringResource(Res.string.hm_result_neutral),
                if (balanced) {
                    "${result.neutralAmps.f()} A"
                } else {
                    stringResource(Res.string.hm_result_neutral_unknown)
                },
            )
            Line(stringResource(Res.string.hm_result_k), result.kFactor.f())
            result.dominantOrder?.let {
                Line(stringResource(Res.string.hm_result_dominant), stringResource(Res.string.hm_order, it))
            }

            if (result.neutralExceedsLines) {
                Text(
                    text = stringResource(Res.string.hm_warn_neutral),
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
            text = stringResource(Res.string.hm_note_scope),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(spacing.lg),
        )
    }
}

private fun Double.f() = NumberFormatter.format(this, decimals = 2)
