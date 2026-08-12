package com.kemalurekli.electricalcalculator.features.calculators.voltagedrop

import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kemalurekli.electricalcalculator.core.designsystem.ElecTestTags
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.domain.repository.HistoryRepository
import com.kemalurekli.electricalcalculator.features.calculators.voltagedrop.presentation.VoltageDropRoute
import com.kemalurekli.electricalcalculator.testing.HiltTestActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * End-to-end coverage of the voltage drop calculator against the real Hilt
 * graph and a real (in-memory) database.
 *
 * The unit tests prove the arithmetic and the screen tests prove the rendering;
 * this proves the wiring between them — that typing into the form, pressing
 * Calculate, and the record reaching the history table are actually connected.
 */
@HiltAndroidTest
class VoltageDropEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var historyRepository: HistoryRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            ElecToolkitTheme {
                VoltageDropRoute(onReferenceClick = {}, onNavigateBack = {})
            }
        }
    }

    @Test
    fun calculatingProducesTheExpectedResultAndSavesHistory() = runTest {
        enterValidRun()

        scrollTo("Calculate")
        composeTestRule.onNodeWithText("Calculate").performClick()
        composeTestRule.waitForIdle()

        // 230 V, 20 A, 30 m, 4 mm² copper at 20 °C:
        //   R  = 0.017241 · 30 / 4 = 0.129308 Ω
        //   ΔU = 2 · 20 · R        = 5.17 V  (2.25 %)
        composeTestRule
            .onNodeWithContentDescription("Voltage drop: 5.17 V", substring = true)
            .assertIsDisplayed()

        val records = historyRepository.observeAll().first()
        assertEquals(1, records.size)
        val record = records.single()
        assertEquals(CalculatorId.VOLTAGE_DROP, record.calculatorId)
        assertTrue(record.summary.contains("5.17"))
        // The raw inputs are stored so the run can be reloaded later.
        assertEquals("230", record.inputs["voltage"])
        assertEquals("4", record.inputs["cross_section"])
    }

    @Test
    fun anInvalidRunShowsAnErrorAndSavesNothing() = runTest {
        // Everything but the cross-section, which is required.
        replaceField("System voltage", "230")
        replaceField("Design current", "20")
        replaceField("Route length (one way)", "30")

        scrollTo("Calculate")
        composeTestRule.onNodeWithText("Calculate").performClick()
        composeTestRule.waitForIdle()

        scrollTo("This value is required")
        composeTestRule.onNodeWithText("This value is required").assertIsDisplayed()

        assertTrue(historyRepository.observeAll().first().isEmpty())
    }

    @Test
    fun editingAnInputAfterCalculatingRemovesTheStaleResult() = runTest {
        enterValidRun()
        scrollTo("Calculate")
        composeTestRule.onNodeWithText("Calculate").performClick()
        composeTestRule.waitForIdle()

        // The screen scrolls the result into view on its own.
        composeTestRule
            .onNodeWithContentDescription("Voltage drop:", substring = true)
            .assertIsDisplayed()

        replaceField("Route length (one way)", "60")
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Voltage drop:", substring = true)
            .assertDoesNotExist()
    }

    private fun enterValidRun() {
        replaceField("System voltage", "230")
        replaceField("Design current", "20")
        replaceField("Route length (one way)", "30")
        replaceField("Cross-section", "4")
        replaceField("Conductor temperature", "20")
    }

    private fun replaceField(label: String, value: String) {
        scrollTo(label)
        composeTestRule.onNodeWithText(label, substring = true).performTextReplacement(value)
        composeTestRule.waitForIdle()
        awaitImeSettled()
    }

    /**
     * Waits for the keyboard to finish animating in or out.
     *
     * Typing into a field focuses it and the keyboard opens. The screen resizes
     * to sit above it — that is the point of `contentWindowInsets` on the
     * Scaffold — so the form relayouts on every frame of the IME animation. Act
     * on the *next* field during those frames and the node resolved a moment
     * earlier has already been disposed, which fails as "the node is no longer
     * in the tree". Intermittently: it depends where the animation had got to.
     *
     * `waitForIdle` does not cover this. The inset animation is driven by the
     * window rather than by Compose, so the composition is legitimately idle
     * between frames. Waiting it out here, right after the keystroke that
     * causes it, means every later scroll and tap sees a still viewport.
     *
     * The condition is that the inset has *held* a value, not merely repeated
     * it: sampling twice before the animation starts finds 0 both times and
     * would settle on a keyboard that is about to move. Returning quietly on
     * timeout is deliberate — a device that never shows a keyboard is not this
     * test's failure to report.
     */
    private fun awaitImeSettled() {
        val deadline = SystemClock.uptimeMillis() + SETTLE_TIMEOUT_MS
        var previous = Int.MIN_VALUE
        var unchangedSince = 0L

        while (SystemClock.uptimeMillis() < deadline) {
            composeTestRule.waitForIdle()
            val bottom = imeInsetBottom()

            if (bottom != previous) {
                previous = bottom
                unchangedSince = SystemClock.uptimeMillis()
            } else if (SystemClock.uptimeMillis() - unchangedSince >= HELD_STILL_MS) {
                return
            }
            Thread.sleep(POLL_MS)
        }
    }

    /** Height the keyboard currently covers, in pixels; 0 when it is closed. */
    private fun imeInsetBottom(): Int =
        ViewCompat.getRootWindowInsets(composeTestRule.activity.window.decorView)
            ?.getInsets(WindowInsetsCompat.Type.ime())
            ?.bottom
            ?: 0

    private fun scrollTo(text: String) {
        composeTestRule
            .onNodeWithTag(ElecTestTags.CALCULATOR_FORM)
            .performScrollToNode(hasText(text, substring = true))
        composeTestRule.waitForIdle()
    }

    private companion object {
        /** Generous: a ceiling on a stuck animation, not an expected wait. */
        const val SETTLE_TIMEOUT_MS = 5_000L

        /** How long the inset must hold one value before it counts as settled. */
        const val HELD_STILL_MS = 250L

        const val POLL_MS = 32L
    }
}
