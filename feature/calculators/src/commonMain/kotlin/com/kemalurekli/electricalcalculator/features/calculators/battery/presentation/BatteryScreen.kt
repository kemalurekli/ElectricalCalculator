package com.kemalurekli.electricalcalculator.features.calculators.battery.presentation

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
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.BatteryResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.roundToInt
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_bank_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_capacity
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_capacity_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_depth_of_discharge
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_depth_of_discharge_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_duration
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_efficiency
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_efficiency_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_export_bank
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_export_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_export_energy
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_export_load
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_export_runtime
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_load_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_note_conditions
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_note_dod
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_note_efficiency
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_note_peukert
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_note_rate_matters
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_peukert
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_peukert_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_rated_hours
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_rated_hours_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_result_c_rate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_result_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_result_energy
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_result_ideal
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_result_runtime
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_result_status
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_result_usable
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_var_dod
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_var_eff
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_var_h
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_var_i
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_var_irated
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_var_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.bt_var_t
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_battery_runtime_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun BatteryRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: BatteryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_battery_runtime_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

    BatteryScreen(
        uiState = uiState,
        onCapacityChange = viewModel::onCapacityChange,
        onRatedHoursChange = viewModel::onRatedHoursChange,
        onVoltageChange = viewModel::onVoltageChange,
        onLoadPowerChange = viewModel::onLoadPowerChange,
        onEfficiencyChange = viewModel::onEfficiencyChange,
        onDepthOfDischargeChange = viewModel::onDepthOfDischargeChange,
        onPeukertChange = viewModel::onPeukertChange,
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
fun BatteryScreen(
    uiState: BatteryUiState,
    onCapacityChange: (String) -> Unit,
    onRatedHoursChange: (String) -> Unit,
    onVoltageChange: (String) -> Unit,
    onLoadPowerChange: (String) -> Unit,
    onEfficiencyChange: (String) -> Unit,
    onDepthOfDischargeChange: (String) -> Unit,
    onPeukertChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<BatteryUiState>) -> Unit,
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
        title = stringResource(Res.string.calculator_battery_runtime_title),
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
                    examples = batteryExamples,
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

            item(key = "capacity") {
                ElecNumericField(
                    value = uiState.capacityAh,
                    onValueChange = onCapacityChange,
                    label = stringResource(Res.string.bt_capacity),
                    unit = "Ah",
                    error = uiState.errors[BatteryField.CAPACITY],
                    supportingText = stringResource(Res.string.bt_capacity_hint),
                )
            }

            item(key = "rated-hours") {
                ElecNumericField(
                    value = uiState.ratedHours,
                    onValueChange = onRatedHoursChange,
                    label = stringResource(Res.string.bt_rated_hours),
                    unit = "h",
                    error = uiState.errors[BatteryField.RATED_HOURS],
                    supportingText = stringResource(Res.string.bt_rated_hours_hint),
                )
            }

            item(key = "voltage") {
                ElecNumericField(
                    value = uiState.voltage,
                    onValueChange = onVoltageChange,
                    label = stringResource(Res.string.bt_bank_voltage),
                    unit = "V",
                    error = uiState.errors[BatteryField.VOLTAGE],
                )
            }

            item(key = "load") {
                ElecNumericField(
                    value = uiState.loadWatts,
                    onValueChange = onLoadPowerChange,
                    label = stringResource(Res.string.bt_load_power),
                    unit = "W",
                    error = uiState.errors[BatteryField.LOAD_POWER],
                )
            }

            item(key = "efficiency") {
                ElecNumericField(
                    value = uiState.efficiency,
                    onValueChange = onEfficiencyChange,
                    label = stringResource(Res.string.bt_efficiency),
                    unit = "%",
                    error = uiState.errors[BatteryField.EFFICIENCY],
                    supportingText = stringResource(Res.string.bt_efficiency_hint),
                )
            }

            item(key = "depth-of-discharge") {
                ElecNumericField(
                    value = uiState.depthOfDischarge,
                    onValueChange = onDepthOfDischargeChange,
                    label = stringResource(Res.string.bt_depth_of_discharge),
                    unit = "%",
                    error = uiState.errors[BatteryField.DEPTH_OF_DISCHARGE],
                    supportingText = stringResource(Res.string.bt_depth_of_discharge_hint),
                )
            }

            item(key = "peukert") {
                ElecNumericField(
                    value = uiState.peukert,
                    onValueChange = onPeukertChange,
                    label = stringResource(Res.string.bt_peukert),
                    error = uiState.errors[BatteryField.PEUKERT],
                    supportingText = stringResource(Res.string.bt_peukert_hint),
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
                    formula = stringResource(Res.string.bt_formula),
                    variables = persistentListOf(
                        FormulaVariable("t", stringResource(Res.string.bt_var_t), "h"),
                        FormulaVariable("H", stringResource(Res.string.bt_var_h), "h"),
                        FormulaVariable("I_r", stringResource(Res.string.bt_var_irated), "A"),
                        FormulaVariable("I", stringResource(Res.string.bt_var_i), "A"),
                        FormulaVariable("k", stringResource(Res.string.bt_var_k), "—"),
                        FormulaVariable("DoD", stringResource(Res.string.bt_var_dod), "—"),
                        FormulaVariable("η", stringResource(Res.string.bt_var_eff), "—"),
                        // Named with the labels of the fields they are read from.
                        FormulaVariable("P", stringResource(Res.string.bt_load_power), "W"),
                        FormulaVariable("U", stringResource(Res.string.bt_bank_voltage), "V"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(Res.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(Res.string.bt_note_peukert),
                        stringResource(Res.string.bt_note_rate_matters),
                        stringResource(Res.string.bt_note_dod),
                        stringResource(Res.string.bt_note_efficiency),
                        stringResource(Res.string.bt_note_conditions),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "selection_battery",
                            label = stringResource(ReferenceCatalog.titleOf("selection_battery")),
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
    result: BatteryResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(Res.string.bt_result_runtime),
            value = NumberFormatter.format(result.runtimeHours, DISPLAY_DECIMALS),
            unit = "h",
            tone = ResultTone.SUCCESS,
            statusMessage = stringResource(
                Res.string.bt_result_status,
                durationText(result.runtimeHours),
            ),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(Res.string.bt_result_ideal),
                    value = NumberFormatter.format(result.idealRuntimeHours, DISPLAY_DECIMALS),
                    unit = "h",
                ),
                ResultRow(
                    label = stringResource(Res.string.bt_result_current),
                    value = NumberFormatter.format(result.dischargeCurrentAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(Res.string.bt_result_c_rate),
                    value = NumberFormatter.format(result.cRate, C_RATE_DECIMALS),
                    unit = "C",
                ),
                ResultRow(
                    label = stringResource(Res.string.bt_result_usable),
                    value = NumberFormatter.format(result.usableCapacityAh, DISPLAY_DECIMALS),
                    unit = "Ah",
                ),
                ResultRow(
                    label = stringResource(Res.string.bt_result_energy),
                    value = NumberFormatter.format(result.energyDeliveredWh, DISPLAY_DECIMALS),
                    unit = "Wh",
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

/**
 * Renders a decimal number of hours the way an electrician reads a backup time.
 * "3.81 h" is precise but "3 h 49 min" is what gets written on the job sheet.
 */
@Composable
private fun durationText(hours: Double): String {
    val wholeHours = floor(hours).toInt()
    val minutes = ((hours - wholeHours) * MINUTES_PER_HOUR).roundToInt()

    // Rounding 59.6 minutes must carry into the hour rather than print "60 min".
    return if (minutes == MINUTES_PER_HOUR.toInt()) {
        stringResource(Res.string.bt_duration, wholeHours + 1, 0)
    } else {
        stringResource(Res.string.bt_duration, wholeHours, minutes)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: BatteryUiState,
    result: BatteryResult,
): String {
    val bank = stringResource(
        Res.string.bt_export_bank,
        uiState.capacityAh,
        uiState.ratedHours,
        uiState.voltage,
    )
    val load = stringResource(
        Res.string.bt_export_load,
        uiState.loadWatts,
        uiState.efficiency,
        uiState.depthOfDischarge,
        uiState.peukert,
    )
    val runtime = stringResource(
        Res.string.bt_export_runtime,
        "${NumberFormatter.format(result.runtimeHours, DISPLAY_DECIMALS)} h " +
            "(${durationText(result.runtimeHours)})",
    )
    val current = stringResource(
        Res.string.bt_export_current,
        NumberFormatter.format(result.dischargeCurrentAmps, DISPLAY_DECIMALS),
    )
    val energy = stringResource(
        Res.string.bt_export_energy,
        NumberFormatter.format(result.energyDeliveredWh, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(bank)
        appendLine(load)
        appendLine(EXPORT_SEPARATOR)
        appendLine(runtime)
        appendLine(current)
        append(energy)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val C_RATE_DECIMALS = 3
private const val MINUTES_PER_HOUR = 60.0
private const val EXPORT_SEPARATOR = "— — —"

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun BatteryScreenPreview() {
    ElecToolkitTheme {
        BatteryScreen(
            uiState = BatteryUiState(
                capacityAh = "100",
                voltage = "48",
                loadWatts = "500",
            ),
            onCapacityChange = {}, onRatedHoursChange = {}, onVoltageChange = {},
            onLoadPowerChange = {}, onEfficiencyChange = {}, onDepthOfDischargeChange = {},
            onPeukertChange = {}, onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {}, onToggleFavorite = {},
            onCopy = {}, onShare = {}, onExportPdf = null, exportLocked = false, onNavigateBack = {},
        )
    }
}
