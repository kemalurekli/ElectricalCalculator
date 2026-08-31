package com.kemalurekli.electricalcalculator.features.pro

import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.domain.ProProduct
import com.kemalurekli.electricalcalculator.core.billing.domain.PurchaseOutcome
import com.kemalurekli.electricalcalculator.core.billing.domain.RestoreOutcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A store that answers whatever the test says, whenever the test says.
 *
 * [gate] is what makes the double-press case testable: a real purchase opens a
 * system sheet and takes as long as the reader does, and the screen has to hold
 * still for all of it.
 */
class FakeEntitlementRepository(
    purchase: PurchaseOutcome = PurchaseOutcome.Purchased,
    restore: RestoreOutcome = RestoreOutcome.Restored,
) : EntitlementRepository {

    override val isPro = MutableStateFlow(false)

    override val product: StateFlow<ProProduct?> =
        MutableStateFlow(ProProduct(id = "voltageboard_pro", formattedPrice = "₺149,99"))

    var refreshes = 0
        private set

    var purchases = 0
        private set

    var restores = 0
        private set

    /** Left uncompleted to hold a call open. */
    var gate: CompletableDeferred<Unit>? = null

    private val purchaseOutcome = purchase
    private val restoreOutcome = restore

    override suspend fun refresh() {
        refreshes++
    }

    override suspend fun purchase(): PurchaseOutcome {
        purchases++
        gate?.await()
        if (purchaseOutcome == PurchaseOutcome.Purchased) isPro.value = true
        return purchaseOutcome
    }

    override suspend fun restore(): RestoreOutcome {
        restores++
        gate?.await()
        if (restoreOutcome == RestoreOutcome.Restored) isPro.value = true
        return restoreOutcome
    }
}
