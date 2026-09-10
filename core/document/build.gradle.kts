plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

/**
 * Drawing a PDF, and nothing about what is in one.
 *
 * The schedule renderer was written inside `:feature:projects`, which was right
 * while a schedule was the only document the app produced. It is not any more:
 * a calculation is about to become one too, and `:feature:calculators` must not
 * depend on `:feature:projects` to draw it.
 *
 * So the two platform renderers and the vocabulary they consume live here, and
 * each feature keeps its own layout — the part with decisions in it. What moves
 * is the part with none: a loop over operations that puts text at coordinates.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.document"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
