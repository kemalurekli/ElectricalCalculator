package com.kemalurekli.electricalcalculator.features.calculators.solarstring.presentation

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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExplainerCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
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
import com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain.SolarStringResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes_tab
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_solar_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_coefficient_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_coefficient_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_export_module
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_export_range
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_export_voc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_inverter_max_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_max_temperature_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_max_temperature_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_min_temperature_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_min_temperature_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_mppt_min_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_note_cell_temperature
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_note_cold
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_note_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_note_hot
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_note_rounding
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_result_max
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_result_min
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_result_status_infeasible
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_result_status_ok
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_result_string_voc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_result_vmp_hot
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_result_voc_cold
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_var_beta
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_var_nmax
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_var_t
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_var_vinv
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_var_vmp
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_var_voc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_vmp_label
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ss_voc_label
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun SolarStringRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: SolarStringViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_solar_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

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
        title = stringResource(Res.string.calculator_solar_title),
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
                    examples = solarStringExamples,
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

            item(key = "voc") {
                ElecNumericField(
                    value = uiState.voc,
                    onValueChange = onVocChange,
                    label = stringResource(Res.string.ss_voc_label),
                    unit = "V",
                    error = uiState.errors[SolarStringField.VOC],
                )
            }

            item(key = "vmp") {
                ElecNumericField(
                    value = uiState.vmp,
                    onValueChange = onVmpChange,
                    label = stringResource(Res.string.ss_vmp_label),
                    unit = "V",
                    error = uiState.errors[SolarStringField.VMP],
                )
            }

            item(key = "beta") {
                ElecNumericField(
                    value = uiState.coefficient,
                    onValueChange = onCoefficientChange,
                    label = stringResource(Res.string.ss_coefficient_label),
                    unit = "%/K",
                    error = uiState.errors[SolarStringField.COEFFICIENT],
                    supportingText = stringResource(Res.string.ss_coefficient_hint),
                )
            }

            item(key = "mintemp") {
                ElecNumericField(
                    value = uiState.minTemperature,
                    onValueChange = onMinTemperatureChange,
                    label = stringResource(Res.string.ss_min_temperature_label),
                    unit = "°C",
                    error = uiState.errors[SolarStringField.MIN_TEMPERATURE],
                    supportingText = stringResource(Res.string.ss_min_temperature_hint),
                )
            }

            item(key = "maxtemp") {
                ElecNumericField(
                    value = uiState.maxTemperature,
                    onValueChange = onMaxTemperatureChange,
                    label = stringResource(Res.string.ss_max_temperature_label),
                    unit = "°C",
                    error = uiState.errors[SolarStringField.MAX_TEMPERATURE],
                    supportingText = stringResource(Res.string.ss_max_temperature_hint),
                )
            }

            item(key = "invmax") {
                ElecNumericField(
                    value = uiState.inverterMax,
                    onValueChange = onInverterMaxChange,
                    label = stringResource(Res.string.ss_inverter_max_label),
                    unit = "V",
                    error = uiState.errors[SolarStringField.INVERTER_MAX],
                )
            }

            item(key = "mppt") {
                ElecNumericField(
                    value = uiState.mpptMin,
                    onValueChange = onMpptMinChange,
                    label = stringResource(Res.string.ss_mppt_min_label),
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
                        Text(text = stringResource(Res.string.action_reset))
                    }
                    Button(onClick = onCalculate, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(Res.string.action_calculate))
                    }
                }
            }

            item(key = "explainer") {
                ElecExplainerCard(
                    formula = stringResource(Res.string.ss_formula),
                    variables = persistentListOf(
                        FormulaVariable("Voc", stringResource(Res.string.ss_var_voc), "V"),
                        FormulaVariable("Vmp", stringResource(Res.string.ss_var_vmp), "V"),
                        FormulaVariable("β", stringResource(Res.string.ss_var_beta), "%/K"),
                        FormulaVariable("T", stringResource(Res.string.ss_var_t), "°C"),
                        FormulaVariable("n_max", stringResource(Res.string.ss_var_nmax), "—"),
                        FormulaVariable("V_inv", stringResource(Res.string.ss_var_vinv), "V"),
                    ),
                    formulaLabel = stringResource(Res.string.calculator_formula),
                    notesLabel = stringResource(Res.string.calculator_notes_tab),
                    steps = uiState.steps,
                    notes = persistentListOf(
                        stringResource(Res.string.ss_note_cold),
                        stringResource(Res.string.ss_note_hot),
                        stringResource(Res.string.ss_note_cell_temperature),
                        stringResource(Res.string.ss_note_rounding),
                        stringResource(Res.string.ss_note_current),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "selection_battery",
                            label = stringResource(
                                ReferenceCatalog.titleOf("selection_battery"),
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
    result: SolarStringResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
) {
    val spacing = ElecTheme.spacing
    ElecResultCard(
        label = stringResource(Res.string.ss_result_max),
        value = result.maximumModules.toString(),
        unit = "",
        tone = if (result.isFeasible) ResultTone.SUCCESS else ResultTone.WARNING,
        statusMessage = stringResource(if (result.isFeasible) Res.string.ss_result_status_ok else Res.string.ss_result_status_infeasible),
        secondaryRows = persistentListOf(
            ResultRow(
                label = stringResource(Res.string.ss_result_min),
                value = result.minimumModules.toString(),
                unit = "",
            ),
            ResultRow(
                label = stringResource(Res.string.ss_result_voc_cold),
                value = NumberFormatter.format(result.vocAtMinimumTemperature, DISPLAY_DECIMALS),
                unit = "V",
            ),
            ResultRow(
                label = stringResource(Res.string.ss_result_vmp_hot),
                value = NumberFormatter.format(result.vmpAtMaximumTemperature, DISPLAY_DECIMALS),
                unit = "V",
            ),
            ResultRow(
                label = stringResource(Res.string.ss_result_string_voc),
                value = NumberFormatter.format(result.stringVocAtMaximum, DISPLAY_DECIMALS),
                unit = "V",
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
    uiState: SolarStringUiState,
    result: SolarStringResult,
): String {
    val part0 = stringResource(
        Res.string.ss_export_module,
        uiState.voc, uiState.coefficient,
    )
    val part1 = stringResource(
        Res.string.ss_export_range,
        result.minimumModules.toString(), result.maximumModules.toString(),
    )
    val part2 = stringResource(
        Res.string.ss_export_voc,
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
            onCopy = {}, onShare = {}, onExportPdf = null, exportLocked = false, onNavigateBack = {},
        )
    }
}
