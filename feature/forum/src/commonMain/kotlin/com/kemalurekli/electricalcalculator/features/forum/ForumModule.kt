package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.features.forum.data.ForumAuthRepositoryImpl
import com.kemalurekli.electricalcalculator.features.forum.data.ForumRepositoryImpl
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumBackend
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumAccountViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumBlockedViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumCategoriesViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumComposeThreadViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumProfileViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumRulesViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumSessionViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadsViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * The forum's screens and the two repositories under them.
 *
 * The client is built here rather than by the platform, now that `ForumConfig`
 * carries the URL and the anon key into shared code. supabase-kt picks a Ktor
 * engine per target, so there is nothing left for either side to supply.
 *
 * It used to be handed an OkHttp client so the app had one connection pool
 * across Retrofit and Ktor. Retrofit turned out to have no callers — the remote
 * data sources it was provided for were never written — so there is nothing
 * left to share a pool with.
 */
val forumModule: Module = module {
    single<ForumBackend> {
        val url = ForumConfig.SUPABASE_URL
        val key = ForumConfig.SUPABASE_ANON_KEY
        if (url.isBlank() || key.isBlank()) {
            // Deliberately not an exception. See ForumBackend.
            ForumBackend.NotConfigured
        } else {
            ForumBackend.Available(
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

    single<ForumRepository> { ForumRepositoryImpl(get(), Dispatchers.Default) }
    single<ForumAuthRepository> { ForumAuthRepositoryImpl(get(), Dispatchers.Default) }

    viewModelOf(::ForumCategoriesViewModel)
    viewModelOf(::ForumThreadsViewModel)
    viewModelOf(::ForumThreadViewModel)
    viewModelOf(::ForumComposeThreadViewModel)
    viewModelOf(::ForumProfileViewModel)
    viewModelOf(::ForumAccountViewModel)
    viewModelOf(::ForumSessionViewModel)
    viewModelOf(::ForumBlockedViewModel)
    viewModelOf(::ForumRulesViewModel)
}
