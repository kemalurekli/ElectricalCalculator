package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListDivider
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItemDefaults
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_pdf
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_owned
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_restore
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_settings_hint
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_unlock
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * What this copy of the app has been paid for, and how to change that.
 *
 * Two things, in the order they matter:
 *
 * 1. **The product.** The section used to be a sentence about restoring and a
 *    button to do it — a shelf named after something you could not buy from it.
 *    The paywall was reachable only by walking into a locked feature, which
 *    asks the reader to be refused something before they are allowed to pay for
 *    it. The row is now the offer, with the app's own mark on it.
 * 2. **Restoring**, kept underneath and kept quiet. Somebody restoring has
 *    already paid, on another device or before a reinstall, and should not have
 *    to meet a sales pitch on the way. Apple requires the control to exist
 *    (3.1.1); this is where it is findable rather than where it is required.
 *
 * Once Pro is on, the row stops being an offer: it says so, it stops moving,
 * and it stops being pressable — there is nothing on the other side of it.
 */
@Composable
fun ProSettingsSection(
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = koinViewModel(),
) {
    val spacing = ElecTheme.spacing
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isPro) Modifier else Modifier.clickable(onClick = onOpenPaywall))
                .padding(ElecListItemDefaults.contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            ProMark(highlighted = !isPro)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xxs),
            ) {
                Text(
                    text = stringResource(if (isPro) Res.string.pro_owned else Res.string.pro_unlock),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                // The heading above the card already says "VoltageBoard Pro",
                // so the second line is spent on what the money buys rather
                // than on the name a second time.
                if (!isPro) {
                    Text(
                        text = stringResource(Res.string.pro_benefit_pdf),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        ElecListDivider(modifier = Modifier.padding(horizontal = spacing.lg))

        Column(
            modifier = Modifier.padding(top = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = stringResource(Res.string.pro_settings_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = spacing.lg),
            )

            // Offered even when Pro is already on. A reader signed into a second
            // store account, or one whose entitlement has gone stale, has nothing
            // else to press — and the button is harmless when there is nothing to
            // restore, which it then says.
            //
            // Inset by the button's own label padding, so its text starts on the
            // same line as the sentence above it instead of 12dp to the right.
            TextButton(
                onClick = viewModel::onRestore,
                enabled = status == PaywallStatus.IDLE,
                modifier = Modifier.padding(horizontal = spacing.lg - TEXT_BUTTON_LABEL_INSET),
            ) {
                Text(stringResource(Res.string.pro_restore))
            }
        }
    }
}

/**
 * The horizontal half of `ButtonDefaults.TextButtonContentPadding`, which is a
 * `PaddingValues` and cannot be subtracted from a `Dp` without a layout
 * direction to resolve it against.
 */
private val TEXT_BUTTON_LABEL_INSET = 12.dp
