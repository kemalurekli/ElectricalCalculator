package com.kemalurekli.electricalcalculator.core.billing.di

import com.kemalurekli.electricalcalculator.core.billing.data.ENTITLEMENTS_FILE
import com.kemalurekli.electricalcalculator.core.billing.data.EntitlementCache
import com.kemalurekli.electricalcalculator.core.billing.data.EntitlementState
import com.kemalurekli.electricalcalculator.core.billing.data.RevenueCatEntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.data.UnconfiguredEntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.domain.BillingBackend
import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.domain.billingApiKey
import com.kemalurekli.electricalcalculator.core.datastore.createPreferencesDataStore
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Billing, and the one decision that has to be made before anything asks.
 *
 * `Purchases.configure` is called here rather than from either entry point,
 * because both of them would have to make the same call with the same key and
 * one of them would eventually be forgotten — which is the bug that gave the
 * forum an iOS client it did not have for three commits.
 *
 * The scope is the application's: the entitlement outlives every screen, and a
 * `StateFlow` sharing in a screen's scope would drop its value the moment the
 * reader navigated away from the one that happened to start it.
 */
val billingModule: Module = module {

    single<BillingBackend> {
        // Deliberately not an exception. See BillingBackend.
        if (billingApiKey.isBlank()) BillingBackend.NotConfigured else BillingBackend.Available
    }

    single<EntitlementRepository> {
        when (get<BillingBackend>()) {
            BillingBackend.NotConfigured -> UnconfiguredEntitlementRepository()

            BillingBackend.Available -> runCatching {
                if (!Purchases.isConfigured) Purchases.configure(apiKey = billingApiKey)
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
                RevenueCatEntitlementRepository(
                    EntitlementState(
                        cache = EntitlementCache(
                            createPreferencesDataStore(scope, ENTITLEMENTS_FILE),
                        ),
                        scope = scope,
                    ),
                )
            }.getOrElse {
                // A key the SDK rejects, or a store client that will not start.
                // This runs while Koin is resolving the graph, which is before
                // the first frame — throwing here takes down an app whose other
                // twenty features have nothing to do with billing.
                //
                // Degrading unlocks the paid feature, which is the lesser of the
                // two: giving something away is recoverable, and a launch crash
                // on a store the reader cannot influence is not.
                UnconfiguredEntitlementRepository()
            }
        }
    }
}
