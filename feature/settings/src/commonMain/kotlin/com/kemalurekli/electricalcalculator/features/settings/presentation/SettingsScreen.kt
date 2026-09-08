package com.kemalurekli.electricalcalculator.features.settings.presentation

import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Modifier
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.Res as ProRes
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_settings_section
import com.kemalurekli.electricalcalculator.features.pro.presentation.ProSettingsSection
import androidx.compose.foundation.layout.PaddingValues
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_language_description
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.DisclaimerDialog
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecOptionSelector
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumAccountRow
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial
import com.kemalurekli.electricalcalculator.core.domain.model.EngineeringDefaults
import com.kemalurekli.electricalcalculator.core.domain.model.InstallationMethod
import com.kemalurekli.electricalcalculator.core.domain.model.ThemeMode
import com.kemalurekli.electricalcalculator.core.domain.model.UnitSystem
import kotlinx.collections.immutable.toImmutableList
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.action_cancel
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.action_close
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.action_reset
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.common_material_aluminium
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.common_material_copper
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_insulation_pvc
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_insulation_xlpe
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_method_b1
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_method_b1_full
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_method_b2
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_method_b2_full
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_method_c
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_method_c_full
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_method_e
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.cs_method_e_full
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.destination_settings
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.forum_account_section
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.privacy_policy_url
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_about
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_appearance
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_ambient
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_frequency
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_insulation
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_material
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_method
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_reset
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_reset_message
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_single_phase
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_summary
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_engineering_three_phase
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_language
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_language_system
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_legal
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_licenses
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_privacy_policy
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_theme
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_theme_dark
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_theme_light
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_theme_system
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_unit_system
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_unit_system_imperial
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_unit_system_metric
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_units
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_version
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.disclaimer_title

@Composable
fun SettingsRoute(
    onNavigateBack: (() -> Unit)?,
    onOpenForumAccount: () -> Unit,
    onOpenLanguage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onThemeModeChange = viewModel::onThemeModeChange,
        onUnitSystemChange = viewModel::onUnitSystemChange,
        onLanguageChange = viewModel::onLanguageChange,
        onEngineeringDefaultsChange = viewModel::onEngineeringDefaultsChange,
        onResetEngineeringDefaults = viewModel::onResetEngineeringDefaults,
        onNavigateBack = onNavigateBack,
        onOpenForumAccount = onOpenForumAccount,
        onOpenLanguage = onOpenLanguage,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onEngineeringDefaultsChange: (EngineeringDefaults) -> Unit,
    onResetEngineeringDefaults: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    onOpenForumAccount: () -> Unit = {},
    onOpenLanguage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()
    var showDisclaimer by rememberSaveable { mutableStateOf(false) }
    var showLicenses by rememberSaveable { mutableStateOf(false) }

    ElecScreenScaffold(
        title = stringResource(Res.string.destination_settings),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = spacing.xl),
        ) {
            ElecSectionHeader(title = stringResource(Res.string.settings_appearance))

            SettingsGroup {
                Text(
                    text = stringResource(Res.string.settings_theme),
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
                            label = stringResource(mode.label()),
                            selected = uiState.preferences.themeMode == mode,
                            onSelect = { onThemeModeChange(mode) },
                        )
                    }
                }
            }

            ElecSectionHeader(title = stringResource(Res.string.settings_language))

            SettingsGroup {
                // One row into a screen of its own. Twelve radio buttons here
                // would bury the units and the engineering defaults under a
                // language list nobody is reading unless they came for it.
                // `spacing.lg` horizontally, like the theme title and every
                // radio row above it. The row is clickable before it is padded,
                // so the touch target is the full width of the card either way
                // — the inset only decides where the text starts, and text hard
                // against the card's edge is the one row in this list that
                // looks unfinished.
                ElecListItem(
                    title = uiState.language.displayName(),
                    description = stringResource(Res.string.settings_language_description),
                    onClick = onOpenLanguage,
                    contentPadding = PaddingValues(
                        horizontal = spacing.lg,
                        vertical = spacing.md,
                    ),
                )
            }

            ElecSectionHeader(title = stringResource(Res.string.forum_account_section))

            // One row, like everything else in this list. What used to be here
            // was a two-step form and three buttons, one of them destructive;
            // it lives on its own screen now.
            SettingsGroup {
                ForumAccountRow(onOpen = onOpenForumAccount)
            }

            // Between the forum's account and the app's own settings, because
            // it is neither: it is what this copy of the app has been paid for.
            ElecSectionHeader(title = stringResource(ProRes.string.pro_settings_section))

            SettingsGroup {
                ProSettingsSection()
            }

            ElecSectionHeader(title = stringResource(Res.string.settings_units))

            SettingsGroup {
                Text(
                    text = stringResource(Res.string.settings_unit_system),
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
                            label = stringResource(system.label()),
                            selected = uiState.preferences.unitSystem == system,
                            onSelect = { onUnitSystemChange(system) },
                        )
                    }
                }
            }

            ElecSectionHeader(title = stringResource(Res.string.settings_engineering))

            EngineeringDefaultsGroup(
                defaults = uiState.engineering,
                onChange = onEngineeringDefaultsChange,
                onReset = onResetEngineeringDefaults,
            )

            ElecSectionHeader(title = stringResource(Res.string.settings_legal))

            SettingsGroup {
                // Reachable at any time, not only on the first launch. Terms
                // that can only be read once are terms nobody can go back to.
                TextButton(
                    onClick = { showDisclaimer = true },
                    modifier = Modifier.padding(horizontal = spacing.sm),
                ) {
                    Text(text = stringResource(DesignSystemRes.string.disclaimer_title))
                }
            }

            ElecSectionHeader(title = stringResource(Res.string.settings_about))

            // Hidden rather than broken when the address has not been filled
            // in. The app now holds accounts and user content, so Play will not
            // take the listing without one — but a link that 404s is worse than
            // no link, and a fresh checkout has nothing to point at.
            val privacyUrl = stringResource(Res.string.privacy_policy_url)
            if (privacyUrl.isNotBlank()) {
                val uriHandler = LocalUriHandler.current
                SettingsGroup {
                    TextButton(
                        onClick = { uriHandler.openUri(privacyUrl) },
                        modifier = Modifier.padding(horizontal = spacing.sm),
                    ) {
                        Text(text = stringResource(Res.string.settings_privacy_policy))
                    }
                }
            }

            // Not optional. The app's typefaces are SIL Open Font License, and
            // that licence requires its own text to travel with them — so a
            // build that ships Inter without a way to read the licence is not
            // licensed to ship Inter.
            SettingsGroup {
                TextButton(
                    onClick = { showLicenses = true },
                    modifier = Modifier.padding(horizontal = spacing.sm),
                ) {
                    Text(text = stringResource(Res.string.settings_licenses))
                }
            }

            SettingsGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(Res.string.settings_version),
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

    if (showDisclaimer) {
        DisclaimerDialog(onAccept = {}, onDismiss = { showDisclaimer = false })
    }

    if (showLicenses) {
        LicensesDialog(onDismiss = { showLicenses = false })
    }
}

