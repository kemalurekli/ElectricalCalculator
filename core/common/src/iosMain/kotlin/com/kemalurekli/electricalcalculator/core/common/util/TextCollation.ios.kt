package com.kemalurekli.electricalcalculator.core.common.util

import platform.Foundation.languageCode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSLocale
import platform.Foundation.NSMakeRange
import platform.Foundation.NSOrderedAscending
import platform.Foundation.NSOrderedDescending
import platform.Foundation.NSString
import platform.Foundation.compare
import platform.Foundation.currentLocale
import platform.Foundation.rangeOfComposedCharacterSequenceAtIndex
import platform.Foundation.uppercaseStringWithLocale

/**
 * `compare(options:range:locale:)` with a locale is Foundation's collator —
 * the same Unicode collation algorithm the JDK's `Collator` implements, which
 * is why the two platforms file *Çalışma* in the same place without either
 * being handed a list of rules.
 *
 * The locale is read once and captured, which is what makes this a comparator
 * and not a compare function; see the expect declaration for why it must not
 * then be held in a field.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun localizedComparator(): Comparator<String> {
    val locale = NSLocale.currentLocale
    return Comparator { left, right ->
        val subject = left as NSString
        when (
            subject.compare(
                string = right,
                options = 0u,
                range = NSMakeRange(0u, subject.length),
                locale = locale,
            )
        ) {
            NSOrderedAscending -> -1
            NSOrderedDescending -> 1
            else -> 0
        }
    }
}

/**
 * Foundation calls a user-perceived character a *composed character sequence*,
 * and will give back the range of the one containing a given index. That range
 * starting at 0 is exactly what a break iterator's first boundary is.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun String.firstCharacter(): String {
    if (isEmpty()) return ""
    val range = (this as NSString).rangeOfComposedCharacterSequenceAtIndex(0u)
    return substring(0, range.useContents { length.toInt() })
}

actual fun String.uppercaseLocalized(): String =
    (this as NSString).uppercaseStringWithLocale(NSLocale.currentLocale)

actual fun currentLanguageTag(): String = NSLocale.currentLocale.languageCode
