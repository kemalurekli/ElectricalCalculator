package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
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
 */
@Composable
internal fun ProComparison(modifier: Modifier = Modifier) {
    val spacing = ElecTheme.spacing

    ElecCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Header()

            Feature(stringResource(Res.string.pro_compare_calculators), inFree = true)
            Feature(stringResource(Res.string.pro_compare_reference), inFree = true)
            Feature(stringResource(Res.string.pro_compare_forum), inFree = true)
            Feature(stringResource(Res.string.pro_benefit_calculation_title), inFree = false)
            Feature(stringResource(Res.string.pro_benefit_schedule_title), inFree = false)
            Feature(
                text = stringResource(Res.string.pro_benefit_future_title),
                inFree = false,
                last = true,
            )
        }
    }
}

@Composable
private fun Header() {
    val spacing = ElecTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.lg, top = spacing.md, bottom = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f))
        ColumnLabel(
            text = stringResource(Res.string.pro_compare_free),
            colour = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ColumnLabel(text = PRO, colour = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ColumnLabel(text: String, colour: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = colour,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(COLUMN),
    )
}

/**
 * One row.
 *
 * The Pro column is tinted down the whole height of the table rather than cell
 * by cell, which is what makes it read as a column somebody could run a finger
 * down instead of as six separate highlights.
 */
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
                .padding(start = spacing.lg, end = spacing.sm),
        )
        Mark(present = inFree, tinted = false)
        Mark(present = true, tinted = true, rounded = last)
    }
    if (!last) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(start = spacing.lg),
        )
    }
}

@Composable
private fun RowScope.Mark(present: Boolean, tinted: Boolean, rounded: Boolean = false) {
    val shape = if (rounded) {
        RoundedCornerShape(bottomStart = TINT_CORNER, bottomEnd = TINT_CORNER)
    } else {
        RoundedCornerShape(0.dp)
    }

    Box(
        modifier = Modifier
            .width(COLUMN)
            .fillMaxHeight()
            .clip(shape)
            .background(
                if (tinted) {
                    MaterialTheme.colorScheme.primary.copy(alpha = TINT_ALPHA)
                } else {
                    Color.Transparent
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (present) {
            Icon(
                imageVector = ElecIcons.StagePass,
                // The row's own text names the feature; the tick is its value.
                contentDescription = null,
                modifier = Modifier.size(MARK),
                tint = if (tinted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
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

private val COLUMN = 64.dp
private val ROW_HEIGHT = 52.dp
private val MARK = 20.dp
private val TINT_CORNER = 14.dp
private const val TINT_ALPHA = 0.08f
