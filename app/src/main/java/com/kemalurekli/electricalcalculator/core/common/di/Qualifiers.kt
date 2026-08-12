package com.kemalurekli.electricalcalculator.core.common.di

import javax.inject.Qualifier

/**
 * Dispatcher qualifiers.
 *
 * Injecting dispatchers rather than referencing `Dispatchers.IO` directly lets
 * tests substitute a deterministic scheduler, which is what makes the data
 * layer testable without a device.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainDispatcher

/** A [kotlinx.coroutines.CoroutineScope] that lives as long as the process. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
