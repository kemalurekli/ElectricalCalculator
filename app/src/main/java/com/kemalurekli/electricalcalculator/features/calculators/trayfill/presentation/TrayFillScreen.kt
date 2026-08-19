package com.kemalurekli.electricalcalculator.features.calculators.trayfill.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCableRow
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
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayArrangement
import com.kemalurekli.electricalcalculator.features.calculators.trayfill.domain.TrayFillResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun TrayFillRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: TrayFillViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_cable_tray_fill_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    TrayFillScreen(
        uiState = uiState,
        onTrayWidthChange = viewModel::onTrayWidthChange,
        onTrayDepthChange = viewModel::onTrayDepthChange,
        onArrangementChange = viewModel::onArrangementChange,
        onSpacingChange = viewModel::onSpacingChange,
        onLimitChange = viewModel::onLimitChange,
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
fun TrayFillScreen(
    uiState: TrayFillUiState,
    onTrayWidthChange: (String) -> Unit,
    onTrayDepthChange: (String) -> Unit,
    onArrangementChange: (TrayArrangement) -> Unit,
    onSpacingChange: (String) -> Unit,
    onLimitChange: (String) -> Unit,
    onCableDiameterChange: (Int, String) -> Unit,
    onCableQuantityChange: (Int, String) -> Unit,
    onAddCable: () -> Unit,
    onRemoveCable: (Int) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<TrayFillUiState>) -> Unit,
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
        title = stringResource(R.string.calculator_cable_tray_fill_title),
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
                    examples = trayFillExamples,
                    onSelect = onApplyExample,
                )
            }

            item(key = "inputs-header") {
                ElecSectionHeader(
                    title = stringResource(R.string.calculator_inputs),
                    modifier = Modifier.padding(horizontal = 0.dp),
                )
            }

            item(key = "arrangement") {
                Column {
                    ElecOptionSelector(
                        label = stringResource(R.string.tf_arrangement),
                        options = TrayArrangement.entries.toImmutableList(),
                        selected = uiState.arrangement,
                        onSelect = onArrangementChange,
                        optionLabel = { stringResource(it.labelRes()) },
                    )
                    Text(
                        text = stringResource(uiState.arrangement.hintRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.xs, start = spacing.xs),
                    )
                }
            }

            item(key = "tray-width") {
                ElecNumericField(
                    value = uiState.trayWidth,
                    onValueChange = onTrayWidthChange,
                    label = stringResource(R.string.tf_tray_width),
                    unit = "mm",
                    error = uiState.errors[TrayFillField.TRAY_WIDTH],
                )
            }

            if (uiState.showDepthAndLimit) {
                item(key = "tray-depth") {
                    ElecNumericField(
                        value = uiState.trayDepth,
                        onValueChange = onTrayDepthChange,
                        label = stringResource(R.string.tf_tray_depth),
                        unit = "mm",
                        error = uiState.errors[TrayFillField.TRAY_DEPTH],
                        supportingText = stringResource(R.string.tf_tray_depth_hint),
                    )
                }
            }

            if (uiState.showSpacing) {
                item(key = "spacing") {
                    ElecNumericField(
                        value = uiState.spacing,
                        onValueChange = onSpacingChange,
                        label = stringResource(R.string.tf_spacing),
                        unit = "mm",
                        error = uiState.errors[TrayFillField.SPACING],
                        supportingText = stringResource(R.string.tf_spacing_hint),
                    )
                }
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
                    isLast = index == uiState.cables.lastIndex && !uiState.showDepthAndLimit,
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

            if (uiState.showDepthAndLimit) {
                item(key = "limit") {
                    ElecNumericField(
                        value = uiState.limit,
                        onValueChange = onLimitChange,
                        label = stringResource(R.string.tf_limit),
                        unit = "%",
                        error = uiState.errors[TrayFillField.LIMIT],
                        supportingText = stringResource(R.string.tf_limit_hint),
                        imeAction = ImeAction.Done,
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
                    formula = stringResource(R.string.tf_formula),
                    variables = persistentListOf(
                        FormulaVariable("W_req", stringResource(R.string.tf_var_wreq), "mm"),
                        FormulaVariable("W", stringResource(R.string.tf_var_w), "mm"),
                        FormulaVariable("H", stringResource(R.string.tf_var_h), "mm"),
                        FormulaVariable("d", stringResource(R.string.tf_var_d), "mm"),
                        FormulaVariable("n", stringResource(R.string.tf_var_n), "—"),
                        FormulaVariable("N", stringResource(R.string.tf_var_bign), "—"),
                        FormulaVariable("s", stringResource(R.string.tf_var_s), "mm"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(R.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(R.string.tf_note_arrangement),
                        stringResource(R.string.tf_note_single_layer),
                        stringResource(R.string.tf_note_spacing),
                        stringResource(R.string.tf_note_standards),
                        stringResource(R.string.tf_note_load),
                        stringResource(R.string.tf_note_depth),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "selection_cabletype",
                            label = stringResource(ReferenceCatalog.titleResOf("selection_cabletype")),
                        ),
                        NoteLink(
                            topicKey = "primer_cableanatomy",
                            label = stringResource(ReferenceCatalog.titleResOf("primer_cableanatomy")),
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
    result: TrayFillResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        when (result) {
            is TrayFillResult.SingleLayer -> SingleLayerCard(result)
            is TrayFillResult.MultiLayer -> MultiLayerCard(result)
        }
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun SingleLayerCard(result: TrayFillResult.SingleLayer) {
    ElecResultCard(
        label = stringResource(R.string.tf_result_width),
        value = NumberFormatter.format(result.widthUsedFraction * PERCENT, DISPLAY_DECIMALS),
        unit = "%",
        tone = if (result.isWithinLimit) ResultTone.SUCCESS else ResultTone.ERROR,
        statusMessage = if (result.isWithinLimit) {
            stringResource(
                R.string.tf_result_width_status,
                NumberFormatter.format(result.requiredWidthMm, DISPLAY_DECIMALS),
                NumberFormatter.format(result.trayWidthMm, DISPLAY_DECIMALS),
            )
        } else {
            stringResource(
                R.string.tf_result_width_over,
                NumberFormatter.format(-result.spareWidthMm, DISPLAY_DECIMALS),
            )
        },
        secondaryRows = buildList {
            add(
                ResultRow(
                    label = stringResource(R.string.tf_result_required_width),
                    value = NumberFormatter.format(result.requiredWidthMm, DISPLAY_DECIMALS),
                    unit = "mm",
                ),
            )
            // Over the limit the useful number is the shortfall, so the same
            // figure is relabelled rather than shown as a negative spare.
            add(
                if (result.isWithinLimit) {
                    ResultRow(
                        label = stringResource(R.string.tf_result_spare_width),
                        value = NumberFormatter.format(result.spareWidthMm, DISPLAY_DECIMALS),
                        unit = "mm",
                    )
                } else {
                    ResultRow(
                        label = stringResource(R.string.tf_result_short_by),
                        value = NumberFormatter.format(-result.spareWidthMm, DISPLAY_DECIMALS),
                        unit = "mm",
                    )
                },
            )
            if (result.isWithinLimit) {
                add(
                    ResultRow(
                        label = stringResource(R.string.tf_result_largest),
                        value = if (result.largestAdditionalCableMm > 0.0) {
                            NumberFormatter.format(
                                result.largestAdditionalCableMm,
                                DISPLAY_DECIMALS,
                            )
                        } else {
                            stringResource(R.string.tf_result_nothing_fits)
                        },
                        unit = if (result.largestAdditionalCableMm > 0.0) "mm" else "",
                    ),
                )
            }
            add(
                ResultRow(
                    label = stringResource(R.string.tf_result_cable_area),
                    value = NumberFormatter.format(result.cableAreaMm2, DISPLAY_DECIMALS),
                    unit = "mm²",
                ),
            )
            add(
                ResultRow(
                    label = stringResource(R.string.tf_result_count),
                    value = result.cableCount.toString(),
                    unit = "",
                ),
            )
        }.toImmutableList(),
    )
}

@Composable
private fun MultiLayerCard(result: TrayFillResult.MultiLayer) {
    val permitted = NumberFormatter.format(result.permittedFraction * PERCENT, LIMIT_DECIMALS)

    val tone = when {
        !result.isWithinLimit -> ResultTone.ERROR
        result.isNearLimit -> ResultTone.WARNING
        else -> ResultTone.SUCCESS
    }
    val status = when {
        !result.isWithinLimit -> stringResource(R.string.tf_result_over, permitted)
        result.isNearLimit -> stringResource(R.string.tf_result_near, permitted)
        else -> stringResource(R.string.tf_result_within, permitted)
    }

    ElecResultCard(
        label = stringResource(R.string.tf_result_fill),
        value = NumberFormatter.format(result.fillFraction * PERCENT, DISPLAY_DECIMALS),
        unit = "%",
        tone = tone,
        statusMessage = status,
        secondaryRows = persistentListOf(
            ResultRow(
                label = stringResource(R.string.tf_result_permitted),
                value = permitted,
                unit = "%",
            ),
            ResultRow(
                label = stringResource(R.string.tf_result_tray_area),
                value = NumberFormatter.format(result.trayAreaMm2, DISPLAY_DECIMALS),
                unit = "mm²",
            ),
            ResultRow(
                label = stringResource(R.string.tf_result_cable_area),
                value = NumberFormatter.format(result.cableAreaMm2, DISPLAY_DECIMALS),
                unit = "mm²",
            ),
            if (result.isWithinLimit) {
                ResultRow(
                    label = stringResource(R.string.tf_result_spare_area),
                    value = NumberFormatter.format(result.spareAreaMm2, DISPLAY_DECIMALS),
                    unit = "mm²",
                )
            } else {
                ResultRow(
                    label = stringResource(R.string.tf_result_over_by),
                    value = NumberFormatter.format(-result.spareAreaMm2, DISPLAY_DECIMALS),
                    unit = "mm²",
                )
            },
            ResultRow(
                label = stringResource(R.string.tf_result_depth),
                value = NumberFormatter.format(result.occupiedDepthMm, DISPLAY_DECIMALS),
                unit = "mm",
            ),
            ResultRow(
                label = stringResource(R.string.tf_result_layers),
                value = result.estimatedLayers.toString(),
                unit = "",
            ),
            ResultRow(
                label = stringResource(R.string.tf_result_count),
                value = result.cableCount.toString(),
                unit = "",
            ),
        ),
    )
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: TrayFillUiState,
    result: TrayFillResult,
): String {
    val tray = stringResource(
        R.string.tf_export_tray,
        uiState.trayWidth,
        stringResource(uiState.arrangement.labelRes()),
    )
    val cableLineTemplate = stringResource(R.string.cf_export_cable_line)
    val cables = stringResource(
        R.string.tf_export_cables,
        uiState.cables.joinToString(separator = " + ") { row ->
            cableLineTemplate.format(row.quantity, row.diameter)
        },
    )
    val verdict = when (result) {
        is TrayFillResult.SingleLayer -> stringResource(
            R.string.tf_export_width,
            NumberFormatter.format(result.requiredWidthMm, DISPLAY_DECIMALS),
            NumberFormatter.format(result.trayWidthMm, DISPLAY_DECIMALS),
        )

        is TrayFillResult.MultiLayer -> stringResource(
            R.string.tf_export_fill,
            NumberFormatter.format(result.fillFraction * PERCENT, DISPLAY_DECIMALS),
            NumberFormatter.format(result.permittedFraction * PERCENT, LIMIT_DECIMALS),
        )
    }

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(tray)
        appendLine(cables)
        appendLine(EXPORT_SEPARATOR)
        append(verdict)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val LIMIT_DECIMALS = 0
private const val PERCENT = 100.0
private val ICON_SIZE = 18.dp
private const val EXPORT_SEPARATOR = "— — —"

private fun TrayArrangement.labelRes(): Int = when (this) {
    TrayArrangement.SINGLE_LAYER -> R.string.tf_arrangement_single
    TrayArrangement.MULTI_LAYER -> R.string.tf_arrangement_multi
}

private fun TrayArrangement.hintRes(): Int = when (this) {
    TrayArrangement.SINGLE_LAYER -> R.string.tf_arrangement_single_hint
    TrayArrangement.MULTI_LAYER -> R.string.tf_arrangement_multi_hint
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun TrayFillScreenPreview() {
    ElecToolkitTheme {
        TrayFillScreen(
            uiState = TrayFillUiState(
                trayWidth = "300",
                cables = persistentListOf(
                    TrayCableRowState(id = 0, diameter = "20.5", quantity = "6"),
                ),
            ),
            onTrayWidthChange = {}, onTrayDepthChange = {}, onArrangementChange = {},
            onSpacingChange = {}, onLimitChange = {},
            onCableDiameterChange = { _, _ -> }, onCableQuantityChange = { _, _ -> },
            onAddCable = {}, onRemoveCable = {}, onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {},
            onToggleFavorite = {}, onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
