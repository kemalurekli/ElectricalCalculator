import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

/**
 * Whether this copy of the app has been paid for.
 *
 * A `:core:` module rather than a feature, because an entitlement is state that
 * features read: the screen that sells it lives in `:feature:pro`, and the
 * screens that are gated by it never depend on either.
 *
 * RevenueCat rather than the two native billing clients. Play Billing is
 * reachable from Kotlin, but StoreKit 2 is Swift-only and cinterop reads
 * Objective-C headers — going native would have meant writing Swift in `iosApp`
 * and bridging it back, which is the work the whole port avoided. From 3.0 the
 * SDK builds the iOS side against the purchases-ios Swift package through a
 * cinterop binding Gradle generates, so the Xcode project stays untouched.
 */

val billingProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun billingProperty(key: String): String = billingProperties.getProperty(key).orEmpty()

/**
 * Carries the two publishable SDK keys into shared code.
 *
 * Modelled on `generateForumConfig`, and the boundary is the same: these keys
 * are meant to ship in the binary. The Play service-account JSON and the App
 * Store Connect key are not, and live only in the RevenueCat dashboard.
 */
val generateBillingConfig = tasks.register("generateBillingConfig") {
    val output = layout.buildDirectory.dir("generated/billingConfig/kotlin")
    val androidKey = billingProperty("revenuecat.androidKey")
    val iosKey = billingProperty("revenuecat.iosKey")
    // Declared as inputs, not just captured. With outputs alone Gradle calls
    // the task up to date as long as the generated file is still there, so a
    // key added to `local.properties` after the first build never reaches the
    // binary — and `checkBillingKey`, which reads the properties file itself,
    // still passes. That split shipped an .aab with an empty key and every
    // paid feature unlocked, past a guard written to prevent exactly that.
    inputs.property("androidKey", androidKey)
    inputs.property("iosKey", iosKey)
    outputs.dir(output)
    doLast {
        val directory = output.get().asFile
            .resolve("com/kemalurekli/electricalcalculator/core/billing/domain")
        directory.mkdirs()
        directory.resolve("BillingConfig.kt").writeText(
            """
            package com.kemalurekli.electricalcalculator.core.billing.domain

            /** GENERATED from `local.properties`; see core/billing/build.gradle.kts. */
            object BillingConfig {
                const val ANDROID_KEY: String = "$androidKey"
                const val IOS_KEY: String = "$iosKey"
            }
            """.trimIndent() + "\n",
        )
    }
}

/**
 * The key as it will actually be compiled in, rather than as it appears in
 * `local.properties`.
 *
 * `:app`'s `checkBillingKey` reads the properties file, which is the right
 * thing to say to a developer — "add this line" — and the wrong thing to trust,
 * because it is not what ships. The two disagreed once already: a stale
 * generated file meant the guard passed and the bundle carried an empty key.
 *
 * Reading the generated file inside the project that owns it keeps this off
 * the configuration cache's cross-project rules; `:app` wires it to the bundle
 * tasks by path.
 */
val checkGeneratedBillingKey = tasks.register("checkGeneratedBillingKey") {
    dependsOn(generateBillingConfig)
    val generated = generateBillingConfig.map { task ->
        task.outputs.files.singleFile
            .resolve("com/kemalurekli/electricalcalculator/core/billing/domain/BillingConfig.kt")
    }
    doLast {
        val text = generated.get().readText()
        val key = Regex("""ANDROID_KEY: String = "([^"]*)"""").find(text)?.groupValues?.get(1)
        check(!key.isNullOrBlank()) {
            "BillingConfig was generated with an empty ANDROID_KEY, so this build " +
                "unlocks every paid feature. Check revenuecat.androidKey in " +
                "local.properties, then re-run."
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.billing"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.configure { kotlin.srcDir(generateBillingConfig) }

        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:datastore"))
            implementation(libs.purchases.kmp.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
