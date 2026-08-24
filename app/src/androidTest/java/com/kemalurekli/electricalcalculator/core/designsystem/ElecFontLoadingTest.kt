package com.kemalurekli.electricalcalculator.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.test.junit4.createComposeRule
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericTextStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

/**
 * Proves the app draws in the typefaces it ships rather than the platform's.
 *
 * This is not a style preference being enforced. On Android, `Font()` reads from
 * the asset manager and nothing else — no exception, no log line — so a build
 * that fails to package the fonts as assets renders the whole app in Roboto and
 * looks, at a glance, fine. It shipped that way, and it was found by measuring
 * glyph advances in a screenshot rather than by anything failing.
 *
 * The two assertions are chosen so that Roboto fails both:
 *
 * - **Figures.** A comma and a digit have the same advance in JetBrains Mono and
 *   in no proportional face. That is the property the design language actually
 *   wants: a column of results that lines up, and a figure that does not shift
 *   the digits beside it when it changes from 5.17 to 18.40.
 * - **Text.** Inter draws its digits proportionally, so `1` is narrower than `4`.
 *   Roboto's are tabular and identical. Measuring body text separately catches
 *   the case where only one of the two faces resolves.
 *
 * Instrumented rather than a host test because the failure was in asset
 * packaging, which only exists in an installed APK.
 */
class ElecFontLoadingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun figuresAreSetInTheBundledMonospacedFace() {
        val (comma, zero) = widthsOf(",", "0") { NumericTextStyle }

        assertEquals(
            "A comma and a digit must occupy the same width. They do not in any " +
                "proportional face, so a difference here means JetBrains Mono did not " +
                "load and Compose fell back to the platform default.",
            zero,
            comma,
        )
    }

    @Test
    fun bodyTextIsSetInTheBundledProportionalFace() {
        val (one, four) = widthsOf("1", "4") { MaterialTheme.typography.bodyLarge }

        assertNotEquals(
            "Inter draws its digits proportionally, so 1 is narrower than 4. Equal " +
                "widths mean the bundled text face did not load — Roboto sets its " +
                "digits tabular.",
            four,
            one,
        )
    }

    /**
     * The rendered widths of [texts], in pixels, in the style [style] resolves to.
     *
     * Measured through the composition rather than read off the font file,
     * because the question is which face Compose resolved and not what the file
     * on disk says.
     *
     * The fonts arrive asynchronously, so the first frame measures the fallback
     * and a recomposition follows with the real one. Measuring in a `SideEffect`
     * means the last value written is the one from the settled frame.
     */
    private fun widthsOf(vararg texts: String, style: @Composable () -> TextStyle): List<Int> {
        val widths = mutableStateOf(emptyList<Int>())

        composeTestRule.setContent {
            ElecToolkitTheme {
                val measurer = rememberTextMeasurer()
                val resolved = style()
                SideEffect {
                    widths.value = texts.map { measurer.measure(it, resolved).size.width }
                }
            }
        }

        composeTestRule.waitUntil(FONT_LOAD_TIMEOUT_MILLIS) { widths.value.size == texts.size }
        composeTestRule.waitForIdle()
        return widths.value
    }

    private companion object {
        const val FONT_LOAD_TIMEOUT_MILLIS = 5_000L
    }
}
