package com.kemalurekli.electricalcalculator.features.calculators.motorstarting.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackArea
import com.kemalurekli.electricalcalculator.core.feedback.presentation.ElecReportIssue
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes_tab
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.core.designsystem.component.NoteLink
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_var_du
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_var_istart
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_var_u
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_var_sstart
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_var_uk
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_var_st
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_var_ssc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.core.designsystem.component.FormulaVariable
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExplainerCard
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.features.calculators.presentation.NameplateScanAction
import com.kemalurekli.electricalcalculator.core.vision.NameplateReading
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultTone
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_export_line
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import kotlinx.collections.immutable.ImmutableList
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
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.presentation.LocalCalculationExport
import com.kemalurekli.electricalcalculator.features.calculators.presentation.rememberDocumentSteps
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.MotorStartingResult
import com.kemalurekli.electricalcalculator.features.calculators.motorstarting.domain.StartingMethod
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_motor_starting_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_system_voltage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_full_load_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_locked_rotor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_locked_rotor_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method_auto
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method_dol
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method_soft
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_method_star_delta
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_note_scope
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_note_torque
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_dip
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_residual
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_short_circuit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_starting_current
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_starting_kva
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_result_torque
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_transformer_impedance
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_transformer_impedance_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_transformer_kva
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_warn_contactor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.ms_warn_flicker
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove

@Composable
fun MotorStartingRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    recordId: Long? = null,
    viewModel: MotorStartingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }

    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_motor_starting_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }
    val export = LocalCalculationExport.current
    val documentFormula = stringResource(Res.string.ms_formula)
    val documentSteps = rememberDocumentSteps(uiState.steps)

    MotorStartingScreen(
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
        // Two fields, and both of them are on the plate in front of the reader.
        // The locked rotor multiple is printed on many plates too, but it is not
        // one of the figures the reader recognises, so it is still typed.
        onNameplate = { plate ->
            plate.currentAmperes?.let {
                viewModel.onFullLoadCurrentChange(NumberFormatter.formatSignificant(it))
            }
            plate.voltageVolts?.let {
                viewModel.onSupplyVoltageChange(NumberFormatter.formatSignificant(it))
            }
        },
        onExportPdf = summary?.let { text -> { export.export(title, documentFormula, text, documentSteps) } },
        exportLocked = !export.isPro,
        snackbarHostState = snackbarHostState,
        onFullLoadCurrentChange = viewModel::onFullLoadCurrentChange,
        onLockedRotorChange = viewModel::onLockedRotorChange,
        onMethodChange = viewModel::onMethodChange,
        onSupplyVoltageChange = viewModel::onSupplyVoltageChange,
        onTransformerKvaChange = viewModel::onTransformerKvaChange,
        onTransformerImpedanceChange = viewModel::onTransformerImpedanceChange,
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
fun MotorStartingScreen(
    uiState: MotorStartingUiState,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onNameplate: (NameplateReading) -> Unit,
    onReferenceClick: (String) -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onFullLoadCurrentChange: (String) -> Unit,
    onLockedRotorChange: (String) -> Unit,
    onMethodChange: (StartingMethod) -> Unit,
    onSupplyVoltageChange: (String) -> Unit,
    onTransformerKvaChange: (String) -> Unit,
    onTransformerImpedanceChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onApplyExample: (WorkedExample<MotorStartingUiState>) -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.calculator_motor_starting_title),
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
                ResultCard(result, onCopy, onShare, onExportPdf, exportLocked)
            }

            ElecExamplesCard(
                examples = motorStartingExamples,
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
                        value = uiState.fullLoadCurrent,
                        onValueChange = onFullLoadCurrentChange,
                        label = stringResource(Res.string.ms_full_load_current),
                        unit = "A",
                        error = uiState.errors[MotorStartingField.FULL_LOAD_CURRENT],
                    )
                    ElecNumericField(
                        value = uiState.lockedRotorMultiple,
                        onValueChange = onLockedRotorChange,
                        label = stringResource(Res.string.ms_locked_rotor),
                        unit = "× In",
                        error = uiState.errors[MotorStartingField.LOCKED_ROTOR_MULTIPLE],
                        supportingText = stringResource(Res.string.ms_locked_rotor_hint),
                    )
                    ElecOptionSelector(
                        label = stringResource(Res.string.ms_method),
                        options = StartingMethod.entries.toImmutableList(),
                        selected = uiState.method,
                        onSelect = onMethodChange,
                        optionLabel = { stringResource(it.label()) },
                    )
                    ElecNumericField(
                        value = uiState.supplyVoltage,
                        onValueChange = onSupplyVoltageChange,
                        label = stringResource(Res.string.common_system_voltage),
                        unit = "V",
                        error = uiState.errors[MotorStartingField.SUPPLY_VOLTAGE],
                    )
                    ElecNumericField(
                        value = uiState.transformerKva,
                        onValueChange = onTransformerKvaChange,
                        label = stringResource(Res.string.ms_transformer_kva),
                        unit = "kVA",
                        error = uiState.errors[MotorStartingField.TRANSFORMER_KVA],
                    )
                    ElecNumericField(
                        value = uiState.transformerImpedance,
                        onValueChange = onTransformerImpedanceChange,
                        label = stringResource(Res.string.ms_transformer_impedance),
                        unit = "%",
                        error = uiState.errors[MotorStartingField.TRANSFORMER_IMPEDANCE],
                        supportingText = stringResource(Res.string.ms_transformer_impedance_hint),
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


            Explainer(
                steps = uiState.steps,
                onLinkClick = onReferenceClick,
            )

            // After the workings, which is where somebody who has
            // found a mistake ends up.
            ElecReportIssue(
                area = FeedbackArea.calculator(CalculatorId.MOTOR_STARTING.key),
                modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
            )
        }
    }
}

