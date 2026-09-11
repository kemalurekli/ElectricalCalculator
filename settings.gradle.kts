pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Electrical Calculator"

// `:app` is still the Android application and still holds most of the code.
// Modules are carved out of it one at a time, each one moving to Kotlin
// Multiplatform as it goes, so that Android keeps building at every step.
include(":app")
include(":core:common")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":core:billing")
include(":core:backend")
include(":core:feedback")
include(":core:data")
include(":core:designsystem")
include(":core:document")
include(":core:vision")
include(":core:navigation")
include(":shell")
include(":feature:converter")
include(":feature:history")
include(":feature:glossary")
include(":feature:fieldnotes")
include(":feature:references")
include(":feature:calculators")
include(":feature:theory")
include(":feature:favorites")
include(":feature:home")
include(":feature:projects")
include(":feature:forum")
include(":feature:settings")
include(":feature:more")
include(":feature:pro")

// The iOS composition root — what MainActivity is on Android. Xcode links the
// framework this produces and calls one function in it.
include(":iosEntry")
