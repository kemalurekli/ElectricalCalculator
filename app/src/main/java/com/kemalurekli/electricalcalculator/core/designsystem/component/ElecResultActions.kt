package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme

/**
 * Copy and share actions for a computed result.
 *
 * Shown only once a result exists, so the buttons are never present in a state
 * where pressing them would copy nothing.
 */
@Composable
fun ElecResultActions(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        OutlinedButton(onClick = onCopy, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = ElecIcons.Copy,
                // The button's own text labels it; announcing the icon too
                // would have a screen reader read "copy" twice.
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.action_copy),
                modifier = Modifier.padding(start = spacing.sm),
            )
        }
        OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = ElecIcons.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.action_share),
                modifier = Modifier.padding(start = spacing.sm),
            )
        }
    }
}
