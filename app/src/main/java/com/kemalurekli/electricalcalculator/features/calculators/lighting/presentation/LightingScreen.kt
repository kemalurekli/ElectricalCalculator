package com.kemalurekli.electricalcalculator.features.calculators.lighting.presentation

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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecStepsCard
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
import com.kemalurekli.electricalcalculator.features.calculators.lighting.domain.LightingResult
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun LightingRoute(
    onReferenceClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    recordId: Long? = null,
    modifier: Modifier = Modifier,
    viewModel: LightingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Opening from the history restores that record's inputs. Keyed on the id so
    // a second record opens over the first without leaving the screen.
    LaunchedEffect(recordId) { recordId?.let(viewModel::onRestore) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = stringResource(R.string.calculator_lighting_title)
    val copiedMessage = stringResource(R.string.message_copied)
    val shareSubject = stringResource(R.string.calculator_share_subject, title)
    val summary = uiState.result?.let { rememberShareText(title, uiState, it) }

    LightingScreen(
        uiState = uiState,
        onIlluminanceChange = viewModel::onIlluminanceChange,
        onLengthChange = viewModel::onLengthChange,
        onWidthChange = viewModel::onWidthChange,
        onMountingHeightChange = viewModel::onMountingHeightChange,
        onFluxChange = viewModel::onFluxChange,
        onUtilisationChange = viewModel::onUtilisationChange,
        onMaintenanceChange = viewModel::onMaintenanceChange,
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
fun LightingScreen(
    uiState: LightingUiState,
    onIlluminanceChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onMountingHeightChange: (String) -> Unit,
    onFluxChange: (String) -> Unit,
    onUtilisationChange: (String) -> Unit,
    onMaintenanceChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReferenceClick: (String) -> Unit,
    onApplyExample: (WorkedExample<LightingUiState>) -> Unit,
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
        title = stringResource(R.string.calculator_lighting_title),
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
                    examples = lightingExamples,
                    onSelect = onApplyExample,
                )
            }

            item(key = "inputs-header") {
                ElecSectionHeader(
                    title = stringResource(R.string.calculator_inputs),
                    modifier = Modifier.padding(horizontal = 0.dp),
                )
            }

            item(key = "illuminance") {
                ElecNumericField(
                    value = uiState.illuminance,
                    onValueChange = onIlluminanceChange,
                    label = stringResource(R.string.lt_illuminance_label),
                    unit = "lx",
                    error = uiState.errors[LightingField.ILLUMINANCE],
                    supportingText = stringResource(R.string.lt_illuminance_hint),
                )
            }

            item(key = "length") {
                ElecNumericField(
                    value = uiState.length,
                    onValueChange = onLengthChange,
                    label = stringResource(R.string.lt_length_label),
                    unit = "m",
                    error = uiState.errors[LightingField.LENGTH],
                )
            }

            item(key = "width") {
                ElecNumericField(
                    value = uiState.width,
                    onValueChange = onWidthChange,
                    label = stringResource(R.string.lt_width_label),
                    unit = "m",
                    error = uiState.errors[LightingField.WIDTH],
                )
            }

            item(key = "height") {
                ElecNumericField(
                    value = uiState.mountingHeight,
                    onValueChange = onMountingHeightChange,
                    label = stringResource(R.string.lt_height_label),
                    unit = "m",
                    error = uiState.errors[LightingField.MOUNTING_HEIGHT],
                    supportingText = stringResource(R.string.lt_height_hint),
                )
            }

            item(key = "flux") {
                ElecNumericField(
                    value = uiState.flux,
                    onValueChange = onFluxChange,
                    label = stringResource(R.string.lt_flux_label),
                    unit = "lm",
                    error = uiState.errors[LightingField.FLUX],
                    supportingText = stringResource(R.string.lt_flux_hint),
                )
            }

            item(key = "uf") {
                ElecNumericField(
                    value = uiState.utilisation,
                    onValueChange = onUtilisationChange,
                    label = stringResource(R.string.lt_utilisation_label),
                    error = uiState.errors[LightingField.UTILISATION],
                    supportingText = stringResource(R.string.lt_utilisation_hint),
                )
            }

            item(key = "mf") {
                ElecNumericField(
                    value = uiState.maintenance,
                    onValueChange = onMaintenanceChange,
                    label = stringResource(R.string.lt_maintenance_label),
                    error = uiState.errors[LightingField.MAINTENANCE],
                    supportingText = stringResource(R.string.lt_maintenance_hint),
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
                    formula = stringResource(R.string.lt_formula),
                    variables = persistentListOf(
                        FormulaVariable("N", stringResource(R.string.lt_var_n), "—"),
                        FormulaVariable("E", stringResource(R.string.lt_var_e), "lx"),
                        FormulaVariable("A", stringResource(R.string.lt_var_a), "m²"),
                        FormulaVariable("Φ", stringResource(R.string.lt_var_flux), "lm"),
                        FormulaVariable("UF", stringResource(R.string.lt_var_uf), "—"),
                        FormulaVariable("MF", stringResource(R.string.lt_var_mf), "—"),
                        FormulaVariable("K", stringResource(R.string.lt_var_k), "—"),
                    ),
                )
            }

            item(key = "notes") {
                ElecNotesCard(
                    title = stringResource(R.string.calculator_notes),
                    notes = persistentListOf(
                        stringResource(R.string.lt_note_average),
                        stringResource(R.string.lt_note_uf),
                        stringResource(R.string.lt_note_height),
                        stringResource(R.string.lt_note_rounding),
                        stringResource(R.string.lt_note_layout),
                    ),
                    links = persistentListOf(
                        NoteLink(
                            topicKey = "primer_powerquality",
                            label = stringResource(
                                ReferenceCatalog.titleResOf("primer_powerquality"),
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
    result: LightingResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val spacing = ElecTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        ElecResultCard(
            label = stringResource(R.string.lt_result_count),
            value = result.luminaireCount.toString(),
            unit = "",
            tone = ResultTone.SUCCESS,
            statusMessage = stringResource(R.string.lt_result_status),
            secondaryRows = buildList {
                add(
                    ResultRow(
                        label = stringResource(R.string.lt_result_achieved),
                        value = NumberFormatter.format(
                            result.achievedIlluminanceLux,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "lx",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.lt_result_exact),
                        value = NumberFormatter.format(
                            result.exactLuminaireCount,
                            DISPLAY_DECIMALS,
                        ),
                        unit = "",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.lt_result_room_index),
                        value = NumberFormatter.format(result.roomIndex, DISPLAY_DECIMALS),
                        unit = "",
                    ),
                )
                add(
                    ResultRow(
                        label = stringResource(R.string.lt_result_area),
                        value = NumberFormatter.format(result.areaSquareMetres, DISPLAY_DECIMALS),
                        unit = "m²",
                    ),
                )
                // Absent for a prime count, where the only "grid" is a line.
                result.luminairesPerRowSuggestion?.let { (along, across) ->
                    add(
                        ResultRow(
                            label = stringResource(R.string.lt_result_layout),
                            value = "$along × $across",
                            unit = "",
                        ),
                    )
                }
            }.toImmutableList(),
        )
        ElecResultActions(onCopy = onCopy, onShare = onShare)
    }
}

@Composable
private fun rememberShareText(
    title: String,
    uiState: LightingUiState,
    result: LightingResult,
): String {
    val part0 = stringResource(
        R.string.lt_export_room,
        uiState.length, uiState.width, uiState.illuminance,
    )
    val part1 = stringResource(
        R.string.lt_export_count,
        result.luminaireCount.toString(),
    )
    val part2 = stringResource(
        R.string.lt_export_achieved,
        NumberFormatter.format(result.achievedIlluminanceLux, DISPLAY_DECIMALS),
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
private fun LightingScreenPreview() {
    ElecToolkitTheme {
        LightingScreen(
            uiState = LightingUiState(length = "12", width = "8", flux = "4000"),
            onIlluminanceChange = {}, onLengthChange = {}, onWidthChange = {}, onMountingHeightChange = {}, onFluxChange = {}, onUtilisationChange = {}, onMaintenanceChange = {},
            onCalculate = {}, onApplyExample = {},
            onReferenceClick = {}, onReset = {}, onToggleFavorite = {},
            onCopy = {}, onShare = {}, onNavigateBack = {},
        )
    }
}
