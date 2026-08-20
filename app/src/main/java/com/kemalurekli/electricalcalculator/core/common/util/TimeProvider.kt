package com.kemalurekli.electricalcalculator.core.common.util

import kotlin.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

/**
 * Supplies the current time.
 *
 * Injected rather than calling [Instant.now] inline so that tests over history
 * ordering and timestamps are deterministic instead of racing the wall clock.
 */
interface TimeProvider {
    fun now(): Instant
}

@Singleton
class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun now(): Instant = Clock.System.now()
}
