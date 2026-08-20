package com.kemalurekli.electricalcalculator.core.common.util

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

/**
 * The folding that lets a user find a term without reaching for the diacritics.
 *
 * Turkish carries most of these cases, and they are not cosmetic: `ı` and `i`
 * are different letters, so without folding a search for "kisa" returns nothing
 * for "Kısa Devre" — the calculator is there and the user is told it is not.
 */
class SearchNormalizerTest {

        
    // -- Turkish ---------------------------------------------------------------------

    @Test
    fun `a dotless i folds to i`() {
        // The case that started this: "Kısa Devre Akımı" typed as "kisa".
        assertEquals(SearchNormalizer.normalise("Kısa Devre Akımı"), "kisa devre akimi")
    }

    @Test
    fun `an undotted capital I still folds to i under Turkish rules`() {
        // "I".lowercase(tr) is "ı", so without the atomic folding a user who
        // capitalises would search for "ıletken" and miss "İletken".
        assertEquals(
            SearchNormalizer.normalise("İletken"),
            SearchNormalizer.normalise("Iletken"),
        )
    }

    @Test
    fun `every Turkish diacritic folds to its base letter`() {
        assertEquals(
            SearchNormalizer.normalise("ÇĞİÖŞÜ"),
            "cgiosu",
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
                SearchNormalizer.contains(term, typed),
                "$typed does not find $term",
            )
        }
    }

    // -- The languages on the roadmap -------------------------------------------------

    @Test
    fun `accents fold across the Latin languages the app will ship in`() {
        assertEquals(SearchNormalizer.normalise("Resistência"), "resistencia")
        assertEquals(SearchNormalizer.normalise("Câble"), "cable")
        assertEquals(SearchNormalizer.normalise("Überlast"), "uberlast")
        assertEquals(SearchNormalizer.normalise("Sección"), "seccion")
    }

    @Test
    fun `letters with no combining mark are folded explicitly`() {
        // NFD leaves these alone because they are atomic, not accented, so the
        // mapping is the only thing that catches them.
        assertEquals(SearchNormalizer.normalise("Straße"), "strasse")
        assertEquals(SearchNormalizer.normalise("ı"), "i")
    }

    // -- Cyrillic ----------------------------------------------------------------------

    @Test
    fun `Cyrillic marks fold so a term is found however it was typed`() {
        // ё is written as е far more often than not, and й is reached for

        assertEquals(
            SearchNormalizer.normalise("заземление"),
            SearchNormalizer.normalise("зазёмлёние"),
        )
        assertEquals(
            SearchNormalizer.normalise("короткии"),
            SearchNormalizer.normalise("короткий"),
        )
    }

    @Test
    fun `Cyrillic is otherwise left alone`() {

        assertEquals(SearchNormalizer.normalise("Напряжение"), "напряжение")
        assertEquals(SearchNormalizer.normalise("Электрод"), "электрод")
    }

    // -- Arabic ------------------------------------------------------------------------

    @Test
    fun `Arabic short vowels are removed`() {
        // The harakat are combining marks, usually omitted when typing and

        assertEquals(SearchNormalizer.normalise("جَهْد"), "جهد")
    }

    @Test
    fun `the hamza-carrying alefs fold to a bare alef`() {
        // NFD already decomposes these, which is why they need no mapping.
        val bare = SearchNormalizer.normalise("ا")

        listOf("أ", "إ", "آ").forEach { form ->
            assertEquals(bare, SearchNormalizer.normalise(form), "$form did not fold")
        }
    }

    @Test
    fun `teh marbuta and alef maksura fold to the letters typed in their place`() {
        // Atomic characters, so only the explicit mapping catches them. Getting

        assertEquals(
            SearchNormalizer.normalise("مقاومه"),
            SearchNormalizer.normalise("مقاومة"),
        )
        assertEquals(
            SearchNormalizer.normalise("علي"),
            SearchNormalizer.normalise("على"),
        )
    }

    @Test
    fun `the kashida never affects whether a word matches`() {

        assertEquals(
            SearchNormalizer.normalise("تيار"),
            SearchNormalizer.normalise("تــيــار"),
        )
    }

    @Test
    fun `Arabic-Indic digits find a code written in Latin ones`() {
        // An Arabic or Persian keyboard produces these, and "IP54" is looked up

        assertEquals(SearchNormalizer.normalise("IP٥٤"), "ip54")
        assertEquals(SearchNormalizer.normalise("IP۵۴"), "ip54")
        assertTrue(SearchNormalizer.contains("IP54", "٥٤"))
    }

    // -- Behaviour --------------------------------------------------------------------

    @Test
    fun `folding is idempotent`() {
        val once = SearchNormalizer.normalise("Kısa Devre Akımı")

        assertEquals(once, SearchNormalizer.normalise(once))
    }

    @Test
    fun `surrounding space is dropped`() {
        assertEquals(SearchNormalizer.normalise("  Motor  "), "motor")
    }

    @Test
    fun `unrelated words still do not match`() {
        // Folding widens what matches; it must not make everything match.
        assertFalse(SearchNormalizer.contains("Kısa Devre", "transformator"))
    }

    @Test
    fun `digits and symbols survive`() {
        // "IP54", "cos φ" and "1.5 mm²" are searched for as written.
        assertEquals(SearchNormalizer.normalise("IP54"), "ip54")
        assertEquals(SearchNormalizer.normalise("1.5 mm²"), "1.5 mm²")
    }

    /**
     * The reason [SearchNormalizer.normalise] no longer takes a locale.
     *
     * It used to lowercase in the reader's locale so that Turkish `I`/`İ`
     * followed Turkish rules. Kotlin's common `lowercase()` is locale-invariant
     * and cannot do that — but it turns out not to matter, because the rest of
     * the pipeline converges the two paths anyway:
     *
     * | input | Turkish rules | invariant rules |
     * |---|---|---|
     * | `I` | `ı`, then folded to `i` | `i` |
     * | `İ` | `i` | `i` + combining dot, then stripped to `i` |
     *
     * If that ever stops being true — a folding removed, the mark-stripping
     * narrowed — this fails, and the locale parameter has to come back as an
     * expect/actual rather than being quietly missed.
     */
    @Test
    fun `folding is the same with or without Turkish rules`() {
        val invariantUpperI = SearchNormalizer.normalise("I")
        val invariantDottedI = SearchNormalizer.normalise("İ")

        // What lowercasing under Turkish rules would have produced, folded the
        // rest of the way by the same pipeline.
        val turkishUpperI = SearchNormalizer.normalise("\u0131")
        val turkishDottedI = SearchNormalizer.normalise("i")

        assertEquals(turkishUpperI, invariantUpperI, "I folds differently")
        assertEquals(turkishDottedI, invariantDottedI, "İ folds differently")
        assertEquals("i", invariantUpperI)
        assertEquals("i", invariantDottedI)
    }

    /** Precomposed and decomposed spellings of the same word must fold alike. */
    @Test
    fun `precomposed and decomposed accents fold to the same text`() {
        val precomposed = "\u015f\u011f\u00fc"
        val decomposed = "s\u0327g\u0306u\u0308"

        assertEquals(
            SearchNormalizer.normalise(precomposed),
            SearchNormalizer.normalise(decomposed),
        )
    }
}
