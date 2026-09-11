package com.kemalurekli.electricalcalculator.features.theory.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Where an advanced topic stops for a reader who has not paid.
 *
 * The arithmetic is worth a test because both ways of getting it wrong are
 * silent: a split that gives away everything sells nothing, and one that gives
 * away nothing is a bounce the reader reads as a bait screen.
 */
class TheoryProGateTest {

    private fun prose(vararg paragraphs: String) = paragraphs.joinToString("\n\n")

    @Test
    fun `the free half ends on a finished paragraph`() {
        val (free, rest) = splitForGate(prose("bir", "iki", "uc", "dort"))

        assertTrue(free.isNotEmpty())
        assertTrue(rest.isNotEmpty())
        assertEquals(prose("bir", "iki", "uc", "dort"), prose(free, rest))
    }

    @Test
    fun `a long opening paragraph still leaves something free`() {
        val (free, rest) = splitForGate(prose("a".repeat(900), "kisa", "kisa"))

        // The first paragraph is already past halfway. It is given away anyway,
        // because a gate before the reader's first sentence is a bounce.
        assertEquals("a".repeat(900), free)
        assertEquals(prose("kisa", "kisa"), rest)
    }

    @Test
    fun `short paragraphs split near the middle rather than at the first one`() {
        val (free, rest) = splitForGate(prose("bir", "iki", "uc", "dort", "bes", "alti"))

        assertTrue(free.split("\n\n").size > 1, "one paragraph out of six is not half")
        assertTrue(rest.split("\n\n").size > 1, "five out of six is not half either")
    }

    @Test
    fun `a single paragraph has nothing to withhold`() {
        val (free, rest) = splitForGate("tek paragraf")

        assertEquals("tek paragraf", free)
        assertEquals("", rest)
    }

    @Test
    fun `the split never loses or repeats a paragraph`() {
        val paragraphs = List(9) { "paragraf $it" }
        val (free, rest) = splitForGate(paragraphs.joinToString("\n\n"))

        val recovered = (free.split("\n\n") + rest.split("\n\n")).filter { it.isNotBlank() }
        assertEquals(paragraphs, recovered)
    }
}
