package com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation

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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecFormulaCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNotesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultActions
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
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
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.SolarStringResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun SolarStringRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: SolarStringViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_solar_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    SolarStringScreen(
        uiState = uiState,
        onVocChange = viewModel::onVocChange,
        onVmpChange = viewModel::onVmpChange,
        onCoefficientChange = viewModel::onCoefficientChange,
        onMinTemperatureChange = viewModel::onMinTemperatureChange,
        onMaxTemperatureChange = viewModel::onMaxTemperatureChange,
        onInverterMaxChange = viewModel::onInverterMaxChange,
        onMpptMinChange = viewModel::onMpptMinChange,
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
fun SolarStringScreen(
    uiState: SolarStringUiState,
    onVocChange: (String) -> Unit,
    onVmpChange: (String) -> Unit,
    onCoefficientChange: (String) -> Unit,
    onMinTemperatureChange: (String) -> Unit,
    onMaxTemperatureChange: (String) -> Unit,
    onInverterMaxChange: (String) -> Unit,
    onMpptMinChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<SolarStringUiState>) -> Unit,
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
                    title = stringResource(R.string.calculator_solar_title),
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
                        examples = solarStringExamples,
                        onSelect = onApplyExample,
                    )
                }

                item(key = "inputs-header") {
                    ElecSectionHeader(
                        title = stringResource(R.string.calculator_inputs),
                        modifier = Modifier.padding(horizontal = 0.dp),
                    )
                }

                item(key = "voc") {
                    ElecNumericField(
                        value = uiState.voc,
                        onValueChange = onVocChange,
                        label = stringResource(R.string.ss_voc_label),
                        unit = "V",
                        error = uiState.errors[SolarStringField.VOC],
                    )
                }

                item(key = "vmp") {
                    ElecNumericField(
                        value = uiState.vmp,
                        onValueChange = onVmpChange,
                        label = stringResource(R.string.ss_vmp_label),
                        unit = "V",
                        error = uiState.errors[SolarStringField.VMP],
                    )
                }

                item(key = "beta") {
                    ElecNumericField(
                        value = uiState.coefficient,
                        onValueChange = onCoefficientChange,
                        label = stringResource(R.string.ss_coefficient_label),
                        unit = "%/K",
                        error = uiState.errors[SolarStringField.COEFFICIENT],
                        supportingText = stringResource(R.string.ss_coefficient_hint),
                    )
                }

                item(key = "mintemp") {
                    ElecNumericField(
                        value = uiState.minTemperature,
                        onValueChange = onMinTemperatureChange,
                        label = stringResource(R.string.ss_min_temperature_label),
                        unit = "°C",
                        error = uiState.errors[SolarStringField.MIN_TEMPERATURE],
                        supportingText = stringResource(R.string.ss_min_temperature_hint),
                    )
                }

                item(key = "maxtemp") {
                    ElecNumericField(
                        value = uiState.maxTemperature,
                        onValueChange = onMaxTemperatureChange,
                        label = stringResource(R.string.ss_max_temperature_label),
                        unit = "°C",
                        error = uiState.errors[SolarStringField.MAX_TEMPERATURE],
                        supportingText = stringResource(R.string.ss_max_temperature_hint),
                    )
                }

                item(key = "invmax") {
                    ElecNumericField(
                        value = uiState.inverterMax,
                        onValueChange = onInverterMaxChange,
                        label = stringResource(R.string.ss_inverter_max_label),
                        unit = "V",
                        error = uiState.errors[SolarStringField.INVERTER_MAX],
                    )
                }

                item(key = "mppt") {
                    ElecNumericField(
                        value = uiState.mpptMin,
                        onValueChange = onMpptMinChange,
                        label = stringResource(R.string.ss_mppt_min_label),
                        unit = "V",
                        error = uiState.errors[SolarStringField.MPPT_MIN],
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
                        formula = stringResource(R.string.ss_formula),
                        variables = persistentListOf(
                            FormulaVariable("Voc", stringResource(R.string.ss_var_voc), "V"),
                            FormulaVariable("Vmp", stringResource(R.string.ss_var_vmp), "V"),
                            FormulaVariable("β", stringResource(R.string.ss_var_beta), "%/K"),
                            FormulaVariable("T", stringResource(R.string.ss_var_t), "°C"),
                            FormulaVariable("n_max", stringResource(R.string.ss_var_nmax), "—"),
                            FormulaVariable("V_inv", stringResource(R.string.ss_var_vinv), "V"),
                        ),
                    )
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.ss_note_cold),
                            stringResource(R.string.ss_note_hot),
                            stringResource(R.string.ss_note_cell_temperature),
                            stringResource(R.string.ss_note_rounding),
                            stringResource(R.string.ss_note_current),
                        ),
                        links = persistentListOf(
                            NoteLink(
                                topicKey = "selection_battery",
                                label = stringResource(
                                    ReferenceCatalog.titleResOf("selection_battery"),
                                ),
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
    result: SolarStringResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.ss_result_max),
            value = result.maximumModules.toString(),
            unit = "",
            tone = if (result.isFeasible) ResultTone.SUCCESS else ResultTone.WARNING,
            statusMessage = stringResource(if (result.isFeasible) R.string.ss_result_status_ok else R.string.ss_result_status_infeasible),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.ss_result_min),
                    value = result.minimumModules.toString(),
                    unit = "",
                ),
                ResultRow(
                    label = stringResource(R.string.ss_result_voc_cold),
                    value = NumberFormatter.format(result.vocAtMinimumTemperature, DISPLAY_DECIMALS),
                    unit = "V",
                ),
                ResultRow(
                    label = stringResource(R.string.ss_result_vmp_hot),
                    value = NumberFormatter.format(result.vmpAtMaximumTemperature, DISPLAY_DECIMALS),
                    unit = "V",
                ),
                ResultRow(
                    label = stringResource(R.string.ss_result_string_voc),
                    value = NumberFormatter.format(result.stringVocAtMaximum, DISPLAY_DECIMALS),
                    unit = "V",
                ),
            ),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: SolarStringUiState,
    result: SolarStringResult,
): String {
    val part0 = stringResource(
        R.string.ss_export_module,
        uiState.voc, uiState.coefficient,
    )
    val part1 = stringResource(
        R.string.ss_export_range,
        result.minimumModules.toString(), result.maximumModules.toString(),
    )
    val part2 = stringResource(
        R.string.ss_export_voc,
        NumberFormatter.format(result.vocAtMinimumTemperature, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(part0)
        appendLine(part1)
        append(part2)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val EXPORT_SEPARATOR = "— — —"

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun SolarStringScreenPreview() {
    ElecToolkitTheme {
        SolarStringScreen(
            uiState = SolarStringUiState(voc = "49.5", vmp = "41.5", coefficient = "-0.27", mpptMin = "200"),
            onVocChange = {}, onVmpChange = {}, onCoefficientChange = {}, onMinTemperatureChange = {}, onMaxTemperatureChange = {}, onInverterMaxChange = {}, onMpptMinChange = {},
            onCalculate = {}, onApplyExample = {},
            onReferenceClick = {}, onReset = {}, onToggleFavorite = {},
            onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
