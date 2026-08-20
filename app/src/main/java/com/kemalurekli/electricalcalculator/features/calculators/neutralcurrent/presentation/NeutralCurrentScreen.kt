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
import com.kemalurekli.electricalcalculator.core.ui.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecFormulaCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNotesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultActions
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.ui.component.ElecStepsCard
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
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.neutralcurrent.domain.NeutralCurrentResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun NeutralCurrentRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: NeutralCurrentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_neutral_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

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
        title = stringResource(R.string.calculator_neutral_title),
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
                    examples = neutralCurrentExamples,
                    onSelect = onApplyExample,
                )
            }

            item(key = "inputs-header") {
                ElecSectionHeader(
                    title = stringResource(R.string.calculator_inputs),
                    modifier = Modifier.padding(horizontal = 0.dp),
                )
            }

            item(key = "line1") {
                ElecNumericField(
                    value = uiState.line1,
                    onValueChange = onLine1Change,
                    label = stringResource(R.string.nc_line_1_label),
                    unit = "A",
                    error = uiState.errors[NeutralCurrentField.LINE_1],
                )
            }

            item(key = "line2") {
                ElecNumericField(
                    value = uiState.line2,
                    onValueChange = onLine2Change,
                    label = stringResource(R.string.nc_line_2_label),
                    unit = "A",
                    error = uiState.errors[NeutralCurrentField.LINE_2],
                )
            }

            item(key = "line3") {
                ElecNumericField(
                    value = uiState.line3,
                    onValueChange = onLine3Change,
                    label = stringResource(R.string.nc_line_3_label),
                    unit = "A",
                    error = uiState.errors[NeutralCurrentField.LINE_3],
                )
            }

            item(key = "harmonic") {
                ElecNumericField(
                    value = uiState.thirdHarmonic,
                    onValueChange = onThirdHarmonicChange,
                    label = stringResource(R.string.nc_harmonic_label),
                    unit = "%",
                    error = uiState.errors[NeutralCurrentField.THIRD_HARMONIC],
                    supportingText = stringResource(R.string.nc_harmonic_hint),
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
                    formula = stringResource(R.string.nc_formula),
                    variables = persistentListOf(
                        FormulaVariable("I_N", stringResource(R.string.nc_var_in), "A"),
                        FormulaVariable("I₁ I₂ I₃", stringResource(R.string.nc_var_i), "A"),
                        FormulaVariable("I_u", stringResource(R.string.nc_var_iu), "A"),
                        FormulaVariable("I₃", stringResource(R.string.nc_var_i3), "A"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(R.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(R.string.nc_note_cancellation),
                        stringResource(R.string.nc_note_triplen),
                        stringResource(R.string.nc_note_derating),
                        stringResource(R.string.nc_note_quadrature),
                        stringResource(R.string.nc_note_measurement),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "primer_harmonics",
                            label = composeStringResource(
                                ReferenceCatalog.titleOf("primer_harmonics"),
                            ),
                        ),
                        NoteLink(
                            topicKey = "primer_cableanatomy",
                            label = composeStringResource(
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
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.nc_result_neutral),
            value = NumberFormatter.format(result.neutralCurrentAmps, DISPLAY_DECIMALS),
            unit = "A",
            // The neutral outrunning every line is the condition that derates
            // the cable, so it is the one the card shouts about.
            tone = if (result.neutralExceedsLines) ResultTone.WARNING else ResultTone.SUCCESS,
            statusMessage = stringResource(
                if (result.neutralExceedsLines) {
                    R.string.nc_result_status_exceeds
                } else {
                    R.string.nc_result_status_ok
                },
            ),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.nc_result_unbalance),
                    value = NumberFormatter.format(result.fundamentalNeutralAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.nc_result_triplen),
                    value = NumberFormatter.format(result.triplenNeutralAmps, DISPLAY_DECIMALS),
                    unit = "A",
                ),
                ResultRow(
                    label = stringResource(R.string.nc_result_ratio),
                    value = NumberFormatter.format(
                        result.neutralToHighestLineRatio * PERCENT,
                        DISPLAY_DECIMALS,
                    ),
                    unit = "%",
                ),
            ),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: NeutralCurrentUiState,
    result: NeutralCurrentResult,
): String {
    val lines = stringResource(
        R.string.nc_export_lines,
        uiState.line1,
        uiState.line2,
        uiState.line3,
    )
    val neutral = stringResource(
        R.string.nc_export_neutral,
        NumberFormatter.format(result.neutralCurrentAmps, DISPLAY_DECIMALS),
    )
    val split = stringResource(
        R.string.nc_export_split,
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
            onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
