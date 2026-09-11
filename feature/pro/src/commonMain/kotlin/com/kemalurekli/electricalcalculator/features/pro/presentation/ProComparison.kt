package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_calculation_title
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_future_title
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_schedule_title
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_compare_calculators
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_compare_forum
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_compare_free
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_compare_reference
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_compare_theory

/**
 * What is free and what is not, in two columns.
 *
 * ### Why a table and not a list of benefits
 *
 * This screen used to be four benefit rows, which answer "what do I get" and
 * leave "what am I paying for" to inference. The two are not the same question,
 * and the second is the one somebody has at a paywall: a list of good things
 * reads as a description of the app, not as a description of the purchase.
 *
 * A table answers it in one glance, and it answers a second question nobody
 * asks out loud — *what happens to what I already use?* Three of the six rows
 * are ticked in both columns. Saying plainly that the calculators, the
 * reference library and the forum stay free is the most trust-building thing
 * on the page, and it costs nothing, because they were never for sale.
 *
 * ### Why the Pro column is a column and not six tinted cells
 *
 * It was the latter, and it read as six separate highlights rather than as one
 * thing you could run a finger down. It is now a single panel drawn behind the
 * rows: tinted, outlined in the gold that means Pro on the mark above and on
 * the button below, and capped with the word. Every paywall that sells one
 * option out of several does some version of this, and the reason is that a
 * reader's eye has to be told where to land before it will read anything at all.
 */
@Composable
internal fun ProComparison(modifier: Modifier = Modifier) {
    val spacing = ElecTheme.spacing

    Box(modifier = modifier.fillMaxWidth()) {
        // Drawn first and behind: the column exists before the rows do, which
        // is what makes it read as a container rather than as a row property.
        //
        // Two boxes, not one. The table sits inside a vertical scroll, so its
        // incoming height constraint is infinite and a bare `fillMaxHeight`
        // resolves to nothing. `matchParentSize` measures against the size the
        // rows settled on, and gives the inner box a bound to fill.
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(COLUMN)
                    .fillMaxHeight()
                    .background(
                        color = ProGold.copy(alpha = TINT_ALPHA),
                        shape = RoundedCornerShape(PANEL_CORNER),
                    )
                    .border(
                        width = PANEL_STROKE,
                        color = ProGold.copy(alpha = BORDER_ALPHA),
                        shape = RoundedCornerShape(PANEL_CORNER),
                    ),
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Header()

            Feature(stringResource(Res.string.pro_compare_calculators), inFree = true)
            Feature(stringResource(Res.string.pro_compare_reference), inFree = true)
            Feature(stringResource(Res.string.pro_compare_forum), inFree = true)
            Feature(stringResource(Res.string.pro_compare_theory), inFree = false)
            Feature(stringResource(Res.string.pro_benefit_calculation_title), inFree = false)
            Feature(stringResource(Res.string.pro_benefit_schedule_title), inFree = false)
            Feature(
                text = stringResource(Res.string.pro_benefit_future_title),
                inFree = false,
                last = true,
            )

            // Inside the panel, under the last row: the column's own foot, so
            // the tint does not stop flush against a tick.
            Box(modifier = Modifier.height(spacing.md))
        }
    }
}

@Composable
private fun Header() {
    val spacing = ElecTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = spacing.sm),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.pro_compare_free),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(COLUMN),
        )
        Text(
            text = PRO,
            style = MaterialTheme.typography.titleSmall,
            color = ProGoldInk(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(COLUMN)
                .padding(top = spacing.md),
        )
    }
}

@Composable
private fun Feature(text: String, inFree: Boolean, last: Boolean = false) {
    val spacing = ElecTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(end = spacing.sm),
        )
        Mark(
            present = inFree,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            size = MARK,
        )
        Mark(present = true, tint = ProGoldInk(), size = MARK_PRO)
    }
    if (!last) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            // Stops short of the Pro column, so the panel is not crossed by
            // five lines that belong to the table rather than to it.
            modifier = Modifier.padding(end = COLUMN),
        )
    }
}

@Composable
private fun Mark(present: Boolean, tint: Color, size: Dp) {
    Box(
        modifier = Modifier.width(COLUMN),
        contentAlignment = Alignment.Center,
    ) {
        if (present) {
            Icon(
                imageVector = ElecIcons.StagePass,
                // The row's own text names the feature; the tick is its value.
                contentDescription = null,
                modifier = Modifier.size(size),
                tint = tint,
            )
        } else {
            // A dash, not an empty cell. Blank reads as "not filled in yet";
            // a dash reads as "deliberately not".
            Text(
                text = ABSENT,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

/** Untranslated, like the name on the store listing. */
private const val PRO = "Pro"
private const val ABSENT = "—"

private val COLUMN = 68.dp
private val ROW_HEIGHT = 52.dp
private val MARK = 20.dp
private val MARK_PRO = 24.dp
private val PANEL_CORNER = 16.dp
private val PANEL_STROKE = 1.dp
private const val TINT_ALPHA = 0.13f
private const val BORDER_ALPHA = 0.55f
