package com.kemalurekli.electricalcalculator.features.calculators.earthfault.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.EarthFaultResult
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun EarthFaultRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EarthFaultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_earth_fault_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
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
            modifier = Modifier
                .widthIn(max = layout.contentMaxWidth)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ElecTopAppBar(
                    title = stringResource(R.string.calculator_earth_fault_title),
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
                    item(key = "result") { ResultSection(result, onCopy, onShare) }
                }

                item(key = "examples") {
                    ElecExamplesCard(
                        examples = earthFaultExamples,
                        onSelect = onApplyExample,
                    )
                }

                item(key = "inputs-header") {
                    ElecSectionHeader(
                        title = stringResource(R.string.calculator_inputs),
                        modifier = Modifier.padding(horizontal = 0.dp),
                    )
                }

                item(key = "device") {
                    Column {
                        ElecOptionSelector(
                            label = stringResource(R.string.ef_device),
                            options = ProtectiveDeviceType.entries.toImmutableList(),
                            selected = uiState.deviceType,
                            onSelect = onDeviceTypeChange,
                            optionLabel = { stringResource(it.labelRes()) },
                        )
                        Text(
                            text = stringResource(uiState.deviceType.hintRes()),
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
                        label = stringResource(uiState.deviceType.ratingLabelRes()),
                        unit = "A",
                        error = uiState.errors[EarthFaultField.DEVICE_RATING],
                    )
                }

                item(key = "external-impedance") {
                    ElecNumericField(
                        value = uiState.externalImpedance,
                        onValueChange = onExternalImpedanceChange,
                        label = stringResource(R.string.ef_ze),
                        unit = "Ω",
                        error = uiState.errors[EarthFaultField.EXTERNAL_IMPEDANCE],
                        supportingText = stringResource(R.string.ef_ze_hint),
                    )
                }

                item(key = "voltage") {
                    ElecNumericField(
                        value = uiState.voltage,
                        onValueChange = onVoltageChange,
                        label = stringResource(R.string.ef_voltage),
                        unit = "V",
                        error = uiState.errors[EarthFaultField.VOLTAGE],
                        supportingText = stringResource(R.string.ef_voltage_hint),
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

                item(key = "line-section") {
                    ElecNumericField(
                        value = uiState.lineSection,
                        onValueChange = onLineSectionChange,
                        label = stringResource(R.string.ef_line_section),
                        unit = "mm²",
                        error = uiState.errors[EarthFaultField.LINE_SECTION],
                    )
                }

                item(key = "protective-section") {
                    ElecNumericField(
                        value = uiState.protectiveSection,
                        onValueChange = onProtectiveSectionChange,
                        label = stringResource(R.string.ef_protective_section),
                        unit = "mm²",
                        error = uiState.errors[EarthFaultField.PROTECTIVE_SECTION],
                    )
                }

                item(key = "length") {
                    ElecNumericField(
                        value = uiState.length,
                        onValueChange = onLengthChange,
                        label = stringResource(R.string.cw_length),
                        unit = "m",
                        error = uiState.errors[EarthFaultField.LENGTH],
                    )
                }

                item(key = "parallel") {
                    ElecNumericField(
                        value = uiState.parallelConductors,
                        onValueChange = onParallelChange,
                        label = stringResource(R.string.common_parallel_conductors),
                        error = uiState.errors[EarthFaultField.PARALLEL],
                    )
                }

                item(key = "clearing-time") {
                    ElecNumericField(
                        value = uiState.clearingTime,
                        onValueChange = onClearingTimeChange,
                        label = stringResource(R.string.ef_clearing_time),
                        unit = "s",
                        error = uiState.errors[EarthFaultField.CLEARING_TIME],
                        supportingText = stringResource(R.string.ef_clearing_time_hint),
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
                        formula = stringResource(R.string.ef_formula),
                        variables = persistentListOf(
                            FormulaVariable("Z_s", stringResource(R.string.ef_var_zs), "Ω"),
                            FormulaVariable("Z_e", stringResource(R.string.ef_var_ze), "Ω"),
                            FormulaVariable("R₁", stringResource(R.string.ef_var_r1), "Ω"),
                            FormulaVariable("R₂", stringResource(R.string.ef_var_r2), "Ω"),
                            FormulaVariable("I_a", stringResource(R.string.ef_var_ia), "A"),
                            FormulaVariable("c", stringResource(R.string.ef_var_c), "—"),
                            FormulaVariable("S", stringResource(R.string.ef_var_s), "mm²"),
                            FormulaVariable("t", stringResource(R.string.ef_var_t), "s"),
                            FormulaVariable("k", stringResource(R.string.ef_var_k), "—"),
                        ),
                    )
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.ef_note_two_checks),
                            stringResource(R.string.ef_note_curve),
                            stringResource(R.string.ef_note_length),
                            stringResource(R.string.ef_note_temperature),
                            stringResource(R.string.ef_note_rcd),
                            stringResource(R.string.ef_note_adiabatic_table),
                            stringResource(R.string.ef_note_k_source),
                            stringResource(R.string.ef_note_measure),
                        ),
                        links = persistentListOf(
                            NoteLink(
                                topicKey = "earthing_systems",
                                label = stringResource(ReferenceCatalog.titleResOf("earthing_systems")),
                            ),
                            NoteLink(
                                topicKey = "disconnection_times",
                                label = stringResource(ReferenceCatalog.titleResOf("disconnection_times")),
                            ),
                            NoteLink(
                                topicKey = "rcd_types",
                                label = stringResource(ReferenceCatalog.titleResOf("rcd_types")),
                            ),
                            NoteLink(
                                topicKey = "primer_bonding",
                                label = stringResource(ReferenceCatalog.titleResOf("primer_bonding")),
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
        result.isCompliant -> stringResource(R.string.ef_result_pass)
        !result.disconnectsInTime && !result.protectiveConductorWithstands ->
            stringResource(R.string.ef_result_fail_both)

        !result.disconnectsInTime ->
            stringResource(R.string.ef_result_fail_disconnect, permitted)

        else -> stringResource(R.string.ef_result_fail_withstand)
    }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.ef_result_zs),
            value = NumberFormatter.format(result.loopImpedanceOhms, IMPEDANCE_DECIMALS),
            unit = "Ω",
            tone = tone,
            statusMessage = status,
            secondaryRows = buildList {
                add(
                    ResultRow(
                        label = stringResource(R.string.ef_result_permitted),
                        value = permitted,
                        unit = "Ω",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.ef_result_utilisation),
                        value = NumberFormatter.format(
                            result.impedanceUtilisation * PERCENT,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "%",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.ef_result_fault_current),
                        value = NumberFormatter.format(result.faultCurrentAmps, DISPLAY_DECIMALS),
                        unit = "A",
                    ),
                )
                // Absent for an RCD, which responds to imbalance rather than to
                // the size of the fault current.
                result.operatingCurrentAmps?.let { operating ->
                    add(
                        ResultRow(
                            label = stringResource(R.string.ef_result_operating_current),
                            value = NumberFormatter.format(operating, DISPLAY_DECIMALS),
                            unit = "A",
                        ),
                    )
                }
                add(
                    ResultRow(
                        label = stringResource(R.string.ef_result_r1),
                        value = NumberFormatter.format(
                            result.lineResistanceOhms,
                            IMPEDANCE_DECIMALS,
                        ),
                        unit = "Ω",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.ef_result_r2),
                        value = NumberFormatter.format(
                            result.protectiveResistanceOhms,
                            IMPEDANCE_DECIMALS,
                        ),
                        unit = "Ω",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.ef_result_adiabatic),
                        value = NumberFormatter.format(
                            result.adiabaticMinimumMm2,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "mm²",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.ef_result_tabulated),
                        value = NumberFormatter.format(
                            result.tabulatedMinimumMm2,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "mm²",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.ef_result_adiabatic_factor),
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
        R.string.ef_export_circuit,
        stringResource(uiState.deviceType.labelRes()),
        uiState.lineSection,
        uiState.protectiveSection,
        uiState.length,
    )
    val zs = stringResource(
        R.string.ef_export_zs,
        NumberFormatter.format(result.loopImpedanceOhms, IMPEDANCE_DECIMALS),
        NumberFormatter.format(result.maximumPermittedOhms, IMPEDANCE_DECIMALS),
    )
    val current = stringResource(
        R.string.ef_export_current,
        NumberFormatter.format(result.faultCurrentAmps, DISPLAY_DECIMALS),
    )
    val adiabatic = stringResource(
        R.string.ef_export_adiabatic,
        NumberFormatter.format(result.adiabaticMinimumMm2, DISPLAY_DECIMALS),
        uiState.protectiveSection,
    )
    val verdict = if (result.isCompliant) {
        stringResource(R.string.ef_result_pass)
    } else if (!result.disconnectsInTime && !result.protectiveConductorWithstands) {
        stringResource(R.string.ef_result_fail_both)
    } else if (!result.disconnectsInTime) {
        stringResource(
            R.string.ef_result_fail_disconnect,
            NumberFormatter.format(result.maximumPermittedOhms, IMPEDANCE_DECIMALS),
        )
    } else {
        stringResource(R.string.ef_result_fail_withstand)
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

private fun ProtectiveDeviceType.labelRes(): Int = when (this) {
    ProtectiveDeviceType.MCB_TYPE_B -> R.string.ef_device_b
    ProtectiveDeviceType.MCB_TYPE_C -> R.string.ef_device_c
    ProtectiveDeviceType.MCB_TYPE_D -> R.string.ef_device_d
    ProtectiveDeviceType.CUSTOM -> R.string.ef_device_custom
    ProtectiveDeviceType.RCD -> R.string.ef_device_rcd
}

private fun ProtectiveDeviceType.hintRes(): Int = when (this) {
    ProtectiveDeviceType.MCB_TYPE_B -> R.string.ef_device_b_hint
    ProtectiveDeviceType.MCB_TYPE_C -> R.string.ef_device_c_hint
    ProtectiveDeviceType.MCB_TYPE_D -> R.string.ef_device_d_hint
    ProtectiveDeviceType.CUSTOM -> R.string.ef_device_custom_hint
    ProtectiveDeviceType.RCD -> R.string.ef_device_rcd_hint
}

/** The rating field means In, Ia or IΔn depending on the device. */
private fun ProtectiveDeviceType.ratingLabelRes(): Int = when (this) {
    ProtectiveDeviceType.CUSTOM -> R.string.ef_rating_ia
    ProtectiveDeviceType.RCD -> R.string.ef_rating_idn
    else -> R.string.ef_rating_in
}

private fun ConductorMaterial.labelRes(): Int = when (this) {
    ConductorMaterial.COPPER -> R.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> R.string.common_material_aluminium
}

private fun CableInsulation.labelRes(): Int = when (this) {
    CableInsulation.PVC -> R.string.cs_insulation_pvc
    CableInsulation.XLPE -> R.string.cs_insulation_xlpe
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
