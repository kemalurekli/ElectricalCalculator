package com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation

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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecFormulaCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNotesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultActions
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecResultCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecExamplesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecStepsCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.FormulaVariable
import com.kemalurekli.electricalcalculator.core.designsystem.component.NoteLink
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultRow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ResultTone
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.designsystem.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_calculate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_cable_weight_title
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_inputs
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_notes
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_share_subject
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_conductor_material
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_material_aluminium
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.common_material_copper
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation_pvc
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cs_insulation_xlpe
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_conductor_count
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_conductor_count_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_cross_section
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_diameter
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_diameter_hint
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_diameter_too_small
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_export_cable
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_export_conductor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_export_sheath
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_export_total
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_formula
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_insulation
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_length
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_note_armour
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_note_drum
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_note_estimate
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_note_exact
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_note_tray
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_result_conductor
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_result_conductor_status
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_result_fraction
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_result_per_km
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_result_sheath
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_result_total
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_result_total_status
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_result_volume
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_var_a
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_var_d
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_var_delta
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_var_deltas
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_var_l
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_var_m
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_var_ms
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.cw_var_n
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.message_copied
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

@Composable
fun CableWeightRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: CableWeightViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val sharing = rememberResultSharing()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(Res.string.calculator_cable_weight_title)
    val copiedMessage = stringResource(Res.string.message_copied)
    val shareSubject = stringResource(Res.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    CableWeightScreen(
        uiState = uiState,
        onMaterialChange = viewModel::onMaterialChange,
        onInsulationChange = viewModel::onInsulationChange,
        onCrossSectionChange = viewModel::onCrossSectionChange,
        onConductorCountChange = viewModel::onConductorCountChange,
        onLengthChange = viewModel::onLengthChange,
        onDiameterChange = viewModel::onDiameterChange,
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
fun CableWeightScreen(
    uiState: CableWeightUiState,
    onMaterialChange: (ConductorMaterial) -> Unit,
    onInsulationChange: (CableInsulation) -> Unit,
    onCrossSectionChange: (String) -> Unit,
    onConductorCountChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onDiameterChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<CableWeightUiState>) -> Unit,
    onReset: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
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
        title = stringResource(Res.string.calculator_cable_weight_title),
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
                item(key = "result") { ResultSection(result, onCopy, onShare) }
            }

            item(key = "examples") {
                ElecExamplesCard(
                    examples = cableWeightExamples,
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

            item(key = "material") {
                ElecOptionSelector(
                    label = stringResource(Res.string.common_conductor_material),
                    options = ConductorMaterial.entries.toImmutableList(),
                    selected = uiState.material,
                    onSelect = onMaterialChange,
                    optionLabel = { stringResource(it.label()) },
                )
            }

            item(key = "cross-section") {
                ElecNumericField(
                    value = uiState.crossSection,
                    onValueChange = onCrossSectionChange,
                    label = stringResource(Res.string.cw_cross_section),
                    unit = "mm²",
                    error = uiState.errors[CableWeightField.CROSS_SECTION],
                )
            }

            item(key = "count") {
                ElecNumericField(
                    value = uiState.conductorCount,
                    onValueChange = onConductorCountChange,
                    label = stringResource(Res.string.cw_conductor_count),
                    error = uiState.errors[CableWeightField.CONDUCTOR_COUNT],
                    supportingText = stringResource(Res.string.cw_conductor_count_hint),
                )
            }

            item(key = "length") {
                ElecNumericField(
                    value = uiState.length,
                    onValueChange = onLengthChange,
                    label = stringResource(Res.string.cw_length),
                    unit = "m",
                    error = uiState.errors[CableWeightField.LENGTH],
                )
            }

            item(key = "diameter") {
                ElecNumericField(
                    value = uiState.diameter,
                    onValueChange = onDiameterChange,
                    label = stringResource(Res.string.cw_diameter),
                    unit = "mm",
                    error = uiState.errors[CableWeightField.DIAMETER],
                    errorMessage = uiState.diameterTooSmallFor?.let {
                        stringResource(
                            Res.string.cw_diameter_too_small,
                            NumberFormatter.format(it, DISPLAY_DECIMALS),
                        )
                    },
                    supportingText = stringResource(Res.string.cw_diameter_hint),
                    imeAction = if (uiState.showInsulation) ImeAction.Next else ImeAction.Done,
                )
            }

            if (uiState.showInsulation) {
                item(key = "insulation") {
                    ElecOptionSelector(
                        label = stringResource(Res.string.cw_insulation),
                        options = CableInsulation.entries.toImmutableList(),
                        selected = uiState.insulation,
                        onSelect = onInsulationChange,
                        optionLabel = { stringResource(it.label()) },
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
                    formula = stringResource(Res.string.cw_formula),
                    variables = persistentListOf(
                        FormulaVariable("m", stringResource(Res.string.cw_var_m), "kg"),
                        FormulaVariable("m_s", stringResource(Res.string.cw_var_ms), "kg"),
                        FormulaVariable("n", stringResource(Res.string.cw_var_n), "—"),
                        FormulaVariable("A", stringResource(Res.string.cw_var_a), "mm²"),
                        FormulaVariable("L", stringResource(Res.string.cw_var_l), "m"),
                        FormulaVariable("D", stringResource(Res.string.cw_var_d), "mm"),
                        FormulaVariable("δ", stringResource(Res.string.cw_var_delta), "kg/dm³"),
                        FormulaVariable("δ_s", stringResource(Res.string.cw_var_deltas), "kg/dm³"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(Res.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(Res.string.cw_note_exact),
                        stringResource(Res.string.cw_note_estimate),
                        stringResource(Res.string.cw_note_armour),
                        stringResource(Res.string.cw_note_tray),
                        stringResource(Res.string.cw_note_drum),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "materials",
                            label = stringResource(ReferenceCatalog.titleOf("materials")),
                        ),
                        NoteLink(
                            topicKey = "primer_cableanatomy",
                            label = stringResource(ReferenceCatalog.titleOf("primer_cableanatomy")),
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
    result: CableWeightResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        // The complete cable is the headline when a diameter made it available;
        // otherwise the conductor mass is the whole answer rather than a detail.
        if (result.hasTotal) {
            ElecResultCard(
                label = stringResource(Res.string.cw_result_total),
                value = NumberFormatter.format(result.totalMassKg!!, DISPLAY_DECIMALS),
                unit = "kg",
                tone = ResultTone.NEUTRAL,
                statusMessage = stringResource(
                    Res.string.cw_result_total_status,
                    NumberFormatter.format(result.totalMassPerMeterKg!!, PER_METRE_DECIMALS),
                ),
                secondaryRows = persistentListOf(
                    ResultRow(
                        label = stringResource(Res.string.cw_result_conductor),
                        value = NumberFormatter.format(result.conductorMassKg, DISPLAY_DECIMALS),
                        unit = "kg",
                    ),
                    ResultRow(
                        label = stringResource(Res.string.cw_result_sheath),
                        value = NumberFormatter.format(
                            result.nonConductorMassKg!!,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "kg",
                    ),
                    ResultRow(
                        label = stringResource(Res.string.cw_result_fraction),
                        value = NumberFormatter.format(
                            result.conductorMassFraction!! * PERCENT,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "%",
                    ),
                    ResultRow(
                        label = stringResource(Res.string.cw_result_volume),
                        value = NumberFormatter.format(result.conductorVolumeDm3, DISPLAY_DECIMALS),
                        unit = "dm³",
                    ),
                ),
            )
        } else {
            ElecResultCard(
                label = stringResource(Res.string.cw_result_conductor),
                value = NumberFormatter.format(result.conductorMassKg, DISPLAY_DECIMALS),
                unit = "kg",
                tone = ResultTone.NEUTRAL,
                statusMessage = stringResource(
                    Res.string.cw_result_conductor_status,
                    NumberFormatter.format(result.conductorMassPerMeterKg, PER_METRE_DECIMALS),
                ),
                secondaryRows = persistentListOf(
                    ResultRow(
                        label = stringResource(Res.string.cw_result_per_km),
                        value = NumberFormatter.format(
                            result.conductorMassPerMeterKg * METRES_PER_KM,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "kg",
                    ),
                    ResultRow(
                        label = stringResource(Res.string.cw_result_volume),
                        value = NumberFormatter.format(result.conductorVolumeDm3, DISPLAY_DECIMALS),
                        unit = "dm³",
                    ),
                ),
            )
        }
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: CableWeightUiState,
    result: CableWeightResult,
): String {
    val cable = stringResource(
        Res.string.cw_export_cable,
        uiState.conductorCount,
        uiState.crossSection,
        stringResource(uiState.material.label()),
        uiState.length,
    )
    val conductor = stringResource(
        Res.string.cw_export_conductor,
        NumberFormatter.format(result.conductorMassKg, DISPLAY_DECIMALS),
        NumberFormatter.format(result.conductorMassPerMeterKg, PER_METRE_DECIMALS),
    )
    val sheath = result.nonConductorMassKg?.let {
        stringResource(Res.string.cw_export_sheath, NumberFormatter.format(it, DISPLAY_DECIMALS))
    }
    val total = result.totalMassKg?.let {
        stringResource(
            Res.string.cw_export_total,
            NumberFormatter.format(it, DISPLAY_DECIMALS),
            NumberFormatter.format(result.totalMassPerMeterKg!!, PER_METRE_DECIMALS),
        )
    }

    return buildString {
        appendLine(title)
        appendLine(EXPORT_SEPARATOR)
        appendLine(cable)
        appendLine(EXPORT_SEPARATOR)
        append(conductor)
        sheath?.let { appendLine(); append(it) }
        total?.let { appendLine(); append(it) }
    }
}

private const val DISPLAY_DECIMALS = 2
private const val PER_METRE_DECIMALS = 3
private const val PERCENT = 100.0
private const val METRES_PER_KM = 1_000.0
private const val EXPORT_SEPARATOR = "— — —"

private fun ConductorMaterial.label(): StringResource = when (this) {
    ConductorMaterial.COPPER -> Res.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> Res.string.common_material_aluminium
}

private fun CableInsulation.label(): StringResource = when (this) {
    CableInsulation.PVC -> Res.string.cs_insulation_pvc
    CableInsulation.XLPE -> Res.string.cs_insulation_xlpe
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun CableWeightScreenPreview() {
    ElecToolkitTheme {
        CableWeightScreen(
            uiState = CableWeightUiState(
                crossSection = "25",
                conductorCount = "4",
                length = "1000",
                diameter = "25",
            ),
            onMaterialChange = {}, onInsulationChange = {}, onCrossSectionChange = {},
            onConductorCountChange = {}, onLengthChange = {}, onDiameterChange = {},
            onCalculate = {}, onApplyExample = {}, onReferenceClick = {}, onReset = {}, onToggleFavorite = {},
            onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
