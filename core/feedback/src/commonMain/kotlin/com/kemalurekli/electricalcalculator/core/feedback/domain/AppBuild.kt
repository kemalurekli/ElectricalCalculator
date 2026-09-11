package com.kemalurekli.electricalcalculator.core.feedback.domain

/**
 * What this copy of the app is, for a report to say so.
 *
 * An `expect` rather than something injected from `:app`, because both halves
 * of the answer are asked of the platform anyway and there is nothing for a
 * caller to decide.
 */
expect fun appBuild(): AppBuild

data class AppBuild(
    /** As shown in the store listing: "1.4.2". Empty if the platform will not say. */
    val version: String,
    /** `android` or `ios`, which is what the table's check constraint allows. */
    val platform: String,
)
