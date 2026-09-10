package com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.presentation

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
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.NeutralCurrentResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_neutral_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_export_lines
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_export_neutral
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_export_split
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_harmonic_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_harmonic_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_line_1_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_line_2_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_line_3_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_note_cancellation
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_note_derating
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_note_measurement
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_note_quadrature
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_note_triplen
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_result_neutral
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_result_ratio
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_result_status_exceeds
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_result_status_ok
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_result_triplen
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_result_unbalance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_var_i
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_var_i3
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_var_in
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.nc_var_iu
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun NeutralCurrentRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: NeutralCurrentViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_neutral_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

    NeutralCurrentScreen(
        uiState = uiState,
        onLine1Change = viewModel::onLine1Change,
        onLine2Change = viewModel::onLine2Change,
        onLine3Change = viewModel::onLine3Change,
        onThirdHarmonicChange = viewModel::onThirdHarmonicChange,
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
fun NeutralCurrentScreen(
    uiState: NeutralCurrentUiState,
    onLine1Change: (String) -> Unit,
    onLine2Change: (String) -> Unit,
    onLine3Change: (String) -> Unit,
    onThirdHarmonicChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<NeutralCurrentUiState>) -> Unit,
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
        title = stringResource(Res.string.calculator_neutral_title),
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
                    examples = neutralCurrentExamples,
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

            item(key = "line1") {
                ElecNumericField(
                    value = uiState.line1,
                    onValueChange = onLine1Change,
                    label = stringResource(Res.string.nc_line_1_label),
                    unit = "A",
                    error = uiState.errors[NeutralCurrentField.LINE_1],
                )
            }

            item(key = "line2") {
                ElecNumericField(
                    value = uiState.line2,
                    onValueChange = onLine2Change,
                    label = stringResource(Res.string.nc_line_2_label),
                    unit = "A",
                    error = uiState.errors[NeutralCurrentField.LINE_2],
                )
            }

            item(key = "line3") {
                ElecNumericField(
                    value = uiState.line3,
                    onValueChange = onLine3Change,
                    label = stringResource(Res.string.nc_line_3_label),
                    unit = "A",
                    error = uiState.errors[NeutralCurrentField.LINE_3],
                )
            }

            item(key = "harmonic") {
                ElecNumericField(
                    value = uiState.thirdHarmonic,
                    onValueChange = onThirdHarmonicChange,
                    label = stringResource(Res.string.nc_harmonic_label),
                    unit = "%",
                    error = uiState.errors[NeutralCurrentField.THIRD_HARMONIC],
                    supportingText = stringResource(Res.string.nc_harmonic_hint),
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
                    formula = stringResource(Res.string.nc_formula),
                    variables = persistentListOf(
                        FormulaVariable("I_N", stringResource(Res.string.nc_var_in), "A"),
                        FormulaVariable("I₁ I₂ I₃", stringResource(Res.string.nc_var_i), "A"),
                        FormulaVariable("I_u", stringResource(Res.string.nc_var_iu), "A"),
                        FormulaVariable("I₃", stringResource(Res.string.nc_var_i3), "A"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(Res.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(Res.string.nc_note_cancellation),
                        stringResource(Res.string.nc_note_triplen),
                        stringResource(Res.string.nc_note_derating),
                        stringResource(Res.string.nc_note_quadrature),
                        stringResource(Res.string.nc_note_measurement),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "primer_harmonics",
                            label = stringResource(
                                ReferenceCatalog.titleOf("primer_harmonics"),
                            ),
                        ),
                        NoteLink(
                            topicKey = "primer_cableanatomy",
                            label = stringResource(
                                ReferenceCatalog.titleOf("primer_cableanatomy"),
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
    result: NeutralCurrentResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(Res.string.nc_result_neutral),
            value = NumberFormatter.format(result.neutralCurrentAmps, DISPLAY_DECIMALS),
            unit = "A",
            // The neutral outrunning every line is the condition that derates
            // the cable, so it is the one the card shouts about.
            tone = if (result.neutralExceedsLines) ResultTone.WARNING else ResultTone.SUCCESS,
            statusMessage = stringResource(
                if (result.neutralExceedsLines) {
                    Res.string.nc_result_status_exceeds
                } else {
                    Res.string.nc_result_status_ok
                },
            ),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(Res.string.nc_result_unbalance),
                    value = NumberFormatter.format(result.fundamentalNeutralAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(Res.string.nc_result_triplen),
                    value = NumberFormatter.format(result.triplenNeutralAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(Res.string.nc_result_ratio),
                    value = NumberFormatter.format(
                        result.neutralToHighestLineRatio * PERCENT,
                        DISPLAY_DECIMALS,
                    ),
                    unit = "%",
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
    uiState: NeutralCurrentUiState,
    result: NeutralCurrentResult,
): String {
    val lines = stringResource(
        Res.string.nc_export_lines,
        uiState.line1,
        uiState.line2,
        uiState.line3,
    )
    val neutral = stringResource(
        Res.string.nc_export_neutral,
        NumberFormatter.format(result.neutralCurrentAmps, DISPLAY_DECIMALS),
    )
    val split = stringResource(
        Res.string.nc_export_split,
        NumberFormatter.format(result.fundamentalNeutralAmps, DISPLAY_DECIMALS),
        NumberFormatter.format(result.triplenNeutralAmps, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(lines)
        appendLine(EXPORT_SEPARATOR)
        appendLine(neutral)
        append(split)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val PERCENT = 100.0
private const val EXPORT_SEPARATOR = "— — —"

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun NeutralCurrentScreenPreview() {
    ElecToolkitTheme {
        NeutralCurrentScreen(
            uiState = NeutralCurrentUiState(line1 = "100", line2 = "80", line3 = "60"),
            onLine1Change = {}, onLine2Change = {}, onLine3Change = {},
            onThirdHarmonicChange = {}, onCalculate = {}, onApplyExample = {},
            onReferenceClick = {}, onReset = {}, onToggleFavorite = {},
            onCopy = {}, onShare = {}, onExportPdf = null, exportLocked = false, onNavigateBack = {},
        )
    }
}
