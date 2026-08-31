package com.kemalurekli.electricalcalculator.core.billing

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.kemalurekli.electricalcalculator.core.billing.data.EntitlementCache
import com.kemalurekli.electricalcalculator.core.billing.data.EntitlementState
import com.kemalurekli.electricalcalculator.core.billing.data.UnconfiguredEntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.domain.BillingFailure
import com.kemalurekli.electricalcalculator.core.billing.domain.PurchaseOutcome
import com.kemalurekli.electricalcalculator.core.billing.domain.RestoreOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Which answer the app shows while the store is being asked.
 *
 * The case that matters is the one nobody sees in an office: the app is used on
 * sites with no signal, and offline the store never answers at all. A reader
 * who paid must open to the app they paid for, not to a locked one that stays
 * locked until they find a bar of reception.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EntitlementStateTest {

    private fun cache(scope: CoroutineScope): EntitlementCache {
        val file = Files.createTempDirectory("entitlements").resolve("test.preferences_pb")
        return EntitlementCache(
            PreferenceDataStoreFactory.createWithPath(
                scope = scope,
                produceFile = { file.toString().toPath() },
            ),
        )
    }

    @Test
    fun `a fresh install is not entitled`() = runTest {
        val state = EntitlementState(cache(backgroundScope), backgroundScope)
        runCurrent()

        assertFalse(state.isPro.value)
    }

    @Test
    fun `the store's answer is published`() = runTest {
        val state = EntitlementState(cache(backgroundScope), backgroundScope)

        state.record(entitled = true)
        runCurrent()

        assertTrue(state.isPro.value)
    }

    @Test
    fun `a purchase survives a cold launch with no store`() = runTest {
        val shared = cache(backgroundScope)

        EntitlementState(shared, backgroundScope).record(entitled = true)
        runCurrent()

        // A second state over the same file, having never heard from a store —
        // which is what a relaunch in a basement is.
        val relaunched = EntitlementState(shared, backgroundScope)
        runCurrent()

        assertTrue(relaunched.isPro.value, "the cached answer must stand until the store replies")
    }

    @Test
    fun `the store overrules the cache`() = runTest {
        val shared = cache(backgroundScope)
        EntitlementState(shared, backgroundScope).record(entitled = true)
        runCurrent()

        val relaunched = EntitlementState(shared, backgroundScope)
        runCurrent()
        // A refund, or a different store account.
        relaunched.record(entitled = false)
        runCurrent()

        assertFalse(relaunched.isPro.value)
    }

    @Test
    fun `revoking is written through, not only shown`() = runTest {
        val shared = cache(backgroundScope)
        EntitlementState(shared, backgroundScope).record(entitled = true)
        runCurrent()
        EntitlementState(shared, backgroundScope).record(entitled = false)
        runCurrent()

        val relaunched = EntitlementState(shared, backgroundScope)
        runCurrent()

        assertFalse(relaunched.isPro.value)
    }
}

/**
 * A build with no store credentials.
 *
 * A checkout without `local.properties` must not be a crippled app. See
 * `BillingBackend` for why this resolves the opposite way to the forum's.
 */
class UnconfiguredEntitlementRepositoryTest {

    private val repository = UnconfiguredEntitlementRepository()

    @Test
    fun `everything is unlocked`() {
        assertTrue(repository.isPro.value)
    }

    @Test
    fun `there is no price to show`() {
        assertEquals(null, repository.product.value)
    }

    @Test
    fun `buying says why it cannot, rather than hanging`() = runTest {
        val outcome = assertIs<PurchaseOutcome.Failed>(repository.purchase())
        assertEquals(BillingFailure.NOT_CONFIGURED, outcome.reason)
    }

    @Test
    fun `restoring says why it cannot`() = runTest {
        val outcome = assertIs<RestoreOutcome.Failed>(repository.restore())
        assertEquals(BillingFailure.NOT_CONFIGURED, outcome.reason)
    }
}
