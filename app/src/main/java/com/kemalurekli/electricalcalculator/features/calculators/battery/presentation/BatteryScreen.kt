package com.kemalurekli.electricalcalculator.features.calculators.battery.presentation

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
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.battery.domain.BatteryResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun BatteryRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BatteryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_battery_runtime_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

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
                    title = stringResource(R.string.calculator_battery_runtime_title),
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
                        examples = batteryExamples,
                        onSelect = onApplyExample,
                    )
                }

                item(key = "inputs-header") {
                    ElecSectionHeader(
                        title = stringResource(R.string.calculator_inputs),
                        modifier = Modifier.padding(horizontal = 0.dp),
                    )
                }

                item(key = "capacity") {
                    ElecNumericField(
                        value = uiState.capacityAh,
                        onValueChange = onCapacityChange,
                        label = stringResource(R.string.bt_capacity),
                        unit = "Ah",
                        error = uiState.errors[BatteryField.CAPACITY],
                        supportingText = stringResource(R.string.bt_capacity_hint),
                    )
                }

                item(key = "rated-hours") {
                    ElecNumericField(
                        value = uiState.ratedHours,
                        onValueChange = onRatedHoursChange,
                        label = stringResource(R.string.bt_rated_hours),
                        unit = "h",
                        error = uiState.errors[BatteryField.RATED_HOURS],
                        supportingText = stringResource(R.string.bt_rated_hours_hint),
                    )
                }

                item(key = "voltage") {
                    ElecNumericField(
                        value = uiState.voltage,
                        onValueChange = onVoltageChange,
                        label = stringResource(R.string.bt_bank_voltage),
                        unit = "V",
                        error = uiState.errors[BatteryField.VOLTAGE],
                    )
                }

                item(key = "load") {
                    ElecNumericField(
                        value = uiState.loadWatts,
                        onValueChange = onLoadPowerChange,
                        label = stringResource(R.string.bt_load_power),
                        unit = "W",
                        error = uiState.errors[BatteryField.LOAD_POWER],
                    )
                }

                item(key = "efficiency") {
                    ElecNumericField(
                        value = uiState.efficiency,
                        onValueChange = onEfficiencyChange,
                        label = stringResource(R.string.bt_efficiency),
                        unit = "%",
                        error = uiState.errors[BatteryField.EFFICIENCY],
                        supportingText = stringResource(R.string.bt_efficiency_hint),
                    )
                }

                item(key = "depth-of-discharge") {
                    ElecNumericField(
                        value = uiState.depthOfDischarge,
                        onValueChange = onDepthOfDischargeChange,
                        label = stringResource(R.string.bt_depth_of_discharge),
                        unit = "%",
                        error = uiState.errors[BatteryField.DEPTH_OF_DISCHARGE],
                        supportingText = stringResource(R.string.bt_depth_of_discharge_hint),
                    )
                }

                item(key = "peukert") {
                    ElecNumericField(
                        value = uiState.peukert,
                        onValueChange = onPeukertChange,
                        label = stringResource(R.string.bt_peukert),
                        error = uiState.errors[BatteryField.PEUKERT],
                        supportingText = stringResource(R.string.bt_peukert_hint),
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
                        formula = stringResource(R.string.bt_formula),
                        variables = persistentListOf(
                            FormulaVariable("t", stringResource(R.string.bt_var_t), "h"),
                            FormulaVariable("H", stringResource(R.string.bt_var_h), "h"),
                            FormulaVariable("I_r", stringResource(R.string.bt_var_irated), "A"),
                            FormulaVariable("I", stringResource(R.string.bt_var_i), "A"),
                            FormulaVariable("k", stringResource(R.string.bt_var_k), "—"),
                            FormulaVariable("DoD", stringResource(R.string.bt_var_dod), "—"),
                            FormulaVariable("η", stringResource(R.string.bt_var_eff), "—"),
                        ),
                    )
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.bt_note_peukert),
                            stringResource(R.string.bt_note_rate_matters),
                            stringResource(R.string.bt_note_dod),
                            stringResource(R.string.bt_note_efficiency),
                            stringResource(R.string.bt_note_conditions),
                        ),
                        links = persistentListOf(
                            NoteLink(
                                topicKey = "selection_battery",
                                label = stringResource(ReferenceCatalog.titleResOf("selection_battery")),
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
    result: BatteryResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.bt_result_runtime),
            value = NumberFormatter.format(result.runtimeHours, DISPLAY_DECIMALS),
            unit = "h",
            tone = ResultTone.SUCCESS,
            statusMessage = stringResource(
                R.string.bt_result_status,
                durationText(result.runtimeHours),
            ),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.bt_result_ideal),
                    value = NumberFormatter.format(result.idealRuntimeHours, DISPLAY_DECIMALS),
                    unit = "h",
                ),
                ResultRow(
                    label = stringResource(R.string.bt_result_current),
                    value = NumberFormatter.format(result.dischargeCurrentAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.bt_result_c_rate),
                    value = NumberFormatter.format(result.cRate, C_RATE_DECIMALS),
                    unit = "C",
                ),
                ResultRow(
                    label = stringResource(R.string.bt_result_usable),
                    value = NumberFormatter.format(result.usableCapacityAh, DISPLAY_DECIMALS),
                    unit = "Ah",
                ),
                ResultRow(
                    label = stringResource(R.string.bt_result_energy),
                    value = NumberFormatter.format(result.energyDeliveredWh, DISPLAY_DECIMALS),
                    unit = "Wh",
                ),
            ),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
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
        stringResource(R.string.bt_duration, wholeHours + 1, 0)
    } else {
        stringResource(R.string.bt_duration, wholeHours, minutes)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: BatteryUiState,
    result: BatteryResult,
): String {
    val bank = stringResource(
        R.string.bt_export_bank,
        uiState.capacityAh,
        uiState.ratedHours,
        uiState.voltage,
    )
    val load = stringResource(
        R.string.bt_export_load,
        uiState.loadWatts,
        uiState.efficiency,
        uiState.depthOfDischarge,
        uiState.peukert,
    )
    val runtime = stringResource(
        R.string.bt_export_runtime,
        "${NumberFormatter.format(result.runtimeHours, DISPLAY_DECIMALS)} h " +
            "(${durationText(result.runtimeHours)})",
    )
    val current = stringResource(
        R.string.bt_export_current,
        NumberFormatter.format(result.dischargeCurrentAmps, DISPLAY_DECIMALS),
    )
    val energy = stringResource(
        R.string.bt_export_energy,
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
            onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
