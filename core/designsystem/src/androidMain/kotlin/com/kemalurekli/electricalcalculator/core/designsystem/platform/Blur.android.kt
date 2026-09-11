package com.kemalurekli.electricalcalculator.core.designsystem.platform

import android.os.Build

/**
 * `RenderEffect`, and therefore `Modifier.blur`, arrived in Android 12.
 *
 * Below it the modifier is accepted and does nothing, which is worse than
 * throwing: the screen looks finished and is not.
 */
actual val canBlurContent: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
