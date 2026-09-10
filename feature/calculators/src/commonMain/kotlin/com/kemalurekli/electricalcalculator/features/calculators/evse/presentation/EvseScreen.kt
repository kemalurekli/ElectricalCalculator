package com.kemalurekli.electricalcalculator.features.calculators.evse.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_var_ib
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_var_f
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_var_n
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_var_in
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_var_u
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_var_k
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.core.designsystem.component.FormulaVariable
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecFormulaCard
import com.kemalurekli.electricalcalculator.features.calculators.presentation.NameplateScanAction
import com.kemalurekli.electricalcalculator.core.vision.NameplateReading
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
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
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.DcFaultDetection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseConnection
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.EvseResult
import com.kemalurekli.electricalcalculator.features.calculators.evse.domain.RcdRequirement
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_evse_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_connection
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_connection_single
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_connection_three
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_dc_builtin
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_dc_detection
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_dc_none
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_note_continuous
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_note_dc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_note_scope
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_point_count
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_rated_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_rcd_a
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_rcd_b
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_result_connected
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_result_design
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_result_device
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_result_device_none
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_result_per_point
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_result_rcd
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_result_total_kw
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_simultaneity
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ev_simultaneity_hint
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove

@Composable
fun EvseRoute(
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    recordId: Long? = null,
    viewModel: EvseViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }

    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_evse_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current

    EvseScreen(
        uiState = uiState,
        onCopy = {
            summary?.let {
                if (sharing.copy(title, it)) {
                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                }
            }
        },
        onShare = { summary?.let { sharing.share(shareSubject, it) } },
        // A charge point's own plate. Phases land on this screen's connection
        // rather than on a supply system, which is the enum it happens to use.
        onNameplate = { plate ->
            plate.phases?.let {
                viewModel.onConnectionChange(
                    if (it == 1) EvseConnection.SINGLE_PHASE else EvseConnection.THREE_PHASE,
                )
            }
            plate.currentAmperes?.let {
                viewModel.onRatedCurrentChange(NumberFormatter.formatSignificant(it))
            }
            plate.voltageVolts?.let {
                viewModel.onSupplyVoltageChange(NumberFormatter.formatSignificant(it))
            }
        },
        onExportPdf = summary?.let { text -> { export.export(title, text) } },
        exportLocked = !export.isPro,
        snackbarHostState = snackbarHostState,
        onPointCountChange = viewModel::onPointCountChange,
        onRatedCurrentChange = viewModel::onRatedCurrentChange,
        onConnectionChange = viewModel::onConnectionChange,
        onSupplyVoltageChange = viewModel::onSupplyVoltageChange,
        onSimultaneityChange = viewModel::onSimultaneityChange,
        onDcDetectionChange = viewModel::onDcDetectionChange,
        onCalculate = viewModel::onCalculate,
        onReset = viewModel::onReset,
        onApplyExample = viewModel::onApplyExample,
        onToggleFavorite = viewModel::onToggleFavorite,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvseScreen(
    uiState: EvseUiState,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onNameplate: (NameplateReading) -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onPointCountChange: (String) -> Unit,
    onRatedCurrentChange: (String) -> Unit,
    onConnectionChange: (EvseConnection) -> Unit,
    onSupplyVoltageChange: (String) -> Unit,
    onSimultaneityChange: (String) -> Unit,
    onDcDetectionChange: (DcFaultDetection) -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onApplyExample: (WorkedExample<EvseUiState>) -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.calculator_evse_title),
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
                examples = evseExamples,
                onSelect = onApplyExample,
                hasResult = uiState.result != null,
                modifier = Modifier.padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.xs,
                ),
            )

            ElecSectionHeader(title = stringResource(Res.string.calculator_inputs))

            // Under the heading and above the first field, where
            // somebody who has just walked up to the equipment is
            // looking. Absent on a device with no camera.
            NameplateScanAction(
                onApply = onNameplate,
                modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
            )

            ElecCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
            ) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    ElecNumericField(
                        value = uiState.pointCount,
                        onValueChange = onPointCountChange,
                        label = stringResource(Res.string.ev_point_count),
                        error = uiState.errors[EvseField.POINT_COUNT],
                    )
                    ElecNumericField(
                        value = uiState.ratedCurrent,
                        onValueChange = onRatedCurrentChange,
                        label = stringResource(Res.string.ev_rated_current),
                        unit = "A",
                        error = uiState.errors[EvseField.RATED_CURRENT],
                    )
                    ElecOptionSelector(
                        label = stringResource(Res.string.ev_connection),
                        options = EvseConnection.entries.toImmutableList(),
                        selected = uiState.connection,
                        onSelect = onConnectionChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    ElecNumericField(
                        value = uiState.supplyVoltage,
                        onValueChange = onSupplyVoltageChange,
                        label = stringResource(Res.string.common_system_voltage),
                        unit = "V",
                        error = uiState.errors[EvseField.SUPPLY_VOLTAGE],
                    )
                    ElecNumericField(
                        value = uiState.simultaneity,
                        onValueChange = onSimultaneityChange,
                        label = stringResource(Res.string.ev_simultaneity),
                        error = uiState.errors[EvseField.SIMULTANEITY],
                        supportingText = stringResource(Res.string.ev_simultaneity_hint),
                        imeAction = ImeAction.Done,
                    )
                    ElecOptionSelector(
                        label = stringResource(Res.string.ev_dc_detection),
                        options = DcFaultDetection.entries.toImmutableList(),
                        selected = uiState.dcFaultDetection,
                        onSelect = onDcDetectionChange,
                        optionLabel = { stringResource(it.label()) },
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

            if (uiState.result != null) {
                ElecStepsCard(
                    steps = uiState.steps,
                    modifier = Modifier.padding(
                        horizontal = spacing.screenHorizontal,
                        vertical = spacing.xs,
                    ),
                )
                NotesCard(
                    showDcNote = uiState.result?.rcdRequirement != RcdRequirement.TYPE_B,
                )
            }

            // Outside the result block on purpose: the method is worth
            // reading before there is an answer, and these four screens
            // were the only ones that never showed it.
            ElecFormulaCard(
                title = stringResource(Res.string.calculator_formula),
                formula = stringResource(Res.string.ev_formula),
                variables = persistentListOf(
                        FormulaVariable("k", stringResource(Res.string.ev_var_k), "—"),
                        FormulaVariable("U", stringResource(Res.string.ev_var_u), "V"),
                        FormulaVariable("I_n", stringResource(Res.string.ev_var_in), "A"),
                        FormulaVariable("n", stringResource(Res.string.ev_var_n), "—"),
                        FormulaVariable("f", stringResource(Res.string.ev_var_f), "—"),
                        FormulaVariable("I_b", stringResource(Res.string.ev_var_ib), "A"),
                ),
                modifier = Modifier.padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.xs,
                ),
            )
        }
    }
}

