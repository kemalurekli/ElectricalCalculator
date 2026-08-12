package com.kemalurekli.electricalcalculator.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecNumericField
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Verifies the input filtering and error rendering that every calculator form
 * depends on, so a regression here is caught once rather than in each screen.
 */
class ElecNumericFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun acceptsDigitsAndASingleSeparator() {
        var value = ""
        setContent(getValue = { value }, onValueChange = { value = it })

        composeTestRule.onNodeWithText(LABEL).performTextInput("12.5")

        assertEquals("12.5", value)
    }

    @Test
    fun rejectsLetters() {
        var value = "12"
        setContent(getValue = { value }, onValueChange = { value = it })

        composeTestRule.onNodeWithText("12").performTextInput("a")

        assertEquals("12", value)
    }

    @Test
    fun rejectsASecondSeparator() {
        var value = "12.5"
        setContent(getValue = { value }, onValueChange = { value = it })

        composeTestRule.onNodeWithText("12.5").performTextInput(".")

        assertEquals("12.5", value)
    }

    @Test
    fun allowsClearingTheField() {
        var value = "12.5"
        setContent(getValue = { value }, onValueChange = { value = it })

        composeTestRule.onNodeWithText("12.5").performTextReplacement("")

        assertEquals("", value)
    }

    @Test
    fun showsTheValidationMessageWhenInErrorState() {
        // Resolved from resources rather than written out: the app's per-app
        // language persists, so an English literal here asserts against a field
        // that may correctly be showing Turkish.
        lateinit var mustBePositive: String
        composeTestRule.setContent {
            mustBePositive = stringResource(R.string.validation_must_be_positive)
            ElecToolkitTheme {
                ElecNumericField(
                    value = "0",
                    onValueChange = {},
                    label = LABEL,
                    unit = "m",
                    error = ValidationError.MustBePositive,
                )
            }
        }

        composeTestRule
            .onNodeWithText(mustBePositive)
            .assertIsDisplayed()
    }

    @Test
    fun showsSupportingTextWhenThereIsNoError() {
        composeTestRule.setContent {
            ElecToolkitTheme {
                ElecNumericField(
                    value = "230",
                    onValueChange = {},
                    label = LABEL,
                    supportingText = SUPPORTING_TEXT,
                )
            }
        }

        composeTestRule.onNodeWithText(SUPPORTING_TEXT).assertIsDisplayed()
    }

    private fun setContent(getValue: () -> String, onValueChange: (String) -> Unit) {
        composeTestRule.setContent {
            // `remember` is required: without it the state object is recreated
            // on every recomposition and typed input is discarded.
            var state by remember { mutableStateOf(getValue()) }
            ElecToolkitTheme {
                ElecNumericField(
                    value = state,
                    onValueChange = {
                        state = it
                        onValueChange(it)
                    },
                    label = LABEL,
                    unit = "m",
                )
            }
        }
    }

    private companion object {
        const val LABEL = "Cable length"
        const val SUPPORTING_TEXT = "Line-to-line for three phase"
    }
}
