package com.kemalurekli.electricalcalculator.features.calculators.motor.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.PowerUnit
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.MotorResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_motor_current_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_power_factor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_supply_system
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_dc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_single_phase
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_three_phase
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_efficiency
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_efficiency_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_export_apparent
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_export_assumptions
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_export_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_export_input_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_export_rating
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_export_starting
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_note_dc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_note_full_load
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_note_nameplate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_note_shaft_output
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_note_starting
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_power_unit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_rated_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_rated_power_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_result_apparent
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_result_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_result_input_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_result_losses
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_result_reactive
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_result_starting
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_result_status
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_starting_ratio
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_starting_ratio_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_unit_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_unit_hp
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_unit_kw
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_unit_ps
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_var_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_var_eff
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_var_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_var_pf
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_var_pin
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_var_pout
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.mt_var_voltage
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun MotorRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: MotorViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_motor_current_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

    MotorScreen(
        uiState = uiState,
        onSystemChange = viewModel::onSystemChange,
        onPowerUnitChange = viewModel::onPowerUnitChange,
        onPowerChange = viewModel::onPowerChange,
        onVoltageChange = viewModel::onVoltageChange,
        onEfficiencyChange = viewModel::onEfficiencyChange,
        onPowerFactorChange = viewModel::onPowerFactorChange,
        onStartingRatioChange = viewModel::onStartingRatioChange,
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
fun MotorScreen(
    uiState: MotorUiState,
    onSystemChange: (SupplySystem) -> Unit,
    onPowerUnitChange: (PowerUnit) -> Unit,
    onPowerChange: (String) -> Unit,
    onVoltageChange: (String) -> Unit,
    onEfficiencyChange: (String) -> Unit,
    onPowerFactorChange: (String) -> Unit,
    onStartingRatioChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<MotorUiState>) -> Unit,
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
        title = stringResource(Res.string.calculator_motor_current_title),
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
                    examples = motorExamples,
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

            item(key = "system") {
                ElecOptionSelector(
                    label = stringResource(Res.string.common_supply_system),
                    options = SupplySystem.entries.toImmutableList(),
                    selected = uiState.system,
                    onSelect = onSystemChange,
                    optionLabel = { stringResource(it.label()) },
                )
            }

            item(key = "power-unit") {
                Column {
                    ElecOptionSelector(
                        label = stringResource(Res.string.mt_power_unit),
                        options = PowerUnit.entries.toImmutableList(),
                        selected = uiState.powerUnit,
                        onSelect = onPowerUnitChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    Text(
                        text = stringResource(Res.string.mt_unit_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.xs, start = spacing.xs),
                    )
                }
            }

            item(key = "power") {
                ElecNumericField(
                    value = uiState.ratedPower,
                    onValueChange = onPowerChange,
                    label = stringResource(Res.string.mt_rated_power),
                    unit = stringResource(uiState.powerUnit.label()),
                    error = uiState.errors[MotorField.POWER],
                    supportingText = stringResource(Res.string.mt_rated_power_hint),
                )
            }

            item(key = "voltage") {
                ElecNumericField(
                    value = uiState.voltage,
                    onValueChange = onVoltageChange,
                    label = stringResource(Res.string.common_system_voltage),
                    unit = "V",
                    error = uiState.errors[MotorField.VOLTAGE],
                    supportingText = if (uiState.system == SupplySystem.THREE_PHASE_AC) {
                        stringResource(Res.string.common_system_voltage_hint)
                    } else {
                        null
                    },
                )
            }

            item(key = "efficiency") {
                ElecNumericField(
                    value = uiState.efficiency,
                    onValueChange = onEfficiencyChange,
                    label = stringResource(Res.string.mt_efficiency),
                    unit = "%",
                    error = uiState.errors[MotorField.EFFICIENCY],
                    supportingText = stringResource(Res.string.mt_efficiency_hint),
                )
            }

            if (uiState.showPowerFactor) {
                item(key = "power-factor") {
                    ElecNumericField(
                        value = uiState.powerFactor,
                        onValueChange = onPowerFactorChange,
                        label = stringResource(Res.string.common_power_factor),
                        error = uiState.errors[MotorField.POWER_FACTOR],
                    )
                }
            }

            item(key = "starting-ratio") {
                ElecNumericField(
                    value = uiState.startingRatio,
                    onValueChange = onStartingRatioChange,
                    label = stringResource(Res.string.mt_starting_ratio),
                    error = uiState.errors[MotorField.STARTING_RATIO],
                    supportingText = stringResource(Res.string.mt_starting_ratio_hint),
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
                    formula = stringResource(Res.string.mt_formula),
                    variables = persistentListOf(
                        FormulaVariable("I", stringResource(Res.string.mt_var_current), "A"),
                        FormulaVariable("P_out", stringResource(Res.string.mt_var_pout), "W"),
                        FormulaVariable("P_in", stringResource(Res.string.mt_var_pin), "W"),
                        FormulaVariable("η", stringResource(Res.string.mt_var_eff), "—"),
                        FormulaVariable("k", stringResource(Res.string.mt_var_k), "—"),
                        FormulaVariable("U", stringResource(Res.string.mt_var_voltage), "V"),
                        FormulaVariable("cos φ", stringResource(Res.string.mt_var_pf), "—"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(Res.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(Res.string.mt_note_shaft_output),
                        stringResource(Res.string.mt_note_full_load),
                        stringResource(Res.string.mt_note_starting),
                        stringResource(Res.string.mt_note_nameplate),
                        stringResource(Res.string.mt_note_dc),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "primer_nameplate",
                            label = stringResource(ReferenceCatalog.titleOf("primer_nameplate")),
                        ),
                        NoteLink(
                            topicKey = "selection_starting",
                            label = stringResource(ReferenceCatalog.titleOf("selection_starting")),
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
    result: MotorResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(Res.string.mt_result_current),
            value = NumberFormatter.format(result.fullLoadCurrent, DISPLAY_DECIMALS),
            unit = "A",
            tone = ResultTone.NEUTRAL,
            statusMessage = stringResource(Res.string.mt_result_status),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(Res.string.mt_result_starting),
                    value = NumberFormatter.format(result.startingCurrent, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(Res.string.mt_result_input_power),
                    value = NumberFormatter.format(result.inputPowerWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kW",
                ),
                ResultRow(
                    label = stringResource(Res.string.mt_result_apparent),
                    value = NumberFormatter.format(result.apparentPowerVa / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kVA",
                ),
                ResultRow(
                    label = stringResource(Res.string.mt_result_reactive),
                    value = NumberFormatter.format(result.reactivePowerVar / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kvar",
                ),
                ResultRow(
                    label = stringResource(Res.string.mt_result_losses),
                    value = NumberFormatter.format(result.lossesWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kW",
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
    uiState: MotorUiState,
    result: MotorResult,
): String {
    val rating = stringResource(
        Res.string.mt_export_rating,
        "${uiState.ratedPower} ${stringResource(uiState.powerUnit.label())}",
        stringResource(uiState.system.label()),
        uiState.voltage,
    )
    val assumptions = stringResource(
        Res.string.mt_export_assumptions,
        uiState.efficiency,
        uiState.powerFactor,
    )
    val current = stringResource(
        Res.string.mt_export_current,
        NumberFormatter.format(result.fullLoadCurrent, DISPLAY_DECIMALS),
    )
    val starting = stringResource(
        Res.string.mt_export_starting,
        NumberFormatter.format(result.startingCurrent, DISPLAY_DECIMALS),
    )
    val inputPower = stringResource(
        Res.string.mt_export_input_power,
        NumberFormatter.format(result.inputPowerWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
    )
    val apparent = stringResource(
        Res.string.mt_export_apparent,
        NumberFormatter.format(result.apparentPowerVa / WATTS_PER_KW, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(rating)
        if (uiState.showPowerFactor) appendLine(assumptions)
        appendLine(EXPORT_SEPARATOR)
        appendLine(current)
        appendLine(starting)
        appendLine(inputPower)
        append(apparent)
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

private fun PowerUnit.label(): StringResource = when (this) {
    PowerUnit.KILOWATT -> Res.string.mt_unit_kw
    PowerUnit.HORSEPOWER -> Res.string.mt_unit_hp
    PowerUnit.METRIC_HORSEPOWER -> Res.string.mt_unit_ps
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun MotorScreenPreview() {
    ElecToolkitTheme {
        MotorScreen(
            uiState = MotorUiState(ratedPower = "7.5", voltage = "400"),
            onSystemChange = {}, onPowerUnitChange = {}, onPowerChange = {},
            onVoltageChange = {}, onEfficiencyChange = {}, onPowerFactorChange = {},
            onStartingRatioChange = {}, onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {},
            onToggleFavorite = {}, onCopy = {}, onShare = {}, onExportPdf = null, exportLocked = false, onNavigateBack = {},
        )
    }
}
