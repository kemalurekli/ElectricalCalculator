plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

/**
 * The app's local store: history, favourites, projects and circuit tests.
 *
 * Room has supported Kotlin Multiplatform since 2.7, so this is a migration
 * rather than a rewrite — the entities, the DAOs and the four exported schemas
 * come across untouched. What changes is how the database is opened: Android
 * hands Room a `Context`, iOS hands it a file path, and both now go through a
 * SQLite driver that Room used to supply implicitly.
 */
kotlin {
    androidLibrary {
        namespace = "com.kemalurekli.electricalcalculator.core.database"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            // `api`, not `implementation`: `ElecToolkitDatabase` extends
            // `RoomDatabase`, so anyone holding one needs that supertype on
            // their classpath to call anything on it.
            api(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
            // StringMapConverter stores the input and result maps as JSON.
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

/**
 * Schemas stay where they were, beside the app, so the four already-committed
 * versions keep their history and the migration tests keep finding them.
 */
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
