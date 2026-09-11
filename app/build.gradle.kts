import java.util.Properties

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

/**
 * Supabase connection details, read from `local.properties`.
 *
 * The anon key is public by design — row level security is what protects the
 * data, not the key's secrecy — but it still stays out of version control so
 * the repository can be shared and so debug and release can point at different
 * projects. A missing value yields an empty string rather than failing the
 * build: the app must still compile and run for everything that is not the
 * forum.
 */
val supabaseProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun supabaseProperty(key: String): String = supabaseProperties.getProperty(key).orEmpty()

/**
 * The upload key, read from `keystore.properties` at the repository root.
 *
 * A separate file from `local.properties` on purpose. That one holds values
 * that are meant to reach the binary; this one holds a passphrase that is meant
 * to reach nothing, and mixing them makes it a matter of luck which of the two
 * somebody pastes into a chat window. Both are gitignored, and so is the
 * keystore itself.
 *
 * Absent, the release variant falls back to the debug key. That keeps
 * `assembleRelease` verifiable on a checkout with no credentials — the same
 * bargain `checkBillingKey` makes — and `bundleRelease`, the artifact that
 * actually goes to the store, refuses to be built that way.
 *
 *     storeFile=/absolute/path/to/voltageboard.jks
 *     storePassword=...
 *     keyAlias=upload
 *     keyPassword=...
 *
 * `storeFile` may be absolute or relative to the repository root. Absolute and
 * outside the working tree is the better of the two: a key that is not in the
 * directory cannot be committed by a careless `git add -A`, and losing it means
 * never being able to update the app on Play again.
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun keystoreProperty(key: String): String = keystoreProperties.getProperty(key).orEmpty()

/** The signing config built from `keystore.properties`. */
val uploadSigningConfig = "upload"

/** Every value that config needs; all four or none is useful. */
val uploadKeyProperties = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")

val hasUploadKey: Boolean = keystoreProperty("storeFile").isNotBlank() &&
    rootProject.file(keystoreProperty("storeFile")).exists()

