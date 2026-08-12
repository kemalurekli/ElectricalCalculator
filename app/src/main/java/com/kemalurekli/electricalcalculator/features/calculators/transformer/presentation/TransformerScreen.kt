package com.kemalurekli.electricalcalculator.features.calculators.transformer.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.transformer.domain.TransformerResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun TransformerRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransformerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_transformer_current_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

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
                    title = stringResource(R.string.calculator_transformer_current_title),
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
                        examples = transformerExamples,
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
                        // A transformer has no DC arrangement, so only the AC
                        // options are offered.
                        options = SupplySystem.acEntries.toImmutableList(),
                        selected = uiState.system,
                        onSelect = onSystemChange,
                        optionLabel = { stringResource(it.labelRes()) },
                    )
                }

                item(key = "rating") {
                    ElecNumericField(
                        value = uiState.ratingKva,
                        onValueChange = onRatingChange,
                        label = stringResource(R.string.tx_rating_label),
                        unit = "kVA",
                        error = uiState.errors[TransformerField.RATING],
                        supportingText = stringResource(R.string.tx_rating_hint),
                    )
                }

                item(key = "primary") {
                    ElecNumericField(
                        value = uiState.primaryVoltage,
                        onValueChange = onPrimaryVoltageChange,
                        label = stringResource(R.string.tx_primary_voltage),
                        unit = "V",
                        error = uiState.errors[TransformerField.PRIMARY_VOLTAGE],
                        supportingText = stringResource(R.string.tx_primary_voltage_hint),
                    )
                }

                item(key = "secondary") {
                    ElecNumericField(
                        value = uiState.secondaryVoltage,
                        onValueChange = onSecondaryVoltageChange,
                        label = stringResource(R.string.tx_secondary_voltage),
                        unit = "V",
                        error = uiState.errors[TransformerField.SECONDARY_VOLTAGE],
                    )
                }

                item(key = "impedance") {
                    ElecNumericField(
                        value = uiState.impedancePercent,
                        onValueChange = onImpedanceChange,
                        label = stringResource(R.string.tx_impedance_label),
                        unit = "%",
                        error = uiState.errors[TransformerField.IMPEDANCE],
                        supportingText = stringResource(R.string.tx_impedance_hint),
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
                        formula = stringResource(R.string.tx_formula),
                        variables = persistentListOf(
                            FormulaVariable("I", stringResource(R.string.tx_var_current), "A"),
                            FormulaVariable("S", stringResource(R.string.tx_var_power), "VA"),
                            FormulaVariable("k", stringResource(R.string.tx_var_k), "—"),
                            FormulaVariable("U", stringResource(R.string.tx_var_voltage), "V"),
                            FormulaVariable("I_sc", stringResource(R.string.tx_var_isc), "A"),
                            FormulaVariable("u_k", stringResource(R.string.tx_var_uk), "%"),
                        ),
                    )
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.tx_note_apparent_power),
                            stringResource(R.string.tx_note_line_voltage),
                            stringResource(R.string.tx_note_ampere_turns),
                            stringResource(R.string.tx_note_infinite_bus),
                            stringResource(R.string.tx_note_symmetrical),
                        ),
                        links = persistentListOf(
                            NoteLink(
                                topicKey = "primer_vectorgroup",
                                label = stringResource(ReferenceCatalog.titleResOf("primer_vectorgroup")),
                            ),
                            NoteLink(
                                topicKey = "primer_nameplate",
                                label = stringResource(ReferenceCatalog.titleResOf("primer_nameplate")),
                            ),
                            NoteLink(
                                topicKey = "primer_selectivity",
                                label = stringResource(ReferenceCatalog.titleResOf("primer_selectivity")),
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
    result: TransformerResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.tx_result_secondary_current),
            value = NumberFormatter.format(result.secondaryCurrent, DISPLAY_DECIMALS),
            unit = "A",
            tone = ResultTone.NEUTRAL,
            statusMessage = stringResource(R.string.tx_result_status),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.tx_result_primary_current),
                    value = NumberFormatter.format(result.primaryCurrent, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.tx_result_ratio),
                    value = NumberFormatter.formatSignificant(result.voltageRatio),
                    unit = "",
                ),
                ResultRow(
                    label = stringResource(R.string.tx_result_short_circuit),
                    value = NumberFormatter.format(result.secondaryShortCircuitCurrent, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.tx_result_short_circuit_power),
                    value = NumberFormatter.format(result.shortCircuitPowerKva, DISPLAY_DECIMALS),
                    unit = "kVA",
                ),
            ),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: TransformerUiState,
    result: TransformerResult,
): String {
    val rating = stringResource(
        R.string.tx_export_rating,
        uiState.ratingKva,
        stringResource(uiState.system.labelRes()),
        uiState.impedancePercent,
    )
    val voltages = stringResource(
        R.string.tx_export_voltages,
        uiState.primaryVoltage,
        uiState.secondaryVoltage,
    )
    val secondary = stringResource(
        R.string.tx_export_secondary,
        NumberFormatter.format(result.secondaryCurrent, DISPLAY_DECIMALS),
    )
    val primary = stringResource(
        R.string.tx_export_primary,
        NumberFormatter.format(result.primaryCurrent, DISPLAY_DECIMALS),
    )
    val ratio = stringResource(
        R.string.tx_export_ratio,
        NumberFormatter.formatSignificant(result.voltageRatio),
    )
    val shortCircuit = stringResource(
        R.string.tx_export_short_circuit,
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

private fun SupplySystem.labelRes(): Int = when (this) {
    SupplySystem.DC -> R.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> R.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> R.string.common_system_three_phase
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
            onReset = {}, onApplyExample = {}, onReferenceClick = {}, onToggleFavorite = {}, onCopy = {}, onShare = {},
            onNavigateBack = {},
        )
    }
}