@Composable
private fun ResultCard(
    result: MotorStartingResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: (() -> Unit)?,
    exportLocked: Boolean,
) {
    // Two things can be wrong with a start, and they are not equally wrong. A
    // board that drops its contactors has stopped the process; lighting that
    // dips is noticed and forgiven. The tones say which is which, and only the
    // worse of the two is stated when both are true.
    val tone = when {
        result.risksContactorDropout -> ResultTone.ERROR
        result.visibleFlicker -> ResultTone.WARNING
        else -> ResultTone.NEUTRAL
    }
    ElecResultCard(
        label = stringResource(Res.string.ms_result_dip),
        value = result.dipPercent.f(),
        unit = "%",
        modifier = Modifier.padding(
            horizontal = ElecTheme.spacing.screenHorizontal,
            vertical = ElecTheme.spacing.xs,
        ),
        tone = tone,
        statusMessage = when (tone) {
            ResultTone.ERROR -> stringResource(Res.string.ms_warn_contactor)
            ResultTone.WARNING -> stringResource(Res.string.ms_warn_flicker)
            else -> null
        },
        secondaryRows = persistentListOf(
            ResultRow(stringResource(Res.string.ms_result_residual), result.residualVoltage.f(), "V"),
            ResultRow(
                stringResource(Res.string.ms_result_starting_current),
                result.startingCurrentAmps.f(),
                "A",
            ),
            ResultRow(stringResource(Res.string.ms_result_starting_kva), result.startingKva.f(), "kVA"),
            *if (result.shortCircuitKva.isFinite()) {
                arrayOf(
                    ResultRow(
                        stringResource(Res.string.ms_result_short_circuit),
                        result.shortCircuitKva.f(),
                        "kVA",
                    ),
                )
            } else {
                emptyArray()
            },
            ResultRow(
                stringResource(Res.string.ms_result_torque),
                result.startingTorquePercent.f(),
                "%",
            ),
        ),
        onCopy = onCopy,
        onShare = onShare,
        onExportPdf = onExportPdf,
        exportLocked = exportLocked,
    )
}

