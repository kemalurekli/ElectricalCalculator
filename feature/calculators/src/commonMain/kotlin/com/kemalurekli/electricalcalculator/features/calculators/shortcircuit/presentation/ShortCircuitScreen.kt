package com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.presentation

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
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.ElecTestTags
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExplainerCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultActions
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
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.FaultType
import com.kemalurekli.electricalcalculator.features.calculators.shortcircuit.domain.ShortCircuitResult
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
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_short_circuit_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_conductor_material
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_material_aluminium
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_material_copper
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_parallel_conductors
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation_pvc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation_xlpe
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_cross_section
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_length
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_export_cable
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_export_maximum
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_export_minimum
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_export_supply
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_fault_line_neutral
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_fault_line_neutral_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_fault_three_phase
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_fault_three_phase_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_fault_type
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_neutral_section
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_neutral_section_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_note_neutral
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_note_next
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_note_origin
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_note_scalar
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_note_temperature
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_note_two_currents
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_reactance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_reactance_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_cable_reactance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_cable_resistance_cold
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_cable_resistance_hot
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_cable_share
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_loop_hot
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_maximum
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_maximum_note
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_minimum
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_minimum_status
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_result_supply_impedance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_supply_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_supply_current_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_var_c
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_var_i
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_var_is
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_var_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_var_r
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_var_x
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_var_zs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_voltage_line_neutral_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sc_voltage_three_phase_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_area
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_length
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_parallel
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.vd_var_resistivity
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun ShortCircuitRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: ShortCircuitViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_short_circuit_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

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
                if (sharing.copy(title, it)) {
                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                }
            }
        },
        onShare = { summary?.let { sharing.share(shareSubject, it) } },
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
        title = stringResource(Res.string.calculator_short_circuit_title),
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
                item(key = "result") { ResultSection(uiState, result, onCopy, onShare, onExportPdf, exportLocked) }
            }

            item(key = "examples") {
                ElecExamplesCard(
                    examples = shortCircuitExamples,
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

            item(key = "fault-type") {
                ElecOptionSelector(
                    label = stringResource(Res.string.sc_fault_type),
                    options = FaultType.entries.toImmutableList(),
                    selected = uiState.faultType,
                    onSelect = onFaultTypeChange,
                    optionLabel = { stringResource(it.label()) },
                    explanation = stringResource(uiState.faultType.hint()),
                )
            }

            item(key = "voltage") {
                ElecNumericField(
                    value = uiState.voltage,
                    onValueChange = onVoltageChange,
                    label = stringResource(Res.string.common_system_voltage),
                    unit = "V",
                    error = uiState.errors[ShortCircuitField.VOLTAGE],
                    supportingText = stringResource(
                        if (uiState.faultType.usesNeutralReturn) {
                            Res.string.sc_voltage_line_neutral_hint
                        } else {
                            Res.string.sc_voltage_three_phase_hint
                        },
                    ),
                )
            }

            item(key = "supply-current") {
                ElecNumericField(
                    value = uiState.supplyCurrent,
                    onValueChange = onSupplyCurrentChange,
                    label = stringResource(Res.string.sc_supply_current),
                    unit = "A",
                    error = uiState.errors[ShortCircuitField.SUPPLY_CURRENT],
                    supportingText = stringResource(Res.string.sc_supply_current_hint),
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

            item(key = "cross-section") {
                ElecNumericField(
                    value = uiState.crossSection,
                    onValueChange = onCrossSectionChange,
                    label = stringResource(Res.string.cw_cross_section),
                    unit = "mm²",
                    error = uiState.errors[ShortCircuitField.CROSS_SECTION],
                )
            }

            if (uiState.showNeutralSection) {
                item(key = "neutral-section") {
                    ElecNumericField(
                        value = uiState.neutralSection,
                        onValueChange = onNeutralSectionChange,
                        label = stringResource(Res.string.sc_neutral_section),
                        unit = "mm²",
                        error = uiState.errors[ShortCircuitField.NEUTRAL_SECTION],
                        supportingText = stringResource(Res.string.sc_neutral_section_hint),
                    )
                }
            }

            item(key = "length") {
                ElecNumericField(
                    value = uiState.length,
                    onValueChange = onLengthChange,
                    label = stringResource(Res.string.cw_length),
                    unit = "m",
                    error = uiState.errors[ShortCircuitField.LENGTH],
                )
            }

            item(key = "parallel") {
                ElecNumericField(
                    value = uiState.parallelConductors,
                    onValueChange = onParallelChange,
                    label = stringResource(Res.string.common_parallel_conductors),
                    error = uiState.errors[ShortCircuitField.PARALLEL],
                )
            }

            item(key = "reactance") {
                ElecNumericField(
                    value = uiState.reactance,
                    onValueChange = onReactanceChange,
                    label = stringResource(Res.string.sc_reactance),
                    unit = "Ω/km",
                    error = uiState.errors[ShortCircuitField.REACTANCE],
                    supportingText = stringResource(Res.string.sc_reactance_hint),
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
                    formula = stringResource(Res.string.sc_formula),
                    variables = persistentListOf(
                        FormulaVariable("I", stringResource(Res.string.sc_var_i), "A"),
                        FormulaVariable("I_s", stringResource(Res.string.sc_var_is), "A"),
                        FormulaVariable("Z_s", stringResource(Res.string.sc_var_zs), "Ω"),
                        FormulaVariable("c", stringResource(Res.string.sc_var_c), "—"),
                        FormulaVariable("k", stringResource(Res.string.sc_var_k), "—"),
                        FormulaVariable("R", stringResource(Res.string.sc_var_r), "Ω"),
                        FormulaVariable("X", stringResource(Res.string.sc_var_x), "Ω"),
                        // The cable resistance is the voltage-drop relation again, so it is
                        // explained in the same words.
                        FormulaVariable("U", stringResource(Res.string.common_system_voltage), "V"),
                        FormulaVariable("ρ(θ)", stringResource(Res.string.vd_var_resistivity), "Ω·mm²/m"),
                        FormulaVariable("L", stringResource(Res.string.vd_var_length), "m"),
                        FormulaVariable("A", stringResource(Res.string.vd_var_area), "mm²"),
                        FormulaVariable("n", stringResource(Res.string.vd_var_parallel), "—"),
                    ),
                    formulaLabel = stringResource(Res.string.calculator_formula),
                    notesLabel = stringResource(Res.string.calculator_notes_tab),
                    steps = uiState.steps,
                    notes = persistentListOf(
                        stringResource(Res.string.sc_note_two_currents),
                        stringResource(Res.string.sc_note_temperature),
                        stringResource(Res.string.sc_note_neutral),
                        stringResource(Res.string.sc_note_scalar),
                        stringResource(Res.string.sc_note_next),
                        stringResource(Res.string.sc_note_origin),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "breaker_curves",
                            label = stringResource(ReferenceCatalog.titleOf("breaker_curves")),
                        ),
                        NoteLink(
                            topicKey = "primer_selectivity",
                            label = stringResource(ReferenceCatalog.titleOf("primer_selectivity")),
                        ),
                        NoteLink(
                            topicKey = "rating_series",
                            label = stringResource(ReferenceCatalog.titleOf("rating_series")),
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
    uiState: ShortCircuitUiState,
    result: ShortCircuitResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
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
            label = stringResource(Res.string.sc_result_minimum),
            value = NumberFormatter.format(result.minimumFaultCurrentAmps, DISPLAY_DECIMALS),
            unit = "A",
            tone = ResultTone.NEUTRAL,
            statusMessage = stringResource(Res.string.sc_result_minimum_status),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(Res.string.sc_result_maximum),
                    value = NumberFormatter.format(result.maximumFaultCurrentAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(Res.string.sc_result_supply_impedance),
                    value = NumberFormatter.format(result.supplyImpedanceOhms, IMPEDANCE_DECIMALS),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(Res.string.sc_result_cable_resistance_cold),
                    value = NumberFormatter.format(
                        result.cableResistanceColdOhms,
                        IMPEDANCE_DECIMALS,
                    ),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(Res.string.sc_result_cable_resistance_hot, hotTemperature),
                    value = NumberFormatter.format(
                        result.cableResistanceHotOhms,
                        IMPEDANCE_DECIMALS,
                    ),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(Res.string.sc_result_cable_reactance),
                    value = NumberFormatter.format(result.cableReactanceOhms, IMPEDANCE_DECIMALS),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(Res.string.sc_result_loop_hot),
                    value = NumberFormatter.format(result.loopImpedanceHotOhms, IMPEDANCE_DECIMALS),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(Res.string.sc_result_cable_share),
                    value = NumberFormatter.format(
                        result.cableShareOfImpedance * PERCENT,
                        DISPLAY_DECIMALS,
                    ),
                    unit = "%",
                ),
            ),
        )
        Text(
            text = stringResource(Res.string.sc_result_maximum_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = spacing.xs),
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
    uiState: ShortCircuitUiState,
    result: ShortCircuitResult,
): String {
    val supply = stringResource(Res.string.sc_export_supply, uiState.supplyCurrent, uiState.voltage)
    val cable = stringResource(
        Res.string.sc_export_cable,
        uiState.crossSection,
        stringResource(uiState.material.label()),
        uiState.length,
    )
    val maximum = stringResource(
        Res.string.sc_export_maximum,
        NumberFormatter.format(result.maximumFaultCurrentAmps, DISPLAY_DECIMALS),
    )
    val minimum = stringResource(
        Res.string.sc_export_minimum,
        NumberFormatter.format(result.minimumFaultCurrentAmps, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(stringResource(uiState.faultType.label()))
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

private fun FaultType.label(): StringResource = when (this) {
    FaultType.THREE_PHASE -> Res.string.sc_fault_three_phase
    FaultType.LINE_TO_NEUTRAL -> Res.string.sc_fault_line_neutral
}

private fun FaultType.hint(): StringResource = when (this) {
    FaultType.THREE_PHASE -> Res.string.sc_fault_three_phase_hint
    FaultType.LINE_TO_NEUTRAL -> Res.string.sc_fault_line_neutral_hint
}

private fun ConductorMaterial.label(): StringResource = when (this) {
    ConductorMaterial.COPPER -> Res.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> Res.string.common_material_aluminium
}

private fun CableInsulation.label(): StringResource = when (this) {
    CableInsulation.PVC -> Res.string.cs_insulation_pvc
    CableInsulation.XLPE -> Res.string.cs_insulation_xlpe
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
            onCopy = {}, onShare = {}, onExportPdf = null, exportLocked = false, onNavigateBack = {},
        )
    }
}
