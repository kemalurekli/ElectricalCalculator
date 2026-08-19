package com.kemalurekli.electricalcalculator.core.common.util

import com.kemalurekli.electricalcalculator.core.common.result.Outcome
import com.kemalurekli.electricalcalculator.core.common.result.ValidationError
import com.kemalurekli.electricalcalculator.core.common.result.errorOrNull
import com.kemalurekli.electricalcalculator.core.common.result.getOrNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class NumericInputTest {

    // -- validate ----------------------------------------------------------

    @Test
    fun `blank input is required`() {
        assertEquals(ValidationError.Required, NumericInput.validate("").errorOrNull())
        assertEquals(ValidationError.Required, NumericInput.validate("   ").errorOrNull())
    }

    @Test
    fun `unparseable input is not a number`() {
        assertEquals(ValidationError.NotANumber, NumericInput.validate("abc").errorOrNull())
    }

    @Test
    fun `zero is rejected unless explicitly allowed`() {
        assertEquals(ValidationError.MustBePositive, NumericInput.validate("0").errorOrNull())
        assertTrue(NumericInput.validate("0", allowZero = true) is Outcome.Success)
    }

    @Test
    fun `negatives are rejected unless explicitly allowed`() {
        assertEquals(ValidationError.MustBePositive, NumericInput.validate("-5").errorOrNull())
        assertEquals(-5.0, NumericInput.validate("-5", allowNegative = true).getOrNull())
    }

    @Test
    fun `negative input reports the zero-allowing error when zero is permitted`() {
        assertEquals(
            ValidationError.MustNotBeNegative,
            NumericInput.validate("-5", allowZero = true).errorOrNull(),
        )
    }

    @Test
    fun `value outside a two-sided range reports the range`() {
        val error = NumericInput.validate("1.5", min = 0.0, max = 1.0, allowZero = true)
            .errorOrNull()
        assertEquals(ValidationError.OutOfRange(0.0, 1.0), error)
    }

    @Test
    fun `value below a lower bound only reports the minimum`() {
        assertEquals(
            ValidationError.BelowMinimum(10.0),
            NumericInput.validate("5", min = 10.0).errorOrNull(),
        )
    }

    @Test
    fun `value above an upper bound only reports the maximum`() {
        assertEquals(
            ValidationError.ExceedsMaximum(100.0),
            NumericInput.validate("150", max = 100.0).errorOrNull(),
        )
    }

    @Test
    fun `bounds are inclusive`() {
        assertTrue(NumericInput.validate("1", min = 1.0, max = 10.0) is Outcome.Success)
        assertTrue(NumericInput.validate("10", min = 1.0, max = 10.0) is Outcome.Success)
    }

    @Test
    fun `valid input yields the parsed value`() {
        assertEquals(230.0, NumericInput.validate("230").getOrNull())
        assertEquals(0.85, NumericInput.validate("0,85").getOrNull())
    }

    // -- sanitise ----------------------------------------------------------

    @Test
    fun `sanitise accepts digits and a single separator`() {
        assertEquals("12.5", NumericInput.sanitise(current = "12.", proposed = "12.5"))
        assertEquals("12,5", NumericInput.sanitise(current = "12,", proposed = "12,5"))
    }

    @Test
    fun `sanitise rejects a second separator`() {
        assertEquals("12.5", NumericInput.sanitise(current = "12.5", proposed = "12.5."))
    }

    @Test
    fun `sanitise rejects letters and symbols`() {
        assertEquals("12", NumericInput.sanitise(current = "12", proposed = "12a"))
        assertEquals("12", NumericInput.sanitise(current = "12", proposed = "12 "))
    }

    @Test
    fun `sanitise rejects a minus sign unless negatives are allowed`() {
        assertEquals("", NumericInput.sanitise(current = "", proposed = "-"))
        assertEquals("-5", NumericInput.sanitise(current = "-", proposed = "-5", allowNegative = true))
    }

    @Test
    fun `sanitise always allows clearing the field`() {
        assertEquals("", NumericInput.sanitise(current = "123", proposed = ""))
    }
}
