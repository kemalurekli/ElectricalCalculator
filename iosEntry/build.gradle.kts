plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Referenced by Xcode in two places — the framework search path and
 * `-framework` in the linker flags — so it is named once here and copied there
 * by hand. Changing it means changing those two settings too.
 *
 * Declared before it is used: a build script runs top to bottom.
 */
val frameworkName = "ElecToolkitKit"

/**
 * What Xcode links against.
 *
 * On Android the entry point is `MainActivity`, which Android's own build
 * system finds through the manifest. iOS has no such convention: Xcode compiles
 * Swift, and Kotlin has to arrive as a framework binary that the Swift side
 * calls into. This module is that binary, and it holds one function —
 * `MainViewController()` — which the app's Swift shell puts on screen.
 *
 * It depends on the shared modules and adds nothing of its own beyond the
 * entry point, so that "what runs on iOS" and "what runs on Android" stay the
 * same code rather than two implementations that drift.
 *
 * The framework is static: nothing else links it, so there is no reason to pay
 * for a dynamic one, and a static framework needs no embedding step.
 */
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = frameworkName
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:designsystem"))
            implementation(project(":feature:converter"))
            implementation(project(":feature:history"))
            implementation(project(":feature:glossary"))
            implementation(project(":core:data"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.compose.adaptive)
            // The tab bar itself. `androidx.compose.material3:material3-adaptive-
            // navigation-suite`, which :app uses, is Android-only; this is the
            // multiplatform build of the same package, so the shell code reads
            // identically on both sides.
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.material3AdaptiveNavigationSuite)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}
