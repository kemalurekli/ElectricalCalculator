package com.kemalurekli.electricalcalculator.features.calculators.power.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.PowerFactorType
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.PowerResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_power_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_pf_lagging
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_pf_leading
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_pf_type
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_pf_type_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_power_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_supply_system
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_dc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_single_phase
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_three_phase
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_export_active
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_export_angle
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_export_apparent
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_export_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_export_pf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_export_reactive
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_note_direction
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_note_line_values
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_note_sinusoidal
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_note_tan
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_note_triangle
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_result_active
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_result_angle
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_result_apparent
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_result_reactive
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_result_status
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_result_tan
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_var_i
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_var_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_var_p
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_var_pf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_var_q
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_var_s
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.pw_var_u
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun PowerRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: PowerViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_power_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

    PowerScreen(
        uiState = uiState,
        onSystemChange = viewModel::onSystemChange,
        onPowerFactorTypeChange = viewModel::onPowerFactorTypeChange,
        onVoltageChange = viewModel::onVoltageChange,
        onCurrentChange = viewModel::onCurrentChange,
        onPowerFactorChange = viewModel::onPowerFactorChange,
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
fun PowerScreen(
    uiState: PowerUiState,
    onSystemChange: (SupplySystem) -> Unit,
    onPowerFactorTypeChange: (PowerFactorType) -> Unit,
    onVoltageChange: (String) -> Unit,
    onCurrentChange: (String) -> Unit,
    onPowerFactorChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<PowerUiState>) -> Unit,
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
        title = stringResource(Res.string.calculator_power_title),
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
                item(key = "result") { ResultSection(result, onCopy, onShare, onExportPdf, exportLocked) }
            }

            item(key = "examples") {
                ElecExamplesCard(
                    examples = powerExamples,
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

            item(key = "voltage") {
                ElecNumericField(
                    value = uiState.voltage,
                    onValueChange = onVoltageChange,
                    label = stringResource(Res.string.common_system_voltage),
                    unit = "V",
                    error = uiState.errors[PowerField.VOLTAGE],
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
                    label = stringResource(Res.string.common_current),
                    unit = "A",
                    error = uiState.errors[PowerField.CURRENT],
                    imeAction = if (uiState.showPowerFactor) ImeAction.Next else ImeAction.Done,
                )
            }

            if (uiState.showPowerFactor) {
                item(key = "power-factor") {
                    ElecNumericField(
                        value = uiState.powerFactor,
                        onValueChange = onPowerFactorChange,
                        label = stringResource(Res.string.common_power_factor),
                        error = uiState.errors[PowerField.POWER_FACTOR],
                        imeAction = ImeAction.Done,
                    )
                }

                item(key = "pf-type") {
                    Column {
                        ElecOptionSelector(
                            label = stringResource(Res.string.common_pf_type),
                            options = PowerFactorType.entries.toImmutableList(),
                            selected = uiState.powerFactorType,
                            onSelect = onPowerFactorTypeChange,
                            optionLabel = { stringResource(it.label()) },
                        )
                        Text(
                            text = stringResource(Res.string.common_pf_type_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = spacing.xs, start = spacing.xs),
                        )
                    }
                }
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
                    formula = stringResource(Res.string.pw_formula),
                    variables = persistentListOf(
                        FormulaVariable("S", stringResource(Res.string.pw_var_s), "VA"),
                        FormulaVariable("P", stringResource(Res.string.pw_var_p), "W"),
                        FormulaVariable("Q", stringResource(Res.string.pw_var_q), "var"),
                        FormulaVariable("k", stringResource(Res.string.pw_var_k), "—"),
                        FormulaVariable("U", stringResource(Res.string.pw_var_u), "V"),
                        FormulaVariable("I", stringResource(Res.string.pw_var_i), "A"),
                        FormulaVariable("cos φ", stringResource(Res.string.pw_var_pf), "—"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(Res.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(Res.string.pw_note_triangle),
                        stringResource(Res.string.pw_note_direction),
                        stringResource(Res.string.pw_note_line_values),
                        stringResource(Res.string.pw_note_tan),
                        stringResource(Res.string.pw_note_sinusoidal),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "power_factors",
                            label = stringResource(ReferenceCatalog.titleOf("power_factors")),
                        ),
                        NoteLink(
                            topicKey = "primer_powerquality",
                            label = stringResource(ReferenceCatalog.titleOf("primer_powerquality")),
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
    result: PowerResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(Res.string.pw_result_active),
            value = NumberFormatter.format(result.activePowerWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
            unit = "kW",
            tone = ResultTone.NEUTRAL,
            statusMessage = stringResource(Res.string.pw_result_status),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(Res.string.pw_result_reactive),
                    value = NumberFormatter.format(result.reactivePowerVar / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kvar",
                ),
                ResultRow(
                    label = stringResource(Res.string.pw_result_apparent),
                    value = NumberFormatter.format(result.apparentPowerVa / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kVA",
                ),
                ResultRow(
                    label = stringResource(Res.string.pw_result_angle),
                    value = NumberFormatter.format(result.phaseAngleDegrees, DISPLAY_DECIMALS),
                    unit = "°",
                ),
                ResultRow(
                    label = stringResource(Res.string.pw_result_tan),
                    value = NumberFormatter.formatSignificant(result.tangentPhi),
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
    uiState: PowerUiState,
    result: PowerResult,
): String {
    val inputs = stringResource(
        Res.string.pw_export_inputs,
        stringResource(uiState.system.label()),
        uiState.voltage,
        uiState.current,
    )
    val powerFactor = stringResource(
        Res.string.pw_export_pf,
        uiState.powerFactor,
        stringResource(uiState.powerFactorType.label()),
    )
    val active = stringResource(
        Res.string.pw_export_active,
        NumberFormatter.format(result.activePowerWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
    )
    val reactive = stringResource(
        Res.string.pw_export_reactive,
        NumberFormatter.format(result.reactivePowerVar / WATTS_PER_KW, DISPLAY_DECIMALS),
    )
    val apparent = stringResource(
        Res.string.pw_export_apparent,
        NumberFormatter.format(result.apparentPowerVa / WATTS_PER_KW, DISPLAY_DECIMALS),
    )
    val angle = stringResource(
        Res.string.pw_export_angle,
        NumberFormatter.format(result.phaseAngleDegrees, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(inputs)
        if (uiState.showPowerFactor) appendLine(powerFactor)
        appendLine(EXPORT_SEPARATOR)
        appendLine(active)
        appendLine(reactive)
        appendLine(apparent)
        append(angle)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val WATTS_PER_KW = 1_000.0
private const val EXPORT_SEPARATOR = "— — —"

private fun SupplySystem.label(): StringResource = when (this) {
    SupplySystem.DC -> Res.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> Res.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> Res.string.common_system_three_phase
}

private fun PowerFactorType.label(): StringResource = when (this) {
    PowerFactorType.LAGGING -> Res.string.common_pf_lagging
    PowerFactorType.LEADING -> Res.string.common_pf_leading
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun PowerScreenPreview() {
    ElecToolkitTheme {
        PowerScreen(
            uiState = PowerUiState(voltage = "400", current = "100"),
            onSystemChange = {}, onPowerFactorTypeChange = {}, onVoltageChange = {},
            onCurrentChange = {}, onPowerFactorChange = {}, onCalculate = {},
            onReset = {}, onApplyExample = {}, onReferenceClick = {}, onToggleFavorite = {}, onCopy = {}, onShare = {}, onNameplate = {}, onExportPdf = null, exportLocked = false,
            onNavigateBack = {},
        )
    }
}
