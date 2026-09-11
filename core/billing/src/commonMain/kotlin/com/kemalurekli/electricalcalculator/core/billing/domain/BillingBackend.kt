package com.kemalurekli.electricalcalculator.core.billing.domain

/**
 * Whether this build can talk to a store at all.
 *
 * A checkout without `local.properties` — CI, or anyone who cloned the
 * repository to work on the calculators — has no RevenueCat key. That must not
 * turn a working app into a broken one, so the absence of configuration is a
 * state the app carries rather than an error it throws. The same decision
 * `AppBackend` makes, for the same reason.
 *
 * What it resolves to is the opposite of the forum's, though. The forum has
 * nothing to show without a backend and says so. Billing has something to show
 * without one: everything. An unconfigured build treats the reader as entitled,
 * because the alternative is a paywall they could not buy from even if they
 * wanted to, guarding a feature that used to be free.
 *
 * A release build must not ship in that state, so `bundleRelease` checks the
 * key is present — see core/billing/build.gradle.kts.
 */
sealed interface BillingBackend {

    data object Available : BillingBackend

    data object NotConfigured : BillingBackend
}
