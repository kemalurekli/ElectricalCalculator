package com.kemalurekli.electricalcalculator.core.designsystem.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith

/**
 * How long things take, and why.
 *
 * `docs/design-language.md` left motion undecided on purpose — it said the app
 * should not get a motion language until the palette and the structure had been
 * lived with. They have been, and in the meantime five components had started
 * animating with numbers typed at the call site. That is how a motion language
 * ends up being three slightly different fades that nobody chose.
 *
 * ### The rules the durations come from
 *
 * **Motion says a state changed. It never decorates.** Nothing here moves to be
 * noticed; every animation below is the visible half of an answer to something
 * the reader did.
 *
 * **Nothing overshoots.** No springs with bounce, no scale, no rotation. This
 * is an instrument, and instruments do not bob. The one exception in the app is
 * the sweep on the Pro mark, which is advertising and is allowed to behave like
 * it.
 *
 * **Content fades; only navigation travels.** A slide implies you went
 * somewhere. Inside a screen you did not, so content that replaces other
 * content crossfades in place.
 *
 * **Short.** The reader is working, not watching. Everything under a quarter of
 * a second except a push, which has a direction to establish.
 */
object ElecMotion {

    /**
     * A control changing under the reader's own finger: a pill lighting up, a
     * field's rule taking the accent. Long enough not to snap, short enough
     * that it still feels like the touch caused it.
     */
    const val REACT_MILLIS: Int = 120

    /**
     * Content replaced in place — an explanation answering a choice, the body
     * behind a tab. Long enough to see that it was a replacement rather than a
     * repaint.
     */
    const val SWAP_MILLIS: Int = 180

    /**
     * The head of a swap spent clearing the old content before the new begins.
     * Without it the two are briefly on top of each other, which on text reads
     * as a smear.
     */
    const val SWAP_OUT_MILLIS: Int = 60

    /**
     * Something opening or closing because the reader asked for it: an
     * accordion, the search field's clear button.
     */
    const val REVEAL_MILLIS: Int = 220

    /** Switching tabs. A tab is a place you are already in, not one you travel to. */
    const val TAB_MILLIS: Int = 150

    /** A push. Long enough to read as a direction, short enough not to be waited on. */
    const val PUSH_MILLIS: Int = 300

    /**
     * Fast out, settle in — the shape of something that was set moving and then
     * came to rest, rather than something being carried at constant speed.
     */
    val Easing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** [REACT_MILLIS] on the standard curve, for colours and small dimensions. */
    fun <T> react(): FiniteAnimationSpec<T> =
        tween(durationMillis = REACT_MILLIS, easing = Easing)

    /** [REVEAL_MILLIS] on the standard curve. */
    fun <T> reveal(): FiniteAnimationSpec<T> =
        tween(durationMillis = REVEAL_MILLIS, easing = Easing)
}

/**
 * One piece of content replacing another in the same place.
 *
 * Out before in, never together: the reader's eye is already on the control
 * they pressed, and two texts dissolving through each other under it is a
 * smear rather than a change.
 */
fun elecSwap(): ContentTransform =
    fadeIn(
        tween(
            durationMillis = ElecMotion.SWAP_MILLIS - ElecMotion.SWAP_OUT_MILLIS,
            delayMillis = ElecMotion.SWAP_OUT_MILLIS,
            easing = ElecMotion.Easing,
        ),
    ) togetherWith fadeOut(
        tween(durationMillis = ElecMotion.SWAP_OUT_MILLIS, easing = ElecMotion.Easing),
    )

/**
 * Something the reader asked to open — an accordion, a disclosure.
 *
 * It grows rather than sliding in over what is already there, because the
 * content below it genuinely has to move down and pretending otherwise makes
 * the page jump when the animation ends.
 */
val elecRevealEnter: EnterTransition
    get() = expandVertically(ElecMotion.reveal()) + fadeIn(ElecMotion.reveal())

val elecRevealExit: ExitTransition
    get() = shrinkVertically(ElecMotion.reveal()) + fadeOut(ElecMotion.reveal())

/** Something appearing in place, with nothing to make room for. */
val elecAppear: EnterTransition
    get() = fadeIn(ElecMotion.react())

val elecVanish: ExitTransition
    get() = fadeOut(ElecMotion.react())
