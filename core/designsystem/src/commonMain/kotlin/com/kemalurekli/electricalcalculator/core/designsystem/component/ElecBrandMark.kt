package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The app's own mark — the bolt on its navy tile — drawn in Kotlin.
 *
 * The launcher icon exists twice over as platform assets (`ic_launcher_*.xml`
 * on Android, an asset catalogue on iOS) and neither is reachable from common
 * code. A screen that wants to show the product *itself* — the Pro section is
 * the only one so far — would otherwise need a third copy per platform, and on
 * iOS that means touching the Xcode project, which the port is explicitly
 * trying not to do.
 *
 * So the path is transcribed here from `app/src/main/res/drawable/`, in the
 * same 108-unit viewport, and scaled to whatever size the caller asks for. It
 * is the one duplicate in the project that is worth it; if the mark ever
 * changes, all three copies change together.
 *
 * The colours do **not** follow the theme. A logo that turns pale in dark mode
 * is not the logo any more — this is the same tile the reader tapped on their
 * home screen, and recognising it is the entire job.
 *
 * Decorative, and hidden from accessibility: the row it sits in has a title,
 * and "VoltageBoard logo" read aloud before it adds nothing.
 */
@Composable
fun ElecBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { hideFromAccessibility() },
    ) {
        val extent = this.size.minDimension
        val scale = extent / VIEWPORT

        drawRoundRect(
            color = MarkBackground,
            cornerRadius = CornerRadius(extent * ElecBrandMarkDefaults.CornerFraction),
        )

        val bolt = Path()
        BOLT_PATH.forEachIndexed { index, (x, y) ->
            if (index == 0) bolt.moveTo(x * scale, y * scale) else bolt.lineTo(x * scale, y * scale)
        }
        bolt.close()
        drawPath(path = bolt, color = MarkBolt)
    }
}

/** Shared constants for callers that draw something concentric with the mark. */
object ElecBrandMarkDefaults {
    /**
     * Corner radius as a fraction of the tile's side.
     *
     * Between Android's 25 % and iOS's squircle, which is nearer 22 % but bows
     * where a plain rounded rect does not. At 0.28 the tile reads as the app
     * icon on both without either platform's mask being imitated badly.
     */
    const val CornerFraction: Float = 0.28f
}

/** The navy of `ic_launcher_background.xml`. */
private val MarkBackground = Color(0xFF14496F)

/** The pale blue of `ic_launcher_foreground.xml`. */
private val MarkBolt = Color(0xFFCFE3F7)

/** The viewport the launcher vectors are authored in. */
private const val VIEWPORT = 108f

/**
 * The bolt, vertex by vertex, from the launcher's single `pathData`. Straight
 * lines only, so no curve support is needed to redraw it.
 */
private val BOLT_PATH = listOf(
    59.64f to 32.40f,
    40.85f to 55.88f,
    52.12f to 55.88f,
    48.36f to 75.60f,
    67.15f to 51.18f,
    55.88f to 51.18f,
)

@Preview
@Composable
private fun ElecBrandMarkPreview() {
    ElecBrandMark(size = 64.dp)
}
