package com.kemalurekli.electricalcalculator.core.common.util

/**
 * Ordering and segmenting text the way the reader's language does it.
 *
 * The three operations here look like string manipulation and are not. Each one
 * has an answer that depends on the language, and each has a plausible-looking
 * shortcut that is wrong in Turkish — which is half this app's audience.
 *
 * Unlike [formatAsDateTime], none of this is a place where the two platforms
 * are allowed to differ. A glossary that files *Çalışma* under Ç on Android and
 * under C on iOS is not localised, it is inconsistent.
 */

/**
 * Compares strings in the reader's alphabet rather than by code point.
 *
 * `String.compareTo` sorts by UTF-16 value, which puts every accented letter
 * after Z: *Çalışma* lands past *Zaman* instead of between *Cihaz* and *Direnç*
 * where a Turkish reader looks for it.
 *
 * Returned as a comparator rather than a compare function because building the
 * underlying collation table is expensive — once per sort, not once per
 * comparison. It reads the current language when it is built, so build it at
 * the point of sorting and do not hold onto it; a comparator kept in a field
 * goes on sorting in yesterday's language after an in-app language change.
 */
expect fun localizedComparator(): Comparator<String>

/**
 * The first character of [this] as a reader sees one.
 *
 * Not `take(1)`, which returns one UTF-16 unit — a letter only by coincidence.
 * It splits a surrogate pair down the middle, and it severs a base letter from
 * a combining mark, so a decomposed *Ç* is filed under *C* with a stray cedilla
 * left over.
 *
 * Returns an empty string for an empty receiver.
 */
expect fun String.firstCharacter(): String

/**
 * Upper-cases [this] in the reader's language.
 *
 * Turkish is the reason this is not `uppercase()`: the dotless *ı* upper-cases
 * to *I* and the dotted *i* to *İ*, and the invariant mapping gets the second
 * one wrong. A glossary heading is the visible consequence — *İletken* filed
 * under *I* rather than *İ*, two headings apart.
 */
expect fun String.uppercaseLocalized(): String

/**
 * The language the device is set to, as a bare subtag: `tr`, `en`.
 *
 * Distinct from [RegionProvider], which answers *where* rather than *in what
 * language*, and used only where the app has no language of its own to fall
 * back on — the forum picks a board from the app's setting first and reads this
 * only when that setting is "follow the system".
 */
expect fun currentLanguageTag(): String
