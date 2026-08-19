plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

/**
 * The first module out of `:app`, and the one the port's hardest problem lives
 * in: number formatting.
 *
 * Deliberately small. It holds only what the design system and the first ported
 * screen need — validation results, numeric input sanitising, and
 * `NumberFormatter`. The rest of the old `core/common` package (time, locale,
 * string resolution, the Hilt modules) stays in `:app` until there is a reason
 * to move it. Moving code you do not need yet is how a port stops being
 * reviewable.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.common"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        // The common tests are the only check on NumberFormatter's rewrite, so
        // they run on the JVM as well as on iOS — the two places the app ships.
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
