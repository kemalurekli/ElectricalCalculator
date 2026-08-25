package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.runtime.Composable
import com.kemalurekli.electricalcalculator.core.common.util.formatAsRelativeTime
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.state_just_now
import kotlin.time.Clock
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

/**
 * How long ago [this] was, in the app's words.
 *
 * The platform formatter does the phrasing — plural rules and thresholds differ
 * by language and both platforms already ship them — but it renders anything
 * under a minute as "0 minutes ago", which reads as broken for something that
 * has just happened. A project saved a moment ago said exactly that.
 *
 * Shared rather than fixed per screen. The home dashboard had already worked
 * this out and kept the answer to itself, so the projects list repeated the bug
 * before repeating the fix. One rule, one string.
 */
@Composable
fun Instant.asRelativeTime(): String {
    val elapsed = Clock.System.now().toEpochMilliseconds() - toEpochMilliseconds()
    if (elapsed < MILLIS_PER_MINUTE) return stringResource(Res.string.state_just_now)
    return formatAsRelativeTime()
}

private const val MILLIS_PER_MINUTE = 60_000L
