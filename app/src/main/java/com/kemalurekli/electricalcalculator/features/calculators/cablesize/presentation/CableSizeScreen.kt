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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
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
import com.kemalurekli.electricalcalculator.core.ui.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.ui.component.ElecStepsCard
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
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.CableSizeResult
import com.kemalurekli.electricalcalculator.features.calculators.cablesize.domain.GoverningConstraint
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun CableSizeRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: CableSizeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_cable_size_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result
        ?.takeIf { it.hasSolution }
        ?.let { rememberShareText(title, uiState, it) }

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
                if (ResultSharing.copy(context, title, it)) {
                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                }
            }
        },
        onShare = { summary?.let { ResultSharing.share(context, shareSubject, it) } },
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
    onNavigateBack: () -> Unit,
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
        title = stringResource(R.string.calculator_cable_size_title),
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
                        ResultSection(result, uiState, onCopy, onShare)
                    } else {
                        ElecEmptyState(
                            title = stringResource(R.string.cs_no_solution_title),
                            message = stringResource(R.string.cs_no_solution_message),
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
                )
            }

            item(key = "inputs-header") {
                ElecSectionHeader(
                    title = stringResource(R.string.calculator_inputs),
                    modifier = Modifier.padding(horizontal = 0.dp),
                )
            }

            item(key = "system") {
                ElecOptionSelector(
                    label = stringResource(R.string.common_supply_system),
                    options = SupplySystem.entries.toImmutableList(),
                    selected = uiState.system,
                    onSelect = onSystemChange,
                    optionLabel = { stringResource(it.labelRes()) },
                )
            }

            item(key = "material") {
                ElecOptionSelector(
                    label = stringResource(R.string.common_conductor_material),
                    options = ConductorMaterial.entries.toImmutableList(),
                    selected = uiState.material,
                    onSelect = onMaterialChange,
                    optionLabel = { stringResource(it.labelRes()) },
                )
            }

            item(key = "insulation") {
                ElecOptionSelector(
                    label = stringResource(R.string.cs_insulation),
                    options = CableInsulation.entries.toImmutableList(),
                    selected = uiState.insulation,
                    onSelect = onInsulationChange,
                    optionLabel = { stringResource(it.labelRes()) },
                )
            }

            item(key = "method") {
                Column {
                    ElecOptionSelector(
                        label = stringResource(R.string.cs_installation_method),
                        options = InstallationMethod.entries.toImmutableList(),
                        selected = uiState.method,
                        onSelect = onMethodChange,
                        optionLabel = { stringResource(it.shortLabelRes()) },
                    )
                    // The segmented buttons only have room for the code, so
                    // the selected method is spelled out beneath them.
                    Text(
                        text = stringResource(uiState.method.fullLabelRes()),
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
                    label = stringResource(R.string.common_system_voltage),
                    unit = "V",
                    error = uiState.errors[CableSizeField.VOLTAGE],
                    supportingText = if (uiState.system == SupplySystem.THREE_PHASE_AC) {
                        stringResource(R.string.common_system_voltage_hint)
                    } else {
                        null
                    },
                )
            }

            item(key = "current") {
                ElecNumericField(
                    value = uiState.current,
                    onValueChange = onCurrentChange,
                    label = stringResource(R.string.common_design_current),
                    unit = "A",
                    error = uiState.errors[CableSizeField.CURRENT],
                )
            }

            item(key = "length") {
                ElecNumericField(
                    value = uiState.length,
                    onValueChange = onLengthChange,
                    label = stringResource(R.string.common_route_length),
                    unit = "m",
                    error = uiState.errors[CableSizeField.LENGTH],
                    supportingText = stringResource(R.string.common_route_length_hint),
                )
            }

            if (uiState.showPowerFactor) {
                item(key = "power-factor") {
                    ElecNumericField(
                        value = uiState.powerFactor,
                        onValueChange = onPowerFactorChange,
                        label = stringResource(R.string.common_power_factor),
                        error = uiState.errors[CableSizeField.POWER_FACTOR],
                    )
                }
            }

            item(key = "max-drop") {
                ElecNumericField(
                    value = uiState.maxDropPercent,
                    onValueChange = onMaxDropChange,
                    label = stringResource(R.string.cs_max_drop_label),
                    unit = "%",
                    error = uiState.errors[CableSizeField.MAX_DROP],
                    supportingText = stringResource(R.string.cs_max_drop_hint),
                )
            }

            item(key = "ambient") {
                ElecNumericField(
                    value = uiState.ambientTemperature,
                    onValueChange = onAmbientChange,
                    label = stringResource(R.string.cs_ambient_label),
                    unit = "°C",
                    error = uiState.errors[CableSizeField.AMBIENT],
                    supportingText = stringResource(R.string.cs_ambient_hint),
                    allowNegative = true,
                )
            }

            item(key = "circuits") {
                ElecNumericField(
                    value = uiState.groupedCircuits,
                    onValueChange = onCircuitsChange,
                    label = stringResource(R.string.cs_circuits_label),
                    error = uiState.errors[CableSizeField.CIRCUITS],
                    supportingText = stringResource(R.string.cs_circuits_hint),
                )
            }

            item(key = "parallel") {
                ElecNumericField(
                    value = uiState.parallelConductors,
                    onValueChange = onParallelConductorsChange,
                    label = stringResource(R.string.common_parallel_conductors),
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
                        Text(text = stringResource(R.string.action_reset))
                    }
                    Button(onClick = onCalculate, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.action_calculate))
                    }
                }
            }

            if (uiState.steps.isNotEmpty()) {
                item(key = "steps") { ElecStepsCard(steps = uiState.steps) }
            }

            item(key = "formula") {
                ElecFormulaCard(
                    title = stringResource(R.string.calculator_formula),
                    formula = stringResource(R.string.cs_formula),
                    variables = persistentListOf(
                        FormulaVariable("I_z", stringResource(R.string.cs_var_iz), "A"),
                        FormulaVariable("I_b", stringResource(R.string.cs_var_ib), "A"),
                        FormulaVariable("Ca", stringResource(R.string.cs_var_ca), "—"),
                        FormulaVariable("Cg", stringResource(R.string.cs_var_cg), "—"),
                        FormulaVariable("A", stringResource(R.string.cs_var_area), "mm²"),
                        FormulaVariable("ΔU", stringResource(R.string.cs_var_dumax), "V"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(R.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(R.string.cs_note_two_constraints),
                        stringResource(R.string.cs_note_derating),
                        stringResource(R.string.cs_note_tables),
                        stringResource(R.string.cs_note_worst_case),
                        stringResource(R.string.cs_note_protection),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "rating_series",
                            label = stringResource(ReferenceCatalog.titleResOf("rating_series")),
                        ),
                        NoteLink(
                            topicKey = "primer_cableanatomy",
                            label = stringResource(ReferenceCatalog.titleResOf("primer_cableanatomy")),
                        ),
                        NoteLink(
                            topicKey = "selection_insulation",
                            label = stringResource(ReferenceCatalog.titleResOf("selection_insulation")),
                        ),
                        NoteLink(
                            topicKey = "selection_cabletype",
                            label = stringResource(ReferenceCatalog.titleResOf("selection_cabletype")),
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
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.cs_result_label),
            value = NumberFormatter.formatSignificant(result.recommendedAreaMm2 ?: 0.0),
            unit = "mm²",
            // Neutral rather than pass/fail: the size *is* the answer, and the
            // governing constraint is information, not a warning.
            tone = ResultTone.SUCCESS,
            statusMessage = stringResource(result.governingConstraint.messageRes()),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.cs_result_by_capacity),
                    value = NumberFormatter.formatSignificant(result.currentCapacityAreaMm2 ?: 0.0),
                    unit = "mm²",
                ),
                ResultRow(
                    label = stringResource(R.string.cs_result_by_drop),
                    value = NumberFormatter.formatSignificant(result.voltageDropAreaMm2 ?: 0.0),
                    unit = "mm²",
                ),
                ResultRow(
                    label = stringResource(R.string.cs_result_required_capacity),
                    value = NumberFormatter.format(result.requiredCapacityAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.cs_result_derated_capacity),
                    value = NumberFormatter.format(result.deratedCapacityAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.cs_result_voltage_drop),
                    value = NumberFormatter.format(result.voltageDropVolts, DISPLAY_DECIMALS),
                    unit = "V",
                ),
                ResultRow(
                    label = stringResource(R.string.cs_result_ambient_factor),
                    value = NumberFormatter.formatSignificant(result.ambientFactor),
                    unit = "",
                ),
                ResultRow(
                    label = stringResource(R.string.cs_result_grouping_factor),
                    value = NumberFormatter.formatSignificant(result.groupingFactor),
                    unit = "",
                ),
            ),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: CableSizeUiState,
    result: CableSizeResult,
): String {
    val conditions = stringResource(
        R.string.cs_export_conditions,
        stringResource(uiState.system.labelRes()),
        stringResource(uiState.material.labelRes()),
        stringResource(uiState.insulation.labelRes()),
    )
    val load = stringResource(
        R.string.cs_export_load,
        uiState.voltage,
        uiState.current,
        uiState.length,
    )
    val environment = stringResource(
        R.string.cs_export_environment,
        uiState.ambientTemperature,
        uiState.groupedCircuits,
        uiState.maxDropPercent,
    )
    val method = stringResource(uiState.method.fullLabelRes())
    val recommended = stringResource(
        R.string.cs_export_result,
        NumberFormatter.formatSignificant(result.recommendedAreaMm2 ?: 0.0),
    )
    val byCapacity = stringResource(
        R.string.cs_export_by_capacity,
        NumberFormatter.formatSignificant(result.currentCapacityAreaMm2 ?: 0.0),
    )
    val byDrop = stringResource(
        R.string.cs_export_by_drop,
        NumberFormatter.formatSignificant(result.voltageDropAreaMm2 ?: 0.0),
    )
    val drop = stringResource(
        R.string.cs_export_drop,
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

private fun SupplySystem.labelRes(): Int = when (this) {
    SupplySystem.DC -> R.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> R.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> R.string.common_system_three_phase
}

private fun ConductorMaterial.labelRes(): Int = when (this) {
    ConductorMaterial.COPPER -> R.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> R.string.common_material_aluminium
}

private fun CableInsulation.labelRes(): Int = when (this) {
    CableInsulation.PVC -> R.string.cs_insulation_pvc
    CableInsulation.XLPE -> R.string.cs_insulation_xlpe
}

private fun InstallationMethod.shortLabelRes(): Int = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> R.string.cs_method_b1
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> R.string.cs_method_b2
    InstallationMethod.C_CLIPPED_DIRECT -> R.string.cs_method_c
    InstallationMethod.E_FREE_AIR -> R.string.cs_method_e
}

private fun InstallationMethod.fullLabelRes(): Int = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> R.string.cs_method_b1_full
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> R.string.cs_method_b2_full
    InstallationMethod.C_CLIPPED_DIRECT -> R.string.cs_method_c_full
    InstallationMethod.E_FREE_AIR -> R.string.cs_method_e_full
}

private fun GoverningConstraint.messageRes(): Int = when (this) {
    GoverningConstraint.CURRENT_CAPACITY -> R.string.cs_status_capacity
    GoverningConstraint.VOLTAGE_DROP -> R.string.cs_status_drop
    GoverningConstraint.BOTH -> R.string.cs_status_both
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
            onShare = {}, onNavigateBack = {},
        )
    }
}
