package com.kemalurekli.electricalcalculator.core.billing.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * The one thing the app sells, and what the store charges for it.
 *
 * The price is never written in the app. Both stores localise it — currency,
 * formatting, and whatever the developer set for that country — and a figure
 * hard-coded here would be wrong in most of the world and illegal in some of it.
 */
data class ProProduct(
    val id: String,
    /** Already formatted by the store, in the reader's currency. */
    val formattedPrice: String,
)

/** How a purchase ended. */
sealed interface PurchaseOutcome {

    data object Purchased : PurchaseOutcome

    /** The reader backed out. Not a failure, and never shown as one. */
    data object Cancelled : PurchaseOutcome

    data class Failed(val reason: BillingFailure) : PurchaseOutcome
}

/** How a restore ended. */
sealed interface RestoreOutcome {

    data object Restored : RestoreOutcome

    /** The store has no record of a purchase for this account. */
    data object NothingToRestore : RestoreOutcome

    data class Failed(val reason: BillingFailure) : RestoreOutcome
}

/**
 * The failures worth telling the reader apart.
 *
 * Collapsed from the SDK's forty-odd error codes, because the reader can only
 * do three things about any of them: try again on a better connection, look at
 * their store account, or nothing at all.
 */
enum class BillingFailure {
    /** No connection, or the request never got an answer. Worth retrying. */
    NO_CONNECTION,

    /** The store itself refused or is unreachable. Not worth retrying now. */
    STORE_UNAVAILABLE,

    /** Already bought on this store account — restore rather than buy. */
    ALREADY_OWNED,

    /** This build has no store credentials. See [BillingBackend]. */
    NOT_CONFIGURED,

    UNKNOWN,
}

/**
 * What the reader has paid for.
 *
 * One flag today. It is an interface rather than a boolean because the second
 * gated feature must be a line at a call site and nothing more — that, and not
 * the PDF export, is what this module exists for.
 */
interface EntitlementRepository {

    /**
     * Whether the Pro features are unlocked.
     *
     * Seeded from a local cache so a cold launch on a site with no signal opens
     * with the answer the reader paid for, rather than with false for as long
     * as the store takes to reply — which offline is forever.
     */
    val isPro: StateFlow<Boolean>

    /** Null until the store has answered, and on a build with no store. */
    val product: StateFlow<ProProduct?>

    /** Re-asks the store. Safe to call on every launch; never throws. */
    suspend fun refresh()

    suspend fun purchase(): PurchaseOutcome

    suspend fun restore(): RestoreOutcome
}

/** The non-consumable sold in both stores, under the same identifier. */
const val PRO_PRODUCT_ID: String = "voltageboard_pro"

/** The entitlement that product grants, as named in the RevenueCat dashboard. */
const val PRO_ENTITLEMENT_ID: String = "pro"
