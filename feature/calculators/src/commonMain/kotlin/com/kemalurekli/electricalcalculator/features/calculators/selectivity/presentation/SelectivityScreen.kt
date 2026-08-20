package com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation

import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecStepsCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityGrade
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityResult
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_selectivity_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_b
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_c
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_custom
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_d
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_rcd
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_downstream
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_downstream_rating
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_fault_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_fault_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_note_bound
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_note_overload
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_result_downstream_threshold
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_result_limit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_result_ratio
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_result_upstream_threshold
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_upstream
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_upstream_rating
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove

@Composable
fun SelectivityRoute(
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    recordId: Long? = null,
    viewModel: SelectivityViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }

    SelectivityScreen(
        uiState = uiState,
        onUpstreamTypeChange = viewModel::onUpstreamTypeChange,
        onUpstreamRatingChange = viewModel::onUpstreamRatingChange,
        onDownstreamTypeChange = viewModel::onDownstreamTypeChange,
        onDownstreamRatingChange = viewModel::onDownstreamRatingChange,
        onFaultCurrentChange = viewModel::onFaultCurrentChange,
        onCalculate = viewModel::onCalculate,
        onReset = viewModel::onReset,
        onApplyExample = viewModel::onApplyExample,
        onToggleFavorite = viewModel::onToggleFavorite,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * Two devices, one fault level, one answer.
 *
 * The fault current sits with the devices rather than in a section of its own,
 * because it is not context — it is the third input the verdict depends on, and
 * separating it invites the reader to treat the answer as a property of the
 * pair.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectivityScreen(
    uiState: SelectivityUiState,
    onUpstreamTypeChange: (ProtectiveDeviceType) -> Unit,
    onUpstreamRatingChange: (String) -> Unit,
    onDownstreamTypeChange: (ProtectiveDeviceType) -> Unit,
    onDownstreamRatingChange: (String) -> Unit,
    onFaultCurrentChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onApplyExample: (WorkedExample<SelectivityUiState>) -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.calculator_selectivity_title),
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
            ElecExamplesCard(
                examples = selectivityExamples,
                onSelect = onApplyExample,
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
                    ElecOptionSelector(
                        label = stringResource(Res.string.sel_upstream),
                        options = ProtectiveDeviceType.entries.toImmutableList(),
                        selected = uiState.upstreamType,
                        onSelect = onUpstreamTypeChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    ElecNumericField(
                        value = uiState.upstreamRating,
                        onValueChange = onUpstreamRatingChange,
                        label = stringResource(Res.string.sel_upstream_rating),
                        unit = "A",
                        error = uiState.errors[SelectivityField.UPSTREAM_RATING],
                    )
                    ElecOptionSelector(
                        label = stringResource(Res.string.sel_downstream),
                        options = ProtectiveDeviceType.entries.toImmutableList(),
                        selected = uiState.downstreamType,
                        onSelect = onDownstreamTypeChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    ElecNumericField(
                        value = uiState.downstreamRating,
                        onValueChange = onDownstreamRatingChange,
                        label = stringResource(Res.string.sel_downstream_rating),
                        unit = "A",
                        error = uiState.errors[SelectivityField.DOWNSTREAM_RATING],
                    )
                    ElecNumericField(
                        value = uiState.faultCurrent,
                        onValueChange = onFaultCurrentChange,
                        label = stringResource(Res.string.sel_fault_current),
                        unit = "A",
                        error = uiState.errors[SelectivityField.FAULT_CURRENT],
                        supportingText = stringResource(Res.string.sel_fault_hint),
                        imeAction = ImeAction.Done,
                    )
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

            uiState.result?.let { result ->
                ResultCard(result)
                ElecStepsCard(
                    steps = uiState.steps,
                    modifier = Modifier.padding(
                        horizontal = spacing.screenHorizontal,
                        vertical = spacing.xs,
                    ),
                )
                NotesCard()
            }
        }
    }
}

@Composable
private fun ResultCard(result: SelectivityResult) {
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
            Text(
                text = stringResource(result.grade.summary()),
                style = MaterialTheme.typography.titleMedium,
                color = when (result.grade) {
                    SelectivityGrade.SELECTIVE -> MaterialTheme.colorScheme.primary
                    SelectivityGrade.PARTIAL -> MaterialTheme.colorScheme.onSurface
                    SelectivityGrade.NONE -> MaterialTheme.colorScheme.error
                },
            )
            result.limitAmps?.let {
                ResultLine(stringResource(Res.string.sel_result_limit), "${it.format()} A")
            }
            ResultLine(stringResource(Res.string.sel_result_ratio), result.ratio.format())
            result.upstreamInstantaneousAmps?.let {
                ResultLine(
                    stringResource(Res.string.sel_result_upstream_threshold),
                    "${it.format()} A",
                )
            }
            result.downstreamInstantaneousAmps?.let {
                ResultLine(
                    stringResource(Res.string.sel_result_downstream_threshold),
                    "${it.format()} A",
                )
            }
        }
    }
}

@Composable
private fun ResultLine(label: String, value: String) {
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

/** The two things a reader must not take away from a green verdict. */
@Composable
private fun NotesCard() {
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
            Text(
                text = stringResource(Res.string.sel_note_overload),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.sel_note_bound),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun ProtectiveDeviceType.label(): StringResource = when (this) {
    ProtectiveDeviceType.MCB_TYPE_B -> Res.string.ef_device_b
    ProtectiveDeviceType.MCB_TYPE_C -> Res.string.ef_device_c
    ProtectiveDeviceType.MCB_TYPE_D -> Res.string.ef_device_d
    ProtectiveDeviceType.CUSTOM -> Res.string.ef_device_custom
    ProtectiveDeviceType.RCD -> Res.string.ef_device_rcd
}

private fun Double.format() =
    com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter.format(this, decimals = 2)
