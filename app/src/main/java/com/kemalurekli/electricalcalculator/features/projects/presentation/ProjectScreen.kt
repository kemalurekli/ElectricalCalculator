package com.kemalurekli.electricalcalculator.features.projects.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ProjectRoute(
    projectId: Long,
    onOpenCircuit: (Long, Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val created by viewModel.created.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) { viewModel.onOpen(projectId) }

    // Deleting the job leaves nothing to show; going back is the only sensible
    // destination, and staying on an empty screen would look like a failure.
    LaunchedEffect(uiState.isGone) { if (uiState.isGone) onNavigateBack() }

    LaunchedEffect(created) {
        created?.let {
            viewModel.onCreatedHandled()
            onOpenCircuit(projectId, it)
        }
    }

    ProjectScreen(
        uiState = uiState,
        onReferenceChange = viewModel::onReferenceChange,
        onSiteChange = viewModel::onSiteChange,
        onSystemChange = viewModel::onSystemChange,
        onVoltageChange = viewModel::onVoltageChange,
        onMaterialChange = viewModel::onMaterialChange,
        onInsulationChange = viewModel::onInsulationChange,
        onMethodChange = viewModel::onMethodChange,
        onAmbientChange = viewModel::onAmbientChange,
        onMaxDropChange = viewModel::onMaxDropChange,
        onExternalImpedanceChange = viewModel::onExternalImpedanceChange,
        onAddCircuit = viewModel::onAddCircuit,
        onOpenCircuit = { onOpenCircuit(projectId, it) },
        onDeleteProject = viewModel::onDeleteProject,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * The job: one supply at the top, the circuits it feeds underneath.
 *
 * The supply sits above the schedule rather than behind a settings icon because
 * it is the thing that explains the sizes below it. A reader looking at a
 * 16 mm² answer and wondering why should be able to see the 45 °C ambient
 * without leaving the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(
    uiState: ProjectUiState,
    onReferenceChange: (String) -> Unit,
    onSiteChange: (String) -> Unit,
    onSystemChange: (SupplySystem) -> Unit,
    onVoltageChange: (String) -> Unit,
    onMaterialChange: (ConductorMaterial) -> Unit,
    onInsulationChange: (CableInsulation) -> Unit,
    onMethodChange: (InstallationMethod) -> Unit,
    onAmbientChange: (String) -> Unit,
    onMaxDropChange: (String) -> Unit,
    onExternalImpedanceChange: (String) -> Unit,
    onAddCircuit: () -> Unit,
    onOpenCircuit: (Long) -> Unit,
    onDeleteProject: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val project = uiState.project

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ElecTopAppBar(
                title = project?.reference?.ifBlank { null }
                    ?: stringResource(R.string.projects_untitled),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(
                            imageVector = ElecIcons.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (project != null) {
                ExtendedFloatingActionButton(
                    onClick = onAddCircuit,
                    icon = { Icon(ElecIcons.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.project_add_circuit)) },
                )
            }
        },
    ) { innerPadding ->
        if (project == null) return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item(key = "supply-header") {
                ElecSectionHeader(title = stringResource(R.string.project_supply))
            }

            item(key = "supply") {
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
                            value = project.reference,
                            onValueChange = onReferenceChange,
                            label = { Text(stringResource(R.string.project_reference)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = project.site,
                            onValueChange = onSiteChange,
                            label = { Text(stringResource(R.string.project_site)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        ElecOptionSelector(
                            label = stringResource(R.string.common_supply_system),
                            options = SupplySystem.entries.toImmutableList(),
                            selected = project.system,
                            onSelect = onSystemChange,
                            optionLabel = { stringResource(it.labelRes()) },
                        )
                        ElecNumericField(
                            value = project.systemVoltage,
                            onValueChange = onVoltageChange,
                            label = stringResource(R.string.common_system_voltage),
                            unit = "V",
                        )
                        ElecOptionSelector(
                            label = stringResource(R.string.settings_engineering_material),
                            options = ConductorMaterial.entries.toImmutableList(),
                            selected = project.material,
                            onSelect = onMaterialChange,
                            optionLabel = { stringResource(it.labelRes()) },
                        )
                        ElecOptionSelector(
                            label = stringResource(R.string.settings_engineering_insulation),
                            options = CableInsulation.entries.toImmutableList(),
                            selected = project.insulation,
                            onSelect = onInsulationChange,
                            optionLabel = { stringResource(it.labelRes()) },
                        )
                        ElecOptionSelector(
                            label = stringResource(R.string.settings_engineering_method),
                            options = InstallationMethod.entries.toImmutableList(),
                            selected = project.method,
                            onSelect = onMethodChange,
                            optionLabel = { stringResource(it.labelRes()) },
                        )
                        ElecNumericField(
                            value = project.ambientTemperatureC,
                            onValueChange = onAmbientChange,
                            label = stringResource(R.string.settings_engineering_ambient),
                            unit = "°C",
                            allowNegative = true,
                        )
                        ElecNumericField(
                            value = project.maxVoltageDropPercent,
                            onValueChange = onMaxDropChange,
                            label = stringResource(R.string.project_max_drop),
                            unit = "%",
                        )
                        ElecNumericField(
                            value = project.externalImpedanceOhms,
                            onValueChange = onExternalImpedanceChange,
                            label = stringResource(R.string.project_external_impedance),
                            unit = "Ω",
                            supportingText = stringResource(R.string.project_external_impedance_hint),
                        )
                    }
                }
            }

            item(key = "schedule-header") {
                ElecSectionHeader(title = stringResource(R.string.project_schedule))
            }

            if (uiState.rows.isEmpty()) {
                item(key = "schedule-empty") {
                    Text(
                        text = stringResource(R.string.projects_circuit_count_none),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.lg),
                    )
                }
            }

            items(uiState.rows, key = { it.circuit.id }) { row ->
                CircuitScheduleRow(row = row, onClick = { onOpenCircuit(row.circuit.id) })
            }

            // Clears the floating button, which would otherwise sit on the last row.
            item(key = "fab-space") { Column(Modifier.padding(bottom = spacing.xxl)) {} }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.projects_delete_title)) },
            text = { Text(stringResource(R.string.projects_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDeleteProject()
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
 * One circuit, as a schedule reads it.
 *
 * The binding constraint is on the row rather than only inside the circuit,
 * because a reader scanning a board wants to see at a glance which circuits are
 * tight and why — a column of "voltage drop" against long runs is a finding
 * about the installation, not about any one circuit.
 */
@Composable
private fun CircuitScheduleRow(row: CircuitRow, onClick: () -> Unit) {
    val spacing = ElecTheme.spacing
    val design = row.design

    ElecCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = row.circuit.name.ifBlank { stringResource(R.string.circuit_untitled) },
                style = MaterialTheme.typography.titleMedium,
            )
            when {
                design == null -> Text(
                    text = stringResource(R.string.project_circuit_incomplete),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                !design.hasSolution -> Text(
                    text = stringResource(R.string.project_circuit_no_solution),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )

                else -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(
                            R.string.project_circuit_summary,
                            design.crossSectionMm2.format(),
                            design.deviceRatingAmps.format(),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(design.bindingConstraint.labelRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
