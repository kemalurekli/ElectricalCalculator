package com.kemalurekli.electricalcalculator.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecTopAppBar
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onThemeModeChange = viewModel::onThemeModeChange,
        onDynamicColorChange = viewModel::onDynamicColorChange,
        onUnitSystemChange = viewModel::onUnitSystemChange,
        onLanguageChange = viewModel::onLanguageChange,
        onEngineeringDefaultsChange = viewModel::onEngineeringDefaultsChange,
        onResetEngineeringDefaults = viewModel::onResetEngineeringDefaults,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onEngineeringDefaultsChange: (EngineeringDefaults) -> Unit,
    onResetEngineeringDefaults: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ElecTopAppBar(
                title = stringResource(R.string.destination_settings),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = spacing.xl),
        ) {
            ElecSectionHeader(title = stringResource(R.string.settings_appearance))

            SettingsGroup {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(
                        start = spacing.lg,
                        top = spacing.md,
                        end = spacing.lg,
                    ),
                )

                // One radio group: exactly one theme is active, and grouping it
                // lets a screen reader announce "1 of 3" as the user moves.
                Column(Modifier.selectableGroup()) {
                    ThemeMode.entries.forEach { mode ->
                        SettingsRadioRow(
                            label = stringResource(mode.labelRes()),
                            selected = uiState.preferences.themeMode == mode,
                            onSelect = { onThemeModeChange(mode) },
                        )
                    }
                }

                if (uiState.isDynamicColorAvailable) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = spacing.lg),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_dynamic_color),
                        subtitle = stringResource(R.string.settings_dynamic_color_summary),
                        checked = uiState.preferences.useDynamicColor,
                        onCheckedChange = onDynamicColorChange,
                    )
                }
            }

            ElecSectionHeader(title = stringResource(R.string.settings_language))

            SettingsGroup {
                Column(Modifier.selectableGroup()) {
                    AppLanguage.entries.forEach { language ->
                        SettingsRadioRow(
                            label = language.displayName(),
                            selected = uiState.language == language,
                            onSelect = { onLanguageChange(language) },
                        )
                    }
                }
            }

            ElecSectionHeader(title = stringResource(R.string.settings_units))

            SettingsGroup {
                Text(
                    text = stringResource(R.string.settings_unit_system),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(
                        start = spacing.lg,
                        top = spacing.md,
                        end = spacing.lg,
                    ),
                )
                Column(Modifier.selectableGroup()) {
                    UnitSystem.entries.forEach { system ->
                        SettingsRadioRow(
                            label = stringResource(system.labelRes()),
                            selected = uiState.preferences.unitSystem == system,
                            onSelect = { onUnitSystemChange(system) },
                        )
                    }
                }
            }

            ElecSectionHeader(title = stringResource(R.string.settings_engineering))

            EngineeringDefaultsGroup(
                defaults = uiState.engineering,
                onChange = onEngineeringDefaultsChange,
                onReset = onResetEngineeringDefaults,
            )

            ElecSectionHeader(title = stringResource(R.string.settings_about))

            SettingsGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.settings_version),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = uiState.versionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * The values every calculator opens with.
 *
 * Written as one card rather than as a row that opens a sub-screen: there are
 * seven controls and a user setting them up for a new job wants to see all of
 * them at once, the way they would read the top of a schedule.
 *
 * The explanatory line is not decoration. These values were guessed from the
 * device's region on first launch, and a reader is entitled to know both that
 * the app guessed and that it will not guess again — otherwise the sensible
 * fear is that changing the app's language would quietly move them.
 */
