package com.kemalurekli.electricalcalculator.features.calculators.motor.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.PowerUnit
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.motor.domain.MotorResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun MotorRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: MotorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_motor_current_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

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
                    title = stringResource(R.string.calculator_motor_current_title),
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
                        examples = motorExamples,
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

                item(key = "power-unit") {
                    Column {
                        ElecOptionSelector(
                            label = stringResource(R.string.mt_power_unit),
                            options = PowerUnit.entries.toImmutableList(),
                            selected = uiState.powerUnit,
                            onSelect = onPowerUnitChange,
                            optionLabel = { stringResource(it.labelRes()) },
                        )
                        Text(
                            text = stringResource(R.string.mt_unit_hint),
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
                        label = stringResource(R.string.mt_rated_power),
                        unit = stringResource(uiState.powerUnit.labelRes()),
                        error = uiState.errors[MotorField.POWER],
                        supportingText = stringResource(R.string.mt_rated_power_hint),
                    )
                }

                item(key = "voltage") {
                    ElecNumericField(
                        value = uiState.voltage,
                        onValueChange = onVoltageChange,
                        label = stringResource(R.string.common_system_voltage),
                        unit = "V",
                        error = uiState.errors[MotorField.VOLTAGE],
                        supportingText = if (uiState.system == SupplySystem.THREE_PHASE_AC) {
                            stringResource(R.string.common_system_voltage_hint)
                        } else {
                            null
                        },
                    )
                }

                item(key = "efficiency") {
                    ElecNumericField(
                        value = uiState.efficiency,
                        onValueChange = onEfficiencyChange,
                        label = stringResource(R.string.mt_efficiency),
                        unit = "%",
                        error = uiState.errors[MotorField.EFFICIENCY],
                        supportingText = stringResource(R.string.mt_efficiency_hint),
                    )
                }

                if (uiState.showPowerFactor) {
                    item(key = "power-factor") {
                        ElecNumericField(
                            value = uiState.powerFactor,
                            onValueChange = onPowerFactorChange,
                            label = stringResource(R.string.common_power_factor),
                            error = uiState.errors[MotorField.POWER_FACTOR],
                        )
                    }
                }

                item(key = "starting-ratio") {
                    ElecNumericField(
                        value = uiState.startingRatio,
                        onValueChange = onStartingRatioChange,
                        label = stringResource(R.string.mt_starting_ratio),
                        error = uiState.errors[MotorField.STARTING_RATIO],
                        supportingText = stringResource(R.string.mt_starting_ratio_hint),
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
                        formula = stringResource(R.string.mt_formula),
                        variables = persistentListOf(
                            FormulaVariable("I", stringResource(R.string.mt_var_current), "A"),
                            FormulaVariable("P_out", stringResource(R.string.mt_var_pout), "W"),
                            FormulaVariable("P_in", stringResource(R.string.mt_var_pin), "W"),
                            FormulaVariable("η", stringResource(R.string.mt_var_eff), "—"),
                            FormulaVariable("k", stringResource(R.string.mt_var_k), "—"),
                            FormulaVariable("U", stringResource(R.string.mt_var_voltage), "V"),
                            FormulaVariable("cos φ", stringResource(R.string.mt_var_pf), "—"),
                        ),
                    )
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.mt_note_shaft_output),
                            stringResource(R.string.mt_note_full_load),
                            stringResource(R.string.mt_note_starting),
                            stringResource(R.string.mt_note_nameplate),
                            stringResource(R.string.mt_note_dc),
                        ),
                        links = persistentListOf(
                            NoteLink(
                                topicKey = "primer_nameplate",
                                label = stringResource(ReferenceCatalog.titleResOf("primer_nameplate")),
                            ),
                            NoteLink(
                                topicKey = "selection_starting",
                                label = stringResource(ReferenceCatalog.titleResOf("selection_starting")),
                            ),
                            NoteLink(
                                topicKey = "primer_powerquality",
                                label = stringResource(ReferenceCatalog.titleResOf("primer_powerquality")),
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
    result: MotorResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.mt_result_current),
            value = NumberFormatter.format(result.fullLoadCurrent, DISPLAY_DECIMALS),
            unit = "A",
            tone = ResultTone.NEUTRAL,
            statusMessage = stringResource(R.string.mt_result_status),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.mt_result_starting),
                    value = NumberFormatter.format(result.startingCurrent, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.mt_result_input_power),
                    value = NumberFormatter.format(result.inputPowerWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kW",
                ),
                ResultRow(
                    label = stringResource(R.string.mt_result_apparent),
                    value = NumberFormatter.format(result.apparentPowerVa / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kVA",
                ),
                ResultRow(
                    label = stringResource(R.string.mt_result_reactive),
                    value = NumberFormatter.format(result.reactivePowerVar / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kvar",
                ),
                ResultRow(
                    label = stringResource(R.string.mt_result_losses),
                    value = NumberFormatter.format(result.lossesWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kW",
                ),
            ),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: MotorUiState,
    result: MotorResult,
): String {
    val rating = stringResource(
        R.string.mt_export_rating,
        "${uiState.ratedPower} ${stringResource(uiState.powerUnit.labelRes())}",
        stringResource(uiState.system.labelRes()),
        uiState.voltage,
    )
    val assumptions = stringResource(
        R.string.mt_export_assumptions,
        uiState.efficiency,
        uiState.powerFactor,
    )
    val current = stringResource(
        R.string.mt_export_current,
        NumberFormatter.format(result.fullLoadCurrent, DISPLAY_DECIMALS),
    )
    val starting = stringResource(
        R.string.mt_export_starting,
        NumberFormatter.format(result.startingCurrent, DISPLAY_DECIMALS),
    )
    val inputPower = stringResource(
        R.string.mt_export_input_power,
        NumberFormatter.format(result.inputPowerWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
    )
    val apparent = stringResource(
        R.string.mt_export_apparent,
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

private fun SupplySystem.labelRes(): Int = when (this) {
    SupplySystem.DC -> R.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> R.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> R.string.common_system_three_phase
}

private fun PowerUnit.labelRes(): Int = when (this) {
    PowerUnit.KILOWATT -> R.string.mt_unit_kw
    PowerUnit.HORSEPOWER -> R.string.mt_unit_hp
    PowerUnit.METRIC_HORSEPOWER -> R.string.mt_unit_ps
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
            onToggleFavorite = {}, onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
