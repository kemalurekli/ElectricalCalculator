package com.kemalurekli.electricalcalculator.core.common.util

import org.jetbrains.compose.resources.StringResource

/**
 * Resolves string resources outside of composition.
 *
 * ViewModels need resolved text to search and sort by it — you cannot rank a
 * glossary by a resource handle — but must not hold a platform context. This
 * narrow seam gives them the strings while keeping them testable: a unit test
 * supplies a map-backed fake instead of a resource table.
 */
interface StringResolver {
    fun get(resource: StringResource): String
}
