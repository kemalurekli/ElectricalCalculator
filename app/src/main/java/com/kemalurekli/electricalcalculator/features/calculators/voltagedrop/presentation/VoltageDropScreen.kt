package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.core.domain.model.SupplySystem
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropResult
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.domain.VoltageDropStatus
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun VoltageDropRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: VoltageDropViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_voltage_drop_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    VoltageDropScreen(
        uiState = uiState,
        onSystemChange = viewModel::onSystemChange,
        onMaterialChange = viewModel::onMaterialChange,
        onVoltageChange = viewModel::onVoltageChange,
        onCurrentChange = viewModel::onCurrentChange,
        onLengthChange = viewModel::onLengthChange,
        onCrossSectionChange = viewModel::onCrossSectionChange,
        onPowerFactorChange = viewModel::onPowerFactorChange,
        onTemperatureChange = viewModel::onTemperatureChange,
        onParallelConductorsChange = viewModel::onParallelConductorsChange,
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
fun VoltageDropScreen(
    uiState: VoltageDropUiState,
    onSystemChange: (SupplySystem) -> Unit,
    onMaterialChange: (ConductorMaterial) -> Unit,
    onVoltageChange: (String) -> Unit,
    onCurrentChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onCrossSectionChange: (String) -> Unit,
    onPowerFactorChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onParallelConductorsChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<VoltageDropUiState>) -> Unit,
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

    // The Calculate button sits below the form, so by the time it is pressed
    // the result — which is inserted at the top — would land off screen. Bring
    // it into view instead of leaving the user to scroll back up hunting for
    // the answer they just asked for.
    LaunchedEffect(uiState.result) {
        if (uiState.result != null) {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Scaffold(
            modifier = Modifier
                .widthIn(max = layout.contentMaxWidth)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ElecTopAppBar(
                    title = stringResource(R.string.calculator_voltage_drop_title),
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
                // The result sits above the form once it exists: after pressing
                // Calculate the user is looking for the number, not the inputs
                // they just finished typing.
                if (uiState.result != null) {
                    item(key = "result") {
                        ResultSection(uiState.result, onCopy, onShare)
                    }
                }

                item(key = "examples") {
                    ElecExamplesCard(
                        examples = voltageDropExamples,
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
                        options = SupplySystem.entries.toImmutableList(),
                        selected = uiState.system,
                        onSelect = onSystemChange,
                        optionLabel = { stringResource(it.labelRes()) },
                    )
                }

                item(key = "material") {
                    ElecOptionSelector(
                        label = stringResource(R.string.common_conductor_material),
                        options = ConductorMaterial.entries.toImmutableList(),
                        selected = uiState.material,
                        onSelect = onMaterialChange,
                        optionLabel = { stringResource(it.labelRes()) },
                    )
                }

                item(key = "voltage") {
                    ElecNumericField(
                        value = uiState.voltage,
                        onValueChange = onVoltageChange,
                        label = stringResource(R.string.common_system_voltage),
                        unit = "V",
                        error = uiState.errors[VoltageDropField.VOLTAGE],
                        supportingText = if (uiState.system == SupplySystem.THREE_PHASE_AC) {
                            stringResource(R.string.common_system_voltage_hint)
                        } else {
                            null
                        },
                    )
                }

                item(key = "current") {
                    ElecNumericField(
                        value = uiState.current,
                        onValueChange = onCurrentChange,
                        label = stringResource(R.string.common_design_current),
                        unit = "A",
                        error = uiState.errors[VoltageDropField.CURRENT],
                    )
                }

                item(key = "length") {
                    ElecNumericField(
                        value = uiState.length,
                        onValueChange = onLengthChange,
                        label = stringResource(R.string.common_route_length),
                        unit = "m",
                        error = uiState.errors[VoltageDropField.LENGTH],
                        supportingText = stringResource(R.string.common_route_length_hint),
                    )
                }

                item(key = "cross-section") {
                    ElecNumericField(
                        value = uiState.crossSection,
                        onValueChange = onCrossSectionChange,
                        label = stringResource(R.string.common_cross_section),
                        unit = "mm²",
                        error = uiState.errors[VoltageDropField.CROSS_SECTION],
                    )
                }

                if (uiState.showPowerFactor) {
                    item(key = "power-factor") {
                        ElecNumericField(
                            value = uiState.powerFactor,
                            onValueChange = onPowerFactorChange,
                            label = stringResource(R.string.common_power_factor),
                            error = uiState.errors[VoltageDropField.POWER_FACTOR],
                        )
                    }
                }

                item(key = "temperature") {
                    ElecNumericField(
                        value = uiState.temperature,
                        onValueChange = onTemperatureChange,
                        label = stringResource(R.string.vd_temperature_label),
                        unit = "°C",
                        error = uiState.errors[VoltageDropField.TEMPERATURE],
                        supportingText = stringResource(R.string.vd_temperature_hint),
                        allowNegative = true,
                    )
                }

                item(key = "parallel") {
                    ElecNumericField(
                        value = uiState.parallelConductors,
                        onValueChange = onParallelConductorsChange,
                        label = stringResource(R.string.common_parallel_conductors),
                        error = uiState.errors[VoltageDropField.PARALLEL_CONDUCTORS],
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
                        formula = stringResource(R.string.vd_formula),
                        variables = persistentListOf(
                            FormulaVariable("ΔU", stringResource(R.string.vd_var_drop), "V"),
                            FormulaVariable("k", stringResource(R.string.vd_var_k), "—"),
                            FormulaVariable("I", stringResource(R.string.vd_var_current), "A"),
                            FormulaVariable("R", stringResource(R.string.vd_var_resistance), "Ω"),
                            FormulaVariable("ρ(θ)", stringResource(R.string.vd_var_resistivity), "Ω·mm²/m"),
                            FormulaVariable("L", stringResource(R.string.vd_var_length), "m"),
                            FormulaVariable("A", stringResource(R.string.vd_var_area), "mm²"),
                            FormulaVariable("n", stringResource(R.string.vd_var_parallel), "—"),
                            FormulaVariable("cos φ", stringResource(R.string.vd_var_power_factor), "—"),
                        ),
                    )
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.vd_note_length),
                            stringResource(R.string.vd_note_temperature),
                            stringResource(R.string.vd_note_reactance),
                            stringResource(R.string.vd_note_limits),
                            stringResource(R.string.vd_note_balanced),
                            stringResource(R.string.vd_note_dc_pf),
                        ),
                        links = persistentListOf(
                            NoteLink(
                                topicKey = "materials",
                                label = stringResource(ReferenceCatalog.titleResOf("materials")),
                            ),
                            NoteLink(
                                topicKey = "primer_cableanatomy",
                                label = stringResource(ReferenceCatalog.titleResOf("primer_cableanatomy")),
                            ),
                            NoteLink(
                                topicKey = "standard_voltages",
                                label = stringResource(ReferenceCatalog.titleResOf("standard_voltages")),
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
    result: VoltageDropResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing
    val percentage = NumberFormatter.format(result.dropPercentage, decimals = 2)

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.vd_result_label),
            value = NumberFormatter.format(result.voltageDrop, decimals = 2),
            unit = "V",
            tone = result.status.tone(),
            statusMessage = stringResource(result.status.messageRes(), percentage),
            secondaryRows = persistentListOf(
                ResultRow(
                    label = stringResource(R.string.vd_result_voltage_at_load),
                    value = NumberFormatter.format(result.voltageAtLoad, decimals = 2),
                    unit = "V",
                ),
                ResultRow(
                    label = stringResource(R.string.vd_result_resistance),
                    value = NumberFormatter.formatSignificant(result.conductorResistance),
                    unit = "Ω",
                ),
                ResultRow(
                    label = stringResource(R.string.vd_result_power_loss),
                    value = NumberFormatter.format(result.powerLossWatts, decimals = 2),
                    unit = "W",
                ),
            ),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

/**
 * Plain-text rendering of the result, used for both copy and share.
 *
 * Every label goes through a string resource. Shared text leaves the app and is
 * pasted into reports and messages, so it has to be in the user's language —
 * hard-coding English here would have silently made the export the one part of
 * the app that never translated.
 */
@Composable
private fun rememberShareText(
    title: String,
    uiState: VoltageDropUiState,
    result: VoltageDropResult,
): String {
    val system = stringResource(uiState.system.labelRes())
    val material = stringResource(uiState.material.labelRes())

    val inputs = stringResource(R.string.vd_export_inputs, system, material)
    val ratings = stringResource(
        R.string.vd_export_ratings,
        uiState.voltage,
        uiState.current,
        uiState.length,
        uiState.crossSection,
    )
    val powerFactor = stringResource(R.string.vd_export_power_factor, uiState.powerFactor)
    val conditions = stringResource(
        R.string.vd_export_conditions,
        uiState.temperature,
        uiState.parallelConductors,
    )
    val drop = stringResource(
        R.string.vd_export_drop,
        NumberFormatter.format(result.voltageDrop, DISPLAY_DECIMALS),
        NumberFormatter.format(result.dropPercentage, DISPLAY_DECIMALS),
    )
    val atLoad = stringResource(
        R.string.vd_export_voltage_at_load,
        NumberFormatter.format(result.voltageAtLoad, DISPLAY_DECIMALS),
    )
    val resistance = stringResource(
        R.string.vd_export_resistance,
        NumberFormatter.formatSignificant(result.conductorResistance),
    )
    val powerLoss = stringResource(
        R.string.vd_export_power_loss,
        NumberFormatter.format(result.powerLossWatts, DISPLAY_DECIMALS),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(inputs)
        appendLine(ratings)
        if (uiState.showPowerFactor) appendLine(powerFactor)
        appendLine(conditions)
        appendLine(EXPORT_SEPARATOR)
        appendLine(drop)
        appendLine(atLoad)
        appendLine(resistance)
        append(powerLoss)
    }
}

private const val DISPLAY_DECIMALS = 2
private const val EXPORT_SEPARATOR = "— — —"

private fun SupplySystem.labelRes(): Int = when (this) {
    SupplySystem.DC -> R.string.common_system_dc
    SupplySystem.SINGLE_PHASE_AC -> R.string.common_system_single_phase
    SupplySystem.THREE_PHASE_AC -> R.string.common_system_three_phase
}

private fun ConductorMaterial.labelRes(): Int = when (this) {
    ConductorMaterial.COPPER -> R.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> R.string.common_material_aluminium
}

private fun VoltageDropStatus.tone(): ResultTone = when (this) {
    VoltageDropStatus.WITHIN_LIGHTING_LIMIT -> ResultTone.SUCCESS
    VoltageDropStatus.WITHIN_POWER_LIMIT -> ResultTone.WARNING
    VoltageDropStatus.EXCEEDS_LIMITS -> ResultTone.ERROR
}

private fun VoltageDropStatus.messageRes(): Int = when (this) {
    VoltageDropStatus.WITHIN_LIGHTING_LIMIT -> R.string.vd_status_within_lighting
    VoltageDropStatus.WITHIN_POWER_LIMIT -> R.string.vd_status_within_power
    VoltageDropStatus.EXCEEDS_LIMITS -> R.string.vd_status_exceeds
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun VoltageDropScreenPreview() {
    ElecToolkitTheme {
        VoltageDropScreen(
            uiState = VoltageDropUiState(
                voltage = "230",
                current = "20",
                length = "30",
                crossSection = "4",
            ),
            onSystemChange = {}, onMaterialChange = {}, onVoltageChange = {},
            onCurrentChange = {}, onLengthChange = {}, onCrossSectionChange = {},
            onPowerFactorChange = {}, onTemperatureChange = {},
            onParallelConductorsChange = {}, onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {},
            onToggleFavorite = {}, onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
