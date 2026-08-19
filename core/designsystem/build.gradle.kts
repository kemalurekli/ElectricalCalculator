plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Where the generated `Res` class lives, and therefore the directory the
 * resource reader looks under at runtime.
 */
val resourcePackage = "com.kemalurekli.electricalcalculator.core.designsystem.generated.resources"

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
 * The app's visual vocabulary: palette, type, spacing, and every shared
 * component.
 *
 * This is the module the whole design-language effort was aimed at. Its theme
 * package already contained no Android API before the port began — the
 * wallpaper-derived colour scheme was the last of it — so what remained was the
 * resource system and the fonts, both of which moved to Compose Resources.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.designsystem"
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
            api(project(":core:common"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            // `api`, not `implementation`: the generated `Res` class is part of
            // this module's public surface while :app still reads strings from
            // it, so consumers need the accessor types on their classpath too.
            api(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.compose.adaptive)
            implementation(libs.kotlinx.collections.immutable)
        }
    }
}

/**
 * The generated `Res` class is public so that `:app` can read the strings that
 * still live here while its own screens are being migrated. Once every feature
 * owns its strings, this can go back to being internal.
 */
compose.resources {
    publicResClass = true
    packageOfResClass = resourcePackage
    generateResClass = auto
}
