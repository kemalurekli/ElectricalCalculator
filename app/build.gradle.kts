import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kemalurekli.electricalcalculator"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        applicationId = "com.kemalurekli.electricalcalculator"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.kemalurekli.electricalcalculator.HiltTestRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // Generates the en-XA (accented, ~30 % longer) and ar-XB (RTL,
            // mirrored) pseudolocales. Selecting them on a debug build surfaces
            // truncation and layout-direction bugs without waiting for a real
            // translation to expose them.
            isPseudoLocalesEnabled = true
        }
        release {
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Placeholder so `assembleRelease` is verifiable locally; replace with a
            // real upload key before publishing.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    lint {
        // Correctness and accessibility problems must not reach a build.
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false

        // Dependency versions here are pinned deliberately (Kotlin tracks the
        // version Hilt is built against). Upgrading is a reviewed change, not
        // something a lint run should nag about on every build.
        disable += setOf(
            "NewerVersionAvailable",
            "GradleDependency",
            "AndroidGradlePluginVersion",
            // Every hit is a standard designation — IEC 60364-5-52, NEC 392.22
            // — where the hyphen is part of the number and an en dash would be
            // wrong. The check has no true positives in a text set built out of
            // standard references.
            "TypographyDashes",
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Room writes generated schemas here so migrations stay reviewable and testable.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

// MigrationTestHelper reads the exported schemas from the test APK's assets, so
// the directory KSP writes them to has to be packaged with the instrumented
// tests. Without this a migration test fails with "cannot find the schema file"
// rather than with anything about the migration.
android.sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")

dependencies {
    // Compose BOM aligns every Compose artifact to one tested version set.
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    // Drives the per-app language picker. On Android 13+ this delegates to the
    // platform LocaleManager; below it, AppCompat provides the backport, which
    // matters because minSdk is 28.
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.adaptive)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Dependency injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Networking — wired up now for future remote modules (standards / cable databases).
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    // Kotlin libraries
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.collections.immutable)

    // Images
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)

    // Instrumented tests
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    kspAndroidTest(libs.hilt.compiler)

    // Debug tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
