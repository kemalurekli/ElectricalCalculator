package com.kemalurekli.electricalcalculator.core.billing.data

import com.kemalurekli.electricalcalculator.core.billing.domain.BillingFailure
import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.domain.ProProduct
import com.kemalurekli.electricalcalculator.core.billing.domain.PurchaseOutcome
import com.kemalurekli.electricalcalculator.core.billing.domain.RestoreOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * What a build with no store credentials uses.
 *
 * Everything is unlocked. See `BillingBackend` for why that is the right way
 * round: the reader of an unconfigured build cannot buy anything, so locking a
 * feature would leave them with a paywall that does nothing, guarding something
 * that used to be free.
 *
 * `purchase` and `restore` still answer honestly rather than pretending, so a
 * screen that somehow reached them says what happened instead of hanging.
 */
internal class UnconfiguredEntitlementRepository : EntitlementRepository {

    override val isPro: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    override val product: StateFlow<ProProduct?> = MutableStateFlow<ProProduct?>(null).asStateFlow()

    override suspend fun refresh() = Unit

    override suspend fun purchase(): PurchaseOutcome =
        PurchaseOutcome.Failed(BillingFailure.NOT_CONFIGURED)

    override suspend fun restore(): RestoreOutcome =
        RestoreOutcome.Failed(BillingFailure.NOT_CONFIGURED)
}
