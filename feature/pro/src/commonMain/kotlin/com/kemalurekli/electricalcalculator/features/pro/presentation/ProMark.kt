package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecBrandMark
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecBrandMarkDefaults

/**
 * The app's mark inside a gold ring, with a highlight travelling around it.
 *
 * ### Why this is allowed to move
 *
 * `docs/design-language.md` says the app has no motion language and should not
 * grow one casually, and this is the first continuous animation in it. The
 * exemption is narrow and deliberate:
 *
 * - It is the only thing on the settings screen that is an *offer* rather than
 *   a control. Everything else there does something when pressed; this row asks
 *   to be pressed in the first place, and a row that asks has to be found.
 * - It stops the moment it is bought ([highlighted] goes false). A badge that
 *   keeps glinting at somebody who already paid is advertising to a customer.
 * - It is 49dp across. The rule exists to keep numbers still — a screen of
 *   readings that shimmers is unreadable — and nothing here is a number.
 *
 * The ring is drawn as a filled tile behind an opaque mark rather than as a
 * stroked outline: the mark covers the middle, so what is left is the border,
 * and no path arithmetic is needed to get an even one.
 */
@Composable
internal fun ProMark(
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "pro-mark")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN,
        animationSpec = infiniteRepeatable(
            // Linear, and slow. Anything eased has a beginning and an end, and
            // a highlight that keeps arriving somewhere is a progress bar.
            animation = tween(durationMillis = TURN_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pro-mark-sweep",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(MARK_SIZE + RING_WIDTH * 2)
                .clip(RoundedCornerShape(MARK_SIZE * ElecBrandMarkDefaults.CornerFraction + RING_WIDTH))
                .drawBehind {
                    // The ring is there whether or not anything is travelling
                    // around it, so the mark always has an edge instead of one
                    // that appears and leaves.
                    drawRect(color = RingBase)

                    if (!highlighted) return@drawBehind

                    // Rotating the canvas rotates the sweep gradient with it.
                    // The rectangle is oversized so its corners still cover the
                    // tile at 45°, where a same-sized one would have left the
                    // corners unpainted.
                    rotate(degrees = sweep) {
                        drawRect(
                            brush = Brush.sweepGradient(*SWEEP_STOPS, center = center),
                            topLeft = Offset(-size.width, -size.height),
                            size = Size(size.width * OVERDRAW, size.height * OVERDRAW),
                        )
                    }
                },
        )

        ElecBrandMark(size = MARK_SIZE)
    }
}

/**
 * The gold. Deliberately *not* `ElecColors.warning`, which is a near neighbour
 * on the wheel: that amber means "this result is close to a limit" everywhere
 * else in the app, and lending it to a badge would spend a meaning the
 * calculators need. This one is a brand accent and appears nowhere but here.
 */
private val ProGold = Color(0xFFE0A72E)

/** The glint at the head of the sweep — gold with the light on it. */
private val ProGlint = Color(0xFFFFE9A8)

/** The ring at rest, under whatever is travelling over it. */
private val RingBase = ProGold.copy(alpha = 0.30f)

/**
 * Stops for one revolution. Both ends fade to *transparent gold* rather than
 * [Color.Transparent], which is a transparent black and would drag the band
 * through grey on its way out.
 */
private val SWEEP_STOPS = arrayOf(
    0.00f to ProGold.copy(alpha = 0f),
    0.68f to ProGold.copy(alpha = 0f),
    0.82f to ProGold,
    0.90f to ProGlint,
    0.97f to ProGold,
    1.00f to ProGold.copy(alpha = 0f),
)

private val MARK_SIZE = 44.dp
private val RING_WIDTH = 2.5.dp
private const val FULL_TURN = 360f
private const val TURN_MILLIS = 3200
private const val OVERDRAW = 3f
