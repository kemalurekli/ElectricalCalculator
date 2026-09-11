package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme

/**
 * The top of the paywall: what is being sold, and one sentence about the price.
 *
 * ### What this replaces
 *
 * A deep navy panel with the document inside it. The panel was a frame — a
 * saturated block with a hard edge pasted onto a light screen — and a white
 * page sitting inside a navy margin read as a picture of a document rather than
 * as a document. Framing a thing is how you say "here is an image of it".
 *
 * Nothing frames it now. The page runs to both edges of the screen and
 * dissolves into the background, which is the opposite claim: not a picture on
 * the wall, a sheet lying under the glass. The only colour left at the top is
 * the gold, which is the app's word for Pro and is also the colour of the
 * column and the button further down — so the eye is led from the page to the
 * price along one line rather than across three coloured regions.
 *
 * The glow stays, as a wash on the page's own background rather than inside a
 * box. It is the single gradient in the app and it is doing something a flat
 * fill cannot: putting a light source behind the one object being sold.
 */
@Composable
internal fun ProShowcase(
    subline: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val spacing = ElecTheme.spacing
    val surface = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SHOWCASE_HEIGHT)
                .clipToBounds()
                .drawWithContent {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(GLOW, Color.Transparent),
                            center = Offset(size.width / 2f, size.height * GLOW_CENTRE),
                            radius = size.width * GLOW_RADIUS,
                        ),
                    )
                    drawContent()
                    // Painted after the content and in the page's own colour,
                    // so the sheet does not end — it stops being visible.
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, surface),
                            startY = size.height * FADE_START,
                            endY = size.height,
                        ),
                    )
                },
            contentAlignment = Alignment.TopCenter,
        ) {
            content()
        }

        ProMark(highlighted = true)

        // The only sentence up here. A headline used to sit above it and was
        // the weaker of the two: it named what the reader had just tried to
        // do, which they knew. On a one-off purchase "no subscription" is the
        // strongest thing the page can say.
        Text(
            text = subline,
            style = MaterialTheme.typography.titleMedium,
            color = ProGoldInk(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = spacing.xl),
        )
    }
}

/**
 * Tall enough for the head of an A4 page — the title, the formula and the
 * figures — and short enough that the table is on the first screen with it.
 */
private val SHOWCASE_HEIGHT = 260.dp

private val GLOW = ProGold.copy(alpha = 0.10f)
private const val GLOW_CENTRE = 0.45f
private const val GLOW_RADIUS = 0.68f

/** Where the page starts giving way to the background. */
private const val FADE_START = 0.52f