/**
 * The full licence text for every third-party asset the app embeds.
 *
 * Read from `assets/licenses/` rather than pasted into `strings.xml`: these are
 * legal texts that must ship verbatim, and a translator's file is the last
 * place a verbatim text should live. Nothing here is translated for the same
 * reason — the OFL is the OFL in every locale.
 */
@Composable
private fun LicensesDialog(onDismiss: () -> Unit) {
    val spacing = ElecTheme.spacing

    // Read once and held, not re-read on every recomposition of the dialog.
    //
    // Named rather than listed: the assets directory could be enumerated and a
    // Compose Resources `files/` directory cannot. Two fonts, two licences, and
    // a third would be added here beside the font it covers anyway.
    val text by produceState(initialValue = "") {
        value = runCatching {
            LICENCE_FILES
                .map { name -> Res.readBytes("files/licenses/$name").decodeToString() }
                .joinToString("\n\n")
        }.getOrDefault("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_licenses)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = LICENSE_DIALOG_MAX_HEIGHT)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_close)) }
        },
    )
}

/**
 * Ceiling for the licence text.
 *
 * An AlertDialog sizes itself to its content, and eight thousand characters of
 * licence would push the dismiss button off the bottom of the screen — leaving
 * a dialog that cannot be closed by the one control meant to close it.
 */
