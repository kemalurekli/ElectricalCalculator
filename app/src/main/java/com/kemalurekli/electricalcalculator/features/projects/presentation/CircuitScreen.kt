package com.kemalurekli.electricalcalculator.features.projects.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CircuitLoadKind
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.design.domain.BindingConstraint
import com.kemalurekli.electricalcalculator.features.design.domain.CircuitDesignResult
import com.kemalurekli.electricalcalculator.features.design.domain.DesignStage
import com.kemalurekli.electricalcalculator.features.inspection.domain.InsulationTestVoltage
import com.kemalurekli.electricalcalculator.features.inspection.domain.RcdType
import com.kemalurekli.electricalcalculator.features.inspection.domain.TestKind
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CircuitRoute(
    projectId: Long,
    circuitId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CircuitViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(projectId, circuitId) { viewModel.onOpen(projectId, circuitId) }
    LaunchedEffect(uiState.isGone) { if (uiState.isGone) onNavigateBack() }

    CircuitScreen(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onLoadKindChange = viewModel::onLoadKindChange,
        onLoadChange = viewModel::onLoadChange,
        onPowerFactorChange = viewModel::onPowerFactorChange,
        onLengthChange = viewModel::onLengthChange,
        onGroupedCircuitsChange = viewModel::onGroupedCircuitsChange,
        onParallelConductorsChange = viewModel::onParallelConductorsChange,
        onDeviceTypeChange = viewModel::onDeviceTypeChange,
        onDisconnectionTimeChange = viewModel::onDisconnectionTimeChange,
        onDelete = viewModel::onDelete,
        onTestValueChange = viewModel::onTestValueChange,
        onTestPolarityChange = viewModel::onTestPolarityChange,
        onInsulationVoltageChange = viewModel::onInsulationVoltageChange,
        onRcdTypeChange = viewModel::onRcdTypeChange,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircuitScreen(
    uiState: CircuitUiState,
    onNameChange: (String) -> Unit,
    onLoadKindChange: (CircuitLoadKind) -> Unit,
    onLoadChange: (String) -> Unit,
    onPowerFactorChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onGroupedCircuitsChange: (String) -> Unit,
    onParallelConductorsChange: (String) -> Unit,
    onDeviceTypeChange: (ProtectiveDeviceType) -> Unit,
    onDisconnectionTimeChange: (String) -> Unit,
    onDelete: () -> Unit,
    onTestValueChange: (TestKind, String) -> Unit,
    onTestPolarityChange: (Boolean?) -> Unit,
    onInsulationVoltageChange: (InsulationTestVoltage) -> Unit,
    onRcdTypeChange: (RcdType) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val circuit = uiState.circuit

    ElecScreenScaffold(
        title = circuit?.name?.ifBlank { null }
                    ?: stringResource(R.string.circuit_untitled),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { confirmDelete = true }) {
                Icon(
                    imageVector = ElecIcons.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        if (circuit == null) return@ElecScreenScaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = spacing.xl),
        ) {
            ElecCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
            ) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    OutlinedTextField(
                        value = circuit.name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.circuit_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ElecOptionSelector(
                        label = stringResource(R.string.circuit_load_kind),
                        options = CircuitLoadKind.entries.toImmutableList(),
                        selected = circuit.loadKind,
                        onSelect = onLoadKindChange,
                        optionLabel = { stringResource(it.labelRes()) },
                    )
                    ElecNumericField(
                        value = circuit.load,
                        onValueChange = onLoadChange,
                        label = when (circuit.loadKind) {
                            CircuitLoadKind.CURRENT -> stringResource(R.string.circuit_load_current)
                            CircuitLoadKind.POWER -> stringResource(R.string.circuit_load_power)
                        },
                        unit = when (circuit.loadKind) {
                            CircuitLoadKind.CURRENT -> "A"
                            CircuitLoadKind.POWER -> "W"
                        },
                    )
                    ElecNumericField(
                        value = circuit.lengthMetres,
                        onValueChange = onLengthChange,
                        label = stringResource(R.string.common_route_length),
                        unit = "m",
                    )
                    ElecNumericField(
                        value = circuit.powerFactor,
                        onValueChange = onPowerFactorChange,
                        label = stringResource(R.string.common_power_factor),
                    )
                    ElecNumericField(
                        value = circuit.groupedCircuits,
                        onValueChange = onGroupedCircuitsChange,
                        label = stringResource(R.string.cs_circuits_label),
                    )
                    ElecNumericField(
                        value = circuit.parallelConductors,
                        onValueChange = onParallelConductorsChange,
                        label = stringResource(R.string.common_parallel_conductors),
                    )
                    ElecOptionSelector(
                        label = stringResource(R.string.ef_device),
                        options = ProtectiveDeviceType.entries.toImmutableList(),
                        selected = circuit.deviceType.toDeviceTypeOrDefault(),
                        onSelect = onDeviceTypeChange,
                        optionLabel = { stringResource(it.labelRes()) },
                    )
                    ElecNumericField(
                        value = circuit.disconnectionTimeSeconds,
                        onValueChange = onDisconnectionTimeChange,
                        label = stringResource(R.string.circuit_disconnection),
                        unit = "s",
                        supportingText = stringResource(R.string.circuit_disconnection_hint),
                    )
                }
            }

            ElecSectionHeader(title = stringResource(R.string.circuit_result))
            DesignCard(uiState.design)

            ElecSectionHeader(title = stringResource(R.string.tests_section))
            TestsCard(
                rows = uiState.tests,
                onValueChange = onTestValueChange,
                onPolarityChange = onTestPolarityChange,
                onInsulationVoltageChange = onInsulationVoltageChange,
                onRcdTypeChange = onRcdTypeChange,
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.circuit_delete_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/**
 * The design, and the four questions that produced it.
 *
 * The stages are shown whether they passed or not, in the order they were
 * asked. A result that only reports its answer has to be re-derived by hand
 * before anyone will sign it; a result that shows Iz against In and Zs against
 * its limit can be read.
 */
@Composable
private fun DesignCard(design: CircuitDesignResult?) {
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
            if (design == null) {
                Text(
                    text = stringResource(R.string.circuit_incomplete),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            design.failure?.let { failure ->
                Text(
                    text = stringResource(failure.messageRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (design.hasSolution) {
                DesignLine(
                    label = stringResource(R.string.circuit_result_size),
                    value = "${design.crossSectionMm2.format()} mm²",
                    emphasised = true,
                )
                DesignLine(
                    label = stringResource(R.string.circuit_result_protective),
                    value = "${design.protectiveCrossSectionMm2.format()} mm²",
                )
            }
            DesignLine(
                label = stringResource(R.string.circuit_result_device),
                value = design.deviceRatingAmps?.let { "${it.format()} A" }.orEmpty(),
            )
            DesignLine(
                label = stringResource(R.string.circuit_result_binding),
                value = stringResource(design.bindingConstraint.labelRes()),
            )

            if (design.stages.isEmpty()) return@Column

            HorizontalDivider(
                modifier = Modifier.padding(vertical = spacing.xs),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = stringResource(R.string.circuit_chain),
                style = MaterialTheme.typography.titleSmall,
            )
            design.stages.forEach { StageLine(it) }
        }
    }
}

@Composable
private fun DesignLine(label: String, value: String, emphasised: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (emphasised) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
        )
    }
}

/** One stage, with the figure it produced and the limit it was judged against. */
@Composable
private fun StageLine(stage: DesignStage) {
    val spacing = ElecTheme.spacing
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (stage.passes) ElecIcons.StagePass else ElecIcons.StageFail,
            contentDescription = null,
            tint = if (stage.passes) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        Text(
            text = stringResource(stage.constraint.stageLabelRes()),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${stage.value.format()} / ${stage.limit.format()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The stage's own wording, which names the comparison rather than the verdict. */
private fun BindingConstraint.stageLabelRes(): Int = when (this) {
    BindingConstraint.CURRENT_CAPACITY -> R.string.circuit_stage_capacity
    BindingConstraint.VOLTAGE_DROP -> R.string.circuit_stage_drop
    BindingConstraint.EARTH_FAULT_LOOP -> R.string.circuit_stage_loop
    BindingConstraint.PROTECTIVE_CONDUCTOR -> R.string.circuit_stage_protective
    BindingConstraint.NONE -> R.string.circuit_binding_none
}

private fun String.toDeviceTypeOrDefault(): ProtectiveDeviceType =
    ProtectiveDeviceType.entries.firstOrNull { it.name == this } ?: ProtectiveDeviceType.MCB_TYPE_B

/**
 * What was measured, beside what the design expected.
 *
 * Every test kind is listed whether or not it has a reading. A card showing
 * only what had been entered would make an untested circuit look finished; the
 * empty rows are the checklist an inspector works down.
 */
@Composable
private fun TestsCard(
    rows: kotlinx.collections.immutable.ImmutableList<TestRow>,
    onValueChange: (TestKind, String) -> Unit,
    onPolarityChange: (Boolean?) -> Unit,
    onInsulationVoltageChange: (InsulationTestVoltage) -> Unit,
    onRcdTypeChange: (RcdType) -> Unit,
) {
    val spacing = ElecTheme.spacing

    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text(
                text = stringResource(R.string.tests_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            rows.forEach { row ->
                when (row.test.kind) {
                    TestKind.POLARITY -> ElecOptionSelector(
                        label = stringResource(row.test.kind.labelRes()),
                        options = POLARITY_OPTIONS,
                        selected = row.test.passed,
                        onSelect = onPolarityChange,
                        optionLabel = {
                            stringResource(
                                if (it == true) {
                                    R.string.tests_polarity_correct
                                } else {
                                    R.string.tests_polarity_wrong
                                },
                            )
                        },
                    )

                    else -> {
                        ElecNumericField(
                            value = row.test.value,
                            onValueChange = { onValueChange(row.test.kind, it) },
                            label = stringResource(row.test.kind.labelRes()),
                            unit = row.test.kind.unit(),
                            supportingText = row.supportingText(),
                        )
                        if (row.test.kind == TestKind.INSULATION) {
                            ElecOptionSelector(
                                label = stringResource(R.string.tests_test_voltage),
                                options = InsulationTestVoltage.entries.toImmutableList(),
                                selected = row.test.insulationVoltage,
                                onSelect = onInsulationVoltageChange,
                                optionLabel = { "${it.volts} V" },
                            )
                        }
                        if (row.test.kind == TestKind.RCD_AT_RATED) {
                            ElecOptionSelector(
                                label = stringResource(R.string.tests_rcd_type),
                                options = RcdType.entries.toImmutableList(),
                                selected = row.test.rcdType,
                                onSelect = onRcdTypeChange,
                                optionLabel = { stringResource(it.labelRes()) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The line under a reading: the verdict, and what the design predicted.
 *
 * The predicted figure is the reason this feature exists, so it is shown
 * whenever there is one — including on readings that passed.
 */
@Composable
private fun TestRow.supportingText(): String {
    val verdict = stringResource(evaluation.verdict.labelRes())
    val expected = evaluation.expected ?: return verdict
    val predicted = stringResource(R.string.tests_expected, expected.format())
    val ratio = evaluation.ratio?.let { stringResource(R.string.tests_ratio, it.format()) }
    return listOfNotNull(verdict, predicted, ratio).joinToString(" · ")
}

/** Correct, or wrong. Null is "not yet measured" and is not offered as a choice. */
private val POLARITY_OPTIONS =
    kotlinx.collections.immutable.persistentListOf<Boolean?>(true, false)
