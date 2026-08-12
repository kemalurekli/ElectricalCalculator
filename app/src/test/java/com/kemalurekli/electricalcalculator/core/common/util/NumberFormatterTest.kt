package com.kemalurekli.electricalcalculator.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

/**
 * Every case passes an explicit [Locale] — relying on the JVM default would
 * make these tests pass or fail depending on the machine running them.
 */
class NumberFormatterTest {

    private val us = Locale.US
    private val germany = Locale.GERMANY

    // -- format ------------------------------------------------------------

    @Test
    fun `format drops trailing zeros`() {
        assertEquals("12.5", NumberFormatter.format(12.50, decimals = 2, locale = us))
        assertEquals("12", NumberFormatter.format(12.00, decimals = 2, locale = us))
    }

    @Test
    fun `format rounds half up`() {
        assertEquals("2.35", NumberFormatter.format(2.345, decimals = 2, locale = us))
        assertEquals("3", NumberFormatter.format(2.5, decimals = 0, locale = us))
    }

    @Test
    fun `format uses the locale decimal separator`() {
        assertEquals("12,5", NumberFormatter.format(12.5, decimals = 2, locale = germany))
    }

    @Test
    fun `format groups thousands`() {
        assertEquals("1,234.5", NumberFormatter.format(1234.5, decimals = 2, locale = us))
    }

    @Test
    fun `format handles negatives`() {
        assertEquals("-12.5", NumberFormatter.format(-12.5, decimals = 2, locale = us))
    }

    // -- formatSignificant -------------------------------------------------

    @Test
    fun `formatSignificant keeps meaningful digits on small values`() {
        // Fixed 2-decimal rounding would flatten this to "0".
        assertEquals(
            "0.001235",
            NumberFormatter.formatSignificant(0.00123456, significantDigits = 4, locale = us),
        )
    }

    @Test
    fun `formatSignificant rounds large values above the decimal point`() {
        assertEquals(
            "123,500",
            NumberFormatter.formatSignificant(123456.0, significantDigits = 4, locale = us),
        )
    }

    @Test
    fun `formatSignificant renders zero without decimals`() {
        assertEquals("0", NumberFormatter.formatSignificant(0.0, locale = us))
    }

    // -- formatEngineering -------------------------------------------------

    @Test
    fun `formatEngineering stays in plain decimal while that is readable`() {
        assertEquals("11,000", NumberFormatter.formatEngineering(11_000.0, 6, us))
        assertEquals("0.001235", NumberFormatter.formatEngineering(0.00123456, 4, us))
        assertEquals("2.5", NumberFormatter.formatEngineering(2.5, 6, us))
    }

    @Test
    fun `formatEngineering switches to scientific notation for tiny values`() {
        // A circular mil in m². Written out in full this is ten zeros the
        // reader has to count.
        assertEquals(
            "5.06707\u00d710\u207b\u00b9\u2070",
            NumberFormatter.formatEngineering(5.067074790974978e-10, 6, us),
        )
    }

    @Test
    fun `formatEngineering switches to scientific notation for huge values`() {
        assertEquals("3.6\u00d710\u00b9\u2070", NumberFormatter.formatEngineering(3.6e10, 6, us))
    }

    @Test
    fun `formatEngineering keeps the sign in scientific notation`() {
        assertEquals("-1.5\u00d710\u207b\u2076", NumberFormatter.formatEngineering(-1.5e-6, 6, us))
    }

    @Test
    fun `formatEngineering follows the locale separator`() {
        assertEquals("1,5\u00d710\u207b\u2076", NumberFormatter.formatEngineering(1.5e-6, 6, germany))
    }

    @Test
    fun `formatEngineering renders zero plainly`() {
        assertEquals("0", NumberFormatter.formatEngineering(0.0, 6, us))
    }

    // -- parseOrNull -------------------------------------------------------

    @Test
    fun `parseOrNull accepts both decimal separators`() {
        assertEquals(12.5, NumberFormatter.parseOrNull("12.5"))
        assertEquals(12.5, NumberFormatter.parseOrNull("12,5"))
    }

    @Test
    fun `parseOrNull trims surrounding whitespace`() {
        assertEquals(230.0, NumberFormatter.parseOrNull("  230  "))
    }

    @Test
    fun `parseOrNull rejects mixed separators as ambiguous`() {
        // "1,234.5" and "1.234,5" are the same characters with opposite
        // meanings across locales, so neither is guessed at.
        assertNull(NumberFormatter.parseOrNull("1,234.5"))
        assertNull(NumberFormatter.parseOrNull("1.234,5"))
    }

    @Test
    fun `parseOrNull rejects Kotlin number syntax that is not user input`() {
        assertNull(NumberFormatter.parseOrNull("1d"))
        assertNull(NumberFormatter.parseOrNull("0x1p3"))
        assertNull(NumberFormatter.parseOrNull("Infinity"))
        assertNull(NumberFormatter.parseOrNull("NaN"))
    }

    @Test
    fun `parseOrNull rejects blank and non numeric text`() {
        assertNull(NumberFormatter.parseOrNull(""))
        assertNull(NumberFormatter.parseOrNull("   "))
        assertNull(NumberFormatter.parseOrNull("abc"))
        assertNull(NumberFormatter.parseOrNull("12 5"))
    }

    @Test
    fun `parseOrNull accepts signs and exponents`() {
        assertEquals(-12.5, NumberFormatter.parseOrNull("-12.5"))
        assertEquals(1500.0, NumberFormatter.parseOrNull("1.5e3"))
        assertEquals(0.5, NumberFormatter.parseOrNull(".5"))
    }

    // -- formatWithSiPrefix ------------------------------------------------

    @Test
    fun `formatWithSiPrefix scales into the nearest prefix`() {
        assertEquals("1.5 kW", NumberFormatter.formatWithSiPrefix(1500.0, "W", locale = us))
        assertEquals("2.2 MW", NumberFormatter.formatWithSiPrefix(2_200_000.0, "W", locale = us))
        assertEquals("470 mA", NumberFormatter.formatWithSiPrefix(0.47, "A", locale = us))
    }

    @Test
    fun `formatWithSiPrefix leaves values in base range unprefixed`() {
        assertEquals("230 V", NumberFormatter.formatWithSiPrefix(230.0, "V", locale = us))
    }

    @Test
    fun `formatWithSiPrefix keeps the sign of negative values`() {
        assertEquals("-1.5 kW", NumberFormatter.formatWithSiPrefix(-1500.0, "W", locale = us))
    }
}
