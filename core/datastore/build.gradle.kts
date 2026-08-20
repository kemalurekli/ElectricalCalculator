plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

/**
 * The user's settings: theme, language, units, engineering defaults, and which
 * forum threads they have pinned.
 *
 * DataStore has been multiplatform for a while, so the reading and writing came
 * across unchanged. What each platform decides for itself is one thing: where
 * the file goes.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.datastore"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.androidx.datastore.preferences)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
