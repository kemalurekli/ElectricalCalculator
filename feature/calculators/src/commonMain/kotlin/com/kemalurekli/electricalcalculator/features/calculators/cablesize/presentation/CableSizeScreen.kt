package com.kemalurekli.electricalcalculator.features.calculators.cablesize.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.features.calculators.presentation.NameplateScanAction
import com.kemalurekli.electricalcalculator.core.vision.NameplateReading
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.ElecTestTags
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecFormulaCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNotesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultActions
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecStepsCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.FormulaVariable
import com.kemalurekli.electricalcalculator.core.designsystem.component.NoteLink
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultTone
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CableSizeResult
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.GoverningConstraint
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_cable_size_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_conductor_material
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_design_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_material_aluminium
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_material_copper
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_parallel_conductors
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_power_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_route_length
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_route_length_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_supply_system
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_dc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_single_phase
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_three_phase
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_ambient_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_ambient_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_circuits_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_circuits_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_export_by_capacity
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_export_by_drop
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_export_conditions
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_export_drop
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_export_environment
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_export_load
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_export_result
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_installation_method
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation_pvc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation_xlpe
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_max_drop_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_max_drop_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_method_b1
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_method_b1_full
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_method_b2
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_method_b2_full
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_method_c
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_method_c_full
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_method_e
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_method_e_full
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_no_solution_message
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_no_solution_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_note_derating
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_note_protection
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_note_tables
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_note_two_constraints
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_note_worst_case
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_result_ambient_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_result_by_capacity
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_result_by_drop
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_result_derated_capacity
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_result_grouping_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_result_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_result_required_capacity
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_result_voltage_drop
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_status_both
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_status_capacity
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_status_drop
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_var_area
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_var_ca
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_var_cg
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_var_dumax
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_var_ib
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_var_iz
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_length
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_parallel
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_power_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_resistivity
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun CableSizeRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: CableSizeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_cable_size_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result
        ?.takeIf { it.hasSolution }
        ?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

    CableSizeScreen(
        uiState = uiState,
        onSystemChange = viewModel::onSystemChange,
        onMaterialChange = viewModel::onMaterialChange,
        onInsulationChange = viewModel::onInsulationChange,
        onMethodChange = viewModel::onMethodChange,
        onVoltageChange = viewModel::onVoltageChange,
        onCurrentChange = viewModel::onCurrentChange,
        onLengthChange = viewModel::onLengthChange,
        onPowerFactorChange = viewModel::onPowerFactorChange,
        onMaxDropChange = viewModel::onMaxDropChange,
        onAmbientChange = viewModel::onAmbientChange,
        onCircuitsChange = viewModel::onCircuitsChange,
        onParallelConductorsChange = viewModel::onParallelConductorsChange,
        onCalculate = viewModel::onCalculate,
        onApplyExample = viewModel::onApplyExample,
        onReset = viewModel::onReset,
        onToggleFavorite = viewModel::onToggleFavorite,
        onCopy = {
            summary?.let {
                if (sharing.copy(title, it)) {
                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                }
            }
        },
        onShare = { summary?.let { sharing.share(shareSubject, it) } },
        // The load half of a long form. Length, cross-section, installation
        // method and ambient are facts about the run, and no plate knows them.
        onNameplate = { plate ->
            plate.phases?.let {
                viewModel.onSystemChange(
                    if (it == 1) SupplySystem.SINGLE_PHASE_AC else SupplySystem.THREE_PHASE_AC,
                )
            }
            plate.voltageVolts?.let {
                viewModel.onVoltageChange(NumberFormatter.formatSignificant(it))
            }
            plate.currentAmperes?.let {
                viewModel.onCurrentChange(NumberFormatter.formatSignificant(it))
            }
            plate.powerFactor?.let {
                viewModel.onPowerFactorChange(NumberFormatter.formatSignificant(it))
            }
        },
        onExportPdf = summary?.let { text -> { export.export(title, text) } },
        exportLocked = !export.isPro,
        onReferenceClick = onReferenceClick,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CableSizeScreen(
    uiState: CableSizeUiState,
    onSystemChange: (SupplySystem) -> Unit,
    onMaterialChange: (ConductorMaterial) -> Unit,
    onInsulationChange: (CableInsulation) -> Unit,
    onMethodChange: (InstallationMethod) -> Unit,
    onVoltageChange: (String) -> Unit,
    onCurrentChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onPowerFactorChange: (String) -> Unit,
    onMaxDropChange: (String) -> Unit,
    onAmbientChange: (String) -> Unit,
    onCircuitsChange: (String) -> Unit,
    onParallelConductorsChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<CableSizeUiState>) -> Unit,
    onReset: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onNameplate: (NameplateReading) -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = rememberElecScrollBehavior()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.result) {
        if (uiState.result != null) listState.animateScrollToItem(0)
    }

    ElecScreenScaffold(
        title = stringResource(Res.string.calculator_cable_size_title),
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
                    tint = if (uiState.isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        },
        scrollBehavior = scrollBehavior,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag(ElecTestTags.CALCULATOR_FORM),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                bottom = spacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            uiState.result?.let { result ->
                item(key = "result") {
                    if (result.hasSolution) {
                        ResultSection(result, uiState, onCopy, onShare, onExportPdf, exportLocked)
                    } else {
                        ElecEmptyState(
                            title = stringResource(Res.string.cs_no_solution_title),
                            message = stringResource(Res.string.cs_no_solution_message),
                            icon = ElecIcons.Calculators,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item(key = "examples") {
                ElecExamplesCard(
                    examples = cableSizeExamples,
                    onSelect = onApplyExample,
                    hasResult = uiState.result != null,
                )
            }

            item(key = "inputs-header") {
                ElecSectionHeader(
                    title = stringResource(Res.string.calculator_inputs),
                    modifier = Modifier.padding(horizontal = 0.dp),
                )
            }

            // Under the heading and above the first field, where
            // somebody who has just walked up to the equipment is
            // looking. Absent on a device with no camera.
            item(key = "nameplate") {
                NameplateScanAction(onApply = onNameplate)
            }

            item(key = "system") {
                ElecOptionSelector(
                    label = stringResource(Res.string.common_supply_system),
                    options = SupplySystem.entries.toImmutableList(),
                    selected = uiState.system,
                    onSelect = onSystemChange,
                    optionLabel = { stringResource(it.label()) },
                )
            }

            item(key = "material") {
                ElecOptionSelector(
                    label = stringResource(Res.string.common_conductor_material),
                    options = ConductorMaterial.entries.toImmutableList(),
                    selected = uiState.material,
                    onSelect = onMaterialChange,
                    optionLabel = { stringResource(it.label()) },
                )
            }

            item(key = "insulation") {
                ElecOptionSelector(
                    label = stringResource(Res.string.cs_insulation),
                    options = CableInsulation.entries.toImmutableList(),
                    selected = uiState.insulation,
                    onSelect = onInsulationChange,
                    optionLabel = { stringResource(it.label()) },
                )
            }

            item(key = "method") {
                Column {
                    ElecOptionSelector(
                        label = stringResource(Res.string.cs_installation_method),
                        options = InstallationMethod.entries.toImmutableList(),
                        selected = uiState.method,
                        onSelect = onMethodChange,
                        optionLabel = { stringResource(it.shortLabel()) },
                    )
                    // The segmented buttons only have room for the code, so
                    // the selected method is spelled out beneath them.
                    Text(
                        text = stringResource(uiState.method.fullLabel()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.xs, start = spacing.xs),
                    )
                }
            }

            item(key = "voltage") {
                ElecNumericField(
                    value = uiState.voltage,
                    onValueChange = onVoltageChange,
                    label = stringResource(Res.string.common_system_voltage),
                    unit = "V",
                    error = uiState.errors[CableSizeField.VOLTAGE],
                    supportingText = if (uiState.system == SupplySystem.THREE_PHASE_AC) {
                        stringResource(Res.string.common_system_voltage_hint)
                    } else {
                        null
                    },
                )
            }

            item(key = "current") {
                ElecNumericField(
                    value = uiState.current,
                    onValueChange = onCurrentChange,
                    label = stringResource(Res.string.common_design_current),
                    unit = "A",
                    error = uiState.errors[CableSizeField.CURRENT],
                )
            }

            item(key = "length") {
                ElecNumericField(
                    value = uiState.length,
                    onValueChange = onLengthChange,
                    label = stringResource(Res.string.common_route_length),
                    unit = "m",
                    error = uiState.errors[CableSizeField.LENGTH],
                    supportingText = stringResource(Res.string.common_route_length_hint),
                )
            }

            if (uiState.showPowerFactor) {
                item(key = "power-factor") {
                    ElecNumericField(
                        value = uiState.powerFactor,
                        onValueChange = onPowerFactorChange,
                        label = stringResource(Res.string.common_power_factor),
                        error = uiState.errors[CableSizeField.POWER_FACTOR],
                    )
                }
            }

            item(key = "max-drop") {
                ElecNumericField(
                    value = uiState.maxDropPercent,
                    onValueChange = onMaxDropChange,
                    label = stringResource(Res.string.cs_max_drop_label),
                    unit = "%",
                    error = uiState.errors[CableSizeField.MAX_DROP],
                    supportingText = stringResource(Res.string.cs_max_drop_hint),
                )
            }

            item(key = "ambient") {
                ElecNumericField(
                    value = uiState.ambientTemperature,
                    onValueChange = onAmbientChange,
                    label = stringResource(Res.string.cs_ambient_label),
                    unit = "°C",
                    error = uiState.errors[CableSizeField.AMBIENT],
                    supportingText = stringResource(Res.string.cs_ambient_hint),
                    allowNegative = true,
                )
            }

            item(key = "circuits") {
                ElecNumericField(
                    value = uiState.groupedCircuits,
                    onValueChange = onCircuitsChange,
                    label = stringResource(Res.string.cs_circuits_label),
                    error = uiState.errors[CableSizeField.CIRCUITS],
                    supportingText = stringResource(Res.string.cs_circuits_hint),
                )
            }

            item(key = "parallel") {
                ElecNumericField(
                    value = uiState.parallelConductors,
                    onValueChange = onParallelConductorsChange,
                    label = stringResource(Res.string.common_parallel_conductors),
                    error = uiState.errors[CableSizeField.PARALLEL_CONDUCTORS],
                    imeAction = ImeAction.Done,
                )
            }

            item(key = "actions") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    OutlinedButton(onClick = onReset) {
                        Text(text = stringResource(Res.string.action_reset))
                    }
                    Button(onClick = onCalculate, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(Res.string.action_calculate))
                    }
                }
            }

            if (uiState.steps.isNotEmpty()) {
                item(key = "steps") { ElecStepsCard(steps = uiState.steps) }
            }

            item(key = "formula") {
                ElecFormulaCard(
                    title = stringResource(Res.string.calculator_formula),
                    formula = stringResource(Res.string.cs_formula),
                    variables = persistentListOf(
                        FormulaVariable("I_z", stringResource(Res.string.cs_var_iz), "A"),
                        FormulaVariable("I_b", stringResource(Res.string.cs_var_ib), "A"),
                        FormulaVariable("Ca", stringResource(Res.string.cs_var_ca), "—"),
                        FormulaVariable("Cg", stringResource(Res.string.cs_var_cg), "—"),
                        FormulaVariable("A", stringResource(Res.string.cs_var_area), "mm²"),
                        FormulaVariable("ΔU_max", stringResource(Res.string.cs_var_dumax), "V"),
                        // The second line is the voltage-drop relation, and its symbols are
                        // explained in the words the voltage-drop card already uses. One
                        // meaning, one wording, in all twelve languages.
                        FormulaVariable("k", stringResource(Res.string.vd_var_k), "—"),
                        FormulaVariable("ρ(θ)", stringResource(Res.string.vd_var_resistivity), "Ω·mm²/m"),
                        FormulaVariable("L", stringResource(Res.string.vd_var_length), "m"),
                        FormulaVariable("n", stringResource(Res.string.vd_var_parallel), "—"),
                        FormulaVariable("cos φ", stringResource(Res.string.vd_var_power_factor), "—"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(Res.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(Res.string.cs_note_two_constraints),
                        stringResource(Res.string.cs_note_derating),
                        stringResource(Res.string.cs_note_tables),
                        stringResource(Res.string.cs_note_worst_case),
                        stringResource(Res.string.cs_note_protection),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "rating_series",
                            label = stringResource(ReferenceCatalog.titleOf("rating_series")),
                        ),
                        NoteLink(
                            topicKey = "primer_cableanatomy",
                            label = stringResource(ReferenceCatalog.titleOf("primer_cableanatomy")),
                        ),
                        NoteLink(
                            topicKey = "selection_insulation",
                            label = stringResource(ReferenceCatalog.titleOf("selection_insulation")),
                        ),
                        NoteLink(
                            topicKey = "selection_cabletype",
                            label = stringResource(ReferenceCatalog.titleOf("selection_cabletype")),
                        ),
                    ),
                    onLinkClick = onReferenceClick,
                )
            }
        }
    }
}

