package com.kemalurekli.electricalcalculator.core.backend

import io.github.jan.supabase.SupabaseClient

/**
 * Whether this build has a backend at all.
 *
 * A checkout without `local.properties`, a CI runner, or anyone who has cloned
 * the repository to work on the calculators has no Supabase credentials. That
 * must not stop the app from starting: everything except the forum and the
 * report button is offline and has nothing to do with a server.
 *
 * So the absence of configuration is a state the app carries rather than an
 * error it throws. The screens that need it read it and say so plainly; the
 * other twenty features never ask.
 *
 * It was `ForumBackend` while the forum was the only thing on the other end of
 * a wire. Error reports are filed through the same client — deliberately, so
 * that a reader who is signed in to the forum is signed in when they report a
 * mistake, and so that one encrypted token store has one owner.
 */
sealed interface AppBackend {

    data class Available(val client: SupabaseClient) : AppBackend

    data object NotConfigured : AppBackend
}
