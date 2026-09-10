package com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation

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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExplainerCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.FormulaVariable
import com.kemalurekli.electricalcalculator.core.designsystem.component.NoteLink
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultTone
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.TransformerResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes_tab
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_transformer_current_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_supply_system
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_dc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_single_phase
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_three_phase
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_export_primary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_export_rating
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_export_ratio
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_export_secondary
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_export_short_circuit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_export_voltages
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_impedance_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_impedance_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_note_ampere_turns
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_note_apparent_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_note_infinite_bus
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_note_line_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_note_symmetrical
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_primary_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_primary_voltage_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_rating_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_rating_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_result_primary_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_result_ratio
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_result_secondary_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_result_short_circuit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_result_short_circuit_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_result_status
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_secondary_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_var_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_var_isc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_var_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_var_power
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_var_uk
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.tx_var_voltage
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun TransformerRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: TransformerViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_transformer_current_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

    TransformerScreen(
        uiState = uiState,
        onSystemChange = viewModel::onSystemChange,
        onRatingChange = viewModel::onRatingChange,
        onPrimaryVoltageChange = viewModel::onPrimaryVoltageChange,
        onSecondaryVoltageChange = viewModel::onSecondaryVoltageChange,
        onImpedanceChange = viewModel::onImpedanceChange,
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
fun TransformerScreen(
    uiState: TransformerUiState,
    onSystemChange: (SupplySystem) -> Unit,
    onRatingChange: (String) -> Unit,
    onPrimaryVoltageChange: (String) -> Unit,
    onSecondaryVoltageChange: (String) -> Unit,
    onImpedanceChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<TransformerUiState>) -> Unit,
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
        title = stringResource(Res.string.calculator_transformer_current_title),
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
                    examples = transformerExamples,
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
                    // A transformer has no DC arrangement, so only the AC
                    // options are offered.
                    options = SupplySystem.acEntries.toImmutableList(),
                    selected = uiState.system,
                    onSelect = onSystemChange,
                    optionLabel = { stringResource(it.label()) },
                )
            }

            item(key = "rating") {
                ElecNumericField(
                    value = uiState.ratingKva,
                    onValueChange = onRatingChange,
                    label = stringResource(Res.string.tx_rating_label),
                    unit = "kVA",
                    error = uiState.errors[TransformerField.RATING],
                    supportingText = stringResource(Res.string.tx_rating_hint),
                )
            }

            item(key = "primary") {
                ElecNumericField(
                    value = uiState.primaryVoltage,
                    onValueChange = onPrimaryVoltageChange,
                    label = stringResource(Res.string.tx_primary_voltage),
                    unit = "V",
                    error = uiState.errors[TransformerField.PRIMARY_VOLTAGE],
                    supportingText = stringResource(Res.string.tx_primary_voltage_hint),
                )
            }

            item(key = "secondary") {
                ElecNumericField(
                    value = uiState.secondaryVoltage,
                    onValueChange = onSecondaryVoltageChange,
                    label = stringResource(Res.string.tx_secondary_voltage),
                    unit = "V",
                    error = uiState.errors[TransformerField.SECONDARY_VOLTAGE],
                )
            }

            item(key = "impedance") {
                ElecNumericField(
                    value = uiState.impedancePercent,
                    onValueChange = onImpedanceChange,
                    label = stringResource(Res.string.tx_impedance_label),
                    unit = "%",
                    error = uiState.errors[TransformerField.IMPEDANCE],
                    supportingText = stringResource(Res.string.tx_impedance_hint),
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

            item(key = "explainer") {
                ElecExplainerCard(
                    formula = stringResource(Res.string.tx_formula),
                    variables = persistentListOf(
                        FormulaVariable("I", stringResource(Res.string.tx_var_current), "A"),
                        FormulaVariable("S", stringResource(Res.string.tx_var_power), "VA"),
                        FormulaVariable("k", stringResource(Res.string.tx_var_k), "—"),
                        FormulaVariable("U", stringResource(Res.string.tx_var_voltage), "V"),
                        FormulaVariable("I_sc", stringResource(Res.string.tx_var_isc), "A"),
                        FormulaVariable("u_k", stringResource(Res.string.tx_var_uk), "%"),
                    ),
                    formulaLabel = stringResource(Res.string.calculator_formula),
                    notesLabel = stringResource(Res.string.calculator_notes_tab),
                    steps = uiState.steps,
                    notes = persistentListOf(
                        stringResource(Res.string.tx_note_apparent_power),
                        stringResource(Res.string.tx_note_line_voltage),
                        stringResource(Res.string.tx_note_ampere_turns),
                        stringResource(Res.string.tx_note_infinite_bus),
                        stringResource(Res.string.tx_note_symmetrical),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "primer_vectorgroup",
                            label = stringResource(ReferenceCatalog.titleOf("primer_vectorgroup")),
                        ),
                        NoteLink(
                            topicKey = "primer_nameplate",
                            label = stringResource(ReferenceCatalog.titleOf("primer_nameplate")),
                        ),
                        NoteLink(
                            topicKey = "primer_selectivity",
                            label = stringResource(ReferenceCatalog.titleOf("primer_selectivity")),
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
    result: TransformerResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
) {
    val spacing = ElecTheme.spacing
    ElecResultCard(
        label = stringResource(Res.string.tx_result_secondary_current),
        value = NumberFormatter.format(result.secondaryCurrent, DISPLAY_DECIMALS),
        unit = "A",
        tone = ResultTone.NEUTRAL,
        statusMessage = stringResource(Res.string.tx_result_status),
        secondaryRows = persistentListOf(
            ResultRow(
                label = stringResource(Res.string.tx_result_primary_current),
                value = NumberFormatter.format(result.primaryCurrent, DISPLAY_DECIMALS),
                unit = "A",
            ),
            ResultRow(
                label = stringResource(Res.string.tx_result_ratio),
                value = NumberFormatter.formatSignificant(result.voltageRatio),
                unit = "",
            ),
            ResultRow(
                label = stringResource(Res.string.tx_result_short_circuit),
                value = NumberFormatter.format(result.secondaryShortCircuitCurrent, DISPLAY_DECIMALS),
                unit = "A",
            ),
            ResultRow(
                label = stringResource(Res.string.tx_result_short_circuit_power),
                value = NumberFormatter.format(result.shortCircuitPowerKva, DISPLAY_DECIMALS),
                unit = "kVA",
            ),
        ),
        onCopy = onCopy,
        onShare = onShare,
        onExportPdf = onExportPdf,
        exportLocked = exportLocked,
    )
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: TransformerUiState,
    result: TransformerResult,
): String {
    val rating = stringResource(
        Res.string.tx_export_rating,
        uiState.ratingKva,
        stringResource(uiState.system.label()),
        uiState.impedancePercent,
    )
    val voltages = stringResource(
        Res.string.tx_export_voltages,
        uiState.primaryVoltage,
        uiState.secondaryVoltage,
    )
    val secondary = stringResource(
        Res.string.tx_export_secondary,
        NumberFormatter.format(result.secondaryCurrent, DISPLAY_DECIMALS),
    )
    val primary = stringResource(
        Res.string.tx_export_primary,
        NumberFormatter.format(result.primaryCurrent, DISPLAY_DECIMALS),
    )
    val ratio = stringResource(
        Res.string.tx_export_ratio,
        NumberFormatter.formatSignificant(result.voltageRatio),
    )
    val shortCircuit = stringResource(
        Res.string.tx_export_short_circuit,
        NumberFormatter.format(result.secondaryShortCircuitCurrent, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(rating)
        appendLine(voltages)
        appendLine(EXPORT_SEPARATOR)
        appendLine(secondary)
        appendLine(primary)
        appendLine(ratio)
        append(shortCircuit)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val EXPORT_SEPARATOR = "— — —"

private fun SupplySystem.label(): StringResource = when (this) {
    SupplySystem.DC -> Res.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> Res.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> Res.string.common_system_three_phase
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun TransformerScreenPreview() {
    ElecToolkitTheme {
        TransformerScreen(
            uiState = TransformerUiState(
                ratingKva = "1000",
                primaryVoltage = "34500",
                secondaryVoltage = "400",
            ),
            onSystemChange = {}, onRatingChange = {}, onPrimaryVoltageChange = {},
            onSecondaryVoltageChange = {}, onImpedanceChange = {}, onCalculate = {},
            onReset = {}, onApplyExample = {}, onReferenceClick = {}, onToggleFavorite = {}, onCopy = {}, onShare = {}, onExportPdf = null, exportLocked = false,
            onNavigateBack = {},
        )
    }
}
