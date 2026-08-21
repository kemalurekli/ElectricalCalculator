// Top-level build file. Plugins are declared here without applying them so that
// every module can opt in via `alias(libs.plugins.*)` against a single shared version.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}

/*
 * Pins kotlinx-datetime to the version supabase-kt was built against.
 *
 * Compose Material3 1.9.0 pulls 0.7.1, which wins by conflict resolution and
 * takes `kotlinx.datetime.serializers.InstantIso8601Serializer` with it — the
 * class moved when `kotlinx.datetime.Instant` became an alias for
 * `kotlin.time.Instant`. supabase-kt 3.1.4 still names that serializer, and on
 * Kotlin/Native a name it cannot resolve is a link-time absence rather than a
 * compile error.
 *
 * What that looked like: signing in with an emailed code returned HTTP 200 and
 * then threw while reading the session out of the response, on iOS only. The
 * JVM finds the serializer reflectively whatever the version, so Android was
 * never affected and the failure read as a rejected code.
 *
 * Remove this when supabase-kt is on a release built against 0.7.
 */
allprojects {
    configurations.configureEach {
        resolutionStrategy {
            force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
        }
    }
}
