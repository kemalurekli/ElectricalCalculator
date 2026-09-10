package com.kemalurekli.electricalcalculator.core.vision

import androidx.compose.runtime.Composable

/**
 * Photographing a nameplate and getting figures back.
 *
 * ### Why the system camera and not one of our own
 *
 * A live preview with recognition running over it is the nicer version of this
 * and is two separate screens — CameraX on one platform, AVFoundation on the
 * other — which is exactly the split `docs/design-language.md` exists to
 * prevent. The system camera is a screen the reader already knows, behaves the
 * same on both, and leaves this module with no interface of its own to keep in
 * step.
 *
 * ### Why nothing is filled in automatically
 *
 * Recognition is a guess. The caller is handed what was read and shows it, so
 * that applying it is the reader's decision — a field quietly filled with a
 * misread figure is worse than an empty one, because the reader has no reason
 * to look at it again.
 */
interface NameplateScanner {

    /** False where there is no camera, or no permission to open one. */
    val isSupported: Boolean

    /** Opens the camera. The result arrives on the callback given at creation. */
    fun scan()
}

/**
 * @param onRead what was recognised. Called with an empty reading when the
 *   photograph had nothing this app can use, and not called at all when the
 *   reader backed out of the camera — cancelling is not a failure and gets no
 *   message.
 */
@Composable
expect fun rememberNameplateScanner(onRead: (NameplateReading) -> Unit): NameplateScanner
