package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_copy
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_export_pdf
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.pro_badge
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_share

/**
 * Copy and share actions for a computed result.
 *
 * Shown only once a result exists, so the buttons are never present in a state
 * where pressing them would copy nothing.
 *
 * Two icons at the trailing edge rather than two full-width outlined buttons.
 * The buttons were the same width and weight as the result card above them —
 * two large blocks arguing with the figure they belong to — for a pair of
 * secondary actions nobody opens a calculator to perform. The glyphs carry
 * their names for a screen reader.
 *
 * Drawn inside [ElecResultCard] rather than under it, which is why the tint is
 * a parameter: on the card the row sits on a toned container and has to take
 * that container's own foreground.
 */
@Composable
fun ElecResultActions(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    onExportPdf: (() -> Unit)? = null,
    exportLocked: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leftmost of the three, and the only one that produces a file. Copy
        // and share hand over text; this hands over a document, which is a
        // different promise and belongs at the start of the row rather than
        // hidden past two glyphs that look like it.
        if (onExportPdf != null) {
            if (exportLocked) {
                ElecPillBadge(text = stringResource(Res.string.pro_badge))
            }
            IconButton(onClick = onExportPdf) {
                Icon(
                    imageVector = ElecIcons.ProDocument,
                    contentDescription = stringResource(Res.string.action_export_pdf),
                    modifier = Modifier.size(ACTION_ICON),
                    tint = tint,
                )
            }
        }
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = ElecIcons.Copy,
                contentDescription = stringResource(Res.string.action_copy),
                modifier = Modifier.size(ACTION_ICON),
                tint = tint,
            )
        }
        IconButton(onClick = onShare) {
            Icon(
                imageVector = ElecIcons.Share,
                contentDescription = stringResource(Res.string.action_share),
                modifier = Modifier.size(ACTION_ICON),
                tint = tint,
            )
        }
    }
}

/** The 48dp target is on the button; the glyph is sized for its importance. */
private val ACTION_ICON = 20.dp
