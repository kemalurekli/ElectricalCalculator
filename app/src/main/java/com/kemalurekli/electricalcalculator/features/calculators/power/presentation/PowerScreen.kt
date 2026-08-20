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
import androidx.compose.ui.res.stringResource
import org.jetbrains.compose.resources.stringResource as composeStringResource
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
import com.kemalurekli.electricalcalculator.core.domain.model.PowerFactorType
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.power.domain.PowerResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun PowerRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: PowerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_power_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

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
        title = stringResource(R.string.calculator_power_title),
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
                    examples = powerExamples,
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

            item(key = "voltage") {
                ElecNumericField(
                    value = uiState.voltage,
                    onValueChange = onVoltageChange,
                    label = stringResource(R.string.common_system_voltage),
                    unit = "V",
                    error = uiState.errors[PowerField.VOLTAGE],
                    supportingText = if (uiState.system == SupplySystem.THREE_PHASE_AC) {
                        stringResource(R.string.common_system_voltage_hint)
                    } else {
                        null
                    },
                )
            }

            item(key = "current") {
                ElecNumericField(
                    value = uiState.current,
                    onValueChange = onCurrentChange,
                    label = stringResource(R.string.common_current),
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
                        label = stringResource(R.string.common_power_factor),
                        error = uiState.errors[PowerField.POWER_FACTOR],
                        imeAction = ImeAction.Done,
                    )
                }

                item(key = "pf-type") {
                    Column {
                        ElecOptionSelector(
                            label = stringResource(R.string.common_pf_type),
                            options = PowerFactorType.entries.toImmutableList(),
                            selected = uiState.powerFactorType,
                            onSelect = onPowerFactorTypeChange,
                            optionLabel = { stringResource(it.labelRes()) },
                        )
                        Text(
                            text = stringResource(R.string.common_pf_type_hint),
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
                    formula = stringResource(R.string.pw_formula),
                    variables = persistentListOf(
                        FormulaVariable("S", stringResource(R.string.pw_var_s), "VA"),
                        FormulaVariable("P", stringResource(R.string.pw_var_p), "W"),
                        FormulaVariable("Q", stringResource(R.string.pw_var_q), "var"),
                        FormulaVariable("k", stringResource(R.string.pw_var_k), "—"),
                        FormulaVariable("U", stringResource(R.string.pw_var_u), "V"),
                        FormulaVariable("I", stringResource(R.string.pw_var_i), "A"),
                        FormulaVariable("cos φ", stringResource(R.string.pw_var_pf), "—"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(R.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(R.string.pw_note_triangle),
                        stringResource(R.string.pw_note_direction),
                        stringResource(R.string.pw_note_line_values),
                        stringResource(R.string.pw_note_tan),
                        stringResource(R.string.pw_note_sinusoidal),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "power_factors",
                            label = composeStringResource(ReferenceCatalog.titleOf("power_factors")),
                        ),
                        NoteLink(
                            topicKey = "primer_powerquality",
                            label = composeStringResource(ReferenceCatalog.titleOf("primer_powerquality")),
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
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.pw_result_active),
            value = NumberFormatter.format(result.activePowerWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
            unit = "kW",
            tone = ResultTone.NEUTRAL,
            statusMessage = stringResource(R.string.pw_result_status),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.pw_result_reactive),
                    value = NumberFormatter.format(result.reactivePowerVar / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kvar",
                ),
                ResultRow(
                    label = stringResource(R.string.pw_result_apparent),
                    value = NumberFormatter.format(result.apparentPowerVa / WATTS_PER_KW, DISPLAY_DECIMALS),
                    unit = "kVA",
                ),
                ResultRow(
                    label = stringResource(R.string.pw_result_angle),
                    value = NumberFormatter.format(result.phaseAngleDegrees, DISPLAY_DECIMALS),
                    unit = "°",
                ),
                ResultRow(
                    label = stringResource(R.string.pw_result_tan),
                    value = NumberFormatter.formatSignificant(result.tangentPhi),
                    unit = "",
                ),
            ),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: PowerUiState,
    result: PowerResult,
): String {
    val inputs = stringResource(
        R.string.pw_export_inputs,
        stringResource(uiState.system.labelRes()),
        uiState.voltage,
        uiState.current,
    )
    val powerFactor = stringResource(
        R.string.pw_export_pf,
        uiState.powerFactor,
        stringResource(uiState.powerFactorType.labelRes()),
    )
    val active = stringResource(
        R.string.pw_export_active,
        NumberFormatter.format(result.activePowerWatts / WATTS_PER_KW, DISPLAY_DECIMALS),
    )
    val reactive = stringResource(
        R.string.pw_export_reactive,
        NumberFormatter.format(result.reactivePowerVar / WATTS_PER_KW, DISPLAY_DECIMALS),
    )
    val apparent = stringResource(
        R.string.pw_export_apparent,
        NumberFormatter.format(result.apparentPowerVa / WATTS_PER_KW, DISPLAY_DECIMALS),
    )
    val angle = stringResource(
        R.string.pw_export_angle,
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

private fun SupplySystem.labelRes(): Int = when (this) {
    SupplySystem.DC -> R.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> R.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> R.string.common_system_three_phase
}

private fun PowerFactorType.labelRes(): Int = when (this) {
    PowerFactorType.LAGGING -> R.string.common_pf_lagging
    PowerFactorType.LEADING -> R.string.common_pf_leading
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun PowerScreenPreview() {
    ElecToolkitTheme {
        PowerScreen(
            uiState = PowerUiState(voltage = "400", current = "100"),
            onSystemChange = {}, onPowerFactorTypeChange = {}, onVoltageChange = {},
            onCurrentChange = {}, onPowerFactorChange = {}, onCalculate = {},
            onReset = {}, onApplyExample = {}, onReferenceClick = {}, onToggleFavorite = {}, onCopy = {}, onShare = {},
            onNavigateBack = {},
        )
    }
}
