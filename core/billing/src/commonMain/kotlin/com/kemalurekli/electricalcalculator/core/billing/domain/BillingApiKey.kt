package com.kemalurekli.electricalcalculator.core.billing.domain

/**
 * RevenueCat issues one publishable key per store, and the SDK is configured
 * with whichever belongs to the platform it is running on.
 *
 * Both are carried into shared code by `BillingConfig` so that `configure` can
 * live in `commonMain` with everything else; only the choice between them is
 * per-platform, and that is the whole of what these two files do.
 */
expect val billingApiKey: String
