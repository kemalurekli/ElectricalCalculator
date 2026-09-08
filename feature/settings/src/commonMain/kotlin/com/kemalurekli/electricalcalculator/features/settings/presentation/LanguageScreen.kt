package com.kemalurekli.electricalcalculator.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.settings.generated.resources.settings_language
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The display language, on a screen of its own.
 *
 * Every option is labelled in its own language. That is the whole reason this
 * list cannot be generated from the current locale: the reader who most needs
 * it is the one who opened the app in a language they cannot read, and
 * "Russian" is no help to them where "Русский" is.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageRoute(
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = ElecTheme.spacing
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = stringResource(Res.string.settings_language),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                top = spacing.sm,
                bottom = spacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            item(key = "languages") {
                ElecCard(modifier = Modifier.fillMaxWidth()) {
                    // The same picker on both platforms. Each stores the choice
                    // where the system stores it, so the row in the phone's own
                    // settings and this one are two doors into one setting
                    // rather than two settings that can disagree.
                    Column(
                        modifier = Modifier
                            .padding(vertical = spacing.sm)
                            .selectableGroup(),
                    ) {
                        AppLanguage.entries.forEach { language ->
                            SettingsRadioRow(
                                label = language.displayName(),
                                selected = uiState.language == language,
                                onSelect = { viewModel.onLanguageChange(language) },
                            )
                        }
                    }
                }
            }
        }
    }
}
