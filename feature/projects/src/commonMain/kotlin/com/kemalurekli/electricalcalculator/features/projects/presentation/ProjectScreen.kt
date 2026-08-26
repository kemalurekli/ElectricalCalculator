package com.kemalurekli.electricalcalculator.features.projects.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.ColumnScope
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecSpacing
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEditableTitle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberFileSharing
import com.kemalurekli.electricalcalculator.core.designsystem.platform.safeFileName
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.action_cancel
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.action_delete
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.circuit_untitled
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.common_supply_system
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.common_system_voltage
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_add_circuit
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_circuit_incomplete
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_circuit_no_solution
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_circuit_summary
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_external_impedance
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_external_impedance_hint
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_max_drop
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_reference
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_schedule
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_site
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_cable_section
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.project_supply
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_circuit_count_none
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_delete_message
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_delete_title
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_untitled
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_export
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_export_csv
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_export_empty
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_export_failed
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_export_pdf
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.report_share_title
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.settings_engineering_ambient
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.settings_engineering_insulation
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.settings_engineering_material
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.settings_engineering_method

@Composable
fun ProjectRoute(
    projectId: Long,
    onOpenCircuit: (Long, Long) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: ProjectViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val created by viewModel.created.collectAsStateWithLifecycle()
    val fileSharing = rememberFileSharing()
    val emptyMessage = stringResource(Res.string.report_export_empty)
    val failedMessage = stringResource(Res.string.report_export_failed)
    val chooserTitle = stringResource(Res.string.report_share_title)
    val fallbackName = stringResource(Res.string.projects_untitled)
    var exportMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(projectId) { viewModel.onOpen(projectId) }

    // Deleting the job leaves nothing to show; going back is the only sensible
    // destination, and staying on an empty screen would look like a failure.
    LaunchedEffect(uiState.isGone) { if (uiState.isGone) onNavigateBack?.invoke() }

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
        onExportCsv = {
            val export = viewModel.exportCsv()
            exportMessage = when {
                export == null -> emptyMessage
                !fileSharing.share(
                    fileName = "${safeFileName(export.reference, fallbackName)}.csv",
                    mimeType = "text/csv",
                    bytes = export.csv.encodeToByteArray(),
                    chooserTitle = chooserTitle,
                ) -> failedMessage

                else -> null
            }
        },
        onExportPdf = {
            val export = viewModel.exportDocument()
            exportMessage = when {
                export == null -> emptyMessage
                !fileSharing.share(
                    fileName = "${safeFileName(export.reference, fallbackName)}.pdf",
                    mimeType = "application/pdf",
                    bytes = renderSchedulePdf(export.report),
                    chooserTitle = chooserTitle,
                ) -> failedMessage

                else -> null
            }
        },
        exportMessage = exportMessage,
        onExportMessageShown = { exportMessage = null },
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
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    exportMessage: String?,
    onExportMessageShown: () -> Unit,
    onOpenCircuit: (Long) -> Unit,
    onDeleteProject: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var exportMenuOpen by remember { mutableStateOf(false) }
    val project = uiState.project

    val snackbarHostState = remember { SnackbarHostState() }

    // Failure is reported, never swallowed: an export that produced nothing and
    // said nothing looks exactly like one the share sheet was dismissed from.
    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            onExportMessageShown()
        }
    }

    ElecScreenScaffold(
        title = stringResource(Res.string.projects_untitled),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        // The name is the bar, and the bar is where it is edited. It used to be
        // a field inside a card while the bar above showed the placeholder for
        // it — one fact in two places, and the readable one was not the one you
        // could change.
        titleContent = project?.let {
            {
                ElecEditableTitle(
                    value = it.reference,
                    onValueChange = onReferenceChange,
                    placeholder = stringResource(Res.string.projects_untitled),
                    label = stringResource(Res.string.project_reference),
                )
            }
        },
        actions = {
            // A menu rather than two icons: the choice is between two
            // formats of one action, and two share buttons side by side
            // reads as two different things to share.
            Box {
                IconButton(onClick = { exportMenuOpen = true }) {
                    Icon(
                        imageVector = ElecIcons.Share,
                        contentDescription = stringResource(Res.string.report_export),
                    )
                }
                DropdownMenu(
                    expanded = exportMenuOpen,
                    onDismissRequest = { exportMenuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.report_export_csv)) },
                        onClick = {
                            exportMenuOpen = false
                            onExportCsv()
                        },
                    )
                    // Absent rather than disabled where the platform cannot
                    // draw one — see `isSchedulePdfSupported`. A greyed-out
                    // item invites the reader to work out what unlocks it,
                    // and nothing does.
                    if (isSchedulePdfSupported) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.report_export_pdf)) },
                            onClick = {
                                exportMenuOpen = false
                                onExportPdf()
                            },
                        )
                    }
                }
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(
                    imageVector = ElecIcons.Delete,
                    contentDescription = stringResource(Res.string.action_delete),
                )
            }
        },
        scrollBehavior = scrollBehavior,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            if (project != null) {
                ExtendedFloatingActionButton(
                    onClick = onAddCircuit,
                    icon = { Icon(ElecIcons.Add, contentDescription = null) },
                    text = { Text(stringResource(Res.string.project_add_circuit)) },
                )
            }
        },
    ) { innerPadding ->
        if (project == null) return@ElecScreenScaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = spacing.sm, bottom = spacing.fabClearance),
        ) {
            // Eleven controls used to be one undivided card. They are three
            // separate decisions — where the job is, what feeds it, and what
            // the cable is — and a reader looking for the ambient temperature
            // had to read every label to find out it was not the voltage.
            // No heading over this one. It holds a single field that already
            // carries its own label, and "Site" above "Site" is a stutter.
            item(key = "site") {
                ProjectCard(spacing) {
                    OutlinedTextField(
                        value = project.site,
                        onValueChange = onSiteChange,
                        label = { Text(stringResource(Res.string.project_site)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item(key = "supply-header") {
                ElecSectionHeader(title = stringResource(Res.string.project_supply))
            }

            item(key = "supply") {
                ProjectCard(spacing) {
                    ElecOptionSelector(
                        label = stringResource(Res.string.common_supply_system),
                        options = SupplySystem.entries.toImmutableList(),
                        selected = project.system,
                        onSelect = onSystemChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    ElecNumericField(
                        value = project.systemVoltage,
                        onValueChange = onVoltageChange,
                        label = stringResource(Res.string.common_system_voltage),
                        unit = "V",
                    )
                    ElecNumericField(
                        value = project.externalImpedanceOhms,
                        onValueChange = onExternalImpedanceChange,
                        label = stringResource(Res.string.project_external_impedance),
                        unit = "Ω",
                        supportingText = stringResource(Res.string.project_external_impedance_hint),
                    )
                }
            }

            item(key = "cable-header") {
                ElecSectionHeader(title = stringResource(Res.string.project_cable_section))
            }

            item(key = "cable") {
                ProjectCard(spacing) {
                    ElecOptionSelector(
                        label = stringResource(Res.string.settings_engineering_material),
                        options = ConductorMaterial.entries.toImmutableList(),
                        selected = project.material,
                        onSelect = onMaterialChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    ElecOptionSelector(
                        label = stringResource(Res.string.settings_engineering_insulation),
                        options = CableInsulation.entries.toImmutableList(),
                        selected = project.insulation,
                        onSelect = onInsulationChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    Column {
                        ElecOptionSelector(
                            label = stringResource(Res.string.settings_engineering_method),
                            options = InstallationMethod.entries.toImmutableList(),
                            selected = project.method,
                            onSelect = onMethodChange,
                            optionLabel = { stringResource(it.label()) },
                        )
                        // The buttons carry the code alone, which means
                        // nothing to a reader who does not already know the
                        // table, so the chosen method is spelled out.
                        Text(
                            text = stringResource(project.method.fullLabel()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = spacing.xs, start = spacing.xs),
                        )
                    }
                    ElecNumericField(
                        value = project.ambientTemperatureC,
                        onValueChange = onAmbientChange,
                        label = stringResource(Res.string.settings_engineering_ambient),
                        unit = "°C",
                        allowNegative = true,
                    )
                    ElecNumericField(
                        value = project.maxVoltageDropPercent,
                        onValueChange = onMaxDropChange,
                        label = stringResource(Res.string.project_max_drop),
                        unit = "%",
                    )
                }
            }

            item(key = "schedule-header") {
                ElecSectionHeader(title = stringResource(Res.string.project_schedule))
            }

            if (uiState.rows.isEmpty()) {
                item(key = "schedule-empty") {
                    Text(
                        text = stringResource(Res.string.projects_circuit_count_none),
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
            title = { Text(stringResource(Res.string.projects_delete_title)) },
            text = { Text(stringResource(Res.string.projects_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDeleteProject()
                    },
                ) {
                    Text(stringResource(Res.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(Res.string.action_cancel))
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
/** One group of the project form, in the app's card. */
@Composable
private fun ProjectCard(
    spacing: ElecSpacing,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
            content = content,
        )
    }
}

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
                text = row.circuit.name.ifBlank { stringResource(Res.string.circuit_untitled) },
                style = MaterialTheme.typography.titleMedium,
            )
            when {
                design == null -> Text(
                    text = stringResource(Res.string.project_circuit_incomplete),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                !design.hasSolution -> Text(
                    text = stringResource(Res.string.project_circuit_no_solution),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )

                else -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(
                            Res.string.project_circuit_summary,
                            design.crossSectionMm2.format(),
                            design.deviceRatingAmps.format(),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(design.bindingConstraint.label()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
