package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.features.forum.domain.ForumBackend
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The Supabase client can be built on this platform at all.
 *
 * It could not. supabase-kt runs on Ktor and Ktor needs an engine per target;
 * the forum module declared none, and Android only worked because `:app`
 * happened to bring OkHttp. iOS had nothing, so the app built, installed, ran,
 * and died the moment anyone opened the Forum tab — the failure named Ktor, not
 * the forum, and nothing on either side of the port caught it.
 *
 * Constructing the client is the whole check: it is where the engine is
 * resolved. Nothing here talks to a server, and the credentials are made up.
 */
class ForumBackendIosTest {

    @Test
    fun `a client can be created`() {
        val client = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "not-a-real-key",
        ) {
            install(Postgrest)
            install(Auth)
        }

        assertTrue(ForumBackend.Available(client) is ForumBackend)
    }
}
