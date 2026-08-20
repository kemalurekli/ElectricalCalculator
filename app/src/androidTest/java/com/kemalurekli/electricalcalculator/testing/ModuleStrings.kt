package com.kemalurekli.electricalcalculator.testing

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Reads a string from whichever module owns it.
 *
 * These used to come from `context.getString(R.string.x)`. They moved with the
 * components that use them when `:core:designsystem` became its own
 * multiplatform module, and Compose Resources generates a separate `Res` per
 * module — so `:app`'s `R` no longer knows about them.
 *
 * Resolving through the design system's own accessor is also the more honest
 * assertion: the test now reads the exact string the component rendered, rather
 * than a copy that could drift.
 *
 * `runBlocking` because the accessor is suspending and these are called from
 * ordinary JUnit setup. The lookup is a table read, not I/O.
 */
fun moduleString(resource: StringResource): String = runBlocking { getString(resource) }

/** The same, with arguments substituted the way the screen substitutes them. */
fun moduleString(resource: StringResource, vararg args: Any): String =
    runBlocking { getString(resource, *args) }
