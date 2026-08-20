package com.kemalurekli.electricalcalculator.core.common.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The claims that hold whatever language the machine running the test is in.
 *
 * Locale-dependent behaviour is deliberately not asserted here. `Locale.getDefault()`
 * on a build machine and `NSLocale.currentLocale` on a simulator are two
 * different settings that nobody sets deliberately, so a test that needed
 * Turkish would pass or fail on where it ran rather than on whether the code
 * is right.
 *
 * What is left is still the whole point of the file: collation ordering differs
 * from code-point ordering in every locale, and grapheme boundaries differ from
 * UTF-16 boundaries in all of them too.
 */
class TextCollationTest {

    @Test
    fun `accented letters sort with their base letter rather than after Z`() {
        // The code-point answer, and the wrong one: Ç is U+00C7, past every
        // unaccented capital, so a naive sort files Çalışma after Zaman.
        assertTrue("Çalışma" > "Zaman")

        assertTrue(localizedComparator().compare("Çalışma", "Zaman") < 0)
    }

    @Test
    fun `collation keeps the plain alphabet in order`() {
        val comparator = localizedComparator()
        assertTrue(comparator.compare("akım", "gerilim") < 0)
        assertTrue(comparator.compare("gerilim", "akım") > 0)
        assertEquals(0, comparator.compare("direnç", "direnç"))
    }

    @Test
    fun `the first character is a character the reader would point at`() {
        assertEquals("G", "Gerilim".firstCharacter())

        // A combining cedilla: two code points, one letter. `take(1)` returns
        // the C and leaves the cedilla behind to attach to whatever follows.
        assertEquals("Ç", "Çalısma".firstCharacter())

        // Outside the basic plane, so two UTF-16 units. `take(1)` returns half
        // a surrogate pair, which is not a character at all.
        assertEquals("🔌", "🔌fiş".firstCharacter())
    }

    @Test
    fun `an empty string has no first character`() {
        assertEquals("", "".firstCharacter())
    }
}
