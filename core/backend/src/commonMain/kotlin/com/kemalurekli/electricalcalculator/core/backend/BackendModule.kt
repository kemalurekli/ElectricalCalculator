package com.kemalurekli.electricalcalculator.core.backend

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The client, built once for the whole app.
 *
 * A `single`, and that is the point rather than a convention: [AppBackend] is
 * where the session lives, and a second client would hold a second session in
 * the same encrypted store — two things refreshing one token.
 */
val backendModule: Module = module {
    single<AppBackend> {
        val url = BackendConfig.SUPABASE_URL
        val key = BackendConfig.SUPABASE_ANON_KEY
        if (url.isBlank() || key.isBlank()) {
            // Deliberately not an exception. See AppBackend.
            AppBackend.NotConfigured
        } else {
            AppBackend.Available(
                createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
                    install(Postgrest)
                    // Auth keeps the session in encrypted storage and refreshes
                    // the access token on its own. Nothing here handles a
                    // password: sign-in is an ID token minted by the provider
                    // and checked by Supabase, so the app never sees a
                    // credential worth stealing.
                    install(Auth)
                },
            )
        }
    }
}