android {
    namespace = "com.kemalurekli.electricalcalculator"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        // The identity the stores and the device know the app by. Not the
        // Kotlin package: the `namespace` above and every source folder stay
        // `com.kemalurekli.*`, which nothing outside the build ever sees.
        // Renaming those would touch every file in the project and change
        // nothing a user, a store or RevenueCat can observe.
        //
        // Permanent once either store has accepted a build under it, so it was
        // settled before the first upload rather than after.
        applicationId = "com.mobronic.voltageboard"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "SUPABASE_URL", "\"${supabaseProperty("supabase.url")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${supabaseProperty("supabase.anonKey")}\"")
        // The *web* OAuth client id, not the Android one. Credential Manager
        // sends it as the server client id, and Supabase checks the token was
        // minted for it; the Android client only ties the SHA-1 to the package.
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${supabaseProperty("supabase.googleWebClientId")}\"",
        )
    }

    signingConfigs {
        if (hasUploadKey) {
            create(uploadSigningConfig) {
                storeFile = rootProject.file(keystoreProperty("storeFile"))
                storePassword = keystoreProperty("storePassword")
                keyAlias = keystoreProperty("keyAlias")
                keyPassword = keystoreProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // Generates the en-XA (accented, ~30 % longer) and ar-XB (RTL,
            // mirrored) pseudolocales. Selecting them on a debug build surfaces
            // truncation and layout-direction bugs without waiting for a real
            // translation to expose them.
            isPseudoLocalesEnabled = true
        }
        release {
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // The upload key when the machine has one, the debug key when it
            // does not. Never a build that quietly ships unsigned: Play rejects
            // a debug-signed artifact, and `checkUploadKey` refuses to make one
            // before Play gets the chance to.
            signingConfig = if (hasUploadKey) {
                signingConfigs.getByName(uploadSigningConfig)
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    lint {
        // Correctness and accessibility problems must not reach a build.
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false

        // Dependency versions here are pinned deliberately (Kotlin tracks the
        // version Hilt is built against). Upgrading is a reviewed change, not
        // something a lint run should nag about on every build.
        disable += setOf(
            "NewerVersionAvailable",
            "GradleDependency",
            "AndroidGradlePluginVersion",
            // Every hit is a standard designation — IEC 60364-5-52, NEC 392.22
            // — where the hyphen is part of the number and an en dash would be
            // wrong. The check has no true positives in a text set built out of
            // standard references.
            "TypographyDashes",
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// MigrationTestHelper reads the exported schemas from the test APK's assets. The
// schemas now live with the database module that generates them, so this points
// there rather than at a second copy that would drift. Without this a migration
// test fails with "cannot find the schema file" rather than with anything about
// the migration.
android.sourceSets.getByName("androidTest").assets.srcDir(
    rootProject.file("core/database/schemas"),
)

/**
 * The design system's resource package, which is also the directory the Compose
 * resource reader looks under. Repeated here rather than read from the module
 * because a Gradle build must not reach into another project's script.
 *
 * A guard test pins it: `ElecFontLoadingTest` renders through the real `Res.font`
 * accessors and fails if what comes back is not the bundled face.
 */
val designSystemResourcePackage =
    "com.kemalurekli.electricalcalculator.core.designsystem.generated.resources"

/**
 * Packages the bundled typefaces as Android assets.
 *
 * Every other Compose resource is read through `ResourceReader`, which tries the
 * assets and then falls back to the classloader — so the design system putting
 * its resources on the classpath is enough for strings and drawables. `Font()`
 * is the exception: on Android it never touches the reader, it hands the path
 * straight to `AssetManager`. With nothing in the assets every `Font(Res.font.…)`
 * call failed, silently, and Compose fell back to the platform default. Nothing
 * crashed and nothing was logged; the whole app simply rendered in Roboto —
 * every figure included, in an app whose design language sets figures in a
 * tabular face so that columns line up and a changing result does not shift the
 * digits beside it.
 *
 * It is done here rather than in the design system because AGP 9's Kotlin
 * Multiplatform library variant has no assets to add to: `variant.sources.assets`
 * is null. `:app` is an Android application and has them.
 *
 * The files are read from the design system's source directory rather than its
 * prepared-resources output: the resource pipeline copies fonts through
 * unchanged, and reading the source keeps this off another project's task graph.
 */
abstract class PackageComposeFontAssets : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fonts: DirectoryProperty

    /** The path the runtime asks the asset manager for, minus the file name. */
    @get:Input
    abstract val resourcePackage: Property<String>

    /** Set by AGP: the assets root this task contributes to. */
    @get:OutputDirectory
    abstract val assets: DirectoryProperty

    @get:Inject
    abstract val files: FileSystemOperations

    @TaskAction
    fun copyFonts() {
        files.sync {
            from(fonts)
            into(assets.get().dir("composeResources/${resourcePackage.get()}/font"))
        }
    }
}

val packageComposeFontAssets = tasks.register<PackageComposeFontAssets>("packageComposeFontAssets") {
    fonts.set(rootProject.layout.projectDirectory.dir("core/designsystem/src/commonMain/composeResources/font"))
    resourcePackage.set(designSystemResourcePackage)
}

androidComponents.onVariants { variant ->
    variant.sources.assets?.addGeneratedSourceDirectory(
        packageComposeFontAssets,
        PackageComposeFontAssets::assets,
    )
}

/**
 * A store build with no store key.
 *
 * `BillingBackend.NotConfigured` unlocks everything, which is the right answer
 * for a contributor's checkout and the wrong one for the Play Store. The two
 * are told apart by which artifact is being built: `bundleRelease` is the one
 * that goes to the store, so it is the one that has to have the key.
 *
 * `assembleRelease` is deliberately left alone — it is what the release build
 * is verified with locally, and failing it would mean nobody without the
 * credentials could check that the release variant still links.
 */
val checkBillingKey = tasks.register("checkBillingKey") {
    val key = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }.getProperty("revenuecat.androidKey").orEmpty()
    doLast {
        check(key.isNotBlank()) {
            "revenuecat.androidKey is missing from local.properties. A bundle built " +
                "without it ships with every paid feature unlocked; see BillingBackend."
        }
    }
}

/**
 * A store build signed with the debug key.
 *
 * Play rejects it, but only after an upload, a scan and a wait. Failing here
 * costs a second instead, and says which of the four values is missing rather
 * than leaving the developer to read a rejection notice about a certificate.
 *
 * Paired with [checkBillingKey] and drawn on the same line: `assembleRelease`
 * stays buildable by anyone, `bundleRelease` needs the credentials.
 */
val checkUploadKey = tasks.register("checkUploadKey") {
    val configured = hasUploadKey
    val missing = uploadKeyProperties.filter { keystoreProperty(it).isBlank() }
    doLast {
        check(configured) {
            "keystore.properties is missing or names a keystore that is not there. " +
                "A bundle signed with the debug key is rejected by Play; see the " +
                "keystoreProperties doc in this file for the four values it needs."
        }
        check(missing.isEmpty()) {
            "keystore.properties is missing ${missing.joinToString()}."
        }
    }
}

/**
 * Both guards, on the task that actually writes the `.aab`.
 *
 * `bundleRelease` alone was not enough. Android Studio's "Generate Signed
 * Bundle" wizard produced a signed bundle with no store key in it while that
 * was the only hook — the command line refused the same build a minute later,
 * which is the worst possible split: the check appears to work, and the
 * artifact that reaches Play is the one that skipped it.
 *
 * `packageReleaseBundle` is what every one of those paths has to run, so it is
 * where the refusal belongs. `bundleRelease` keeps its copy only so the failure
 * arrives in a second rather than after a full optimised build.
 */
tasks.matching { it.name == "bundleRelease" || it.name == "packageReleaseBundle" }
    .configureEach {
        // The third is the one that reads what actually gets compiled in. The
        // first two read the files a developer edits, which is what makes their
        // messages useful and what makes them insufficient on their own.
        dependsOn(checkBillingKey, checkUploadKey, ":core:billing:checkGeneratedBillingKey")
    }

dependencies {
    // Compose BOM aligns every Compose artifact to one tested version set.
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    // Drives the per-app language picker. On Android 13+ this delegates to the
    // platform LocaleManager; below it, AppCompat provides the backport, which
    // matters because minSdk is 28.
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.adaptive)
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:billing"))
    implementation(project(":core:backend"))
    implementation(project(":core:feedback"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":shell"))
    implementation(project(":feature:converter"))
    implementation(project(":feature:history"))
    implementation(project(":feature:glossary"))
    implementation(project(":feature:fieldnotes"))
    implementation(project(":feature:references"))
    implementation(project(":feature:calculators"))
    implementation(project(":feature:theory"))
    implementation(project(":feature:favorites"))
    implementation(project(":feature:home"))
    implementation(project(":feature:projects"))
    implementation(project(":feature:forum"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:more"))
    implementation(project(":feature:pro"))
    implementation(libs.androidx.compose.material3.navigation.suite)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Dependency injection. Hilt still wires :app; Koin wires the modules
    // that have left it. The two coexist until the migration finishes.
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    // Persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Forum backend
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.identity.googleid)
    implementation(libs.ktor.client.okhttp)

    // Networking — wired up now for future remote modules (standards / cable databases).
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    // Kotlin libraries
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.collections.immutable)

    // Images
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)

    // Instrumented tests
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // Debug tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
