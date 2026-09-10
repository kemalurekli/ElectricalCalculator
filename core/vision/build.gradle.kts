plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Reading what is printed on a nameplate.
 *
 * Two halves that are deliberately not mixed. Recognising characters in a
 * photograph is the platform's job — ML Kit on Android, Vision on iOS, both
 * offline and both already on the device — and it is the half with no decisions
 * in it. Working out that "7,5kW" and "cos φ 0,86" are a power and a power
 * factor is this module's own, is ordinary Kotlin, and is where every mistake
 * a reader would notice actually lives.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.vision"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.mlkit.text.recognition)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
