package com.kemalurekli.electricalcalculator.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * The folding that lets a user find a term without reaching for the diacritics.
 *
 * Turkish carries most of these cases, and they are not cosmetic: `ı` and `i`
 * are different letters, so without folding a search for "kisa" returns nothing
 * for "Kısa Devre" — the calculator is there and the user is told it is not.
 */
class SearchNormalizerTest {

    private val turkey = Locale.forLanguageTag("tr-TR")
    private val us = Locale.US

    // -- Turkish ---------------------------------------------------------------------

    @Test
    fun `a dotless i folds to i`() {
        // The case that started this: "Kısa Devre Akımı" typed as "kisa".
        assertEquals("kisa devre akimi", SearchNormalizer.normalise("Kısa Devre Akımı", turkey))
    }

    @Test
    fun `an undotted capital I still folds to i under Turkish rules`() {
        // "I".lowercase(tr) is "ı", so without the atomic folding a user who
        // capitalises would search for "ıletken" and miss "İletken".
        assertEquals(
            SearchNormalizer.normalise("İletken", turkey),
            SearchNormalizer.normalise("Iletken", turkey),
        )
    }

    @Test
    fun `every Turkish diacritic folds to its base letter`() {
        assertEquals(
            "cgiosu",
            SearchNormalizer.normalise("ÇĞİÖŞÜ", turkey),
        )
    }

    @Test
    fun `Turkish terms are found by their bare spelling`() {
        listOf(
            "Kısa Devre" to "kisa",
            "Gerilim Düşümü" to "dusum",
            "Güç Faktörü" to "guc",
            "Yalıtım" to "yalitim",
            "Şebeke" to "sebeke",
            "Çalışma" to "calisma",
        ).forEach { (term, typed) ->
            assertTrue(
                "$typed does not find $term",
                SearchNormalizer.contains(term, typed, turkey),
            )
        }
    }

    // -- The languages on the roadmap -------------------------------------------------

    @Test
    fun `accents fold across the Latin languages the app will ship in`() {
        assertEquals("resistencia", SearchNormalizer.normalise("Resistência", us))
        assertEquals("cable", SearchNormalizer.normalise("Câble", us))
        assertEquals("uberlast", SearchNormalizer.normalise("Überlast", us))
        assertEquals("seccion", SearchNormalizer.normalise("Sección", us))
    }

    @Test
    fun `letters with no combining mark are folded explicitly`() {
        // NFD leaves these alone because they are atomic, not accented, so the
        // mapping is the only thing that catches them.
        assertEquals("strasse", SearchNormalizer.normalise("Straße", us))
        assertEquals("i", SearchNormalizer.normalise("ı", us))
    }

    // -- Cyrillic ----------------------------------------------------------------------

    @Test
    fun `Cyrillic marks fold so a term is found however it was typed`() {
        // ё is written as е far more often than not, and й is reached for
        // interchangeably with и on a hurried keyboard.
        val russia = Locale.forLanguageTag("ru-RU")

        assertEquals(
            SearchNormalizer.normalise("заземление", russia),
            SearchNormalizer.normalise("зазёмлёние", russia),
        )
        assertEquals(
            SearchNormalizer.normalise("короткии", russia),
            SearchNormalizer.normalise("короткий", russia),
        )
    }

    @Test
    fun `Cyrillic is otherwise left alone`() {
        // Folding must not quietly mangle a script it has no business in.
        val russia = Locale.forLanguageTag("ru-RU")

        assertEquals("напряжение", SearchNormalizer.normalise("Напряжение", russia))
        assertEquals("электрод", SearchNormalizer.normalise("Электрод", russia))
    }

    // -- Arabic ------------------------------------------------------------------------

    @Test
    fun `Arabic short vowels are removed`() {
        // The harakat are combining marks, usually omitted when typing and
        // always omitted when searching.
        val arabic = Locale.forLanguageTag("ar")

        assertEquals("جهد", SearchNormalizer.normalise("جَهْد", arabic))
    }

    @Test
    fun `the hamza-carrying alefs fold to a bare alef`() {
        // NFD already decomposes these, which is why they need no mapping.
        val arabic = Locale.forLanguageTag("ar")
        val bare = SearchNormalizer.normalise("ا", arabic)

        listOf("أ", "إ", "آ").forEach { form ->
            assertEquals("$form did not fold", bare, SearchNormalizer.normalise(form, arabic))
        }
    }

    @Test
    fun `teh marbuta and alef maksura fold to the letters typed in their place`() {
        // Atomic characters, so only the explicit mapping catches them. Getting
        // this wrong fails on ordinary spellings, not on unusual ones.
        val arabic = Locale.forLanguageTag("ar")

        assertEquals(
            SearchNormalizer.normalise("مقاومه", arabic),
            SearchNormalizer.normalise("مقاومة", arabic),
        )
        assertEquals(
            SearchNormalizer.normalise("علي", arabic),
            SearchNormalizer.normalise("على", arabic),
        )
    }

    @Test
    fun `the kashida never affects whether a word matches`() {
        // A decorative stretch inserted to justify text; it carries no meaning.
        val arabic = Locale.forLanguageTag("ar")

        assertEquals(
            SearchNormalizer.normalise("تيار", arabic),
            SearchNormalizer.normalise("تــيــار", arabic),
        )
    }

    @Test
    fun `Arabic-Indic digits find a code written in Latin ones`() {
        // An Arabic or Persian keyboard produces these, and "IP54" is looked up
        // with whichever digits are under the user's fingers.
        val arabic = Locale.forLanguageTag("ar")

        assertEquals("ip54", SearchNormalizer.normalise("IP٥٤", arabic))
        assertEquals("ip54", SearchNormalizer.normalise("IP۵۴", arabic))
        assertTrue(SearchNormalizer.contains("IP54", "٥٤", arabic))
    }

    // -- Behaviour --------------------------------------------------------------------

    @Test
    fun `folding is idempotent`() {
        val once = SearchNormalizer.normalise("Kısa Devre Akımı", turkey)

        assertEquals(once, SearchNormalizer.normalise(once, turkey))
    }

    @Test
    fun `surrounding space is dropped`() {
        assertEquals("motor", SearchNormalizer.normalise("  Motor  ", us))
    }

    @Test
    fun `unrelated words still do not match`() {
        // Folding widens what matches; it must not make everything match.
        assertFalse(SearchNormalizer.contains("Kısa Devre", "transformator", turkey))
    }

    @Test
    fun `digits and symbols survive`() {
        // "IP54", "cos φ" and "1.5 mm²" are searched for as written.
        assertEquals("ip54", SearchNormalizer.normalise("IP54", us))
        assertEquals("1.5 mm²", SearchNormalizer.normalise("1.5 mm²", us))
    }
}
