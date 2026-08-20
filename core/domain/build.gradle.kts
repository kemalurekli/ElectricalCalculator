plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

/**
 * The app's vocabulary: what a calculation, a project, a favourite and a set of
 * engineering defaults are, and what the repositories that store them promise.
 *
 * No Compose, no resources, no platform. The one thing that keeps it that way
 * is that `CalculatorDescriptor` — which carries Android string ids for every
 * calculator's title — stays behind in `:app` for now. Its strings belong with
 * the calculator screens, and moving them is a hundred-file job that has no
 * reason to happen before those screens do.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.domain"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    // `kotlin.time.Instant` replaced `java.time.Instant`, and is still marked
    // experimental. Opting in once here beats an annotation on every file that
    // names a timestamp.
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
