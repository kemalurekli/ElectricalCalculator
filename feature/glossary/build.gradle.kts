plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

val resourcePackage = "com.kemalurekli.electricalcalculator.feature.glossary.generated.resources"

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
 * The glossary — and the pattern every catalogue-driven screen will copy.
 *
 * It is the root of the feature dependency tree: field notes, theory and
 * favourites all reach into `glossary.domain`, and nothing reaches into them
 * from below. So it goes first not because it is easy — it is not — but
 * because nothing else can go before it.
 *
 * What it settles is how a catalogue carries text. Until now that was
 * `@StringRes val termRes: Int`, an Android resource id, in 48 places across
 * the app. Here it becomes a `StringResource`, and the generator that writes
 * the catalogue writes the strings beside it. Every catalogue that follows —
 * references, theory, and the 129 calculator files — copies this shape.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.feature.glossary"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        // The conversion tables are the app's most transcribed data; they run
        // on both platforms, not just the one that happens to be quicker.
        // The ViewModel's ordering tests set the JVM default locale to prove
        // the Turkish alphabet is honoured, which only a JVM can do — see
        // `androidHostTest`.
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
            implementation(libs.kotlinx.coroutines.test)
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutines.test)
        }

        commonMain.dependencies {
            api(project(":core:designsystem"))
            api(project(":core:data"))
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
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = resourcePackage
    generateResClass = auto
}
