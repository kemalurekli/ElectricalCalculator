package com.kemalurekli.electricalcalculator.features.pro

import com.kemalurekli.electricalcalculator.core.billing.domain.BillingFailure
import com.kemalurekli.electricalcalculator.core.billing.domain.PurchaseOutcome
import com.kemalurekli.electricalcalculator.core.billing.domain.RestoreOutcome
import com.kemalurekli.electricalcalculator.features.pro.presentation.PaywallMessage
import com.kemalurekli.electricalcalculator.features.pro.presentation.PaywallStatus
import com.kemalurekli.electricalcalculator.features.pro.presentation.PaywallViewModel
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The screen that asks for money, and the three ways a store call ends.
 *
 * The one worth spelling out is cancellation. Backing out of a payment sheet is
 * a decision, and an app that answers it with an error message is telling the
 * reader off for not buying something.
 */
class PaywallViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeEntitlementRepository) = PaywallViewModel(repository)

    @Test
    fun `the price is asked for on arrival`() = runTest {
        val store = FakeEntitlementRepository()
        viewModel(store)
        advanceUntilIdle()

        assertEquals(1, store.refreshes, "the paywall cannot name a price it never asked for")
    }

    @Test
    fun `a purchase says nothing and unlocks`() = runTest {
        val store = FakeEntitlementRepository(purchase = PurchaseOutcome.Purchased)
        val viewModel = viewModel(store)

        viewModel.onBuy()
        advanceUntilIdle()

        assertTrue(store.isPro.value)
        assertNull(viewModel.message.value, "the unlocked feature is the confirmation")
    }

    @Test
    fun `backing out is not reported as a failure`() = runTest {
        val store = FakeEntitlementRepository(purchase = PurchaseOutcome.Cancelled)
        val viewModel = viewModel(store)

        viewModel.onBuy()
        advanceUntilIdle()

        assertNull(viewModel.message.value)
        assertEquals(PaywallStatus.IDLE, viewModel.status.value)
    }

    @Test
    fun `a refused purchase says which refusal it was`() = runTest {
        val store = FakeEntitlementRepository(
            purchase = PurchaseOutcome.Failed(BillingFailure.NO_CONNECTION),
        )
        val viewModel = viewModel(store)

        viewModel.onBuy()
        advanceUntilIdle()

        val message = assertIs<PaywallMessage.Failed>(viewModel.message.value)
        assertEquals(BillingFailure.NO_CONNECTION, message.reason)
    }

    @Test
    fun `a second press while the sheet is open is ignored`() = runTest {
        val store = FakeEntitlementRepository()
        store.gate = CompletableDeferred()
        val viewModel = viewModel(store)

        viewModel.onBuy()
        runCurrent()
        viewModel.onBuy()
        runCurrent()

        assertEquals(1, store.purchases, "two sheets means the second fails and reads as the first")
        assertEquals(PaywallStatus.PURCHASING, viewModel.status.value)

        store.gate?.complete(Unit)
        advanceUntilIdle()
        assertEquals(PaywallStatus.IDLE, viewModel.status.value)
    }

    @Test
    fun `restoring nothing says so rather than claiming success`() = runTest {
        val store = FakeEntitlementRepository(restore = RestoreOutcome.NothingToRestore)
        val viewModel = viewModel(store)

        viewModel.onRestore()
        advanceUntilIdle()

        assertEquals(PaywallMessage.NothingToRestore, viewModel.message.value)
        assertTrue(!store.isPro.value)
    }

    @Test
    fun `a restore that finds something unlocks and says so`() = runTest {
        val store = FakeEntitlementRepository(restore = RestoreOutcome.Restored)
        val viewModel = viewModel(store)

        viewModel.onRestore()
        advanceUntilIdle()

        assertEquals(PaywallMessage.Restored, viewModel.message.value)
        assertTrue(store.isPro.value)
    }

    @Test
    fun `restoring cannot start while a purchase is open`() = runTest {
        val store = FakeEntitlementRepository()
        store.gate = CompletableDeferred()
        val viewModel = viewModel(store)

        viewModel.onBuy()
        runCurrent()
        viewModel.onRestore()
        runCurrent()

        assertEquals(0, store.restores)

        store.gate?.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun `a message is cleared once it has been shown`() = runTest {
        val store = FakeEntitlementRepository(restore = RestoreOutcome.NothingToRestore)
        val viewModel = viewModel(store)

        viewModel.onRestore()
        advanceUntilIdle()
        viewModel.onMessageShown()

        assertNull(viewModel.message.value, "or it reappears on the next recomposition")
    }
}
