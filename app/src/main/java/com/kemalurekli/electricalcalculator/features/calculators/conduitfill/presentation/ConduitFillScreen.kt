package com.kemalurekli.electricalcalculator.features.calculators.conduitfill.presentation

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
import androidx.compose.foundation.layout.size
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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCableRow
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
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.ConduitFillResult
import com.kemalurekli.electricalcalculator.features.calculators.conduitfill.domain.FillRule
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun ConduitFillRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConduitFillViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_conduit_fill_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    ConduitFillScreen(
        uiState = uiState,
        onConduitDiameterChange = viewModel::onConduitDiameterChange,
        onRuleChange = viewModel::onRuleChange,
        onCustomLimitChange = viewModel::onCustomLimitChange,
        onCableDiameterChange = viewModel::onCableDiameterChange,
        onCableQuantityChange = viewModel::onCableQuantityChange,
        onAddCable = viewModel::onAddCable,
        onRemoveCable = viewModel::onRemoveCable,
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
fun ConduitFillScreen(
    uiState: ConduitFillUiState,
    onConduitDiameterChange: (String) -> Unit,
    onRuleChange: (FillRule) -> Unit,
    onCustomLimitChange: (String) -> Unit,
    onCableDiameterChange: (Int, String) -> Unit,
    onCableQuantityChange: (Int, String) -> Unit,
    onAddCable: () -> Unit,
    onRemoveCable: (Int) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<ConduitFillUiState>) -> Unit,
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
                    title = stringResource(R.string.calculator_conduit_fill_title),
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
                        examples = conduitFillExamples,
                        onSelect = onApplyExample,
                    )
                }

                item(key = "inputs-header") {
                    ElecSectionHeader(
                        title = stringResource(R.string.calculator_inputs),
                        modifier = Modifier.padding(horizontal = 0.dp),
                    )
                }

                item(key = "conduit-diameter") {
                    ElecNumericField(
                        value = uiState.conduitDiameter,
                        onValueChange = onConduitDiameterChange,
                        label = stringResource(R.string.cf_conduit_diameter),
                        unit = "mm",
                        error = uiState.errors[ConduitFillField.CONDUIT_DIAMETER],
                        supportingText = stringResource(R.string.cf_conduit_diameter_hint),
                    )
                }

                item(key = "cables-header") {
                    ElecSectionHeader(
                        title = stringResource(R.string.cable_list_title),
                        modifier = Modifier.padding(horizontal = 0.dp),
                    )
                }

                items(
                    count = uiState.cables.size,
                    key = { index -> "cable-${uiState.cables[index].id}" },
                ) { index ->
                    val row = uiState.cables[index]
                    ElecCableRow(
                        diameter = row.diameter,
                        quantity = row.quantity,
                        onDiameterChange = { onCableDiameterChange(row.id, it) },
                        onQuantityChange = { onCableQuantityChange(row.id, it) },
                        onRemove = { onRemoveCable(row.id) },
                        diameterError = row.diameterError,
                        quantityError = row.quantityError,
                        canRemove = uiState.canRemoveCable,
                        isLast = index == uiState.cables.lastIndex,
                    )
                }

                if (uiState.canAddCable) {
                    item(key = "add-cable") {
                        OutlinedButton(onClick = onAddCable) {
                            Icon(
                                imageVector = ElecIcons.Add,
                                contentDescription = null,
                                modifier = Modifier.size(ICON_SIZE),
                            )
                            Text(
                                text = stringResource(R.string.cable_list_add),
                                modifier = Modifier.padding(start = spacing.xs),
                            )
                        }
                    }
                }

                item(key = "rule") {
                    ElecOptionSelector(
                        label = stringResource(R.string.cf_rule),
                        options = FillRule.entries.toImmutableList(),
                        selected = uiState.rule,
                        onSelect = onRuleChange,
                        optionLabel = { stringResource(it.labelRes()) },
                    )
                }

                if (uiState.showCustomLimit) {
                    item(key = "custom-limit") {
                        ElecNumericField(
                            value = uiState.customLimit,
                            onValueChange = onCustomLimitChange,
                            label = stringResource(R.string.cf_custom_limit),
                            unit = "%",
                            error = uiState.errors[ConduitFillField.CUSTOM_LIMIT],
                            supportingText = stringResource(R.string.cf_custom_limit_hint),
                            imeAction = ImeAction.Done,
                        )
                    }
                } else {
                    item(key = "rule-hint") {
                        Text(
                            text = stringResource(R.string.cf_rule_nec_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = spacing.xs),
                        )
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
                        formula = stringResource(R.string.cf_formula),
                        variables = persistentListOf(
                            FormulaVariable("A", stringResource(R.string.cf_var_a), "mm²"),
                            FormulaVariable("D", stringResource(R.string.cf_var_bigd), "mm"),
                            FormulaVariable("d", stringResource(R.string.cf_var_d), "mm"),
                            FormulaVariable("n", stringResource(R.string.cf_var_n), "—"),
                        ),
                    )
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.cf_note_why),
                            stringResource(R.string.cf_note_nec),
                            stringResource(R.string.cf_note_standards),
                            stringResource(R.string.cf_note_diameter),
                            stringResource(R.string.cf_note_pull),
                            stringResource(R.string.cf_note_grouping),
                        ),
                        links = persistentListOf(
                            NoteLink(
                                topicKey = "selection_cabletype",
                                label = stringResource(ReferenceCatalog.titleResOf("selection_cabletype")),
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
    result: ConduitFillResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing
    val permitted = NumberFormatter.format(result.permittedFraction * PERCENT, LIMIT_DECIMALS)

    val tone = when {
        !result.isWithinLimit -> ResultTone.ERROR
        result.isNearLimit -> ResultTone.WARNING
        else -> ResultTone.SUCCESS
    }
    val status = when {
        !result.isWithinLimit -> stringResource(R.string.cf_result_over, permitted)
        result.isNearLimit -> stringResource(R.string.cf_result_near, permitted)
        else -> stringResource(R.string.cf_result_within, permitted)
    }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.cf_result_fill),
            value = NumberFormatter.format(result.fillFraction * PERCENT, DISPLAY_DECIMALS),
            unit = "%",
            tone = tone,
            statusMessage = status,
            secondaryRows = buildList {
                add(
                    ResultRow(
                        label = stringResource(R.string.cf_result_permitted),
                        value = permitted,
                        unit = "%",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.cf_result_conduit_area),
                        value = NumberFormatter.format(result.conduitAreaMm2, DISPLAY_DECIMALS),
                        unit = "mm²",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.cf_result_cable_area),
                        value = NumberFormatter.format(result.cableAreaMm2, DISPLAY_DECIMALS),
                        unit = "mm²",
                    ),
                )
                // Over the limit the useful number is how much has to come out,
                // so the same figure is relabelled rather than shown negative.
                add(
                    if (result.isWithinLimit) {
                        ResultRow(
                            label = stringResource(R.string.cf_result_spare),
                            value = NumberFormatter.format(result.spareAreaMm2, DISPLAY_DECIMALS),
                            unit = "mm²",
                        )
                    } else {
                        ResultRow(
                            label = stringResource(R.string.cf_result_over_by),
                            value = NumberFormatter.format(-result.spareAreaMm2, DISPLAY_DECIMALS),
                            unit = "mm²",
                        )
                    },
                )
                // Only meaningful while compliant: offering more room in an
                // over-filled conduit would read as contradictory advice.
                if (result.isWithinLimit) {
                    add(
                        ResultRow(
                            label = stringResource(R.string.cf_result_largest),
                            value = if (result.largestAdditionalCableMm > 0.0) {
                                NumberFormatter.format(
                                    result.largestAdditionalCableMm,
                                    DISPLAY_DECIMALS,
                                )
                            } else {
                                stringResource(R.string.cf_result_nothing_fits)
                            },
                            unit = if (result.largestAdditionalCableMm > 0.0) "mm" else "",
                        ),
                    )
                }
                add(
                    ResultRow(
                        label = stringResource(R.string.cf_result_count),
                        value = result.cableCount.toString(),
                        unit = "",
                    ),
                )
            }.toImmutableList(),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: ConduitFillUiState,
    result: ConduitFillResult,
): String {
    val conduit = stringResource(
        R.string.cf_export_conduit,
        uiState.conduitDiameter,
        NumberFormatter.format(result.conduitAreaMm2, DISPLAY_DECIMALS),
    )
    // The template is read once outside the loop: `stringResource` is composable
    // and cannot be called from inside `joinToString`'s lambda.
    val cableLineTemplate = stringResource(R.string.cf_export_cable_line)
    val cableList = uiState.cables.joinToString(separator = " + ") { row ->
        cableLineTemplate.format(row.quantity, row.diameter)
    }
    val cables = stringResource(
        R.string.cf_export_cables,
        cableList,
        NumberFormatter.format(result.cableAreaMm2, DISPLAY_DECIMALS),
    )
    val fill = stringResource(
        R.string.cf_export_fill,
        NumberFormatter.format(result.fillFraction * PERCENT, DISPLAY_DECIMALS),
        NumberFormatter.format(result.permittedFraction * PERCENT, LIMIT_DECIMALS),
    )
    val verdict = if (result.isWithinLimit) {
        stringResource(
            R.string.cf_result_within,
            NumberFormatter.format(result.permittedFraction * PERCENT, LIMIT_DECIMALS),
        )
    } else {
        stringResource(
            R.string.cf_result_over,
            NumberFormatter.format(result.permittedFraction * PERCENT, LIMIT_DECIMALS),
        )
    }

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(conduit)
        appendLine(cables)
        appendLine(EXPORT_SEPARATOR)
        appendLine(fill)
        append(verdict)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val LIMIT_DECIMALS = 0
private const val PERCENT = 100.0
private val ICON_SIZE = 18.dp
private const val EXPORT_SEPARATOR = "— — —"

private fun FillRule.labelRes(): Int = when (this) {
    FillRule.NEC_TABLE_1 -> R.string.cf_rule_nec
    FillRule.CUSTOM -> R.string.cf_rule_custom
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun ConduitFillScreenPreview() {
    ElecToolkitTheme {
        ConduitFillScreen(
            uiState = ConduitFillUiState(
                conduitDiameter = "25",
                cables = persistentListOf(
                    CableRowState(id = 0, diameter = "8.5", quantity = "3"),
                    CableRowState(id = 1, diameter = "11.9", quantity = "1"),
                ),
            ),
            onConduitDiameterChange = {}, onRuleChange = {}, onCustomLimitChange = {},
            onCableDiameterChange = { _, _ -> }, onCableQuantityChange = { _, _ -> },
            onAddCable = {}, onRemoveCable = {}, onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {},
            onToggleFavorite = {}, onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
