package com.kemalurekli.electricalcalculator.core.common.util

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
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
}
