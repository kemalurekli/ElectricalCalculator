import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val resourcePackage = "com.kemalurekli.electricalcalculator.feature.forum.generated.resources"

/** See core/designsystem for why Android needs this copied by hand. */
val packageComposeResourcesForAndroid = tasks.register<Copy>("packageComposeResourcesForAndroid") {
    from(
        layout.buildDirectory.dir(
            "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources",
        ),
    )
    into(
        layout.buildDirectory.dir(
            "generated/compose/androidResources/composeResources/$resourcePackage",
        ),
    )
    dependsOn("prepareComposeResourcesTaskForCommonMain")
}

/**
 * Field notes — the pattern the glossary settled, applied a second time.
 *
 * A generated catalogue, a `StringResource` per entry, a search over resolved
 * text. The interesting thing about this module is how little there is to say
 * about it: the shape was worked out next door, and the whole move is the
 * generator retargeted and the strings decoded. That is what it should look
 * like from here on.
 *
 * It depends on `:feature:glossary` because a note can point at a term.
 */
/**
 * The forum's backend settings, generated into shared code.
 *
 * `:app` reads the same three keys into `BuildConfig`, which is an Android
 * build artefact and therefore useless to the iOS half of this module. They are
 * emitted as a plain Kotlin object instead, so both platforms read one source.
 *
 * ### What is in here and what is not
 *
 * The anon key is public by design — it identifies the project and row-level
 * security is what protects the data — and the Google *web* client id is public
 * for the same reason it is already in the shipped APK. The `service_role` key
 * is not here, has never been in this repository, and must never be: it
 * bypasses RLS entirely.
 *
 * Missing values are empty strings rather than a build failure. A checkout
 * without `local.properties` — a fork, a CI runner, anyone working on the
 * calculators — gets an app that starts and a forum that says it is not
 * configured.
 */
val forumProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun forumProperty(key: String): String = forumProperties.getProperty(key).orEmpty()

val generateForumConfig = tasks.register("generateForumConfig") {
    val output = layout.buildDirectory.dir("generated/forumConfig/kotlin")
    val url = forumProperty("supabase.url")
    val anonKey = forumProperty("supabase.anonKey")
    val googleWebClientId = forumProperty("supabase.googleWebClientId")
    outputs.dir(output)
    doLast {
        val directory = output.get().asFile
            .resolve("com/kemalurekli/electricalcalculator/features/forum/domain")
        directory.mkdirs()
        directory.resolve("ForumConfig.kt").writeText(
            """
            package com.kemalurekli.electricalcalculator.features.forum.domain

            /** GENERATED from `local.properties`; see feature/forum/build.gradle.kts. */
            object ForumConfig {
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
        namespace = "com.kemalurekli.electricalcalculator.feature.forum"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        // The conversion tables are the app's most transcribed data; they run
        // on both platforms, not just the one that happens to be quicker.
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.configure {
            resources.srcDir(
                packageComposeResourcesForAndroid.map { it.destinationDir.parentFile.parentFile },
            )
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.turbine)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            // Ktor's engine for this target. It used to come from `:app`, which
            // worked until the forum moved out and iOS had none — the app built,
            // installed, and died the first time anyone opened the Forum tab.
            implementation(libs.ktor.client.okhttp)

            // Credential Manager and the Google ID helper: the Android half of
            // `ForumSignIn`, and the only thing in this module that is not
            // multiplatform.
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.google.identity.googleid)
        }

        commonMain.configure { kotlin.srcDir(generateForumConfig) }

        iosMain.dependencies {
            // The counterpart of OkHttp above. Darwin is the only engine on
            // this platform and NSURLSession is what it wraps.
            implementation(libs.ktor.client.darwin)
        }

        commonMain.dependencies {
            api(project(":core:designsystem"))
            api(project(":core:data"))
            api(project(":core:navigation"))
            // Every shelf a pin can come from.

            implementation(libs.lifecycle.viewmodel.savedstate)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            api(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.collections.immutable)

            // supabase-kt is Kotlin Multiplatform; its Ktor transport picks an
            // engine per target, which is why this is the one network stack in
            // the app that needed no seam.
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = resourcePackage
    generateResClass = auto
}
