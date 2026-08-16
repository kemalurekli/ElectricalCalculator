package com.kemalurekli.electricalcalculator.features.theory

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.ElecTestTags
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.features.theory.presentation.TheoryTopicRoute
import com.kemalurekli.electricalcalculator.testing.HiltTestActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end coverage of a theory topic against the real Hilt graph.
 *
 * The unit tests prove the arithmetic and the formatting; this proves the wiring
 * — that a route argument loads a topic, that the declared fields become real
 * inputs, and that pressing Calculate puts a derivation on the screen.
 *
 * ### On the locale
 *
 * Every string comes from resources and every number through [NumberFormatter],
 * never written out in English. The app's per-app language persists in app
 * storage, so literals would make this suite report on a preference rather than
 * on the code — the lesson of commit `c3db95b`, which had to undo exactly that.
 */
@HiltAndroidTest
class TheoryEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = composeTestRule.activity
    }

    private fun setContent(topicKey: String) {
        composeTestRule.setContent {
            ElecToolkitTheme {
                TheoryTopicRoute(
                    topicKey = topicKey,
                    onCalculatorClick = {},
                    onReferenceClick = {},
                    onGlossaryClick = {},
                    onNavigateBack = {},
                )
            }
        }
    }

    @Test
    fun aTopicOpensOnItsDerivationRatherThanItsForm() {
        setContent("ohm_law")

        // The prose and the formula start expanded: on this shelf the derivation
        // is the subject, not a caveat folded away under the answer.
        composeTestRule
            .onNodeWithText(context.getString(R.string.th_section_theory))
            .assertIsDisplayed()
    }

    @Test
    fun typingIntoTheDeclaredFieldsAndCalculatingProducesAWorkedSolution() {
        setContent("ohm_law")

        // The form sits below the diagram and the derivation, so every node has
        // to be scrolled into the lazy list before it exists to be touched.
        // "Resistance" is both a field label and a target the selector offers, so
        // the field is picked by the one thing only it has: a text input action.
        val resistance = context.getString(R.string.th_field_resistance)
        composeTestRule.scrollTo(hasText(resistance) and hasSetTextAction())
        composeTestRule.onNode(hasText(resistance) and hasSetTextAction())
            .performTextReplacement("529")

        val calculate = context.getString(R.string.action_calculate)
        composeTestRule.scrollTo(hasText(calculate))
        composeTestRule.onNodeWithText(calculate).performClick()

        // The result card announces itself as one node, and the screen scrolls
        // back to it, so no scroll is needed here — if one were, the scroll-to-
        // result effect would be broken and this would be the test that says so.
        val current = NumberFormatter.format(230.0 / 529.0, DISPLAY_DECIMALS)
        composeTestRule
            .onNodeWithContentDescription(current, substring = true)
            .assertIsDisplayed()

        // And the derivation, which is what separates this from a calculator:
        // the symbolic line, then the reader's own numbers in it.
        composeTestRule.scrollTo(hasText(OHM_FORMULA, substring = true))
        composeTestRule
            .onNodeWithText(OHM_FORMULA, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun switchingTheTargetKeepsAValueTheNewTargetAlsoAsksFor() {
        setContent("ohm_law")

        val voltageField = hasText(context.getString(R.string.th_field_voltage)) and
            hasSetTextAction()
        composeTestRule.scrollTo(voltageField)
        composeTestRule.onNode(voltageField).performTextReplacement("400")

        // The selector option carries the same word as a field label; it is
        // told apart by being selectable rather than typeable.
        val solveForResistance =
            hasText(context.getString(R.string.th_ohm_law_solve_resistance)) and isSelectable()
        composeTestRule.scrollTo(solveForResistance)
        composeTestRule.onNode(solveForResistance).performClick()

        // Rearranging keeps the voltage, because it is the same voltage.
        composeTestRule.scrollTo(voltageField)
        composeTestRule.onNode(voltageField).assertTextContains("400")
    }

    @Test
    fun aRouteNamingATopicThatIsGoneLandsSoftly() {
        setContent("no_such_topic")

        // ElecEmptyState collapses to one node carrying "title. message", so it
        // is asserted by description — which also checks the screen-reader text.
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.th_not_found_title),
                substring = true,
            )
            .assertIsDisplayed()
    }

    /** Brings a node into the lazy list before touching it. */
    private fun androidx.compose.ui.test.junit4.ComposeTestRule.scrollTo(
        matcher: androidx.compose.ui.test.SemanticsMatcher,
    ) {
        onNodeWithTag(ElecTestTags.THEORY_FORM).performScrollToNode(matcher)
    }

    private companion object {
        const val DISPLAY_DECIMALS = 2
        const val OHM_FORMULA = "I = U / R"
    }
}
