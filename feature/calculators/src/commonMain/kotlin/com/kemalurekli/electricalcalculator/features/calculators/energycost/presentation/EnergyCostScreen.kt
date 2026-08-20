package com.kemalurekli.electricalcalculator.features.calculators.energycost.presentation

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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecFormulaCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNotesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultActions
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
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
import com.kemalurekli.electricalcalculator.features.calculators.energycost.domain.EnergyCostResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_energy_cost_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_comparison_header
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_days_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_days_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_export_annual
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_export_load
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_export_saving
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_hours_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_note_currency
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_note_energy_only
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_note_hours
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_note_payback
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_power_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_replacement_cost_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_replacement_power_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_result_annual
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_result_daily
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_result_energy
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_result_monthly
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_result_payback
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_result_replacement
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_result_saving
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_result_status
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_result_years
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_tariff_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_tariff_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_var_c
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_var_d
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_var_e
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_var_h
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ec_var_p
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun EnergyCostRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: EnergyCostViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_energy_cost_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    EnergyCostScreen(
        uiState = uiState,
        onPowerChange = viewModel::onPowerChange,
        onHoursChange = viewModel::onHoursChange,
        onDaysChange = viewModel::onDaysChange,
        onTariffChange = viewModel::onTariffChange,
        onReplacementPowerChange = viewModel::onReplacementPowerChange,
        onReplacementCostChange = viewModel::onReplacementCostChange,
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
fun EnergyCostScreen(
    uiState: EnergyCostUiState,
    onPowerChange: (String) -> Unit,
    onHoursChange: (String) -> Unit,
    onDaysChange: (String) -> Unit,
    onTariffChange: (String) -> Unit,
    onReplacementPowerChange: (String) -> Unit,
    onReplacementCostChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<EnergyCostUiState>) -> Unit,
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
        title = stringResource(Res.string.calculator_energy_cost_title),
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
                    examples = energyCostExamples,
                    onSelect = onApplyExample,
                )
            }

            item(key = "inputs-header") {
                ElecSectionHeader(
                    title = stringResource(Res.string.calculator_inputs),
                    modifier = Modifier.padding(horizontal = 0.dp),
                )
            }

            item(key = "power") {
                ElecNumericField(
                    value = uiState.power,
                    onValueChange = onPowerChange,
                    label = stringResource(Res.string.ec_power_label),
                    unit = "W",
                    error = uiState.errors[EnergyCostField.POWER],
                )
            }

            item(key = "hours") {
                ElecNumericField(
                    value = uiState.hoursPerDay,
                    onValueChange = onHoursChange,
                    label = stringResource(Res.string.ec_hours_label),
                    unit = "h",
                    error = uiState.errors[EnergyCostField.HOURS_PER_DAY],
                )
            }

            item(key = "days") {
                ElecNumericField(
                    value = uiState.daysPerYear,
                    onValueChange = onDaysChange,
                    label = stringResource(Res.string.ec_days_label),
                    error = uiState.errors[EnergyCostField.DAYS_PER_YEAR],
                    supportingText = stringResource(Res.string.ec_days_hint),
                )
            }

            item(key = "tariff") {
                ElecNumericField(
                    value = uiState.tariff,
                    onValueChange = onTariffChange,
                    label = stringResource(Res.string.ec_tariff_label),
                    error = uiState.errors[EnergyCostField.TARIFF],
                    supportingText = stringResource(Res.string.ec_tariff_hint),
                )
            }

            item(key = "comparison-header") {
                ElecSectionHeader(
                    title = stringResource(Res.string.ec_comparison_header),
                    modifier = Modifier.padding(horizontal = 0.dp),
                )
            }

            item(key = "replacement-power") {
                ElecNumericField(
                    value = uiState.replacementPower,
                    onValueChange = onReplacementPowerChange,
                    label = stringResource(Res.string.ec_replacement_power_label),
                    unit = "W",
                    error = uiState.errors[EnergyCostField.REPLACEMENT_POWER],
                )
            }

            item(key = "replacement-cost") {
                ElecNumericField(
                    value = uiState.replacementCost,
                    onValueChange = onReplacementCostChange,
                    label = stringResource(Res.string.ec_replacement_cost_label),
                    error = uiState.errors[EnergyCostField.REPLACEMENT_COST],
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
                    formula = stringResource(Res.string.ec_formula),
                    variables = persistentListOf(
                        FormulaVariable("E", stringResource(Res.string.ec_var_e), "kWh"),
                        FormulaVariable("P", stringResource(Res.string.ec_var_p), "W"),
                        FormulaVariable("h", stringResource(Res.string.ec_var_h), "—"),
                        FormulaVariable("d", stringResource(Res.string.ec_var_d), "—"),
                        FormulaVariable("C", stringResource(Res.string.ec_var_c), "—"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(Res.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(Res.string.ec_note_energy_only),
                        stringResource(Res.string.ec_note_hours),
                        stringResource(Res.string.ec_note_payback),
                        stringResource(Res.string.ec_note_currency),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "primer_powerquality",
                            label = stringResource(
                                ReferenceCatalog.titleOf("primer_powerquality"),
                            ),
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
    result: EnergyCostResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(Res.string.ec_result_annual),
            value = NumberFormatter.format(result.annualCost, DISPLAY_DECIMALS),
            unit = "",
            tone = ResultTone.NEUTRAL,
            statusMessage = stringResource(Res.string.ec_result_status),
            // The comparison rows appear only when an alternative was
            // described. A row reading "saving: 0" where nothing was compared
            // would look like a finding rather than an absence.
            secondaryRows = buildList {
                add(
                    ResultRow(
                        label = stringResource(Res.string.ec_result_energy),
                        value = NumberFormatter.format(result.annualEnergyKwh, DISPLAY_DECIMALS),
                        unit = "kWh",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(Res.string.ec_result_daily),
                        value = NumberFormatter.format(result.dailyCost, DISPLAY_DECIMALS),
                        unit = "",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(Res.string.ec_result_monthly),
                        value = NumberFormatter.format(result.monthlyCost, DISPLAY_DECIMALS),
                        unit = "",
                    ),
                )
                result.replacementAnnualCost?.let { replacement ->
                    add(
                        ResultRow(
                            label = stringResource(Res.string.ec_result_replacement),
                            value = NumberFormatter.format(replacement, DISPLAY_DECIMALS),
                            unit = "",
                        ),
                    )
                }
                result.annualSaving?.let { saving ->
                    add(
                        ResultRow(
                            label = stringResource(Res.string.ec_result_saving),
                            value = NumberFormatter.format(saving, DISPLAY_DECIMALS),
                            unit = "",
                        ),
                    )
                }
                result.paybackYears?.let { payback ->
                    add(
                        ResultRow(
                            label = stringResource(Res.string.ec_result_payback),
                            value = NumberFormatter.format(payback, DISPLAY_DECIMALS),
                            unit = stringResource(Res.string.ec_result_years),
                        ),
                    )
                }
            }.toImmutableList(),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: EnergyCostUiState,
    result: EnergyCostResult,
): String {
    val part0 = stringResource(
        Res.string.ec_export_load,
        uiState.power, uiState.hoursPerDay, uiState.daysPerYear,
    )
    val part1 = stringResource(
        Res.string.ec_export_annual,
        NumberFormatter.format(result.annualEnergyKwh, DISPLAY_DECIMALS), NumberFormatter.format(result.annualCost, DISPLAY_DECIMALS),
    )

    val saving = result.annualSaving?.takeIf { it > 0.0 }?.let {
        stringResource(Res.string.ec_export_saving, NumberFormatter.format(it, DISPLAY_DECIMALS))
    }

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(part0)
        append(part1)
        saving?.let {
            appendLine()
            append(it)
        }
    }
}

private const val DISPLAY_DECIMALS = 2
private const val EXPORT_SEPARATOR = "— — —"

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun EnergyCostScreenPreview() {
    ElecToolkitTheme {
        EnergyCostScreen(
            uiState = EnergyCostUiState(power = "1000", hoursPerDay = "10", tariff = "3"),
            onPowerChange = {}, onHoursChange = {}, onDaysChange = {}, onTariffChange = {}, onReplacementPowerChange = {}, onReplacementCostChange = {},
            onCalculate = {}, onApplyExample = {},
            onReferenceClick = {}, onReset = {}, onToggleFavorite = {},
            onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
