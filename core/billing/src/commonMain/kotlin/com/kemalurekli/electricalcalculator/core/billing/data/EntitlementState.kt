package com.kemalurekli.electricalcalculator.core.billing.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Which of the two answers wins, and when.
 *
 * There are two: what the store last told this device, which is on disk and
 * available immediately, and what it says now, which needs a network round trip
 * and offline never arrives at all. The live one wins once it exists; until
 * then the cached one stands.
 *
 * That ordering is the entitlement's whole behaviour, so it is here on its own
 * rather than tangled into the code that calls the SDK — which cannot be
 * constructed in a test, because `Purchases.sharedInstance` is a singleton the
 * store configures.
 */
internal class EntitlementState(
    private val cache: EntitlementCache,
    scope: CoroutineScope,
) {

    /** Null until the store has answered in this session. */
    private val live = MutableStateFlow<Boolean?>(null)

    val isPro: StateFlow<Boolean> =
        combine(cache.isPro, live) { cached, fresh -> fresh ?: cached }
            .stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * Takes the store's answer, and writes it through.
     *
     * Both, always: publishing without caching would lose it on the next cold
     * launch, and caching without publishing would leave the screen showing the
     * old answer until something else happened to restart the flow.
     */
    suspend fun record(entitled: Boolean): Boolean {
        live.value = entitled
        cache.set(entitled)
        return entitled
    }
}
