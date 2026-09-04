package com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation

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
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultResult
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_earth_fault_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_conductor_material
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_material_aluminium
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_material_copper
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_parallel_conductors
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation_pvc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation_xlpe
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_length
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_clearing_time
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_clearing_time_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_b
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_b_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_c
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_c_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_custom
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_custom_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_d
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_d_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_rcd
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_rcd_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_export_adiabatic
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_export_circuit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_export_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_export_zs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_line_section
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_note_adiabatic_table
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_note_curve
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_note_k_source
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_note_length
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_note_measure
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_note_rcd
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_note_temperature
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_note_two_checks
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_protective_section
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_rating_ia
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_rating_idn
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_rating_in
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_adiabatic
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_adiabatic_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_fail_both
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_fail_disconnect
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_fail_withstand
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_fault_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_operating_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_pass
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_permitted
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_r1
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_r2
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_tabulated
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_utilisation
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_result_zs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_var_c
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_var_ia
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_var_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_var_r1
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_var_r2
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_var_s
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_var_t
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_var_ze
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_var_zs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_voltage_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_ze
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_ze_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun EarthFaultRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: EarthFaultViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_earth_fault_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    EarthFaultScreen(
        uiState = uiState,
        onDeviceTypeChange = viewModel::onDeviceTypeChange,
        onMaterialChange = viewModel::onMaterialChange,
        onInsulationChange = viewModel::onInsulationChange,
        onExternalImpedanceChange = viewModel::onExternalImpedanceChange,
        onVoltageChange = viewModel::onVoltageChange,
        onLengthChange = viewModel::onLengthChange,
        onLineSectionChange = viewModel::onLineSectionChange,
        onProtectiveSectionChange = viewModel::onProtectiveSectionChange,
        onParallelChange = viewModel::onParallelChange,
        onDeviceRatingChange = viewModel::onDeviceRatingChange,
        onClearingTimeChange = viewModel::onClearingTimeChange,
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
        onReferenceClick = onReferenceClick,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthFaultScreen(
    uiState: EarthFaultUiState,
    onDeviceTypeChange: (ProtectiveDeviceType) -> Unit,
    onMaterialChange: (ConductorMaterial) -> Unit,
    onInsulationChange: (CableInsulation) -> Unit,
    onExternalImpedanceChange: (String) -> Unit,
    onVoltageChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onLineSectionChange: (String) -> Unit,
    onProtectiveSectionChange: (String) -> Unit,
    onParallelChange: (String) -> Unit,
    onDeviceRatingChange: (String) -> Unit,
    onClearingTimeChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<EarthFaultUiState>) -> Unit,
    onReset: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
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
        title = stringResource(Res.string.calculator_earth_fault_title),
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
                item(key = "result") { ResultSection(result, onCopy, onShare) }
            }

            item(key = "examples") {
                ElecExamplesCard(
                    examples = earthFaultExamples,
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

            item(key = "device") {
                Column {
                    ElecOptionSelector(
                        label = stringResource(Res.string.ef_device),
                        options = ProtectiveDeviceType.entries.toImmutableList(),
                        selected = uiState.deviceType,
                        onSelect = onDeviceTypeChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    Text(
                        text = stringResource(uiState.deviceType.hint()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.xs, start = spacing.xs),
                    )
                }
            }

            item(key = "device-rating") {
                ElecNumericField(
                    value = uiState.deviceRating,
                    onValueChange = onDeviceRatingChange,
                    label = stringResource(uiState.deviceType.ratingLabel()),
                    unit = "A",
                    error = uiState.errors[EarthFaultField.DEVICE_RATING],
                )
            }

            item(key = "external-impedance") {
                ElecNumericField(
                    value = uiState.externalImpedance,
                    onValueChange = onExternalImpedanceChange,
                    label = stringResource(Res.string.ef_ze),
                    unit = "Ω",
                    error = uiState.errors[EarthFaultField.EXTERNAL_IMPEDANCE],
                    supportingText = stringResource(Res.string.ef_ze_hint),
                )
            }

            item(key = "voltage") {
                ElecNumericField(
                    value = uiState.voltage,
                    onValueChange = onVoltageChange,
                    label = stringResource(Res.string.ef_voltage),
                    unit = "V",
                    error = uiState.errors[EarthFaultField.VOLTAGE],
                    supportingText = stringResource(Res.string.ef_voltage_hint),
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

            item(key = "line-section") {
                ElecNumericField(
                    value = uiState.lineSection,
                    onValueChange = onLineSectionChange,
                    label = stringResource(Res.string.ef_line_section),
                    unit = "mm²",
                    error = uiState.errors[EarthFaultField.LINE_SECTION],
                )
            }

            item(key = "protective-section") {
                ElecNumericField(
                    value = uiState.protectiveSection,
                    onValueChange = onProtectiveSectionChange,
                    label = stringResource(Res.string.ef_protective_section),
                    unit = "mm²",
                    error = uiState.errors[EarthFaultField.PROTECTIVE_SECTION],
                )
            }

            item(key = "length") {
                ElecNumericField(
                    value = uiState.length,
                    onValueChange = onLengthChange,
                    label = stringResource(Res.string.cw_length),
                    unit = "m",
                    error = uiState.errors[EarthFaultField.LENGTH],
                )
            }

            item(key = "parallel") {
                ElecNumericField(
                    value = uiState.parallelConductors,
                    onValueChange = onParallelChange,
                    label = stringResource(Res.string.common_parallel_conductors),
                    error = uiState.errors[EarthFaultField.PARALLEL],
                )
            }

            item(key = "clearing-time") {
                ElecNumericField(
                    value = uiState.clearingTime,
                    onValueChange = onClearingTimeChange,
                    label = stringResource(Res.string.ef_clearing_time),
                    unit = "s",
                    error = uiState.errors[EarthFaultField.CLEARING_TIME],
                    supportingText = stringResource(Res.string.ef_clearing_time_hint),
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
                    formula = stringResource(Res.string.ef_formula),
                    variables = persistentListOf(
                        FormulaVariable("Z_s", stringResource(Res.string.ef_var_zs), "Ω"),
                        FormulaVariable("Z_e", stringResource(Res.string.ef_var_ze), "Ω"),
                        FormulaVariable("R₁", stringResource(Res.string.ef_var_r1), "Ω"),
                        FormulaVariable("R₂", stringResource(Res.string.ef_var_r2), "Ω"),
                        FormulaVariable("I_a", stringResource(Res.string.ef_var_ia), "A"),
                        FormulaVariable("c", stringResource(Res.string.ef_var_c), "—"),
                        FormulaVariable("S", stringResource(Res.string.ef_var_s), "mm²"),
                        FormulaVariable("t", stringResource(Res.string.ef_var_t), "s"),
                        FormulaVariable("k", stringResource(Res.string.ef_var_k), "—"),
                        // Line to earth, which is what the field above asks for.
                        FormulaVariable("U₀", stringResource(Res.string.ef_voltage), "V"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(Res.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(Res.string.ef_note_two_checks),
                        stringResource(Res.string.ef_note_curve),
                        stringResource(Res.string.ef_note_length),
                        stringResource(Res.string.ef_note_temperature),
                        stringResource(Res.string.ef_note_rcd),
                        stringResource(Res.string.ef_note_adiabatic_table),
                        stringResource(Res.string.ef_note_k_source),
                        stringResource(Res.string.ef_note_measure),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "earthing_systems",
                            label = stringResource(ReferenceCatalog.titleOf("earthing_systems")),
                        ),
                        NoteLink(
                            topicKey = "disconnection_times",
                            label = stringResource(ReferenceCatalog.titleOf("disconnection_times")),
                        ),
                        NoteLink(
                            topicKey = "rcd_types",
                            label = stringResource(ReferenceCatalog.titleOf("rcd_types")),
                        ),
                        NoteLink(
                            topicKey = "primer_bonding",
                            label = stringResource(ReferenceCatalog.titleOf("primer_bonding")),
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
    result: EarthFaultResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing
    val permitted = NumberFormatter.format(result.maximumPermittedOhms, IMPEDANCE_DECIMALS)

    // Two independent verdicts, so the tone reports the worse of them and the
    // message says which one failed. "Fails" without saying which would send
    // the user to change the wrong thing.
    val tone = if (result.isCompliant) ResultTone.SUCCESS else ResultTone.ERROR
    val status = when {
        result.isCompliant -> stringResource(Res.string.ef_result_pass)
        !result.disconnectsInTime && !result.protectiveConductorWithstands ->
            stringResource(Res.string.ef_result_fail_both)

        !result.disconnectsInTime ->
            stringResource(Res.string.ef_result_fail_disconnect, permitted)

        else -> stringResource(Res.string.ef_result_fail_withstand)
    }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(Res.string.ef_result_zs),
            value = NumberFormatter.format(result.loopImpedanceOhms, IMPEDANCE_DECIMALS),
            unit = "Ω",
            tone = tone,
            statusMessage = status,
            secondaryRows = buildList {
                add(
                    ResultRow(
                        label = stringResource(Res.string.ef_result_permitted),
                        value = permitted,
                        unit = "Ω",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(Res.string.ef_result_utilisation),
                        value = NumberFormatter.format(
                            result.impedanceUtilisation * PERCENT,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "%",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(Res.string.ef_result_fault_current),
                        value = NumberFormatter.format(result.faultCurrentAmps, DISPLAY_DECIMALS),
                        unit = "A",
                    ),
                )
                // Absent for an RCD, which responds to imbalance rather than to
                // the size of the fault current.
                result.operatingCurrentAmps?.let { operating ->
                    add(
                        ResultRow(
                            label = stringResource(Res.string.ef_result_operating_current),
                            value = NumberFormatter.format(operating, DISPLAY_DECIMALS),
                            unit = "A",
                        ),
                    )
                }
                add(
                    ResultRow(
                        label = stringResource(Res.string.ef_result_r1),
                        value = NumberFormatter.format(
                            result.lineResistanceOhms,
                            IMPEDANCE_DECIMALS,
                        ),
                        unit = "Ω",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(Res.string.ef_result_r2),
                        value = NumberFormatter.format(
                            result.protectiveResistanceOhms,
                            IMPEDANCE_DECIMALS,
                        ),
                        unit = "Ω",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(Res.string.ef_result_adiabatic),
                        value = NumberFormatter.format(
                            result.adiabaticMinimumMm2,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "mm²",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(Res.string.ef_result_tabulated),
                        value = NumberFormatter.format(
                            result.tabulatedMinimumMm2,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "mm²",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(Res.string.ef_result_adiabatic_factor),
                        value = NumberFormatter.format(result.adiabaticFactor, FACTOR_DECIMALS),
                        unit = "",
                    ),
                )
            }.toImmutableList(),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: EarthFaultUiState,
    result: EarthFaultResult,
): String {
    val circuit = stringResource(
        Res.string.ef_export_circuit,
        stringResource(uiState.deviceType.label()),
        uiState.lineSection,
        uiState.protectiveSection,
        uiState.length,
    )
    val zs = stringResource(
        Res.string.ef_export_zs,
        NumberFormatter.format(result.loopImpedanceOhms, IMPEDANCE_DECIMALS),
        NumberFormatter.format(result.maximumPermittedOhms, IMPEDANCE_DECIMALS),
    )
    val current = stringResource(
        Res.string.ef_export_current,
        NumberFormatter.format(result.faultCurrentAmps, DISPLAY_DECIMALS),
    )
    val adiabatic = stringResource(
        Res.string.ef_export_adiabatic,
        NumberFormatter.format(result.adiabaticMinimumMm2, DISPLAY_DECIMALS),
        uiState.protectiveSection,
    )
    val verdict = if (result.isCompliant) {
        stringResource(Res.string.ef_result_pass)
    } else if (!result.disconnectsInTime && !result.protectiveConductorWithstands) {
        stringResource(Res.string.ef_result_fail_both)
    } else if (!result.disconnectsInTime) {
        stringResource(
            Res.string.ef_result_fail_disconnect,
            NumberFormatter.format(result.maximumPermittedOhms, IMPEDANCE_DECIMALS),
        )
    } else {
        stringResource(Res.string.ef_result_fail_withstand)
    }

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(circuit)
        appendLine(EXPORT_SEPARATOR)
        appendLine(zs)
        appendLine(current)
        appendLine(adiabatic)
        append(verdict)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val IMPEDANCE_DECIMALS = 3
private const val FACTOR_DECIMALS = 0
private const val PERCENT = 100.0
private const val EXPORT_SEPARATOR = "— — —"

private fun ProtectiveDeviceType.label(): StringResource = when (this) {
    ProtectiveDeviceType.MCB_TYPE_B -> Res.string.ef_device_b
    ProtectiveDeviceType.MCB_TYPE_C -> Res.string.ef_device_c
    ProtectiveDeviceType.MCB_TYPE_D -> Res.string.ef_device_d
    ProtectiveDeviceType.CUSTOM -> Res.string.ef_device_custom
    ProtectiveDeviceType.RCD -> Res.string.ef_device_rcd
}

private fun ProtectiveDeviceType.hint(): StringResource = when (this) {
    ProtectiveDeviceType.MCB_TYPE_B -> Res.string.ef_device_b_hint
    ProtectiveDeviceType.MCB_TYPE_C -> Res.string.ef_device_c_hint
    ProtectiveDeviceType.MCB_TYPE_D -> Res.string.ef_device_d_hint
    ProtectiveDeviceType.CUSTOM -> Res.string.ef_device_custom_hint
    ProtectiveDeviceType.RCD -> Res.string.ef_device_rcd_hint
}

/** The rating field means In, Ia or IΔn depending on the device. */
private fun ProtectiveDeviceType.ratingLabel(): StringResource = when (this) {
    ProtectiveDeviceType.CUSTOM -> Res.string.ef_rating_ia
    ProtectiveDeviceType.RCD -> Res.string.ef_rating_idn
    else -> Res.string.ef_rating_in
}

private fun ConductorMaterial.label(): StringResource = when (this) {
    ConductorMaterial.COPPER -> Res.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> Res.string.common_material_aluminium
}

private fun CableInsulation.label(): StringResource = when (this) {
    CableInsulation.PVC -> Res.string.cs_insulation_pvc
    CableInsulation.XLPE -> Res.string.cs_insulation_xlpe
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun EarthFaultScreenPreview() {
    ElecToolkitTheme {
        EarthFaultScreen(
            uiState = EarthFaultUiState(
                externalImpedance = "0.35",
                length = "30",
                lineSection = "4",
                protectiveSection = "2.5",
                deviceRating = "32",
            ),
            onDeviceTypeChange = {}, onMaterialChange = {}, onInsulationChange = {},
            onExternalImpedanceChange = {}, onVoltageChange = {}, onLengthChange = {},
            onLineSectionChange = {}, onProtectiveSectionChange = {}, onParallelChange = {},
            onDeviceRatingChange = {}, onClearingTimeChange = {}, onCalculate = {},
            onReset = {}, onApplyExample = {}, onReferenceClick = {}, onToggleFavorite = {}, onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
