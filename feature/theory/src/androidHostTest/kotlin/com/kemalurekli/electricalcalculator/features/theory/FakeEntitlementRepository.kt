package com.kemalurekli.electricalcalculator.features.theory

import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.domain.ProProduct
import com.kemalurekli.electricalcalculator.core.billing.domain.PurchaseOutcome
import com.kemalurekli.electricalcalculator.core.billing.domain.RestoreOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * An entitlement that is whatever the test says it is.
 *
 * The real one is a cache combined with a store; nothing in the theory shelf
 * cares which, only whether the answer is yes.
 */
class FakeEntitlementRepository(pro: Boolean = false) : EntitlementRepository {

    private val _isPro = MutableStateFlow(pro)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    override val product: StateFlow<ProProduct?> = MutableStateFlow<ProProduct?>(null).asStateFlow()

    override suspend fun refresh() = Unit

    override suspend fun purchase(): PurchaseOutcome = PurchaseOutcome.Cancelled

    override suspend fun restore(): RestoreOutcome = RestoreOutcome.NothingToRestore
}
