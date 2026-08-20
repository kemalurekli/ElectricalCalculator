package com.kemalurekli.electricalcalculator.core.common.util

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves string resources outside of composition.
 *
 * ViewModels need resolved text to search and sort by it, but must not hold a
 * `Context`. This narrow seam gives them the strings while keeping them
 * testable — unit tests supply a map-backed fake instead of an Android context.
 */
interface StringResolver {
    fun get(@StringRes id: Int): String

    /**
     * The same, for text that has moved to a multiplatform module.
     *
     * Compose Resources replaces the integer id with a [StringResource], so a
     * screen index that mixes `:app`'s strings with a feature module's needs
     * both. This overload disappears when the last feature has moved and there
     * are no integer ids left.
     */
    fun get(resource: StringResource): String
}

@Singleton
class AndroidStringResolver @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : StringResolver {

    /**
     * Reads through to the application context on every call rather than
     * caching, so text follows the active locale after an in-app language
     * change without the ViewModel having to be recreated.
     */
    override fun get(@StringRes id: Int): String = context.getString(id)

    /**
     * Blocking, because [StringResolver] is synchronous and the Compose
     * Resources accessor is not. The call is a lookup in an already-loaded
     * table rather than I/O, and the one caller — building the search index —
     * is off the main thread.
     */
    override fun get(resource: StringResource): String = runBlocking { getString(resource) }
}
