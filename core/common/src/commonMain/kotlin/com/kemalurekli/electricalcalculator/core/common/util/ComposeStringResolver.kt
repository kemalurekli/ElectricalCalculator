package com.kemalurekli.electricalcalculator.core.common.util

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Reads strings straight from the Compose Resources table.
 *
 * Android has a second implementation, `AndroidStringResolver`, and will keep
 * it until the last `R.string` id is gone — it does the same thing for
 * `StringResource` and adds the integer overload beside it. This one is what
 * iOS uses, and what both platforms use once that overload disappears.
 *
 * Blocking, because [StringResolver] is synchronous and the resource accessor
 * is not. The call is a lookup in an already-loaded table rather than I/O, and
 * the callers — building a search index, sorting a glossary — are off the main
 * thread.
 */
class ComposeStringResolver : StringResolver {
    override fun get(resource: StringResource): String = runBlocking { getString(resource) }

    override fun get(resource: StringResource, vararg args: Any): String =
        runBlocking { getString(resource, *args) }
}