private val LICENSE_DIALOG_MAX_HEIGHT = 420.dp

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
                text = stringResource(Res.string.settings_engineering_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ElecNumericField(
                value = defaults.singlePhaseVoltage,
                onValueChange = { onChange(defaults.copy(singlePhaseVoltage = it)) },
                label = stringResource(Res.string.settings_engineering_single_phase),
                unit = "V",
            )
            ElecNumericField(
                value = defaults.threePhaseVoltage,
                onValueChange = { onChange(defaults.copy(threePhaseVoltage = it)) },
                label = stringResource(Res.string.settings_engineering_three_phase),
                unit = "V",
            )
            ElecNumericField(
                value = defaults.frequency,
                onValueChange = { onChange(defaults.copy(frequency = it)) },
                label = stringResource(Res.string.settings_engineering_frequency),
                unit = "Hz",
            )
            ElecNumericField(
                value = defaults.ambientTemperature,
                onValueChange = { onChange(defaults.copy(ambientTemperature = it)) },
                label = stringResource(Res.string.settings_engineering_ambient),
                unit = "°C",
                allowNegative = true,
                imeAction = ImeAction.Done,
            )

            ElecOptionSelector(
                label = stringResource(Res.string.settings_engineering_material),
                options = ConductorMaterial.entries.toImmutableList(),
                selected = defaults.material,
                onSelect = { onChange(defaults.copy(material = it)) },
                optionLabel = { stringResource(it.label()) },
            )
            ElecOptionSelector(
                label = stringResource(Res.string.settings_engineering_insulation),
                options = CableInsulation.entries.toImmutableList(),
                selected = defaults.insulation,
                onSelect = { onChange(defaults.copy(insulation = it)) },
                optionLabel = { stringResource(it.label()) },
            )
            Column {
                ElecOptionSelector(
                    label = stringResource(Res.string.settings_engineering_method),
                    options = InstallationMethod.entries.toImmutableList(),
                    selected = defaults.installationMethod,
                    onSelect = { onChange(defaults.copy(installationMethod = it)) },
                    optionLabel = { stringResource(it.label()) },
                )
                // B1 on its own is a table column heading, not an instruction.
                Text(
                    text = stringResource(defaults.installationMethod.fullLabel()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = spacing.xs, start = spacing.xs),
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        TextButton(
            onClick = { confirmReset = true },
            modifier = Modifier.padding(horizontal = spacing.sm),
        ) {
            Text(text = stringResource(Res.string.settings_engineering_reset))
        }
    }

    if (confirmReset) {
        // Confirmed rather than immediate: this is the one action in the app
        // that throws away values the user set, and it is one tap from them.
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(Res.string.settings_engineering_reset)) },
            text = { Text(stringResource(Res.string.settings_engineering_reset_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        onReset()
                    },
                ) {
                    Text(stringResource(Res.string.action_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text(stringResource(Res.string.action_cancel))
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
internal fun SettingsRadioRow(
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

private fun ThemeMode.label(): StringResource = when (this) {
    ThemeMode.LIGHT -> Res.string.settings_theme_light
    ThemeMode.DARK -> Res.string.settings_theme_dark
    ThemeMode.SYSTEM -> Res.string.settings_theme_system
}

/**
 * The name of a language, written in that language.
 *
 * Deliberately not a translated string resource, and deliberately not asked of
 * the platform. A reader who opened the app in a language they cannot read has
 * to recognise their own in the list to escape it: "Русский" is legible to a
 * Russian speaker whatever the interface currently says, and a translated
 * "Russian" is not. `Locale.getDisplayLanguage` would also answer in the
 * *current* locale by default, which is the wrong answer, and its iOS
 * counterpart capitalises differently.
 */
@Composable
internal fun AppLanguage.displayName(): String = when (this) {
    AppLanguage.SYSTEM -> stringResource(Res.string.settings_language_system)
    AppLanguage.ENGLISH -> "English"
    AppLanguage.TURKISH -> "Türkçe"
    AppLanguage.GERMAN -> "Deutsch"
    AppLanguage.SPANISH -> "Español"
    AppLanguage.FRENCH -> "Français"
    AppLanguage.INDONESIAN -> "Bahasa Indonesia"
    AppLanguage.ITALIAN -> "Italiano"
    AppLanguage.DUTCH -> "Nederlands"
    AppLanguage.POLISH -> "Polski"
    AppLanguage.PORTUGUESE -> "Português"
    AppLanguage.VIETNAMESE -> "Tiếng Việt"
    AppLanguage.RUSSIAN -> "Русский"
}

private fun UnitSystem.label(): StringResource = when (this) {
    UnitSystem.METRIC -> Res.string.settings_unit_system_metric
    UnitSystem.IMPERIAL -> Res.string.settings_unit_system_imperial
}

// The same labels the calculators use, so a default set here reads identically
// where it lands.
private fun ConductorMaterial.label(): StringResource = when (this) {
    ConductorMaterial.COPPER -> Res.string.common_material_copper
    ConductorMaterial.ALUMINIUM -> Res.string.common_material_aluminium
}

private fun CableInsulation.label(): StringResource = when (this) {
    CableInsulation.PVC -> Res.string.cs_insulation_pvc
    CableInsulation.XLPE -> Res.string.cs_insulation_xlpe
}

private fun InstallationMethod.label(): StringResource = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> Res.string.cs_method_b1
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> Res.string.cs_method_b2
    InstallationMethod.C_CLIPPED_DIRECT -> Res.string.cs_method_c
    InstallationMethod.E_FREE_AIR -> Res.string.cs_method_e
}

private fun InstallationMethod.fullLabel(): StringResource = when (this) {
    InstallationMethod.B1_CONDUIT_ON_WALL -> Res.string.cs_method_b1_full
    InstallationMethod.B2_MULTICORE_IN_CONDUIT -> Res.string.cs_method_b2_full
    InstallationMethod.C_CLIPPED_DIRECT -> Res.string.cs_method_c_full
    InstallationMethod.E_FREE_AIR -> Res.string.cs_method_e_full
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun SettingsScreenPreview() {
    ElecToolkitTheme {
        SettingsScreen(
            uiState = SettingsUiState(),
            onThemeModeChange = {},
            onUnitSystemChange = {},
            onLanguageChange = {},
            onEngineeringDefaultsChange = {},
            onResetEngineeringDefaults = {},
            onNavigateBack = {},
        )
    }
}

private val LICENCE_FILES = listOf("inter-OFL.txt", "jetbrains-mono-OFL.txt")
