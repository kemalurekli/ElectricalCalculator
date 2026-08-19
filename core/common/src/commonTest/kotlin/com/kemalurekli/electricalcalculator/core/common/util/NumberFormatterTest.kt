package com.kemalurekli.electricalcalculator.core.common.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Every case passes explicit [NumberSymbols] — relying on the device default
 * would make these tests pass or fail depending on the machine running them.
 *
 * These run on the JVM *and* on iOS. That is the point of them: they were
 * written against `DecimalFormat` and `BigDecimal`, and they are the only thing
 * standing between the common-Kotlin reimplementation and a silent change to
 * every number the app displays.
 */
class NumberFormatterTest {

    private val us = NumberSymbols.Point
    private val germany = NumberSymbols.Comma

    // -- format ------------------------------------------------------------

    @Test
    fun `format drops trailing zeros`() {
        assertEquals("12.5", NumberFormatter.format(12.50, decimals = 2, symbols = us))
        assertEquals("12", NumberFormatter.format(12.00, decimals = 2, symbols = us))
    }

    @Test
    fun `format rounds half up`() {
        assertEquals("2.35", NumberFormatter.format(2.345, decimals = 2, symbols = us))
        assertEquals("3", NumberFormatter.format(2.5, decimals = 0, symbols = us))
    }

    @Test
    fun `format uses the locale decimal separator`() {
        assertEquals("12,5", NumberFormatter.format(12.5, decimals = 2, symbols = germany))
    }

    @Test
    fun `format groups thousands`() {
        assertEquals("1,234.5", NumberFormatter.format(1234.5, decimals = 2, symbols = us))
    }

    @Test
    fun `format handles negatives`() {
        assertEquals("-12.5", NumberFormatter.format(-12.5, decimals = 2, symbols = us))
    }

    // -- formatSignificant -------------------------------------------------

    @Test
    fun `formatSignificant keeps meaningful digits on small values`() {
        // Fixed 2-decimal rounding would flatten this to "0".
        assertEquals(
            "0.001235",
            NumberFormatter.formatSignificant(0.00123456, significantDigits = 4, symbols = us),
        )
    }

    @Test
    fun `formatSignificant rounds large values above the decimal point`() {
        assertEquals(
            "123,500",
            NumberFormatter.formatSignificant(123456.0, significantDigits = 4, symbols = us),
        )
    }

    @Test
    fun `formatSignificant renders zero without decimals`() {
        assertEquals("0", NumberFormatter.formatSignificant(0.0, symbols = us))
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
        assertEquals("1.5 kW", NumberFormatter.formatWithSiPrefix(1500.0, "W", symbols = us))
        assertEquals("2.2 MW", NumberFormatter.formatWithSiPrefix(2_200_000.0, "W", symbols = us))
        assertEquals("470 mA", NumberFormatter.formatWithSiPrefix(0.47, "A", symbols = us))
    }

    @Test
    fun `formatWithSiPrefix leaves values in base range unprefixed`() {
        assertEquals("230 V", NumberFormatter.formatWithSiPrefix(230.0, "V", symbols = us))
    }

    @Test
    fun `formatWithSiPrefix keeps the sign of negative values`() {
        assertEquals("-1.5 kW", NumberFormatter.formatWithSiPrefix(-1500.0, "W", symbols = us))
    }

    // -- halfway cases -----------------------------------------------------

    /**
     * These are the reason `DecimalDigits` expands doubles exactly instead of
     * reading `Double.toString`.
     *
     * All four look like a `…5` that half-up should carry. Two of them do not,
     * because the double is not really sitting on the halfway line — `2.345` is
     * `2.34500000000000019…` and rounds up, while `735.49875` is
     * `735.49874999999997…` and rounds down. `0.125` is exactly representable,
     * so it is a real tie and half-up carries it away from zero.
     *
     * An implementation that rounds the shortest representation gets two of
     * these wrong, and would have shifted the last digit of results throughout
     * the app.
     */
    @Test
    fun `rounding follows the exact value rather than the printed one`() {
        assertEquals("2.35", NumberFormatter.format(2.345, decimals = 2, symbols = us))
        assertEquals("735.4987", NumberFormatter.format(735.49875, decimals = 4, symbols = us))
        assertEquals("1", NumberFormatter.format(1.005, decimals = 2, symbols = us))
        assertEquals("0.13", NumberFormatter.format(0.125, decimals = 2, symbols = us))
    }

    @Test
    fun `rounding carries across every digit`() {
        assertEquals("1", NumberFormatter.format(0.9999, decimals = 2, symbols = us))
        assertEquals("10", NumberFormatter.format(9.999, decimals = 2, symbols = us))
        assertEquals("1,000", NumberFormatter.format(999.999, decimals = 2, symbols = us))
    }
}
