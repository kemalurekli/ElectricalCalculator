package com.kemalurekli.electricalcalculator.core.common.util


/**
 * Folds text down to what a search should actually compare.
 *
 * ### Why lowercasing is not enough
 *
 * A user hunting for a term types it the fast way: no diacritics, whatever
 * keyboard is in front of them. In Turkish that is not a cosmetic difference —
 * it changes letters. "Kısa Devre" lowercases to `kısa devre`, and someone
 * typing `kisa` gets nothing at all, because `ı` (U+0131 DOTLESS I) and `i` are
 * separate characters, not a case pair.
 *
 * Turkish locale rules make it worse in both directions: `"I".lowercase(tr)` is
 * `ı`, so a user who capitalises the first letter of `Iletken` searches for
 * `ıletken` and misses `İletken`. Folding both to `i` is what makes the two
 * spellings meet.
 *
 * ### How
 *
 * Lowercase, then decompose to NFD and drop the combining marks.
 *
 * The lowercasing is locale-*independent*, which took some checking. Turkish
 * rules would map `I → ı` and `İ → i`; the invariant rules map `I → i` and
 * `İ → i̇`. Those differ — but not after the rest of the pipeline runs, because
 * [ATOMIC_FOLDINGS] already folds `ı → i` and the mark-stripping already
 * removes the combining dot. Both paths land on `i`, which is what
 * `foldingIsTheSameWithOrWithoutTurkishRules` pins.
 *
 * So the locale parameter this used to take bought nothing, and dropping it
 * removed a platform capability the shared code would otherwise have needed. That handles
 * every accent that is a base letter plus a mark: `ş → s`, `ğ → g`, `ö → o`,
 * `ü → u`, `ç → c`, and the same across French, German, Spanish and Portuguese,
 * which this app will ship in. In Cyrillic it folds `ё → е` and `й → и`, and in
 * Arabic it removes the harakat.
 *
 * Decomposition alone cannot fold letters that are atomic rather than accented,
 * so those are listed explicitly in [ATOMIC_FOLDINGS], alongside the kashida
 * and the Arabic-Indic digits.
 *
 * ### The direction of the compromise
 *
 * Every rule here *widens* what matches. Folding `ı → i` also means a search
 * for `kisa` finds a term spelt `kisa`, and folding `й → и` merges two letters
 * Russian considers distinct. That is the right way to be wrong for a search
 * box: an extra result is a glance, a missing one is a user concluding the term
 * does not exist. It would be the wrong trade for sorting, which is why
 * ordering does not come through here.
 *
 * ### What this deliberately does not do
 *
 * It is not a collator. Ordering a list is a different question with a
 * different right answer — Turkish sorts `ç` after `c` rather than treating
 * them as equal — so display order uses [java.text.Collator] and only *matching*
 * comes through here.
 */
object SearchNormalizer {

    /**
     * Letters that carry no combining mark to strip, so NFD leaves them alone.
     *
     * Each maps to the letter a user is likely to type in its place.
     *
     * The Latin entries cover the languages on the roadmap. The Arabic ones are
     * not optional decoration: ة and ه, and ى and ي, are routinely typed for one
     * another, and a search that treats them as distinct fails on ordinary
     * spellings rather than on unusual ones. The hamza-carrying alefs — أ إ آ —
     * need no entry because NFD already decomposes them to bare ا.
     */
    private val ATOMIC_FOLDINGS = mapOf(
        // Latin
        'ı' to "i",
        'ø' to "o",
        'đ' to "d",
        'ð' to "d",
        'ł' to "l",
        'ß' to "ss",
        'æ' to "ae",
        'œ' to "oe",
        'þ' to "th",
        // Arabic
        'ة' to "ه",
        'ى' to "ي",
        'ؤ' to "و",
        'ئ' to "ي",
        'ء' to "",
    )

    /**
     * Digits that are the same number written in another script.
     *
     * An Arabic or Persian keyboard produces these, and a code like `IP54` is
     * looked up by whichever digits are under the user's fingers.
     */
    private val DIGIT_FOLDINGS: Map<Char, Char> = buildMap {
        // Arabic-Indic ٠–٩ and the Extended (Persian) forms ۰–۹.
        for (offset in 0..9) {
            put('٠' + offset, '0' + offset)
            put('۰' + offset, '0' + offset)
        }
    }

    /**
     * Unicode combining marks, which is what NFD separates an accent into.
     *
     * In Arabic this also removes the harakat — the short-vowel marks — which
     * are usually omitted when typing and always omitted when searching.
     */
    private val COMBINING_MARKS = Regex("\\p{Mn}+")

    /**
     * The kashida, a decorative stretch inserted to justify Arabic text.
     *
     * It carries no meaning and must never affect whether a word matches.
     */
    private const val TATWEEL = 'ـ'

    /**
     * Returns [text] reduced to its searchable form: lowercase, unaccented,
     * and trimmed.
     */
    fun normalise(text: String): String {
        val stripped = COMBINING_MARKS.replace(text.lowercase().decomposeCanonically(), "")

        return buildString(stripped.length) {
            stripped.forEach { char ->
                when {
                    char == TATWEEL -> Unit
                    ATOMIC_FOLDINGS.containsKey(char) -> append(ATOMIC_FOLDINGS.getValue(char))
                    DIGIT_FOLDINGS.containsKey(char) -> append(DIGIT_FOLDINGS.getValue(char))
                    else -> append(char)
                }
            }
        }.trim()
    }

    /** True when [haystack] contains [needle] once both are folded. */
    fun contains(haystack: String, needle: String): Boolean =
        normalise(haystack).contains(normalise(needle))
}
