package com.kemalurekli.electricalcalculator.core.common.util

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

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
    override fun now(): Instant = Instant.now()
}
