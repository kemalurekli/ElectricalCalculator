import java.util.Properties

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/**
 * Supabase connection details, read from `local.properties`.
 *
 * The anon key is public by design — row level security is what protects the
 * data, not the key's secrecy — but it still stays out of version control so
 * the repository can be shared and so debug and release can point at different
 * projects. A missing value yields an empty string rather than failing the
 * build: the app must still compile and run for everything that is not the
 * forum.
 */
val supabaseProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun supabaseProperty(key: String): String = supabaseProperties.getProperty(key).orEmpty()

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

        buildConfigField("String", "SUPABASE_URL", "\"${supabaseProperty("supabase.url")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${supabaseProperty("supabase.anonKey")}\"")
        // The *web* OAuth client id, not the Android one. Credential Manager
        // sends it as the server client id, and Supabase checks the token was
        // minted for it; the Android client only ties the SHA-1 to the package.
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${supabaseProperty("supabase.googleWebClientId")}\"",
        )
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

// MigrationTestHelper reads the exported schemas from the test APK's assets. The
// schemas now live with the database module that generates them, so this points
// there rather than at a second copy that would drift. Without this a migration
// test fails with "cannot find the schema file" rather than with anything about
// the migration.
android.sourceSets.getByName("androidTest").assets.srcDir(
    rootProject.file("core/database/schemas"),
)

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
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:converter"))
    implementation(project(":feature:history"))
    implementation(project(":feature:glossary"))
    implementation(project(":feature:fieldnotes"))
    implementation(libs.androidx.compose.material3.navigation.suite)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Dependency injection. Hilt still wires :app; Koin wires the modules
    // that have left it. The two coexist until the migration finishes.
    implementation(libs.hilt.android)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Forum backend
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.identity.googleid)
    implementation(libs.ktor.client.okhttp)

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
