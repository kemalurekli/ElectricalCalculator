package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.features.forum.presentation.ComposerHint
import com.kemalurekli.electricalcalculator.features.forum.presentation.composerCounter
import com.kemalurekli.electricalcalculator.features.forum.presentation.composerHint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * What the new-thread page will and will not let out the door.
 *
 * The rules are the schema's: a title of at least five characters and a body of
 * at least two, checked again in Postgres. Breaking one of them used to reach
 * the server and come back as a generic "the thread could not be opened", so
 * the page has to be the one that says no — and has to say which rule.
 */
class ForumComposerRulesTest {

    @Test
    fun `an empty page asks for the title first`() {
        assertEquals(ComposerHint.TITLE_TOO_SHORT, composerHint("", ""))
    }

    @Test
    fun `a title of four characters is still short`() {
        assertEquals(ComposerHint.TITLE_TOO_SHORT, composerHint("Faz", "Panoda"))
    }

    @Test
    fun `whitespace does not count towards the title`() {
        // The server trims before it checks, so five spaces and a letter is a
        // one-character title there and has to be one here too.
        assertEquals(ComposerHint.TITLE_TOO_SHORT, composerHint("  ab   ", "Panoda"))
    }

    @Test
    fun `a title on its own is not a question`() {
        assertEquals(ComposerHint.NEEDS_BODY, composerHint("Salter isiniyor", ""))
    }

    @Test
    fun `a title and a body are enough`() {
        assertEquals(ComposerHint.NONE, composerHint("Salter isiniyor", "16A C tipi"))
    }

    @Test
    fun `a count is not shown while both fields have room`() {
        assertNull(composerCounter("Salter isiniyor", "16A C tipi salter"))
    }

    @Test
    fun `the title count appears within twenty characters of the limit`() {
        // Twenty left is within twenty; twenty-one is not.
        assertNull(composerCounter("t".repeat(119), ""))
        assertEquals("120 / 140", composerCounter("t".repeat(120), ""))
    }

    @Test
    fun `the body count appears within two hundred characters of the limit`() {
        assertNull(composerCounter("Salter", "b".repeat(7799)))
        assertEquals("7800 / 8000", composerCounter("Salter", "b".repeat(7800)))
    }

    @Test
    fun `a title near its limit is reported before a body that is not`() {
        // Both can be near at once. The title is the one the writer is being
        // stopped on, since it fills a hundred and forty characters sooner.
        assertEquals("140 / 140", composerCounter("t".repeat(140), "b".repeat(7900)))
    }
}
