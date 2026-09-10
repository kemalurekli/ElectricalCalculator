package com.kemalurekli.electricalcalculator.features.calculators.selectivity.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.core.designsystem.component.NoteLink
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNotesCard
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_var_iinst
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_var_m
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_var_indn
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_var_inup
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_var_n
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.core.designsystem.component.FormulaVariable
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecFormulaCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultActions
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultTone
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_export_line
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecStepsCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityGrade
import com.kemalurekli.electricalcalculator.features.calculators.selectivity.domain.SelectivityResult
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_selectivity_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_b
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_c
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_custom
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_d
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ef_device_rcd
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_downstream
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_downstream_rating
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_fault_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_fault_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_note_bound
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_note_overload
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_result_downstream_threshold
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_result_limit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_result_ratio
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_result_upstream_threshold
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_upstream
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.sel_upstream_rating
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove

@Composable
fun SelectivityRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    recordId: Long? = null,
    viewModel: SelectivityViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }

    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_selectivity_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

    SelectivityScreen(
        uiState = uiState,
        onCopy = {
            summary?.let {
                if (sharing.copy(title, it)) {
                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                }
            }
        },
        onShare = { summary?.let { sharing.share(shareSubject, it) } },
        onReferenceClick = onReferenceClick,
        onExportPdf = summary?.let { text -> { export.export(title, text) } },
        exportLocked = !export.isPro,
        snackbarHostState = snackbarHostState,
        onUpstreamTypeChange = viewModel::onUpstreamTypeChange,
        onUpstreamRatingChange = viewModel::onUpstreamRatingChange,
        onDownstreamTypeChange = viewModel::onDownstreamTypeChange,
        onDownstreamRatingChange = viewModel::onDownstreamRatingChange,
        onFaultCurrentChange = viewModel::onFaultCurrentChange,
        onCalculate = viewModel::onCalculate,
        onReset = viewModel::onReset,
        onApplyExample = viewModel::onApplyExample,
        onToggleFavorite = viewModel::onToggleFavorite,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * Two devices, one fault level, one answer.
 *
 * The fault current sits with the devices rather than in a section of its own,
 * because it is not context — it is the third input the verdict depends on, and
 * separating it invites the reader to treat the answer as a property of the
 * pair.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectivityScreen(
    uiState: SelectivityUiState,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onUpstreamTypeChange: (ProtectiveDeviceType) -> Unit,
    onUpstreamRatingChange: (String) -> Unit,
    onDownstreamTypeChange: (ProtectiveDeviceType) -> Unit,
    onDownstreamRatingChange: (String) -> Unit,
    onFaultCurrentChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onReset: () -> Unit,
    onApplyExample: (WorkedExample<SelectivityUiState>) -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.calculator_selectivity_title),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
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
                )
            }
        },
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = spacing.xl),
        ) {
            // The result sits above the form once it exists: after pressing
            // Calculate the user is looking for the number, not the fields
            // they just finished filling in. The workings and the notes stay
            // below, where they are read second.
            uiState.result?.let { result ->
                ResultCard(result)
                ElecResultActions(
                    onCopy = onCopy,
                    onShare = onShare,
                    onExportPdf = onExportPdf,
                    exportLocked = exportLocked,
                    modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
                )
            }

            ElecExamplesCard(
                examples = selectivityExamples,
                onSelect = onApplyExample,
                hasResult = uiState.result != null,
                modifier = Modifier.padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.xs,
                ),
            )

            ElecSectionHeader(title = stringResource(Res.string.calculator_inputs))

            ElecCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
            ) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    ElecOptionSelector(
                        label = stringResource(Res.string.sel_upstream),
                        options = ProtectiveDeviceType.entries.toImmutableList(),
                        selected = uiState.upstreamType,
                        onSelect = onUpstreamTypeChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    ElecNumericField(
                        value = uiState.upstreamRating,
                        onValueChange = onUpstreamRatingChange,
                        label = stringResource(Res.string.sel_upstream_rating),
                        unit = "A",
                        error = uiState.errors[SelectivityField.UPSTREAM_RATING],
                    )
                    ElecOptionSelector(
                        label = stringResource(Res.string.sel_downstream),
                        options = ProtectiveDeviceType.entries.toImmutableList(),
                        selected = uiState.downstreamType,
                        onSelect = onDownstreamTypeChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    ElecNumericField(
                        value = uiState.downstreamRating,
                        onValueChange = onDownstreamRatingChange,
                        label = stringResource(Res.string.sel_downstream_rating),
                        unit = "A",
                        error = uiState.errors[SelectivityField.DOWNSTREAM_RATING],
                    )
                    ElecNumericField(
                        value = uiState.faultCurrent,
                        onValueChange = onFaultCurrentChange,
                        label = stringResource(Res.string.sel_fault_current),
                        unit = "A",
                        error = uiState.errors[SelectivityField.FAULT_CURRENT],
                        supportingText = stringResource(Res.string.sel_fault_hint),
                        imeAction = ImeAction.Done,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                OutlinedButton(onClick = onReset) {
                    Text(stringResource(Res.string.action_reset))
                }
                Button(onClick = onCalculate, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.action_calculate))
                }
            }

            // Above the result rather than below it. The method is worth
            // reading before there is an answer, and a card that sits
            // under the working moves down the page the moment somebody
            // presses calculate — which is exactly when they were
            // reading it.
            ElecFormulaCard(
                title = stringResource(Res.string.calculator_formula),
                formula = stringResource(Res.string.sel_formula),
                variables = persistentListOf(
                        FormulaVariable("n", stringResource(Res.string.sel_var_n), "—"),
                        FormulaVariable("I_n(up)", stringResource(Res.string.sel_var_inup), "A"),
                        FormulaVariable("I_n(down)", stringResource(Res.string.sel_var_indn), "A"),
                        FormulaVariable("m", stringResource(Res.string.sel_var_m), "—"),
                        FormulaVariable("I_inst", stringResource(Res.string.sel_var_iinst), "A"),
                ),
                modifier = Modifier.padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.xs,
                ),
            )

            if (uiState.result != null) {
                ElecStepsCard(
                    steps = uiState.steps,
                    modifier = Modifier.padding(
                        horizontal = spacing.screenHorizontal,
                        vertical = spacing.xs,
                    ),
                )
                NotesCard(onLinkClick = onReferenceClick)
            }
        }
    }
}

