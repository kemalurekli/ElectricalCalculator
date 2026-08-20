package com.kemalurekli.electricalcalculator.core.common.util

import java.text.BreakIterator
import java.text.Collator
import java.util.Locale

/**
 * `Collator` is the JDK's implementation of the Unicode collation algorithm,
 * which is the same specification iOS follows — so the two platforms agree on
 * the answer without either being told what the answer is.
 */
actual fun localizedComparator(): Comparator<String> = Collator.getInstance(Locale.getDefault())
    .let { collator -> Comparator { left, right -> collator.compare(left, right) } }

actual fun String.firstCharacter(): String {
    if (isEmpty()) return ""
    val characters = BreakIterator.getCharacterInstance(Locale.getDefault())
    characters.setText(this)
    return substring(0, characters.next())
}

actual fun String.uppercaseLocalized(): String = uppercase(Locale.getDefault())
