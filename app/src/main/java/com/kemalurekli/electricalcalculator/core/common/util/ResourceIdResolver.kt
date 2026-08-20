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
 * [StringResolver] plus the integer resource ids that have not moved yet.
 *
 * An `R.string` id is an Android build artefact and cannot exist in shared
 * code, so a screen still holding one cannot leave `:app`. That makes the
 * number of ViewModels injecting this type a running count of the migration
 * that is left, and the type disappears when the count reaches zero.
 *
 * Screens that have moved inject [StringResolver] instead and carry
 * `StringResource` handles, which are the same idea without the platform.
 */
interface ResourceIdResolver : StringResolver {
    fun get(@StringRes id: Int): String
}

@Singleton
class AndroidStringResolver @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ResourceIdResolver {

    /**
     * Reads through to the application context on every call rather than
     * caching, so text follows the active locale after an in-app language
     * change without the ViewModel having to be recreated.
     */
    override fun get(@StringRes id: Int): String = context.getString(id)

    /**
     * Blocking, because [StringResolver] is synchronous and the Compose
     * Resources accessor is not. The call is a lookup in an already-loaded
     * table rather than I/O, and the callers — building a search index, or
     * sorting a glossary — are off the main thread.
     */
    override fun get(resource: StringResource): String = runBlocking { getString(resource) }

    override fun get(resource: StringResource, vararg args: Any): String =
        runBlocking { getString(resource, *args) }
}
