package com.kemalurekli.electricalcalculator.core.designsystem.platform

import androidx.compose.runtime.Composable

/**
 * Copying and sharing of calculation results.
 *
 * The two are kept together because they format the same text, and a reader who
 * finds one expects the other beside it.
 *
 * ### Why this is obtained from composition rather than injected
 *
 * Neither platform can do this from nothing. Android needs a `Context` to reach
 * the clipboard service and to start the chooser; iOS needs the view controller
 * the share sheet will be presented from. Both of those are available in
 * composition and awkward anywhere else, so the split is at the implementation
 * and the call sites read the same on both platforms:
 *
 * ```
 * val sharing = rememberResultSharing()
 * sharing.share(subject, text)
 * ```
 *
 * It used to be an `object` taking a `Context` on every call, which is the same
 * dependency spelled out at 34 call sites instead of one.
 */
interface ResultSharing {

    /**
     * Puts [text] on the clipboard.
     *
     * @return true when the caller should show its own confirmation. Android 13
     *   and later display a system clipboard preview, and iOS shows nothing, so
     *   the answer differs by platform and by version — which is exactly why it
     *   is returned rather than assumed.
     */
    fun copy(label: String, text: String): Boolean

    /** Opens the system share sheet with [text]. */
    fun share(subject: String, text: String)
}

@Composable
expect fun rememberResultSharing(): ResultSharing
