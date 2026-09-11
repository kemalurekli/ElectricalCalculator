package com.kemalurekli.electricalcalculator.features.theory.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.platform.canBlurContent
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_pro_action
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_pro_body
import com.kemalurekli.electricalcalculator.feature.theory.generated.resources.th_pro_title

/**
 * Where an advanced topic stops for a reader who has not paid.
 *
 * ### The shape of the gate
 *
 * They read the first half at full size and full contrast. Then the text keeps
 * going and stops being readable, and under it is a card saying why. Nothing
 * jumps: the sentence they were reading does not end at a hard edge, it thins
 * out — which is the difference between a page that continues elsewhere and a
 * door slammed in the middle of a paragraph.
 *
 * The split is on a paragraph boundary nearest the halfway mark, so the free
 * half always ends on a finished thought.
 *
 * ### What hides the text, and what merely helps
 *
 * The fade hides it, and the fade is drawn the same way everywhere: the teaser
 * is painted to transparent over its own height. Blur is added on top where the
 * platform can do it.
 *
 * That division is deliberate and it is the whole reason [canBlurContent]
 * exists. `Modifier.blur` compiles on every target and does nothing on Android
 * below 12 — no exception, no warning, the text simply drawn in full. A gate
 * built on it would be open on three Android versions and nobody would find out
 * from the code. So the gate is the fade, which needs no API at all.
 */
@Composable
internal fun TheoryProseTeaser(text: String, modifier: Modifier = Modifier) {
    // The card's own colour, so the paragraph thins into the container it is
    // in rather than into the page behind it.
    val container = MaterialTheme.colorScheme.surfaceContainerLow

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TEASER_HEIGHT)
            .clipToBounds(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (canBlurContent) Modifier.blur(TEASER_BLUR) else Modifier)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, container),
                            startY = 0f,
                            endY = size.height,
                        ),
                    )
                },
        )
    }
}

@Composable
internal fun TheoryProGate(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing

    Box(modifier = modifier.fillMaxWidth()) {
        ElecCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Icon(
                        imageVector = ElecIcons.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(LOCK),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(Res.string.th_pro_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = stringResource(Res.string.th_pro_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onUnlock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.xs),
                ) {
                    Text(text = stringResource(Res.string.th_pro_action))
                }
            }
        }
    }
}

/**
 * Splits prose in half on a paragraph boundary.
 *
 * Nearest the midpoint by character count rather than by paragraph count: a
 * topic whose first paragraph is a page and whose next four are a line each
 * would otherwise give away almost everything or almost nothing.
 *
 * At least one paragraph is always free. A gate that starts before the reader
 * has read a sentence is not a teaser, it is a bounce.
 */
internal fun splitForGate(text: String): Pair<String, String> {
    val paragraphs = text.split(PARAGRAPH_BREAK).filter { it.isNotBlank() }
    if (paragraphs.size < 2) return text to ""

    val half = text.length / 2
    var taken = 0
    var index = 0
    while (index < paragraphs.lastIndex && taken + paragraphs[index].length <= half) {
        taken += paragraphs[index].length
        index++
    }
    val free = paragraphs.take(index.coerceAtLeast(1))
    val rest = paragraphs.drop(index.coerceAtLeast(1))
    return free.joinToString(PARAGRAPH_BREAK) to rest.joinToString(PARAGRAPH_BREAK)
}

private const val PARAGRAPH_BREAK = "\n\n"

/** Enough to see three or four lines going: more than a hint, less than a page. */
private val TEASER_HEIGHT = 96.dp
private val TEASER_BLUR = 5.dp
private val LOCK = 20.dp
