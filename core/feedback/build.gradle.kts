plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val resourcePackage = "com.kemalurekli.electricalcalculator.core.feedback.generated.resources"

/** See core/designsystem for why Android needs this copied by hand. */
val packageComposeResourcesForAndroid = tasks.register<Sync>("packageComposeResourcesForAndroid") {
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
 * Telling us something is wrong, from the screen it is wrong on.
 *
 * A `:core:` module because every kind of screen has this button — a
 * calculator, a theory topic, a reference table — and none of them should have
 * to depend on each other to get it.
 *
 * It owns its own strings rather than borrowing the design system's: these are
 * sentences about a feature, and `:core:designsystem` holds the words that
 * belong to components ("Retry", "Close").
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.feedback"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.configure {
            resources.srcDir(
                packageComposeResourcesForAndroid.map {
                    it.destinationDir.parentFile.parentFile
                },
            )
        }

        commonMain.dependencies {
            api(project(":core:designsystem"))
            implementation(project(":core:backend"))
            implementation(project(":core:datastore"))
            implementation(project(":core:domain"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            api(compose.components.resources)

            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = resourcePackage
    generateResClass = auto
}
