package com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecFormulaCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNotesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultActions
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecStepsCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.component.FormulaVariable
import com.kemalurekli.electricalcalculator.core.designsystem.component.NoteLink
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultTone
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.FaultType
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.ShortCircuitResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun ShortCircuitRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: ShortCircuitViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_short_circuit_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    ShortCircuitScreen(
        uiState = uiState,
        onFaultTypeChange = viewModel::onFaultTypeChange,
        onMaterialChange = viewModel::onMaterialChange,
        onInsulationChange = viewModel::onInsulationChange,
        onVoltageChange = viewModel::onVoltageChange,
        onSupplyCurrentChange = viewModel::onSupplyCurrentChange,
        onLengthChange = viewModel::onLengthChange,
        onCrossSectionChange = viewModel::onCrossSectionChange,
        onNeutralSectionChange = viewModel::onNeutralSectionChange,
        onParallelChange = viewModel::onParallelChange,
        onReactanceChange = viewModel::onReactanceChange,
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
fun ShortCircuitScreen(
    uiState: ShortCircuitUiState,
    onFaultTypeChange: (FaultType) -> Unit,
    onMaterialChange: (ConductorMaterial) -> Unit,
    onInsulationChange: (CableInsulation) -> Unit,
    onVoltageChange: (String) -> Unit,
    onSupplyCurrentChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onCrossSectionChange: (String) -> Unit,
    onNeutralSectionChange: (String) -> Unit,
    onParallelChange: (String) -> Unit,
    onReactanceChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<ShortCircuitUiState>) -> Unit,
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.result) {
        if (uiState.result != null) listState.animateScrollToItem(0)
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier
                .widthIn(max = layout.contentMaxWidth)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ElecTopAppBar(
                    title = stringResource(R.string.calculator_short_circuit_title),
                    onNavigateBack = onNavigateBack,
                    scrollBehavior = scrollBehavior,
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
                )
            },
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
                    item(key = "result") { ResultSection(uiState, result, onCopy, onShare) }
                }

                item(key = "examples") {
                    ElecExamplesCard(
                        examples = shortCircuitExamples,
                        onSelect = onApplyExample,
                    )
                }

                item(key = "inputs-header") {
                    ElecSectionHeader(
                        title = stringResource(R.string.calculator_inputs),
                        modifier = Modifier.padding(horizontal = 0.dp),
                    )
                }

                item(key = "fault-type") {
                    Column {
                        ElecOptionSelector(
                            label = stringResource(R.string.sc_fault_type),
                            options = FaultType.entries.toImmutableList(),
                            selected = uiState.faultType,
                            onSelect = onFaultTypeChange,
                            optionLabel = { stringResource(it.labelRes()) },
                        )
                        Text(
                            text = stringResource(uiState.faultType.hintRes()),
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
                        error = uiState.errors[ShortCircuitField.VOLTAGE],
                        supportingText = stringResource(
                            if (uiState.faultType.usesNeutralReturn) {
                                R.string.sc_voltage_line_neutral_hint
                            } else {
                                R.string.sc_voltage_three_phase_hint
                            },
                        ),
                    )
                }

                item(key = "supply-current") {
                    ElecNumericField(
                        value = uiState.supplyCurrent,
                        onValueChange = onSupplyCurrentChange,
                        label = stringResource(R.string.sc_supply_current),
                        unit = "A",
                        error = uiState.errors[ShortCircuitField.SUPPLY_CURRENT],
                        supportingText = stringResource(R.string.sc_supply_current_hint),
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

                item(key = "cross-section") {
                    ElecNumericField(
                        value = uiState.crossSection,
                        onValueChange = onCrossSectionChange,
                        label = stringResource(R.string.cw_cross_section),
                        unit = "mm²",
                        error = uiState.errors[ShortCircuitField.CROSS_SECTION],
                    )
                }

                if (uiState.showNeutralSection) {
                    item(key = "neutral-section") {
                        ElecNumericField(
                            value = uiState.neutralSection,
                            onValueChange = onNeutralSectionChange,
                            label = stringResource(R.string.sc_neutral_section),
                            unit = "mm²",
                            error = uiState.errors[ShortCircuitField.NEUTRAL_SECTION],
                            supportingText = stringResource(R.string.sc_neutral_section_hint),
                        )
                    }
                }

                item(key = "length") {
                    ElecNumericField(
                        value = uiState.length,
                        onValueChange = onLengthChange,
                        label = stringResource(R.string.cw_length),
                        unit = "m",
                        error = uiState.errors[ShortCircuitField.LENGTH],
                    )
                }

                item(key = "parallel") {
                    ElecNumericField(
                        value = uiState.parallelConductors,
                        onValueChange = onParallelChange,
                        label = stringResource(R.string.common_parallel_conductors),
                        error = uiState.errors[ShortCircuitField.PARALLEL],
                    )
                }

                item(key = "reactance") {
                    ElecNumericField(
                        value = uiState.reactance,
                        onValueChange = onReactanceChange,
                        label = stringResource(R.string.sc_reactance),
                        unit = "Ω/km",
                        error = uiState.errors[ShortCircuitField.REACTANCE],
                        supportingText = stringResource(R.string.sc_reactance_hint),
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
                        formula = stringResource(R.string.sc_formula),
                        variables = persistentListOf(
                            FormulaVariable("I", stringResource(R.string.sc_var_i), "A"),
                            FormulaVariable("I_s", stringResource(R.string.sc_var_is), "A"),
                            FormulaVariable("Z_s", stringResource(R.string.sc_var_zs), "Ω"),
                            FormulaVariable("c", stringResource(R.string.sc_var_c), "—"),
                            FormulaVariable("k", stringResource(R.string.sc_var_k), "—"),
                            FormulaVariable("R", stringResource(R.string.sc_var_r), "Ω"),
                            FormulaVariable("X", stringResource(R.string.sc_var_x), "Ω"),
                        ),
                    )
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.sc_note_two_currents),
                            stringResource(R.string.sc_note_temperature),
                            stringResource(R.string.sc_note_neutral),
                            stringResource(R.string.sc_note_scalar),
                            stringResource(R.string.sc_note_next),
                            stringResource(R.string.sc_note_origin),
                        ),
                        links = persistentListOf(
                            NoteLink(
                                topicKey = "breaker_curves",
                                label = stringResource(ReferenceCatalog.titleResOf("breaker_curves")),
                            ),
                            NoteLink(
                                topicKey = "primer_selectivity",
                                label = stringResource(ReferenceCatalog.titleResOf("primer_selectivity")),
                            ),
                            NoteLink(
                                topicKey = "rating_series",
                                label = stringResource(ReferenceCatalog.titleResOf("rating_series")),
                            ),
                        ),
                        onLinkClick = onReferenceClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultSection(
    uiState: ShortCircuitUiState,
    result: ShortCircuitResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing
    val hotTemperature = NumberFormatter.format(
        uiState.insulation.maxConductorTemperatureC,
        TEMPERATURE_DECIMALS,
    )

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        // The minimum leads: it is the figure that decides whether the circuit
        // is protected. The maximum is a secondary row rather than a second
        // headline, so there is never a question which one is being read.
        ElecResultCard(
            label = stringResource(R.string.sc_result_minimum),
            value = NumberFormatter.format(result.minimumFaultCurrentAmps, DISPLAY_DECIMALS),
            unit = "A",
            tone = ResultTone.NEUTRAL,
            statusMessage = stringResource(R.string.sc_result_minimum_status),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.sc_result_maximum),
                    value = NumberFormatter.format(result.maximumFaultCurrentAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.sc_result_supply_impedance),
                    value = NumberFormatter.format(result.supplyImpedanceOhms, IMPEDANCE_DECIMALS),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(R.string.sc_result_cable_resistance_cold),
                    value = NumberFormatter.format(
                        result.cableResistanceColdOhms,
                        IMPEDANCE_DECIMALS,
                    ),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(R.string.sc_result_cable_resistance_hot, hotTemperature),
                    value = NumberFormatter.format(
                        result.cableResistanceHotOhms,
                        IMPEDANCE_DECIMALS,
                    ),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(R.string.sc_result_cable_reactance),
                    value = NumberFormatter.format(result.cableReactanceOhms, IMPEDANCE_DECIMALS),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(R.string.sc_result_loop_hot),
                    value = NumberFormatter.format(result.loopImpedanceHotOhms, IMPEDANCE_DECIMALS),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(R.string.sc_result_cable_share),
                    value = NumberFormatter.format(
                        result.cableShareOfImpedance * PERCENT,
                        DISPLAY_DECIMALS,
                    ),
                    unit = "%",
                ),
            ),
        )
        Text(
            text = stringResource(R.string.sc_result_maximum_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = spacing.xs),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: ShortCircuitUiState,
    result: ShortCircuitResult,
): String {
    val supply = stringResource(R.string.sc_export_supply, uiState.supplyCurrent, uiState.voltage)
    val cable = stringResource(
        R.string.sc_export_cable,
        uiState.crossSection,
        stringResource(uiState.material.labelRes()),
        uiState.length,
    )
    val maximum = stringResource(
        R.string.sc_export_maximum,
        NumberFormatter.format(result.maximumFaultCurrentAmps, DISPLAY_DECIMALS),
    )
    val minimum = stringResource(
        R.string.sc_export_minimum,
        NumberFormatter.format(result.minimumFaultCurrentAmps, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(stringResource(uiState.faultType.labelRes()))
        appendLine(supply)
        appendLine(cable)
        appendLine(EXPORT_SEPARATOR)
        appendLine(minimum)
        append(maximum)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val IMPEDANCE_DECIMALS = 4
private const val TEMPERATURE_DECIMALS = 0
private const val PERCENT = 100.0
private const val EXPORT_SEPARATOR = "— — —"

private fun FaultType.labelRes(): Int = when (this) {
    FaultType.THREE_PHASE -> R.string.sc_fault_three_phase
    FaultType.LINE_TO_NEUTRAL -> R.string.sc_fault_line_neutral
}

private fun FaultType.hintRes(): Int = when (this) {
    FaultType.THREE_PHASE -> R.string.sc_fault_three_phase_hint
    FaultType.LINE_TO_NEUTRAL -> R.string.sc_fault_line_neutral_hint
}

private fun ConductorMaterial.labelRes(): Int = when (this) {
    ConductorMaterial.COPPER -> R.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> R.string.common_material_aluminium
}

private fun CableInsulation.labelRes(): Int = when (this) {
    CableInsulation.PVC -> R.string.cs_insulation_pvc
    CableInsulation.XLPE -> R.string.cs_insulation_xlpe
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun ShortCircuitScreenPreview() {
    ElecToolkitTheme {
        ShortCircuitScreen(
            uiState = ShortCircuitUiState(
                supplyCurrent = "20000",
                length = "50",
                crossSection = "25",
            ),
            onFaultTypeChange = {}, onMaterialChange = {}, onInsulationChange = {},
            onVoltageChange = {}, onSupplyCurrentChange = {}, onLengthChange = {},
            onCrossSectionChange = {}, onNeutralSectionChange = {}, onParallelChange = {},
            onReactanceChange = {}, onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {}, onToggleFavorite = {},
            onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
