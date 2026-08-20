package com.kemalurekli.electricalcalculator.core.data.network.di

import com.kemalurekli.electricalcalculator.features.forum.domain.ForumBackend
import android.util.Log
import com.kemalurekli.electricalcalculator.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Whether this build can reach the forum at all.
 *
 * A checkout without `local.properties`, a CI runner, or anyone who has cloned
 * the repository to work on the calculators has no Supabase credentials. That
 * must not stop the app from starting: everything except the forum is offline
 * and has nothing to do with the backend.
 *
 * So the absence of configuration is a state the app carries rather than an
 * error it throws. The forum screens read it and say so plainly; the other
 * twenty features never ask.
 */

/**
 * The Supabase client, kept apart from [NetworkModule].
 *
 * That module provides a `Retrofit.Builder` for the remote data sources the app
 * still plans to have — cable and standards databases. The forum speaks
 * PostgREST through supabase-kt instead, and mixing the two sets of providers
 * in one file would suggest they are alternatives rather than two different
 * jobs.
 *
 * They do share the OkHttp client. supabase-kt runs on Ktor, and Ktor's OkHttp
 * engine can be handed an existing client — so the app has one connection pool
 * and one set of timeouts rather than two stacks that happen to be in the same
 * binary.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    private const val TAG = "SupabaseModule"

    @Provides
    @Singleton
    fun provideForumBackend(okHttpClient: OkHttpClient): ForumBackend {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY

        if (url.isBlank() || key.isBlank()) {
            // Deliberately not an exception. See ForumBackend.
            Log.i(TAG, "Supabase is not configured; the forum will report itself unavailable")
            return ForumBackend.NotConfigured
        }

        return ForumBackend.Available(
            createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
                install(Postgrest)
                // Auth keeps the session in encrypted storage and refreshes the
                // access token on its own. Nothing here handles a password:
                // sign-in is an ID token minted by Google and checked by
                // Supabase, so the app never sees a credential worth stealing.
                install(Auth)
                httpEngine = OkHttp.create { preconfigured = okHttpClient }
            },
        )
    }
}
