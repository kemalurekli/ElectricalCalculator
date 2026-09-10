package com.kemalurekli.electricalcalculator.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The three glyphs of the app's chrome that are drawn rather than taken from
 * Material.
 *
 * `docs/design-language.md` left these open: the set is Material Outlined, and
 * two of its marks — the back arrow and the vertical overflow ellipsis — read
 * as foreign on iOS, where nothing has ever used them. Share is a third: the
 * three-node graph means "share" on Android and means nothing on iOS.
 *
 * The one rule says the app does not branch on platform, so the answer is not
 * a Cupertino variant. It is a single glyph that is *right on iOS and not
 * wrong on Android*, which is what each of these is: a bare chevron for back,
 * a tray with an ascending arrow for share, and a circled horizontal ellipsis
 * for overflow. All three are what an iOS reader expects, and all three appear
 * often enough on Android — chevrons in navigation, the tray in every
 * screenshot of an iPhone — to be read without instruction.
 *
 * They are drawn on Material's own 24 grid at a 2 px stroke so they sit beside
 * the rest of the set without looking traced by a different hand.
 */

private const val CHROME_SIZE = 24f
private const val CHROME_STROKE = 2f

private fun chromeIcon(name: String, autoMirror: Boolean = false) = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = CHROME_SIZE,
    viewportHeight = CHROME_SIZE,
    autoMirror = autoMirror,
)

/**
 * A stroked path, declared black and re-tinted by the caller — the same
 * arrangement the electrical symbols use, and what lets one glyph serve both
 * colour schemes.
 */
private fun ImageVector.Builder.stroked(
    width: Float = CHROME_STROKE,
    content: PathBuilder.() -> Unit,
) = path(
    stroke = SolidColor(Color.Black),
    strokeLineWidth = width,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = content,
)

private fun ImageVector.Builder.filled(content: PathBuilder.() -> Unit) = path(
    fill = SolidColor(Color.Black),
    pathBuilder = content,
)

/** A filled disc. Two half-arcs, because a 360° arc has no length to sweep. */
private fun PathBuilder.dot(cx: Float, cy: Float, radius: Float) {
    moveTo(cx - radius, cy)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, cx + radius, cy)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, cx - radius, cy)
    close()
}

internal object ElecChromeIcons {

    /**
     * Back.
     *
     * Seven wide by fourteen tall — taller than it is wide, which is the
     * proportion that reads as a navigation chevron rather than as a
     * disclosure arrow. Auto-mirrored, so a right-to-left locale flips it
     * without a second asset.
     */
    val Back: ImageVector = chromeIcon("ElecBack", autoMirror = true)
        .stroked(width = 2.2f) {
            moveTo(15.5f, 5f)
            lineTo(8.5f, 12f)
            lineTo(15.5f, 19f)
        }
        .build()

    /**
     * Share: a document leaving an open tray.
     *
     * The tray is open at the top so the arrow reads as passing through it
     * rather than as sitting on a lid.
     */
    val Share: ImageVector = chromeIcon("ElecShare")
        .stroked(width = 1.9f) {
            // The tray, drawn from one lip round to the other.
            moveTo(8.6f, 9.6f)
            lineTo(7.0f, 9.6f)
            quadTo(5.4f, 9.6f, 5.4f, 11.2f)
            lineTo(5.4f, 18.4f)
            quadTo(5.4f, 20.0f, 7.0f, 20.0f)
            lineTo(17.0f, 20.0f)
            quadTo(18.6f, 20.0f, 18.6f, 18.4f)
            lineTo(18.6f, 11.2f)
            quadTo(18.6f, 9.6f, 17.0f, 9.6f)
            lineTo(15.4f, 9.6f)
            // The shaft and its head.
            moveTo(12f, 14.6f)
            lineTo(12f, 3.8f)
            moveTo(8.5f, 7.3f)
            lineTo(12f, 3.8f)
            lineTo(15.5f, 7.3f)
        }
        .build()

    /**
     * Overflow: a horizontal ellipsis inside a ring.
     *
     * The ring is not decoration. `ElecIcons.MoreTab` is a bare horizontal
     * ellipsis and the two must stay distinguishable — one opens a section of
     * the app, the other opens actions on the row beside it — which is the
     * distinction the vertical ellipsis used to carry before it was dropped
     * for being unreadable on iOS.
     */
    val Overflow: ImageVector = chromeIcon("ElecOverflow")
        .stroked(width = 1.5f) {
            moveTo(3f, 12f)
            arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 12f)
            arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 12f)
        }
        .filled {
            dot(8.2f, 12f, 1.15f)
            dot(12f, 12f, 1.15f)
            dot(15.8f, 12f, 1.15f)
        }
        .build()
}
