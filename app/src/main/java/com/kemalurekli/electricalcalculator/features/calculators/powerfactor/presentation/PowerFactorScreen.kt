package com.kemalurekli.electricalcalculator.features.calculators.powerfactor.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.CapacitorConnection
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.powerfactor.domain.PowerFactorResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun PowerFactorRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: PowerFactorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_power_factor_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    PowerFactorScreen(
        uiState = uiState,
        onSystemChange = viewModel::onSystemChange,
        onConnectionChange = viewModel::onConnectionChange,
        onActivePowerChange = viewModel::onActivePowerChange,
        onExistingFactorChange = viewModel::onExistingFactorChange,
        onTargetFactorChange = viewModel::onTargetFactorChange,
        onVoltageChange = viewModel::onVoltageChange,
        onFrequencyChange = viewModel::onFrequencyChange,
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
fun PowerFactorScreen(
    uiState: PowerFactorUiState,
    onSystemChange: (SupplySystem) -> Unit,
    onConnectionChange: (CapacitorConnection) -> Unit,
    onActivePowerChange: (String) -> Unit,
    onExistingFactorChange: (String) -> Unit,
    onTargetFactorChange: (String) -> Unit,
    onVoltageChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<PowerFactorUiState>) -> Unit,
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
        title = stringResource(R.string.calculator_power_factor_title),
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
                item(key = "result") { ResultSection(result, onCopy, onShare) }
            }

            item(key = "examples") {
                ElecExamplesCard(
                    examples = powerFactorExamples,
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
                    // Power factor has no meaning on DC.
                    options = SupplySystem.acEntries.toImmutableList(),
                    selected = uiState.system,
                    onSelect = onSystemChange,
                    optionLabel = { stringResource(it.labelRes()) },
                )
            }

            item(key = "active-power") {
                ElecNumericField(
                    value = uiState.activePowerKw,
                    onValueChange = onActivePowerChange,
                    label = stringResource(R.string.pf_active_power),
                    unit = "kW",
                    error = uiState.errors[PowerFactorField.ACTIVE_POWER],
                    supportingText = stringResource(R.string.pf_active_power_hint),
                )
            }

            item(key = "existing") {
                ElecNumericField(
                    value = uiState.existingFactor,
                    onValueChange = onExistingFactorChange,
                    label = stringResource(R.string.pf_existing),
                    error = uiState.errors[PowerFactorField.EXISTING_FACTOR],
                    supportingText = stringResource(R.string.pf_existing_hint),
                )
            }

            item(key = "target") {
                ElecNumericField(
                    value = uiState.targetFactor,
                    onValueChange = onTargetFactorChange,
                    label = stringResource(R.string.pf_target),
                    error = uiState.errors[PowerFactorField.TARGET_FACTOR],
                    supportingText = stringResource(R.string.pf_target_hint),
                )
            }

            item(key = "voltage") {
                ElecNumericField(
                    value = uiState.voltage,
                    onValueChange = onVoltageChange,
                    label = stringResource(R.string.common_system_voltage),
                    unit = "V",
                    error = uiState.errors[PowerFactorField.VOLTAGE],
                    supportingText = if (uiState.system == SupplySystem.THREE_PHASE_AC) {
                        stringResource(R.string.common_system_voltage_hint)
                    } else {
                        null
                    },
                )
            }

            item(key = "frequency") {
                ElecNumericField(
                    value = uiState.frequency,
                    onValueChange = onFrequencyChange,
                    label = stringResource(R.string.pf_frequency),
                    unit = "Hz",
                    error = uiState.errors[PowerFactorField.FREQUENCY],
                    imeAction = ImeAction.Done,
                )
            }

            if (uiState.showConnection) {
                item(key = "connection") {
                    Column {
                        ElecOptionSelector(
                            label = stringResource(R.string.pf_connection),
                            options = CapacitorConnection.entries.toImmutableList(),
                            selected = uiState.connection,
                            onSelect = onConnectionChange,
                            optionLabel = { stringResource(it.labelRes()) },
                        )
                        Text(
                            text = stringResource(R.string.pf_connection_hint),
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
                    formula = stringResource(R.string.pf_formula),
                    variables = persistentListOf(
                        FormulaVariable("Q_c", stringResource(R.string.pf_var_qc), "var"),
                        FormulaVariable("P", stringResource(R.string.pf_var_p), "W"),
                        FormulaVariable("φ₁", stringResource(R.string.pf_var_phi1), "—"),
                        FormulaVariable("φ₂", stringResource(R.string.pf_var_phi2), "—"),
                        FormulaVariable("C", stringResource(R.string.pf_var_c), "F"),
                        FormulaVariable("n", stringResource(R.string.pf_var_n), "—"),
                        FormulaVariable("f", stringResource(R.string.pf_var_f), "Hz"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(R.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(R.string.pf_note_active_unchanged),
                        stringResource(R.string.pf_note_released),
                        stringResource(R.string.pf_note_connection),
                        stringResource(R.string.pf_note_fixed_bank),
                        stringResource(R.string.pf_note_harmonics),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "power_factors",
                            label = stringResource(ReferenceCatalog.titleResOf("power_factors")),
                        ),
                        NoteLink(
                            topicKey = "primer_harmonics",
                            label = stringResource(ReferenceCatalog.titleResOf("primer_harmonics")),
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
    result: PowerFactorResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.pf_result_capacitor),
            value = NumberFormatter.format(result.requiredCapacitorVar / PER_KILO, DISPLAY_DECIMALS),
            unit = "kvar",
            tone = ResultTone.SUCCESS,
            statusMessage = stringResource(R.string.pf_result_status),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.pf_result_capacitance),
                    value = NumberFormatter.format(result.capacitancePerPhaseFarads * MICRO, DISPLAY_DECIMALS),
                    unit = "µF",
                ),
                ResultRow(
                    label = stringResource(R.string.pf_result_released),
                    value = NumberFormatter.format(result.releasedCapacityVa / PER_KILO, DISPLAY_DECIMALS),
                    unit = "kVA",
                ),
                ResultRow(
                    label = stringResource(R.string.pf_result_apparent_before),
                    value = NumberFormatter.format(result.apparentBeforeVa / PER_KILO, DISPLAY_DECIMALS),
                    unit = "kVA",
                ),
                ResultRow(
                    label = stringResource(R.string.pf_result_apparent_after),
                    value = NumberFormatter.format(result.apparentAfterVa / PER_KILO, DISPLAY_DECIMALS),
                    unit = "kVA",
                ),
                ResultRow(
                    label = stringResource(R.string.pf_result_current_before),
                    value = NumberFormatter.format(result.currentBeforeAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.pf_result_current_after),
                    value = NumberFormatter.format(result.currentAfterAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.pf_result_reactive_before),
                    value = NumberFormatter.format(result.reactiveBeforeVar / PER_KILO, DISPLAY_DECIMALS),
                    unit = "kvar",
                ),
                ResultRow(
                    label = stringResource(R.string.pf_result_reactive_after),
                    value = NumberFormatter.format(result.reactiveAfterVar / PER_KILO, DISPLAY_DECIMALS),
                    unit = "kvar",
                ),
            ),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: PowerFactorUiState,
    result: PowerFactorResult,
): String {
    val inputs = stringResource(
        R.string.pf_export_inputs,
        stringResource(uiState.system.labelRes()),
        uiState.activePowerKw,
        uiState.voltage,
        uiState.frequency,
    )
    val factors = stringResource(
        R.string.pf_export_factors,
        uiState.existingFactor,
        uiState.targetFactor,
    )
    val capacitor = stringResource(
        R.string.pf_export_capacitor,
        NumberFormatter.format(result.requiredCapacitorVar / PER_KILO, DISPLAY_DECIMALS),
    )
    val capacitance = stringResource(
        R.string.pf_export_capacitance,
        NumberFormatter.format(result.capacitancePerPhaseFarads * MICRO, DISPLAY_DECIMALS),
        stringResource(uiState.connection.labelRes()),
    )
    val released = stringResource(
        R.string.pf_export_released,
        NumberFormatter.format(result.releasedCapacityVa / PER_KILO, DISPLAY_DECIMALS),
    )
    val current = stringResource(
        R.string.pf_export_current,
        NumberFormatter.format(result.currentBeforeAmps, DISPLAY_DECIMALS),
        NumberFormatter.format(result.currentAfterAmps, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(inputs)
        appendLine(factors)
        appendLine(EXPORT_SEPARATOR)
        appendLine(capacitor)
        if (uiState.showConnection) appendLine(capacitance)
        appendLine(released)
        append(current)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val PER_KILO = 1_000.0
private const val MICRO = 1_000_000.0
private const val EXPORT_SEPARATOR = "— — —"

private fun SupplySystem.labelRes(): Int = when (this) {
    SupplySystem.DC -> R.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> R.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> R.string.common_system_three_phase
}

private fun CapacitorConnection.labelRes(): Int = when (this) {
    CapacitorConnection.DELTA -> R.string.pf_connection_delta
    CapacitorConnection.STAR -> R.string.pf_connection_star
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun PowerFactorScreenPreview() {
    ElecToolkitTheme {
        PowerFactorScreen(
            uiState = PowerFactorUiState(
                activePowerKw = "100",
                existingFactor = "0.75",
                voltage = "400",
            ),
            onSystemChange = {}, onConnectionChange = {}, onActivePowerChange = {},
            onExistingFactorChange = {}, onTargetFactorChange = {}, onVoltageChange = {},
            onFrequencyChange = {}, onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {}, onToggleFavorite = {},
            onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