@Composable
private fun ResultCard(result: EvseResult) {
    // The design current is the headline because it is what the rest of the
    // design hangs off. The RCD type stays the last row rather than being
    // buried among the currents — it is the answer most often got wrong — and
    // when a Type B is what the installation needs, that becomes the card's
    // status line, since it is a requirement a designer has to act on rather
    // than a figure to read.
    val needsTypeB = result.rcdRequirement == RcdRequirement.TYPE_B
    ElecResultCard(
        label = stringResource(Res.string.ev_result_design),
        value = result.designCurrentAmps.f(),
        unit = "A",
        modifier = Modifier.padding(
            horizontal = ElecTheme.spacing.screenHorizontal,
            vertical = ElecTheme.spacing.xs,
        ),
        tone = if (needsTypeB) ResultTone.WARNING else ResultTone.NEUTRAL,
        statusMessage = if (needsTypeB) stringResource(Res.string.ev_note_dc) else null,
        secondaryRows = persistentListOf(
            ResultRow(stringResource(Res.string.ev_result_per_point), result.powerPerPointKw.f(), "kW"),
            ResultRow(stringResource(Res.string.ev_result_total_kw), result.totalConnectedKw.f(), "kW"),
            ResultRow(
                stringResource(Res.string.ev_result_connected),
                result.totalConnectedAmps.f(),
                "A",
            ),
            ResultRow(
                label = stringResource(Res.string.ev_result_device),
                value = result.deviceRatingAmps?.f() ?: "—",
                unit = if (result.deviceRatingAmps != null) "A" else "",
            ),
            ResultRow(
                stringResource(Res.string.ev_result_rcd),
                stringResource(result.rcdRequirement.label()),
                "",
            ),
        ),
    )
}

@Composable
private fun NotesCard(showDcNote: Boolean) {
    val spacing = ElecTheme.spacing
    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            // The DC note is the result card's status line when a Type B is
            // what the installation needs, and printing the same paragraph
            // twice on one screen teaches the reader to skip both.
            listOfNotNull(
                Res.string.ev_note_dc.takeIf { showDcNote },
                Res.string.ev_note_continuous,
                Res.string.ev_note_scope,
            )
                .forEach { note ->
                    Text(
                        text = stringResource(note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
        }
    }
}

private fun EvseConnection.label(): StringResource = when (this) {
    EvseConnection.SINGLE_PHASE -> Res.string.ev_connection_single
    EvseConnection.THREE_PHASE -> Res.string.ev_connection_three
}

private fun DcFaultDetection.label(): StringResource = when (this) {
    DcFaultDetection.BUILT_IN_6MA -> Res.string.ev_dc_builtin
    DcFaultDetection.NONE -> Res.string.ev_dc_none
}

private fun RcdRequirement.label(): StringResource = when (this) {
    RcdRequirement.TYPE_A -> Res.string.ev_rcd_a
    RcdRequirement.TYPE_B -> Res.string.ev_rcd_b
}

private fun Double.f() = NumberFormatter.format(this, decimals = 2)

@Composable
private fun rememberShareText(
    title: String,
    uiState: EvseUiState,
    result: EvseResult,
): String {
    val points = line(
        stringResource(Res.string.ev_point_count),
        "${uiState.pointCount} × ${uiState.ratedCurrent} A",
    )
    val supply = line(
        stringResource(Res.string.ev_connection),
        "${stringResource(uiState.connection.label())} · ${uiState.supplyVoltage} V",
    )
    val simultaneity = line(stringResource(Res.string.ev_simultaneity), uiState.simultaneity)
    val design = line(stringResource(Res.string.ev_result_design), "${result.designCurrentAmps.f()} A")
    val connected = line(
        stringResource(Res.string.ev_result_connected),
        "${result.totalConnectedAmps.f()} A · ${result.totalConnectedKw.f()} kW",
    )
    val device = line(
        stringResource(Res.string.ev_result_device),
        result.deviceRatingAmps?.let { "${it.f()} A" }
            ?: stringResource(Res.string.ev_result_device_none),
    )
    val rcd = line(
        stringResource(Res.string.ev_result_rcd),
        stringResource(result.rcdRequirement.label()),
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(points)
        appendLine(supply)
        appendLine(simultaneity)
        appendLine(EXPORT_SEPARATOR)
        appendLine(design)
        appendLine(connected)
        appendLine(device)
        append(rcd)
    }
}

@Composable
private fun line(label: String, value: String): String =
    stringResource(Res.string.calculator_export_line, label, value)

private const val EXPORT_SEPARATOR = "— — —"
