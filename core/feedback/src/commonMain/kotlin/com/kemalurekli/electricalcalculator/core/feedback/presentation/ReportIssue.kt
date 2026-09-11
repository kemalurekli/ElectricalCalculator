package com.kemalurekli.electricalcalculator.core.feedback.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.feedback.generated.resources.feedback_open
import org.jetbrains.compose.resources.stringResource

/**
 * "Is something wrong on this page?", at the end of the page.
 *
 * ### Why here and not in the top bar
 *
 * Somebody who has found a mistake has found it by reading, and reading ends at
 * the bottom. Putting it in the app bar would mean an extra glyph on
 * forty-five screens to serve the one occasion in a thousand openings when
 * somebody wants it — this app's chrome is deliberately thin, and a permanent
 * icon is a permanent cost.
 *
 * ### Why it is quiet
 *
 * A `TextButton` under a hairline, not a card and not an accent colour. It is
 * an offer, not an invitation: a prominent "report a mistake" on an
 * engineering tool reads as an admission that there are many.
 *
 * @param area a stable key from `FeedbackArea` — never a translated title.
 */
@Composable
fun ElecReportIssue(
    area: String,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    var reporting by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(top = spacing.lg, bottom = spacing.xs),
        )
        TextButton(
            onClick = { reporting = true },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Icon(
                    imageVector = ElecIcons.ReportIssue,
                    contentDescription = null,
                    modifier = Modifier.size(GLYPH),
                )
                Text(
                    text = stringResource(Res.string.feedback_open),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (reporting) {
        ReportIssueDialog(area = area, onDismiss = { reporting = false })
    }
}

private val GLYPH = 18.dp

/**
 * The same offer, as the last row of a `LazyColumn`.
 *
 * Most screens that show content are lazy lists, and a composable cannot be
 * dropped into one — it has to be an item. Keyed, so that it is not recreated
 * when the list above it changes.
 */
fun LazyListScope.reportIssueItem(area: String) {
    item(key = "report-issue") {
        ElecReportIssue(area = area)
    }
}
