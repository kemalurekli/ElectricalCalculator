plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * Where the generated `Res` class lives, and therefore the directory the
 * resource reader looks under at runtime.
 */
val resourcePackage = "com.kemalurekli.electricalcalculator.core.navigation.generated.resources"

/**
 * Puts the Compose resources where Android can find them at runtime.
 *
 * The Compose plugin creates `assemble<Target>MainResources` tasks for the
 * native targets and leaves Android to AGP's asset pipeline — which AGP 9's new
 * Kotlin Multiplatform library plugin does not wire up. The result was an APK
 * that built and installed cleanly and then threw `MissingResourceException`
 * from the first string it tried to read.
 *
 * The layout matters as much as the presence: the reader wants
 * `composeResources/<resource package>/…`, and copying the prepared directory
 * in flat is not enough — that was the first attempt, and it landed the files
 * one directory too high, which looks correct in a listing and fails at
 * runtime just the same.
 *
 * Declared above `kotlin { }` because a build script runs top to bottom, and
 * passed to `srcDir` as a task provider rather than a path so that packaging
 * depends on generation. A bare directory works until the first clean build.
 */
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
 * Where the app can go, and the bar that takes you there.
 *
 * Only the declarations: the route types, the five top-level destinations, and
 * the adaptive shell that renders them. The graph itself — `ElecNavHost` —
 * stays in `:app`, because it names every feature's entry composable and a
 * module that did that would depend on all of them.
 *
 * Split out so `:feature:home` and `:feature:more` can say where a tap goes
 * without depending on the graph that resolves it.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.navigation"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.configure {
            // The copy's *parent* is the classpath root; the task writes the
            // package directory beneath it.
            resources.srcDir(
                packageComposeResourcesForAndroid.map {
                    it.destinationDir.parentFile.parentFile
                },
            )
        }

        commonMain.dependencies {
            api(project(":core:designsystem"))
            api(project(":core:domain"))
            api(libs.navigation.compose)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            // `api`, not `implementation`: the generated `Res` class is part of
            // this module's public surface while :app still reads strings from
            // it, so consumers need the accessor types on their classpath too.
            api(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.compose.adaptive)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.collections.immutable)
        }
    }
}


compose.resources {
    publicResClass = true
    packageOfResClass = resourcePackage
    generateResClass = auto
}
