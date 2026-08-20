package com.kemalurekli.electricalcalculator.core.common.util

import kotlin.time.Instant
import kotlin.time.Clock

/**
 * Supplies the current time.
 *
 * Injected rather than calling the clock inline so that tests over history
 * ordering and timestamps are deterministic instead of racing the wall clock.
 */
interface TimeProvider {
    fun now(): Instant
}

class SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Clock.System.now()
}
