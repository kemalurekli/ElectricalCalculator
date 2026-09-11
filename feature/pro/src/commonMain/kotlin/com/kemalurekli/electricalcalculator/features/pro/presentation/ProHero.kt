package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme

/**
 * The panel a paywall opens with.
 *
 * ### Why this screen breaks the palette
 *
 * `docs/design-language.md` says surfaces are neutral-cool greys so a screen
 * full of figures reads as paper, and that brand colour is muted so it can sit
 * under numbers all day. Both of those are rules for a working screen. This is
 * not one: nothing is being calculated here and nothing has to be read for an
 * hour. It is the one surface in the app whose job is to feel like an occasion,
 * and a paywall that looks exactly like the settings page is a paywall nobody
 * reads.
 *
 * So the band is deep navy in both schemes — the same decision the document
 * preview makes about paper — with the gold that marks Pro everywhere else in
 * the app, and a soft radial glow behind the mark. The glow is the only
 * gradient in the app and it is doing something a flat fill cannot: it puts a
 * light source behind the one object on the screen that is being sold.
 *
 * What it deliberately is not: a stock purple-to-pink gradient, a countdown, a
 * fabricated user count. The first would belong to some other app, and the
 * other two are lies — this one has no users yet and a timer on a one-off
 * purchase is against the spirit of both stores' rules.
 */
@Composable
internal fun ProHero(
    headline: String,
    subline: String,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Rounded where it meets the page and square where it meets the
            // bar, so it reads as a panel the content sits below rather than
            // as a card floating at the top.
            .clip(RoundedCornerShape(bottomStart = HERO_CORNER, bottomEnd = HERO_CORNER))
            .background(HERO_INK),
    ) {
        // matchParentSize, not a size of its own: a glow that measures itself
        // makes the band as tall as the glow, which left a hand's width of
        // empty navy under the text.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(GLOW, Color.Transparent),
                        // Behind the mark rather than in the middle of the
                        // panel: the light is coming from the thing being sold.
                        center = Offset(GLOW_CENTRE_X, GLOW_CENTRE_Y),
                        radius = GLOW_RADIUS,
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xl)
                .padding(top = spacing.xl, bottom = spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            ProMark(highlighted = true)

            Text(
                text = headline,
                style = MaterialTheme.typography.headlineSmall,
                color = HERO_TEXT,
                textAlign = TextAlign.Center,
            )

            // Gold, and said here rather than beside the button. On a one-off
            // purchase "no subscription" is the strongest sentence on the
            // page, and a reader deciding whether to keep reading is deciding
            // now.
            Text(
                text = subline,
                style = MaterialTheme.typography.titleSmall,
                color = ProGold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Committed to in both schemes, like the document preview's paper. */
private val HERO_INK = Color(0xFF10344F)
private val HERO_TEXT = Color(0xFFF2F6FA)
private val GLOW = ProGold.copy(alpha = 0.20f)

private val HERO_CORNER = 28.dp
/** In pixels, measured from the panel's own top-left. */
private const val GLOW_CENTRE_X = 540f
private const val GLOW_CENTRE_Y = 130f
private const val GLOW_RADIUS = 620f
