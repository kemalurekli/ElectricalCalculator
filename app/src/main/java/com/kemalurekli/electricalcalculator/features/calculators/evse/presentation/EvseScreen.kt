package com.kemalurekli.electricalcalculator.features.calculators.evse.presentation

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
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
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.DcFaultDetection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseConnection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseResult
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.RcdRequirement
import kotlinx.collections.immutable.toImmutableList

@Composable
fun EvseRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    recordId: Long? = null,
    viewModel: EvseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }

    EvseScreen(
        uiState = uiState,
        onPointCountChange = viewModel::onPointCountChange,
        onRatedCurrentChange = viewModel::onRatedCurrentChange,
        onConnectionChange = viewModel::onConnectionChange,
        onSupplyVoltageChange = viewModel::onSupplyVoltageChange,
        onSimultaneityChange = viewModel::onSimultaneityChange,
        onDcDetectionChange = viewModel::onDcDetectionChange,
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
fun EvseScreen(
    uiState: EvseUiState,
    onPointCountChange: (String) -> Unit,
    onRatedCurrentChange: (String) -> Unit,
    onConnectionChange: (EvseConnection) -> Unit,
    onSupplyVoltageChange: (String) -> Unit,
    onSimultaneityChange: (String) -> Unit,
    onDcDetectionChange: (DcFaultDetection) -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onApplyExample: (WorkedExample<EvseUiState>) -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(R.string.calculator_evse_title),
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
                            R.string.action_favorite_remove
                        } else {
                            R.string.action_favorite_add
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
                examples = evseExamples,
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
                        value = uiState.pointCount,
                        onValueChange = onPointCountChange,
                        label = stringResource(R.string.ev_point_count),
                        error = uiState.errors[EvseField.POINT_COUNT],
                    )
                    ElecNumericField(
                        value = uiState.ratedCurrent,
                        onValueChange = onRatedCurrentChange,
                        label = stringResource(R.string.ev_rated_current),
                        unit = "A",
                        error = uiState.errors[EvseField.RATED_CURRENT],
                    )
                    ElecOptionSelector(
                        label = stringResource(R.string.ev_connection),
                        options = EvseConnection.entries.toImmutableList(),
                        selected = uiState.connection,
                        onSelect = onConnectionChange,
                        optionLabel = { stringResource(it.labelRes()) },
                    )
                    ElecNumericField(
                        value = uiState.supplyVoltage,
                        onValueChange = onSupplyVoltageChange,
                        label = stringResource(R.string.common_system_voltage),
                        unit = "V",
                        error = uiState.errors[EvseField.SUPPLY_VOLTAGE],
                    )
                    ElecNumericField(
                        value = uiState.simultaneity,
                        onValueChange = onSimultaneityChange,
                        label = stringResource(R.string.ev_simultaneity),
                        error = uiState.errors[EvseField.SIMULTANEITY],
                        supportingText = stringResource(R.string.ev_simultaneity_hint),
                        imeAction = ImeAction.Done,
                    )
                    ElecOptionSelector(
                        label = stringResource(R.string.ev_dc_detection),
                        options = DcFaultDetection.entries.toImmutableList(),
                        selected = uiState.dcFaultDetection,
                        onSelect = onDcDetectionChange,
                        optionLabel = { stringResource(it.labelRes()) },
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
                    Text(stringResource(R.string.action_reset))
                }
                Button(onClick = onCalculate, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_calculate))
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
private fun ResultCard(result: EvseResult) {
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
                    text = stringResource(R.string.ev_result_design),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${result.designCurrentAmps.f()} A",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Line(stringResource(R.string.ev_result_per_point), "${result.powerPerPointKw.f()} kW")
            Line(stringResource(R.string.ev_result_total_kw), "${result.totalConnectedKw.f()} kW")
            Line(
                stringResource(R.string.ev_result_connected),
                "${result.totalConnectedAmps.f()} A",
            )
            Line(
                stringResource(R.string.ev_result_device),
                result.deviceRatingAmps?.let { "${it.f()} A" }
                    ?: stringResource(R.string.ev_result_device_none),
            )

            // The RCD is the answer most likely to be got wrong, so it is not
            // buried in the list above with the currents.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.ev_result_rcd),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(result.rcdRequirement.labelRes()),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (result.rcdRequirement == RcdRequirement.TYPE_B) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
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

/** The three things about this circuit that are not obvious from the numbers. */
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
            listOf(R.string.ev_note_dc, R.string.ev_note_continuous, R.string.ev_note_scope)
                .forEach { note ->
                    Text(
                        text = stringResource(note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
        }
    }
}

private fun EvseConnection.labelRes(): Int = when (this) {
    EvseConnection.SINGLE_PHASE -> R.string.ev_connection_single
    EvseConnection.THREE_PHASE -> R.string.ev_connection_three
}

private fun DcFaultDetection.labelRes(): Int = when (this) {
    DcFaultDetection.BUILT_IN_6MA -> R.string.ev_dc_builtin
    DcFaultDetection.NONE -> R.string.ev_dc_none
}

private fun RcdRequirement.labelRes(): Int = when (this) {
    RcdRequirement.TYPE_A -> R.string.ev_rcd_a
    RcdRequirement.TYPE_B -> R.string.ev_rcd_b
}

private fun Double.f() = NumberFormatter.format(this, decimals = 2)
