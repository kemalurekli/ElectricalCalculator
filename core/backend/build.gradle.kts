import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

/**
 * The one Supabase client, and the settings that build it.
 *
 * This used to live in `:feature:forum`, which was true for as long as the
 * forum was the only thing that talked to a server. It is not any more: an
 * error report has to be filed by the same client, because "who sent this" is
 * the session, and two clients would mean two sessions fighting over one
 * encrypted token store.
 *
 * A `:core:` module rather than a feature, on the same reasoning as
 * `:core:billing`: this is state and plumbing that features read. Nothing here
 * knows what a thread or a report is.
 */

val backendProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun backendProperty(key: String): String = backendProperties.getProperty(key).orEmpty()

/**
 * The backend settings, generated into shared code.
 *
 * `:app` reads the same keys into `BuildConfig`, which is an Android build
 * artefact and therefore useless to the iOS half. They are emitted as a plain
 * Kotlin object instead, so both platforms read one source.
 *
 * ### What is in here and what is not
 *
 * The anon key is public by design — it identifies the project, and row level
 * security is what protects the data — and the Google *web* client id is public
 * for the same reason it is already in the shipped APK. The `service_role` key
 * is not here, has never been in this repository, and must never be: it
 * bypasses RLS entirely.
 *
 * Missing values are empty strings rather than a build failure. A checkout
 * without `local.properties` — a fork, a CI runner, anyone working on the
 * calculators — gets an app that starts, a forum that says it is not
 * configured, and a report button that says the same.
 */
val generateBackendConfig = tasks.register("generateBackendConfig") {
    val output = layout.buildDirectory.dir("generated/backendConfig/kotlin")
    val url = backendProperty("supabase.url")
    val anonKey = backendProperty("supabase.anonKey")
    val googleWebClientId = backendProperty("supabase.googleWebClientId")
    // See the same declaration in core/billing: without these the task is up
    // to date whenever its output file exists, and a value changed in
    // `local.properties` is silently ignored from the second build onwards.
    inputs.property("url", url)
    inputs.property("anonKey", anonKey)
    inputs.property("googleWebClientId", googleWebClientId)
    outputs.dir(output)
    doLast {
        val directory = output.get().asFile
            .resolve("com/kemalurekli/electricalcalculator/core/backend")
        directory.mkdirs()
        directory.resolve("BackendConfig.kt").writeText(
            """
            package com.kemalurekli.electricalcalculator.core.backend

            /** GENERATED from `local.properties`; see core/backend/build.gradle.kts. */
            object BackendConfig {
                const val SUPABASE_URL: String = "$url"
                const val SUPABASE_ANON_KEY: String = "$anonKey"
                const val GOOGLE_WEB_CLIENT_ID: String = "$googleWebClientId"
            }
            """.trimIndent() + "\n",
        )
    }
}

kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.backend"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.configure { kotlin.srcDir(generateBackendConfig) }

        commonMain.dependencies {
            // supabase-kt is Kotlin Multiplatform; its Ktor transport picks an
            // engine per target, which is why this is the one network stack in
            // the app that needed no seam.
            api(project.dependencies.platform(libs.supabase.bom))
            api(libs.supabase.postgrest)
            api(libs.supabase.auth)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
