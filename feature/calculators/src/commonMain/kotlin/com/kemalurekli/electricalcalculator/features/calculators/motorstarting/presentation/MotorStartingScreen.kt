package com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingResult
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.StartingMethod
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_motor_starting_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_full_load_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_locked_rotor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_locked_rotor_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method_auto
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method_dol
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method_soft
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method_star_delta
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_note_scope
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_note_torque
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_dip
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_residual
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_short_circuit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_starting_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_starting_kva
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_torque
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_transformer_impedance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_transformer_impedance_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_transformer_kva
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_warn_contactor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_warn_flicker
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove

@Composable
fun MotorStartingRoute(
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    recordId: Long? = null,
    viewModel: MotorStartingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }

    MotorStartingScreen(
        uiState = uiState,
        onFullLoadCurrentChange = viewModel::onFullLoadCurrentChange,
        onLockedRotorChange = viewModel::onLockedRotorChange,
        onMethodChange = viewModel::onMethodChange,
        onSupplyVoltageChange = viewModel::onSupplyVoltageChange,
        onTransformerKvaChange = viewModel::onTransformerKvaChange,
        onTransformerImpedanceChange = viewModel::onTransformerImpedanceChange,
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
fun MotorStartingScreen(
    uiState: MotorStartingUiState,
    onFullLoadCurrentChange: (String) -> Unit,
    onLockedRotorChange: (String) -> Unit,
    onMethodChange: (StartingMethod) -> Unit,
    onSupplyVoltageChange: (String) -> Unit,
    onTransformerKvaChange: (String) -> Unit,
    onTransformerImpedanceChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onApplyExample: (WorkedExample<MotorStartingUiState>) -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.calculator_motor_starting_title),
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
            uiState.result?.let { result -> ResultCard(result) }

            ElecExamplesCard(
                examples = motorStartingExamples,
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
                        value = uiState.fullLoadCurrent,
                        onValueChange = onFullLoadCurrentChange,
                        label = stringResource(Res.string.ms_full_load_current),
                        unit = "A",
                        error = uiState.errors[MotorStartingField.FULL_LOAD_CURRENT],
                    )
                    ElecNumericField(
                        value = uiState.lockedRotorMultiple,
                        onValueChange = onLockedRotorChange,
                        label = stringResource(Res.string.ms_locked_rotor),
                        unit = "× In",
                        error = uiState.errors[MotorStartingField.LOCKED_ROTOR_MULTIPLE],
                        supportingText = stringResource(Res.string.ms_locked_rotor_hint),
                    )
                    ElecOptionSelector(
                        label = stringResource(Res.string.ms_method),
                        options = StartingMethod.entries.toImmutableList(),
                        selected = uiState.method,
                        onSelect = onMethodChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    ElecNumericField(
                        value = uiState.supplyVoltage,
                        onValueChange = onSupplyVoltageChange,
                        label = stringResource(Res.string.common_system_voltage),
                        unit = "V",
                        error = uiState.errors[MotorStartingField.SUPPLY_VOLTAGE],
                    )
                    ElecNumericField(
                        value = uiState.transformerKva,
                        onValueChange = onTransformerKvaChange,
                        label = stringResource(Res.string.ms_transformer_kva),
                        unit = "kVA",
                        error = uiState.errors[MotorStartingField.TRANSFORMER_KVA],
                    )
                    ElecNumericField(
                        value = uiState.transformerImpedance,
                        onValueChange = onTransformerImpedanceChange,
                        label = stringResource(Res.string.ms_transformer_impedance),
                        unit = "%",
                        error = uiState.errors[MotorStartingField.TRANSFORMER_IMPEDANCE],
                        supportingText = stringResource(Res.string.ms_transformer_impedance_hint),
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

            if (uiState.result != null) {
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
private fun ResultCard(result: MotorStartingResult) {
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
                    text = stringResource(Res.string.ms_result_dip),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${result.dipPercent.f()} %",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (result.risksContactorDropout) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            Line(stringResource(Res.string.ms_result_residual), "${result.residualVoltage.f()} V")
            Line(
                stringResource(Res.string.ms_result_starting_current),
                "${result.startingCurrentAmps.f()} A",
            )
            Line(stringResource(Res.string.ms_result_starting_kva), "${result.startingKva.f()} kVA")
            if (result.shortCircuitKva.isFinite()) {
                Line(
                    stringResource(Res.string.ms_result_short_circuit),
                    "${result.shortCircuitKva.f()} kVA",
                )
            }
            Line(
                stringResource(Res.string.ms_result_torque),
                "${result.startingTorquePercent.f()} %",
            )

            // Ordered worst first: a board that shuts itself down outranks a
            // lamp that blinks, and only the more serious one is worth colour.
            if (result.risksContactorDropout) {
                Text(
                    text = stringResource(Res.string.ms_warn_contactor),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (result.visibleFlicker) {
                Text(
                    text = stringResource(Res.string.ms_warn_flicker),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

/** What the number leaves out, and what it costs to make it smaller. */
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
                text = stringResource(Res.string.ms_note_torque),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.ms_note_scope),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun StartingMethod.label(): StringResource = when (this) {
    StartingMethod.DIRECT_ON_LINE -> Res.string.ms_method_dol
    StartingMethod.STAR_DELTA -> Res.string.ms_method_star_delta
    StartingMethod.SOFT_STARTER_50 -> Res.string.ms_method_soft
    StartingMethod.AUTOTRANSFORMER_65 -> Res.string.ms_method_auto
}

private fun Double.f() = NumberFormatter.format(this, decimals = 2)
