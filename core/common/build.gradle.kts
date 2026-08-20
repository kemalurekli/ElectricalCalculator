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
 * `NumberFormatter`. The rest of the old `core/common` package stays in `:app`
 * until there is a reason to move it — moving code you do not need yet is how a
 * port stops being reviewable — and each thing arrives when the first screen
 * that needs it does: dates with the history screen, collation and string
 * resolution with the glossary.
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
        commonMain.dependencies {
            // `api`: `StringResolver` has a `StringResource` in its signature,
            // so every caller needs the type too.
            //
            // Named by coordinate rather than through the `compose` accessor
            // because that accessor needs the Compose plugin, and the plugin
            // brings a compiler that refuses to run without a Compose runtime.
            // There is no Compose here — a `StringResource` is a handle to a
            // row in a resource table, not anything that draws.
            api(libs.compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
