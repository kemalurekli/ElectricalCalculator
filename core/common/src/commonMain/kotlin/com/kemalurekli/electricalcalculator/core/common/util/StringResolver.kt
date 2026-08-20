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

    /**
     * The same, with [args] substituted into the template.
     *
     * A separate method rather than `get(resource).format(args)`, because
     * `String.format` is a JVM method and does not exist in shared code. Both
     * platforms delegate to Compose Resources, which does the substitution
     * itself — and does it with the argument order the translation declares,
     * so a Turkish string that swaps `%1$s` and `%2$s` keeps working.
     */
    fun get(resource: StringResource, vararg args: Any): String
}
