
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

        iosMain.dependencies {
            // The counterpart of OkHttp above. Darwin is the only engine on
            // this platform and NSURLSession is what it wraps.
            implementation(libs.ktor.client.darwin)
        }

        commonMain.dependencies {
            api(project(":core:designsystem"))
            api(project(":core:backend"))
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

            implementation(libs.kotlinx.serialization.json)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = resourcePackage
    generateResClass = auto
}
