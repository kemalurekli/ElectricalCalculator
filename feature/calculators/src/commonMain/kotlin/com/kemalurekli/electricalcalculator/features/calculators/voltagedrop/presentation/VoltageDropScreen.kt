package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackArea
import com.kemalurekli.electricalcalculator.core.feedback.presentation.reportIssueItem
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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExplainerCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.presentation.rememberDocumentSteps
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.FormulaVariable
import com.kemalurekli.electricalcalculator.core.designsystem.component.NoteLink
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultTone
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropResult
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropStatus
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes_tab
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_voltage_drop_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_conductor_material
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_cross_section
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
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_export_conditions
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_export_drop
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_export_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_export_power_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_export_power_loss
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_export_ratings
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_export_resistance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_export_voltage_at_load
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_note_balanced
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_note_dc_pf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_note_length
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_note_limits
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_note_reactance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_note_temperature
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_result_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_result_power_loss
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_result_resistance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_result_voltage_at_load
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_status_exceeds
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_status_within_lighting
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_status_within_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_temperature_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_temperature_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_area
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_drop
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_length
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_parallel
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_power_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_resistance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_resistivity
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun VoltageDropRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: VoltageDropViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_voltage_drop_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current
    val documentFormula = stringResource(Res.string.vd_formula)
    val documentSteps = rememberDocumentSteps(uiState.steps)

    VoltageDropScreen(
        uiState = uiState,
        onSystemChange = viewModel::onSystemChange,
        onMaterialChange = viewModel::onMaterialChange,
        onVoltageChange = viewModel::onVoltageChange,
        onCurrentChange = viewModel::onCurrentChange,
        onLengthChange = viewModel::onLengthChange,
        onCrossSectionChange = viewModel::onCrossSectionChange,
        onPowerFactorChange = viewModel::onPowerFactorChange,
        onTemperatureChange = viewModel::onTemperatureChange,
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
        // The load half of the form. The run's own facts — length, section,
        // conductor temperature — stay where the reader typed them.
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
        onExportPdf = summary?.let { text -> { export.export(title, documentFormula, text, documentSteps) } },
        exportLocked = !export.isPro,
        onReferenceClick = onReferenceClick,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltageDropScreen(
    uiState: VoltageDropUiState,
    onSystemChange: (SupplySystem) -> Unit,
    onMaterialChange: (ConductorMaterial) -> Unit,
    onVoltageChange: (String) -> Unit,
    onCurrentChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onCrossSectionChange: (String) -> Unit,
    onPowerFactorChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onParallelConductorsChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<VoltageDropUiState>) -> Unit,
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

    // The Calculate button sits below the form, so by the time it is pressed
    // the result — which is inserted at the top — would land off screen. Bring
    // it into view instead of leaving the user to scroll back up hunting for
    // the answer they just asked for.
    LaunchedEffect(uiState.result) {
        if (uiState.result != null) {
            listState.animateScrollToItem(0)
        }
    }

    ElecScreenScaffold(
        title = stringResource(Res.string.calculator_voltage_drop_title),
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
            // The result sits above the form once it exists: after pressing
            // Calculate the user is looking for the number, not the inputs
            // they just finished typing.
            if (uiState.result != null) {
                item(key = "result") {
                    ResultSection(uiState.result, onCopy, onShare, onExportPdf, exportLocked)
                }
            }

            item(key = "examples") {
                ElecExamplesCard(
                    examples = voltageDropExamples,
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

            item(key = "voltage") {
                ElecNumericField(
                    value = uiState.voltage,
                    onValueChange = onVoltageChange,
                    label = stringResource(Res.string.common_system_voltage),
                    unit = "V",
                    error = uiState.errors[VoltageDropField.VOLTAGE],
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
                    error = uiState.errors[VoltageDropField.CURRENT],
                )
            }

            item(key = "length") {
                ElecNumericField(
                    value = uiState.length,
                    onValueChange = onLengthChange,
                    label = stringResource(Res.string.common_route_length),
                    unit = "m",
                    error = uiState.errors[VoltageDropField.LENGTH],
                    supportingText = stringResource(Res.string.common_route_length_hint),
                )
            }

            item(key = "cross-section") {
                ElecNumericField(
                    value = uiState.crossSection,
                    onValueChange = onCrossSectionChange,
                    label = stringResource(Res.string.common_cross_section),
                    unit = "mm²",
                    error = uiState.errors[VoltageDropField.CROSS_SECTION],
                )
            }

            if (uiState.showPowerFactor) {
                item(key = "power-factor") {
                    ElecNumericField(
                        value = uiState.powerFactor,
                        onValueChange = onPowerFactorChange,
                        label = stringResource(Res.string.common_power_factor),
                        error = uiState.errors[VoltageDropField.POWER_FACTOR],
                    )
                }
            }

            item(key = "temperature") {
                ElecNumericField(
                    value = uiState.temperature,
                    onValueChange = onTemperatureChange,
                    label = stringResource(Res.string.vd_temperature_label),
                    unit = "°C",
                    error = uiState.errors[VoltageDropField.TEMPERATURE],
                    supportingText = stringResource(Res.string.vd_temperature_hint),
                    allowNegative = true,
                )
            }

            item(key = "parallel") {
                ElecNumericField(
                    value = uiState.parallelConductors,
                    onValueChange = onParallelConductorsChange,
                    label = stringResource(Res.string.common_parallel_conductors),
                    error = uiState.errors[VoltageDropField.PARALLEL_CONDUCTORS],
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

            item(key = "explainer") {
                ElecExplainerCard(
                    formula = stringResource(Res.string.vd_formula),
                    variables = persistentListOf(
                        FormulaVariable("ΔU", stringResource(Res.string.vd_var_drop), "V"),
                        FormulaVariable("k", stringResource(Res.string.vd_var_k), "—"),
                        FormulaVariable("I", stringResource(Res.string.vd_var_current), "A"),
                        FormulaVariable("R", stringResource(Res.string.vd_var_resistance), "Ω"),
                        FormulaVariable("ρ(θ)", stringResource(Res.string.vd_var_resistivity), "Ω·mm²/m"),
                        FormulaVariable("L", stringResource(Res.string.vd_var_length), "m"),
                        FormulaVariable("A", stringResource(Res.string.vd_var_area), "mm²"),
                        FormulaVariable("n", stringResource(Res.string.vd_var_parallel), "—"),
                        FormulaVariable("cos φ", stringResource(Res.string.vd_var_power_factor), "—"),
                    ),
                    formulaLabel = stringResource(Res.string.calculator_formula),
                    notesLabel = stringResource(Res.string.calculator_notes_tab),
                    steps = uiState.steps,
                    notes = persistentListOf(
                        stringResource(Res.string.vd_note_length),
                        stringResource(Res.string.vd_note_temperature),
                        stringResource(Res.string.vd_note_reactance),
                        stringResource(Res.string.vd_note_limits),
                        stringResource(Res.string.vd_note_balanced),
                        stringResource(Res.string.vd_note_dc_pf),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "materials",
                            label = stringResource(ReferenceCatalog.titleOf("materials")),
                        ),
                        NoteLink(
                            topicKey = "primer_cableanatomy",
                            label = stringResource(ReferenceCatalog.titleOf("primer_cableanatomy")),
                        ),
                        NoteLink(
                            topicKey = "standard_voltages",
                            label = stringResource(ReferenceCatalog.titleOf("standard_voltages")),
                        ),
                    ),
                    onLinkClick = onReferenceClick,
                )
            }

            // After the workings, which is where somebody who has found a
            // mistake ends up.
            reportIssueItem(FeedbackArea.calculator(CalculatorId.VOLTAGE_DROP.key))
        }
    }
}

@Composable
private fun ResultSection(
    result: VoltageDropResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
) {
    val spacing = ElecTheme.spacing
    val percentage = NumberFormatter.format(result.dropPercentage, decimals = 2)
    ElecResultCard(
        label = stringResource(Res.string.vd_result_label),
        value = NumberFormatter.format(result.voltageDrop, decimals = 2),
        unit = "V",
        tone = result.status.tone(),
        statusMessage = stringResource(result.status.message(), percentage),
        secondaryRows = persistentListOf(
            ResultRow(
                label = stringResource(Res.string.vd_result_voltage_at_load),
                value = NumberFormatter.format(result.voltageAtLoad, decimals = 2),
                unit = "V",
            ),
            ResultRow(
                label = stringResource(Res.string.vd_result_resistance),
                value = NumberFormatter.formatSignificant(result.conductorResistance),
                unit = "Ω",
            ),
            ResultRow(
                label = stringResource(Res.string.vd_result_power_loss),
                value = NumberFormatter.format(result.powerLossWatts, decimals = 2),
                unit = "W",
            ),
        ),
        onCopy = onCopy,
        onShare = onShare,
        onExportPdf = onExportPdf,
        exportLocked = exportLocked,
    )
}

/**
 * Plain-text rendering of the result, used for both copy and share.
 *
 * Every label goes through a string resource. Shared text leaves the app and is
 * pasted into reports and messages, so it has to be in the user's language —
 * hard-coding English here would have silently made the export the one part of
 * the app that never translated.
 */
@Composable
private fun rememberShareText(
    title: String,
    uiState: VoltageDropUiState,
    result: VoltageDropResult,
): String {
    val system = stringResource(uiState.system.label())
    val material = stringResource(uiState.material.label())

    val inputs = stringResource(Res.string.vd_export_inputs, system, material)
    val ratings = stringResource(
        Res.string.vd_export_ratings,
        uiState.voltage,
        uiState.current,
        uiState.length,
        uiState.crossSection,
    )
    val powerFactor = stringResource(Res.string.vd_export_power_factor, uiState.powerFactor)
    val conditions = stringResource(
        Res.string.vd_export_conditions,
        uiState.temperature,
        uiState.parallelConductors,
    )
    val drop = stringResource(
        Res.string.vd_export_drop,
        NumberFormatter.format(result.voltageDrop, DISPLAY_DECIMALS),
        NumberFormatter.format(result.dropPercentage, DISPLAY_DECIMALS),
    )
    val atLoad = stringResource(
        Res.string.vd_export_voltage_at_load,
        NumberFormatter.format(result.voltageAtLoad, DISPLAY_DECIMALS),
    )
    val resistance = stringResource(
        Res.string.vd_export_resistance,
        NumberFormatter.formatSignificant(result.conductorResistance),
    )
    val powerLoss = stringResource(
        Res.string.vd_export_power_loss,
        NumberFormatter.format(result.powerLossWatts, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(inputs)
        appendLine(ratings)
        if (uiState.showPowerFactor) appendLine(powerFactor)
        appendLine(conditions)
        appendLine(EXPORT_SEPARATOR)
        appendLine(drop)
        appendLine(atLoad)
        appendLine(resistance)
        append(powerLoss)
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

private fun VoltageDropStatus.tone(): ResultTone = when (this) {
    VoltageDropStatus.WITHIN_LIGHTING_LIMIT -> ResultTone.SUCCESS
    VoltageDropStatus.WITHIN_POWER_LIMIT -> ResultTone.WARNING
    VoltageDropStatus.EXCEEDS_LIMITS -> ResultTone.ERROR
}

private fun VoltageDropStatus.message(): StringResource = when (this) {
    VoltageDropStatus.WITHIN_LIGHTING_LIMIT -> Res.string.vd_status_within_lighting
    VoltageDropStatus.WITHIN_POWER_LIMIT -> Res.string.vd_status_within_power
    VoltageDropStatus.EXCEEDS_LIMITS -> Res.string.vd_status_exceeds
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun VoltageDropScreenPreview() {
    ElecToolkitTheme {
        VoltageDropScreen(
            uiState = VoltageDropUiState(
                voltage = "230",
                current = "20",
                length = "30",
                crossSection = "4",
            ),
            onSystemChange = {}, onMaterialChange = {}, onVoltageChange = {},
            onCurrentChange = {}, onLengthChange = {}, onCrossSectionChange = {},
            onPowerFactorChange = {}, onTemperatureChange = {},
            onParallelConductorsChange = {}, onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {},
            onToggleFavorite = {}, onCopy = {}, onShare = {}, onNameplate = {}, onNavigateBack = {},
            onExportPdf = null, exportLocked = false,
        )
    }
}
