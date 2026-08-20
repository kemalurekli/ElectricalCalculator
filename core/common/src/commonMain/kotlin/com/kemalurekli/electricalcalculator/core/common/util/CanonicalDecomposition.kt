package com.kemalurekli.electricalcalculator.core.common.util

/**
 * Splits precomposed characters into a base letter and its combining marks —
 * Unicode's NFD form.
 *
 * `ş` arrives from a keyboard as one character (U+015F) but is also expressible
 * as `s` followed by a combining cedilla. [SearchNormalizer] folds accents by
 * stripping the marks, which only works once the accents *are* marks.
 *
 * The one platform capability search folding needs. Everything else in
 * [SearchNormalizer] — lowercasing, the combining-mark regex, the explicit
 * folding table — turned out to work identically in common code.
 */
expect fun String.decomposeCanonically(): String
