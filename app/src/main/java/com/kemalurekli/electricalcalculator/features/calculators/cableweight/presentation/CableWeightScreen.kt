package com.kemalurekli.electricalcalculator.features.calculators.cableweight.presentation

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
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.ui.ResultSharing
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.core.ui.model.WorkedExample
import com.kemalurekli.electricalcalculator.features.calculators.cableweight.domain.CableWeightResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun CableWeightRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: CableWeightViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_cable_weight_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
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
                    title = stringResource(R.string.calculator_cable_weight_title),
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
                        examples = cableWeightExamples,
                        onSelect = onApplyExample,
                    )
                }

                item(key = "inputs-header") {
                    ElecSectionHeader(
                        title = stringResource(R.string.calculator_inputs),
                        modifier = Modifier.padding(horizontal = 0.dp),
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

                item(key = "cross-section") {
                    ElecNumericField(
                        value = uiState.crossSection,
                        onValueChange = onCrossSectionChange,
                        label = stringResource(R.string.cw_cross_section),
                        unit = "mm²",
                        error = uiState.errors[CableWeightField.CROSS_SECTION],
                    )
                }

                item(key = "count") {
                    ElecNumericField(
                        value = uiState.conductorCount,
                        onValueChange = onConductorCountChange,
                        label = stringResource(R.string.cw_conductor_count),
                        error = uiState.errors[CableWeightField.CONDUCTOR_COUNT],
                        supportingText = stringResource(R.string.cw_conductor_count_hint),
                    )
                }

                item(key = "length") {
                    ElecNumericField(
                        value = uiState.length,
                        onValueChange = onLengthChange,
                        label = stringResource(R.string.cw_length),
                        unit = "m",
                        error = uiState.errors[CableWeightField.LENGTH],
                    )
                }

                item(key = "diameter") {
                    ElecNumericField(
                        value = uiState.diameter,
                        onValueChange = onDiameterChange,
                        label = stringResource(R.string.cw_diameter),
                        unit = "mm",
                        error = uiState.errors[CableWeightField.DIAMETER],
                        errorMessage = uiState.diameterTooSmallFor?.let {
                            stringResource(
                                R.string.cw_diameter_too_small,
                                NumberFormatter.format(it, DISPLAY_DECIMALS),
                            )
                        },
                        supportingText = stringResource(R.string.cw_diameter_hint),
                        imeAction = if (uiState.showInsulation) ImeAction.Next else ImeAction.Done,
                    )
                }

                if (uiState.showInsulation) {
                    item(key = "insulation") {
                        ElecOptionSelector(
                            label = stringResource(R.string.cw_insulation),
                            options = CableInsulation.entries.toImmutableList(),
                            selected = uiState.insulation,
                            onSelect = onInsulationChange,
                            optionLabel = { stringResource(it.labelRes()) },
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
                        formula = stringResource(R.string.cw_formula),
                        variables = persistentListOf(
                            FormulaVariable("m", stringResource(R.string.cw_var_m), "kg"),
                            FormulaVariable("m_s", stringResource(R.string.cw_var_ms), "kg"),
                            FormulaVariable("n", stringResource(R.string.cw_var_n), "—"),
                            FormulaVariable("A", stringResource(R.string.cw_var_a), "mm²"),
                            FormulaVariable("L", stringResource(R.string.cw_var_l), "m"),
                            FormulaVariable("D", stringResource(R.string.cw_var_d), "mm"),
                            FormulaVariable("δ", stringResource(R.string.cw_var_delta), "kg/dm³"),
                            FormulaVariable("δ_s", stringResource(R.string.cw_var_deltas), "kg/dm³"),
                        ),
                    )
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.cw_note_exact),
                            stringResource(R.string.cw_note_estimate),
                            stringResource(R.string.cw_note_armour),
                            stringResource(R.string.cw_note_tray),
                            stringResource(R.string.cw_note_drum),
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
                label = stringResource(R.string.cw_result_total),
                value = NumberFormatter.format(result.totalMassKg!!, DISPLAY_DECIMALS),
                unit = "kg",
                tone = ResultTone.NEUTRAL,
                statusMessage = stringResource(
                    R.string.cw_result_total_status,
                    NumberFormatter.format(result.totalMassPerMeterKg!!, PER_METRE_DECIMALS),
                ),
                secondaryRows = persistentListOf(
                    ResultRow(
                        label = stringResource(R.string.cw_result_conductor),
                        value = NumberFormatter.format(result.conductorMassKg, DISPLAY_DECIMALS),
                        unit = "kg",
                    ),
                    ResultRow(
                        label = stringResource(R.string.cw_result_sheath),
                        value = NumberFormatter.format(
                            result.nonConductorMassKg!!,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "kg",
                    ),
                    ResultRow(
                        label = stringResource(R.string.cw_result_fraction),
                        value = NumberFormatter.format(
                            result.conductorMassFraction!! * PERCENT,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "%",
                    ),
                    ResultRow(
                        label = stringResource(R.string.cw_result_volume),
                        value = NumberFormatter.format(result.conductorVolumeDm3, DISPLAY_DECIMALS),
                        unit = "dm³",
                    ),
                ),
            )
        } else {
            ElecResultCard(
                label = stringResource(R.string.cw_result_conductor),
                value = NumberFormatter.format(result.conductorMassKg, DISPLAY_DECIMALS),
                unit = "kg",
                tone = ResultTone.NEUTRAL,
                statusMessage = stringResource(
                    R.string.cw_result_conductor_status,
                    NumberFormatter.format(result.conductorMassPerMeterKg, PER_METRE_DECIMALS),
                ),
                secondaryRows = persistentListOf(
                    ResultRow(
                        label = stringResource(R.string.cw_result_per_km),
                        value = NumberFormatter.format(
                            result.conductorMassPerMeterKg * METRES_PER_KM,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "kg",
                    ),
                    ResultRow(
                        label = stringResource(R.string.cw_result_volume),
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
        R.string.cw_export_cable,
        uiState.conductorCount,
        uiState.crossSection,
        stringResource(uiState.material.labelRes()),
        uiState.length,
    )
    val conductor = stringResource(
        R.string.cw_export_conductor,
        NumberFormatter.format(result.conductorMassKg, DISPLAY_DECIMALS),
        NumberFormatter.format(result.conductorMassPerMeterKg, PER_METRE_DECIMALS),
    )
    val sheath = result.nonConductorMassKg?.let {
        stringResource(R.string.cw_export_sheath, NumberFormatter.format(it, DISPLAY_DECIMALS))
    }
    val total = result.totalMassKg?.let {
        stringResource(
            R.string.cw_export_total,
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

private fun ConductorMaterial.labelRes(): Int = when (this) {
    ConductorMaterial.COPPER -> R.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> R.string.common_material_aluminium
}

private fun CableInsulation.labelRes(): Int = when (this) {
    CableInsulation.PVC -> R.string.cs_insulation_pvc
    CableInsulation.XLPE -> R.string.cs_insulation_xlpe
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
