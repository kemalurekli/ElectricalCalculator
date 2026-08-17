package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.runtime.Immutable
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumFailure

/**
 * What a forum screen is showing.
 *
 * Three states and no fourth. "Loaded but empty" is [Content] holding an empty
 * list, because an empty category and a failed request are different things
 * and the reader has to be able to tell them apart — one means nobody has
 * asked yet, the other means we do not know.
 *
 * [isRefreshing] rides alongside rather than replacing the content: pulling to
 * refresh must not blank the screen you were reading.
 */
@Immutable
sealed interface ForumScreenState<out T> {

    data object Loading : ForumScreenState<Nothing>

    data class Content<T>(val value: T, val isRefreshing: Boolean = false) : ForumScreenState<T>

    data class Error(val failure: ForumFailure) : ForumScreenState<Nothing>
}