@Composable
private fun EngineeringDefaultsGroup(
    defaults: EngineeringDefaults,
    onChange: (EngineeringDefaults) -> Unit,
    onReset: () -> Unit,
) {
    val spacing = ElecTheme.spacing
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    SettingsGroup {
        Column(
            modifier = Modifier.padding(
                start = spacing.lg,
                top = spacing.md,
                end = spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text(
                text = stringResource(R.string.settings_engineering_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ElecNumericField(
                value = defaults.singlePhaseVoltage,
                onValueChange = { onChange(defaults.copy(singlePhaseVoltage = it)) },
                label = stringResource(R.string.settings_engineering_single_phase),
                unit = "V",
            )
            ElecNumericField(
                value = defaults.threePhaseVoltage,
                onValueChange = { onChange(defaults.copy(threePhaseVoltage = it)) },
                label = stringResource(R.string.settings_engineering_three_phase),
                unit = "V",
            )
            ElecNumericField(
                value = defaults.frequency,
                onValueChange = { onChange(defaults.copy(frequency = it)) },
                label = stringResource(R.string.settings_engineering_frequency),
                unit = "Hz",
            )
            ElecNumericField(
                value = defaults.ambientTemperature,
                onValueChange = { onChange(defaults.copy(ambientTemperature = it)) },
                label = stringResource(R.string.settings_engineering_ambient),
                unit = "°C",
                allowNegative = true,
                imeAction = ImeAction.Done,
            )

            ElecOptionSelector(
                label = stringResource(R.string.settings_engineering_material),
                options = ConductorMaterial.entries.toImmutableList(),
                selected = defaults.material,
                onSelect = { onChange(defaults.copy(material = it)) },
                optionLabel = { stringResource(it.labelRes()) },
            )
            ElecOptionSelector(
                label = stringResource(R.string.settings_engineering_insulation),
                options = CableInsulation.entries.toImmutableList(),
                selected = defaults.insulation,
                onSelect = { onChange(defaults.copy(insulation = it)) },
                optionLabel = { stringResource(it.labelRes()) },
            )
            ElecOptionSelector(
                label = stringResource(R.string.settings_engineering_method),
                options = InstallationMethod.entries.toImmutableList(),
                selected = defaults.installationMethod,
                onSelect = { onChange(defaults.copy(installationMethod = it)) },
                optionLabel = { stringResource(it.labelRes()) },
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        TextButton(
            onClick = { confirmReset = true },
            modifier = Modifier.padding(horizontal = spacing.sm),
        ) {
            Text(text = stringResource(R.string.settings_engineering_reset))
        }
    }

    if (confirmReset) {
        // Confirmed rather than immediate: this is the one action in the app
        // that throws away values the user set, and it is one tap from them.
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.settings_engineering_reset)) },
            text = { Text(stringResource(R.string.settings_engineering_reset_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        onReset()
                    },
                ) {
                    Text(stringResource(R.string.action_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val spacing = ElecTheme.spacing
    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
    ) {
        Column(
            modifier = Modifier.padding(bottom = spacing.sm),
            content = content,
        )
    }
}

@Composable
private fun SettingsRadioRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val spacing = ElecTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // `selectable` on the row makes the whole row one target and gives
            // it the correct radio-button semantics; the RadioButton itself is
            // then purely visual.
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val spacing = ElecTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
    ThemeMode.SYSTEM -> R.string.settings_theme_system
}

/**
 * The name of a language, written in that language.
 *
 * Deliberately not a translated string resource: a user who has the app in a
 * language they cannot read needs to recognise their own language in the list
 * to escape. "Türkçe" is legible to a Turkish speaker whatever the current UI
 * language is; a translated "Turkish" is not.
 */
@Composable
private fun AppLanguage.displayName(): String {
    val tag = languageTag ?: return stringResource(R.string.settings_language_system)
    val locale = Locale.forLanguageTag(tag)
    return locale.getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }
}

private fun UnitSystem.labelRes(): Int = when (this) {
    UnitSystem.METRIC -> R.string.settings_unit_system_metric
    UnitSystem.IMPERIAL -> R.string.settings_unit_system_imperial
}

// The same labels the calculators use, so a default set here reads identically
// where it lands.
private fun ConductorMaterial.labelRes(): Int = when (this) {
    ConductorMaterial.COPPER -> R.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> R.string.common_material_aluminium
}

private fun CableInsulation.labelRes(): Int = when (this) {
    CableInsulation.PVC -> R.string.cs_insulation_pvc
    CableInsulation.XLPE -> R.string.cs_insulation_xlpe
}

private fun InstallationMethod.labelRes(): Int = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> R.string.cs_method_b1
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> R.string.cs_method_b2
    InstallationMethod.C_CLIPPED_DIRECT -> R.string.cs_method_c
    InstallationMethod.E_FREE_AIR -> R.string.cs_method_e
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun SettingsScreenPreview() {
    ElecToolkitTheme {
        SettingsScreen(
            uiState = SettingsUiState(isDynamicColorAvailable = true),
            onThemeModeChange = {},
            onDynamicColorChange = {},
            onUnitSystemChange = {},
            onLanguageChange = {},
            onEngineeringDefaultsChange = {},
            onResetEngineeringDefaults = {},
            onNavigateBack = {},
        )
    }
}
