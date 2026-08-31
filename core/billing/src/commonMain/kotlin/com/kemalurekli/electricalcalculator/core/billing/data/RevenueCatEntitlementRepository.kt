package com.kemalurekli.electricalcalculator.core.billing.data

import com.kemalurekli.electricalcalculator.core.billing.domain.BillingFailure
import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.domain.PRO_ENTITLEMENT_ID
import com.kemalurekli.electricalcalculator.core.billing.domain.PRO_PRODUCT_ID
import com.kemalurekli.electricalcalculator.core.billing.domain.ProProduct
import com.kemalurekli.electricalcalculator.core.billing.domain.PurchaseOutcome
import com.kemalurekli.electricalcalculator.core.billing.domain.RestoreOutcome
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitGetProducts
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The store's answer, through RevenueCat.
 *
 * Which answer is shown while the store is being asked — and offline, when it
 * never replies — is [EntitlementState]'s decision, not this class's.
 *
 * Nothing here throws. Every call the store can refuse is caught and turned
 * into a value, because a failed refresh on a screen the reader did not ask
 * for is not their problem, and a crash on it certainly is not.
 */
internal class RevenueCatEntitlementRepository(
    private val state: EntitlementState,
) : EntitlementRepository {

    override val isPro: StateFlow<Boolean> get() = state.isPro

    private val _product = MutableStateFlow<ProProduct?>(null)
    override val product: StateFlow<ProProduct?> = _product

    override suspend fun refresh() {
        runCatching { Purchases.sharedInstance.awaitCustomerInfo() }
            .onSuccess { record(it) }

        // Asked separately, and its failure ignored: the price is only needed
        // by the paywall, and not knowing it must not cost the reader their
        // entitlement on a bad connection.
        runCatching { Purchases.sharedInstance.awaitGetProducts(listOf(PRO_PRODUCT_ID)) }
            .onSuccess { products -> _product.value = products.firstOrNull()?.toDomain() }
    }

    override suspend fun purchase(): PurchaseOutcome {
        val product = runCatching {
            Purchases.sharedInstance.awaitGetProducts(listOf(PRO_PRODUCT_ID)).firstOrNull()
        }.getOrNull() ?: return PurchaseOutcome.Failed(BillingFailure.STORE_UNAVAILABLE)

        return try {
            record(Purchases.sharedInstance.awaitPurchase(storeProduct = product).customerInfo)
            PurchaseOutcome.Purchased
        } catch (e: PurchasesTransactionException) {
            // Backing out is not a failure and is never reported as one.
            if (e.userCancelled) PurchaseOutcome.Cancelled
            else PurchaseOutcome.Failed(e.code.toFailure())
        } catch (e: PurchasesException) {
            PurchaseOutcome.Failed(e.code.toFailure())
        }
    }

    override suspend fun restore(): RestoreOutcome = try {
        val entitled = record(Purchases.sharedInstance.awaitRestore())
        // Restoring against an account that never bought anything succeeds and
        // returns nothing. Saying "restored" there would be a lie the reader
        // finds out about at the next locked feature.
        if (entitled) RestoreOutcome.Restored else RestoreOutcome.NothingToRestore
    } catch (e: PurchasesException) {
        RestoreOutcome.Failed(e.code.toFailure())
    }

    /** Reads the entitlement out of the store's answer and publishes it. */
    private suspend fun record(info: CustomerInfo): Boolean =
        state.record(info.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true)
}

private fun StoreProduct.toDomain() = ProProduct(id = id, formattedPrice = price.formatted)

/**
 * Forty-odd codes down to the four things a reader can act on.
 *
 * Anything unrecognised is [BillingFailure.UNKNOWN] rather than being guessed
 * at: a wrong explanation is worse than none, and the screen's wording for
 * unknown is the one that tells them to try again later.
 */
private fun PurchasesErrorCode.toFailure(): BillingFailure = when (this) {
    PurchasesErrorCode.NetworkError,
    PurchasesErrorCode.OfflineConnectionError,
    -> BillingFailure.NO_CONNECTION

    PurchasesErrorCode.StoreProblemError,
    PurchasesErrorCode.ProductNotAvailableForPurchaseError,
    PurchasesErrorCode.PurchaseNotAllowedError,
    PurchasesErrorCode.PaymentPendingError,
    -> BillingFailure.STORE_UNAVAILABLE

    PurchasesErrorCode.ProductAlreadyPurchasedError,
    PurchasesErrorCode.ReceiptAlreadyInUseError,
    -> BillingFailure.ALREADY_OWNED

    PurchasesErrorCode.ConfigurationError,
    PurchasesErrorCode.InvalidCredentialsError,
    -> BillingFailure.NOT_CONFIGURED

    else -> BillingFailure.UNKNOWN
}