@Composable
private fun Explainer(
    steps: ImmutableList<CalculationStep>,
    onLinkClick: (String) -> Unit,
) {
    val spacing = ElecTheme.spacing
    // The same card the other calculators end on. This screen used to split
    // it — the formula above the result, the steps and notes below — which
    // was a reasonable answer while they were three separate cards and is
    // one card's worth of inconsistency now.
    ElecExplainerCard(
        formula = stringResource(Res.string.ms_formula),
        variables = persistentListOf(
            FormulaVariable("S_sc", stringResource(Res.string.ms_var_ssc), "kVA"),
            FormulaVariable("S_t", stringResource(Res.string.ms_var_st), "kVA"),
            FormulaVariable("u_k", stringResource(Res.string.ms_var_uk), "%"),
            FormulaVariable("S_start", stringResource(Res.string.ms_var_sstart), "kVA"),
            FormulaVariable("U", stringResource(Res.string.ms_var_u), "V"),
            FormulaVariable("I_start", stringResource(Res.string.ms_var_istart), "A"),
            FormulaVariable("ΔU/U", stringResource(Res.string.ms_var_du), "—"),
                ),
        formulaLabel = stringResource(Res.string.calculator_formula),
        notesLabel = stringResource(Res.string.calculator_notes_tab),
        steps = steps,
        notes = buildList {
            add(stringResource(Res.string.ms_note_torque))
            add(stringResource(Res.string.ms_note_scope))
        }.toImmutableList(),
        links = persistentListOf(
            NoteLink(
                topicKey = "selection_starting",
                label = stringResource(ReferenceCatalog.titleOf("selection_starting")),
            ),
            NoteLink(
                topicKey = "primer_nameplate",
                label = stringResource(ReferenceCatalog.titleOf("primer_nameplate")),
            ),
        ),
        onLinkClick = onLinkClick,
        modifier = Modifier.padding(
            horizontal = spacing.screenHorizontal,
            vertical = spacing.xs,
        ),
    )
}

private fun StartingMethod.label(): StringResource = when (this) {
    StartingMethod.DIRECT_ON_LINE -> Res.string.ms_method_dol
    StartingMethod.STAR_DELTA -> Res.string.ms_method_star_delta
    StartingMethod.SOFT_STARTER_50 -> Res.string.ms_method_soft
    StartingMethod.AUTOTRANSFORMER_65 -> Res.string.ms_method_auto
}

private fun Double.f() = NumberFormatter.format(this, decimals = 2)

@Composable
private fun rememberShareText(
    title: String,
    uiState: MotorStartingUiState,
    result: MotorStartingResult,
): String {
    val motor = line(
        stringResource(Res.string.ms_full_load_current),
        "${uiState.fullLoadCurrent} A · ${uiState.lockedRotorMultiple} × In",
    )
    val method = line(stringResource(Res.string.ms_method), stringResource(uiState.method.label()))
    val supply = line(
        stringResource(Res.string.ms_transformer_kva),
        "${uiState.transformerKva} kVA · u_k ${uiState.transformerImpedance} % · " +
            "${uiState.supplyVoltage} V",
    )
    val dip = line(stringResource(Res.string.ms_result_dip), "${result.dipPercent.f()} %")
    val residual = line(stringResource(Res.string.ms_result_residual), "${result.residualVoltage.f()} V")
    val starting = line(
        stringResource(Res.string.ms_result_starting_current),
        "${result.startingCurrentAmps.f()} A · ${result.startingKva.f()} kVA",
    )
    val torque = line(
        stringResource(Res.string.ms_result_torque),
        "${result.startingTorquePercent.f()} %",
    )

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(motor)
        appendLine(method)
        appendLine(supply)
        appendLine(EXPORT_SEPARATOR)
        appendLine(dip)
        appendLine(residual)
        appendLine(starting)
        append(torque)
    }
}

@Composable
private fun line(label: String, value: String): String =
    stringResource(Res.string.calculator_export_line, label, value)

private const val EXPORT_SEPARATOR = "— — —"
