package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_owned
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_restore
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_settings_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Restoring a purchase from settings, without meeting a paywall on the way.
 *
 * Somebody restoring is somebody who has already paid — on another device, or
 * on this one before they reinstalled. Making them find a locked feature first,
 * so that the paywall can offer them the button, is asking them to be sold
 * something they own.
 *
 * Apple requires the control to exist at all (3.1.1); this is where it is
 * findable rather than where it is required.
 */
@Composable
fun ProSettingsSection(
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = koinViewModel(),
) {
    val spacing = ElecTheme.spacing
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxWidth().padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Text(
            text = stringResource(if (isPro) Res.string.pro_owned else Res.string.pro_settings_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Offered even when Pro is already on. A reader signed into a second
        // store account, or one whose entitlement has gone stale, has nothing
        // else to press — and the button is harmless when there is nothing to
        // restore, which it then says.
        TextButton(
            onClick = viewModel::onRestore,
            enabled = status == PaywallStatus.IDLE,
        ) {
            Text(stringResource(Res.string.pro_restore))
        }
    }
}