@Composable
private fun ResultSection(
    result: CableSizeResult,
    uiState: CableSizeUiState,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(Res.string.cs_result_label),
            value = NumberFormatter.formatSignificant(result.recommendedAreaMm2 ?: 0.0),
            unit = "mm²",
            // Neutral rather than pass/fail: the size *is* the answer, and the
            // governing constraint is information, not a warning.
            tone = ResultTone.SUCCESS,
            statusMessage = stringResource(result.governingConstraint.message()),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(Res.string.cs_result_by_capacity),
                    value = NumberFormatter.formatSignificant(result.currentCapacityAreaMm2 ?: 0.0),
                    unit = "mm²",
                ),
                ResultRow(
                    label = stringResource(Res.string.cs_result_by_drop),
                    value = NumberFormatter.formatSignificant(result.voltageDropAreaMm2 ?: 0.0),
                    unit = "mm²",
                ),
                ResultRow(
                    label = stringResource(Res.string.cs_result_required_capacity),
                    value = NumberFormatter.format(result.requiredCapacityAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(Res.string.cs_result_derated_capacity),
                    value = NumberFormatter.format(result.deratedCapacityAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(Res.string.cs_result_voltage_drop),
                    value = NumberFormatter.format(result.voltageDropVolts, DISPLAY_DECIMALS),
                    unit = "V",
                ),
                ResultRow(
                    label = stringResource(Res.string.cs_result_ambient_factor),
                    value = NumberFormatter.formatSignificant(result.ambientFactor),
                    unit = "",
                ),
                ResultRow(
                    label = stringResource(Res.string.cs_result_grouping_factor),
                    value = NumberFormatter.formatSignificant(result.groupingFactor),
                    unit = "",
                ),
            ),
        )
        ElecResultActions(
            onCopy = onCopy,
            onShare = onShare,
            onExportPdf = onExportPdf,
            exportLocked = exportLocked,
        )
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: CableSizeUiState,
    result: CableSizeResult,
): String {
    val conditions = stringResource(
        Res.string.cs_export_conditions,
        stringResource(uiState.system.label()),
        stringResource(uiState.material.label()),
        stringResource(uiState.insulation.label()),
    )
    val load = stringResource(
        Res.string.cs_export_load,
        uiState.voltage,
        uiState.current,
        uiState.length,
    )
    val environment = stringResource(
        Res.string.cs_export_environment,
        uiState.ambientTemperature,
        uiState.groupedCircuits,
        uiState.maxDropPercent,
    )
    val method = stringResource(uiState.method.fullLabel())
    val recommended = stringResource(
        Res.string.cs_export_result,
        NumberFormatter.formatSignificant(result.recommendedAreaMm2 ?: 0.0),
    )
    val byCapacity = stringResource(
        Res.string.cs_export_by_capacity,
        NumberFormatter.formatSignificant(result.currentCapacityAreaMm2 ?: 0.0),
    )
    val byDrop = stringResource(
        Res.string.cs_export_by_drop,
        NumberFormatter.formatSignificant(result.voltageDropAreaMm2 ?: 0.0),
    )
    val drop = stringResource(
        Res.string.cs_export_drop,
        NumberFormatter.format(result.voltageDropVolts, DISPLAY_DECIMALS),
        NumberFormatter.format(result.voltageDropPercent, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(conditions)
        appendLine(method)
        appendLine(load)
        appendLine(environment)
        appendLine(EXPORT_SEPARATOR)
        appendLine(recommended)
        appendLine(byCapacity)
        appendLine(byDrop)
        append(drop)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val EXPORT_SEPARATOR = "— — —"

private fun SupplySystem.label(): StringResource = when (this) {
    SupplySystem.DC -> Res.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> Res.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> Res.string.common_system_three_phase
}

private fun ConductorMaterial.label(): StringResource = when (this) {
    ConductorMaterial.COPPER -> Res.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> Res.string.common_material_aluminium
}

private fun CableInsulation.label(): StringResource = when (this) {
    CableInsulation.PVC -> Res.string.cs_insulation_pvc
    CableInsulation.XLPE -> Res.string.cs_insulation_xlpe
}

private fun InstallationMethod.shortLabel(): StringResource = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> Res.string.cs_method_b1
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> Res.string.cs_method_b2
    InstallationMethod.C_CLIPPED_DIRECT -> Res.string.cs_method_c
    InstallationMethod.E_FREE_AIR -> Res.string.cs_method_e
}

private fun InstallationMethod.fullLabel(): StringResource = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> Res.string.cs_method_b1_full
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> Res.string.cs_method_b2_full
    InstallationMethod.C_CLIPPED_DIRECT -> Res.string.cs_method_c_full
    InstallationMethod.E_FREE_AIR -> Res.string.cs_method_e_full
}

private fun GoverningConstraint.message(): StringResource = when (this) {
    GoverningConstraint.CURRENT_CAPACITY -> Res.string.cs_status_capacity
    GoverningConstraint.VOLTAGE_DROP -> Res.string.cs_status_drop
    GoverningConstraint.BOTH -> Res.string.cs_status_both
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun CableSizeScreenPreview() {
    ElecToolkitTheme {
        CableSizeScreen(
            uiState = CableSizeUiState(voltage = "230", current = "25", length = "30"),
            onSystemChange = {}, onMaterialChange = {}, onInsulationChange = {},
            onMethodChange = {}, onVoltageChange = {}, onCurrentChange = {},
            onLengthChange = {}, onPowerFactorChange = {}, onMaxDropChange = {},
            onAmbientChange = {}, onCircuitsChange = {}, onParallelConductorsChange = {},
            onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {}, onToggleFavorite = {}, onCopy = {},
            onShare = {}, onNameplate = {}, onExportPdf = null, exportLocked = false, onNavigateBack = {},
        )
    }
}