@Composable
private fun ResultCard(result: SelectivityResult) {
    // The headline is the fault level selectivity survives to, because that is
    // the number a designer writes down. The verdict itself is the status line
    // under it — "selective", "partial", "not" — where every other calculator
    // puts its verdict. Where the pair is selective at any fault the standard
    // models, there is no limit to print and the figure is a dash.
    ElecResultCard(
        label = stringResource(Res.string.sel_result_limit),
        value = result.limitAmps?.format() ?: "—",
        unit = if (result.limitAmps != null) "A" else "",
        modifier = Modifier.padding(
            horizontal = ElecTheme.spacing.screenHorizontal,
            vertical = ElecTheme.spacing.xs,
        ),
        tone = when (result.grade) {
            SelectivityGrade.SELECTIVE -> ResultTone.SUCCESS
            SelectivityGrade.PARTIAL -> ResultTone.WARNING
            SelectivityGrade.NONE -> ResultTone.ERROR
        },
        statusMessage = stringResource(result.grade.summary()),
        secondaryRows = persistentListOf(
            ResultRow(stringResource(Res.string.sel_result_ratio), result.ratio.format(), ""),
            *result.upstreamInstantaneousAmps?.let {
                arrayOf(
                    ResultRow(
                        stringResource(Res.string.sel_result_upstream_threshold),
                        it.format(),
                        "A",
                    ),
                )
            }.orEmpty(),
            *result.downstreamInstantaneousAmps?.let {
                arrayOf(
                    ResultRow(
                        stringResource(Res.string.sel_result_downstream_threshold),
                        it.format(),
                        "A",
                    ),
                )
            }.orEmpty(),
        ),
    )
}

@Composable
private fun NotesCard(onLinkClick: (String) -> Unit) {
    val spacing = ElecTheme.spacing
    // The shared card, like the other sixteen calculators. Its own was
    // a plain container of paragraphs, which is why this screen had
    // nowhere to put the references its notes were already talking about.
    ElecNotesCard(
        title = stringResource(Res.string.calculator_notes),
        notes = buildList {
            add(stringResource(Res.string.sel_note_overload))
            add(stringResource(Res.string.sel_note_bound))
        }.toImmutableList(),
        links = persistentListOf(
            NoteLink(
                topicKey = "primer_selectivity",
                label = stringResource(ReferenceCatalog.titleOf("primer_selectivity")),
            ),
            NoteLink(
                topicKey = "breaker_curves",
                label = stringResource(ReferenceCatalog.titleOf("breaker_curves")),
            ),
        ),
        onLinkClick = onLinkClick,
        modifier = Modifier.padding(
            horizontal = spacing.screenHorizontal,
            vertical = spacing.xs,
        ),
    )
}

private fun ProtectiveDeviceType.label(): StringResource = when (this) {
    ProtectiveDeviceType.MCB_TYPE_B -> Res.string.ef_device_b
    ProtectiveDeviceType.MCB_TYPE_C -> Res.string.ef_device_c
    ProtectiveDeviceType.MCB_TYPE_D -> Res.string.ef_device_d
    ProtectiveDeviceType.CUSTOM -> Res.string.ef_device_custom
    ProtectiveDeviceType.RCD -> Res.string.ef_device_rcd
}

private fun Double.format() =
    com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter.format(this, decimals = 2)

@Composable
private fun rememberShareText(
    title: String,
    uiState: SelectivityUiState,
    result: SelectivityResult,
): String {
    val upstream = line(
        stringResource(Res.string.sel_upstream),
        "${stringResource(uiState.upstreamType.label())} · ${uiState.upstreamRating} A",
    )
    val downstream = line(
        stringResource(Res.string.sel_downstream),
        "${stringResource(uiState.downstreamType.label())} · ${uiState.downstreamRating} A",
    )
    val fault = line(stringResource(Res.string.sel_fault_current), "${uiState.faultCurrent} A")
    val verdict = stringResource(result.grade.summary())
    val limit = line(
        stringResource(Res.string.sel_result_limit),
        result.limitAmps?.let { "${it.format()} A" } ?: "—",
    )
    val ratio = line(stringResource(Res.string.sel_result_ratio), result.ratio.format())

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(upstream)
        appendLine(downstream)
        appendLine(fault)
        appendLine(EXPORT_SEPARATOR)
        appendLine(verdict)
        appendLine(limit)
        append(ratio)
    }
}

@Composable
private fun line(label: String, value: String): String =
    stringResource(Res.string.calculator_export_line, label, value)

private const val EXPORT_SEPARATOR = "— — —"
