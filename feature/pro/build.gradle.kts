plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

val resourcePackage = "com.kemalurekli.electricalcalculator.feature.pro.generated.resources"

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
 * The one screen that asks for money.
 *
 * Separate from `:core:billing` on the same line the rest of the app is drawn:
 * state in core, screens in features. It matters more here than usual, because
 * the screens being gated must not depend on the screen doing the selling —
 * `:feature:projects` reads the entitlement from core and reaches this one by
 * route, so a second gated feature adds a call site and nothing else.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.feature.pro"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
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
            implementation(libs.turbine)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        commonMain.dependencies {
            api(project(":core:designsystem"))
            api(project(":core:billing"))
            api(project(":core:navigation"))

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
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = resourcePackage
    generateResClass = auto
}
