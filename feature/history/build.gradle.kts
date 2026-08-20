plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

val resourcePackage = "com.kemalurekli.electricalcalculator.feature.history.generated.resources"

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
 * Saved calculations — the first feature that reads real data.
 *
 * The converter proved a screen could be shared; this one proves the stack
 * underneath it can. It goes through the repository, the database and the type
 * converters to put rows on screen, which is every layer moved so far working
 * together rather than each passing its own test.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.feature.history"
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

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        commonMain.dependencies {
            api(project(":core:designsystem"))
            api(project(":core:data"))

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
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = resourcePackage
    generateResClass = auto
}
