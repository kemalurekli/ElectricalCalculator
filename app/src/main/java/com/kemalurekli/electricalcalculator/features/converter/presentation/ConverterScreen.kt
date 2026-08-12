package com.kemalurekli.electricalcalculator.features.converter.presentation

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.NumericInput
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNotesCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericTextStyle
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.features.converter.domain.MeasurementUnit
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCatalog
import com.kemalurekli.electricalcalculator.features.converter.domain.UnitCategory
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ConverterRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConverterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ConverterScreen(
        uiState = uiState,
        onInputChange = viewModel::onInputChange,
        onCategoryChange = viewModel::onCategoryChange,
        onFromChange = viewModel::onFromChange,
        onToChange = viewModel::onToChange,
        onSwap = viewModel::onSwap,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    uiState: ConverterUiState,
    onInputChange: (String) -> Unit,
    onCategoryChange: (UnitCategory) -> Unit,
    onFromChange: (MeasurementUnit) -> Unit,
    onToChange: (MeasurementUnit) -> Unit,
    onSwap: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier
                .widthIn(max = layout.contentMaxWidth)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                ElecTopAppBar(
                    title = stringResource(R.string.dashboard_converter_title),
                    onNavigateBack = onNavigateBack,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                item(key = "categories") {
                    // Thirteen categories will not fit as a segmented control,
                    // and a dropdown would hide the choice behind a tap. A
                    // scrolling chip row keeps every one a single gesture away.
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = spacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        uiState.categories.forEach { category ->
                            FilterChip(
                                selected = category.key == uiState.category.key,
                                onClick = { onCategoryChange(category) },
                                label = { Text(text = stringResource(category.categoryLabelRes())) },
                            )
                        }
                    }
                }

                item(key = "value") {
                    OutlinedTextField(
                        value = uiState.input,
                        onValueChange = {
                            onInputChange(
                                NumericInput.sanitise(uiState.input, it, allowNegative = true),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.screenHorizontal),
                        label = { Text(text = stringResource(R.string.cv_value)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        // Negatives are ordinary input here: −40 °C, and AWG
                        // 0000 entered as −3.
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                    )
                }

                item(key = "units") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UnitDropdown(
                            label = stringResource(R.string.cv_from),
                            selected = uiState.from,
                            units = uiState.category.units,
                            onSelect = onFromChange,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onSwap) {
                            Icon(
                                imageVector = ElecIcons.Converter,
                                contentDescription = stringResource(R.string.cv_swap),
                            )
                        }
                        UnitDropdown(
                            label = stringResource(R.string.cv_to),
                            selected = uiState.to,
                            units = uiState.category.units,
                            onSelect = onToChange,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item(key = "result") {
                    ResultBanner(
                        uiState = uiState,
                        modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
                    )
                }

                if (!uiState.isEmpty) {
                    item(key = "all-header") {
                        ElecSectionHeader(title = stringResource(R.string.cv_all_units))
                    }

                    items(
                        count = uiState.allUnits.size,
                        key = { index -> "unit-${uiState.allUnits[index].unit.key}" },
                    ) { index ->
                        val converted = uiState.allUnits[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = spacing.screenHorizontal,
                                    vertical = spacing.xs,
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = converted.unit.symbol,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = NumberFormatter.formatEngineering(
                                    converted.value,
                                    SIGNIFICANT_DIGITS,
                                ),
                                style = NumericCompactTextStyle,
                            )
                        }
                    }

                    item(key = "divider") {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                horizontal = spacing.screenHorizontal,
                                vertical = spacing.sm,
                            ),
                        )
                    }
                }

                item(key = "notes") {
                    ElecNotesCard(
                        title = stringResource(R.string.calculator_notes),
                        notes = persistentListOf(
                            stringResource(R.string.cv_note_awg),
                            stringResource(R.string.cv_note_kcmil),
                            stringResource(R.string.cv_note_va),
                            stringResource(R.string.cv_note_horsepower),
                            stringResource(R.string.cv_note_temperature),
                            stringResource(R.string.cv_note_calorie),
                        ),
                        modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultBanner(uiState: ConverterUiState, modifier: Modifier = Modifier) {
    val spacing = ElecTheme.spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        val result = uiState.result
        if (result == null) {
            Text(
                text = stringResource(R.string.cv_enter_value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = stringResource(
                    R.string.cv_result_label,
                    uiState.from.symbol,
                    uiState.to.symbol,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = NumberFormatter.formatEngineering(result, SIGNIFICANT_DIGITS),
                    style = NumericTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = uiState.to.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    label: String,
    selected: MeasurementUnit,
    units: List<MeasurementUnit>,
    onSelect: (MeasurementUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected.symbol,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(text = unit.symbol) },
                    onClick = {
                        onSelect(unit)
                        expanded = false
                    },
                )
            }
        }
    }
}

private const val SIGNIFICANT_DIGITS = 6

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ConverterScreenPreview() {
    val category = UnitCatalog.wireGauge
    ElecToolkitTheme {
        ConverterScreen(
            uiState = ConverterUiState(
                categories = UnitCatalog.all.toImmutableList(),
                category = category,
                from = category.units[0],
                to = category.units[1],
                input = "16",
            ),
            onInputChange = {}, onCategoryChange = {}, onFromChange = {},
            onToChange = {}, onSwap = {}, onNavigateBack = {},
        )
    }
}
