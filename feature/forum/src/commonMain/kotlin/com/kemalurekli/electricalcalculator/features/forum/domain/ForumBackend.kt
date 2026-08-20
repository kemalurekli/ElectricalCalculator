package com.kemalurekli.electricalcalculator.features.forum.domain

import io.github.jan.supabase.SupabaseClient

/**
 * Whether this build has a forum backend at all.
 *
 * A checkout without `local.properties`, a CI runner, or anyone who has cloned
 * the repository to work on the calculators has no Supabase credentials. That
 * must not stop the app from starting: everything except the forum is offline
 * and has nothing to do with the backend.
 *
 * So the absence of configuration is a state the app carries rather than an
 * error it throws. The forum screens read it and say so plainly; the other
 * twenty features never ask.
 *
 * iOS is currently `NotConfigured` for a second reason: the client is built in
 * `:app` from `BuildConfig`, and there is no equivalent on that side yet.
 */
sealed interface ForumBackend {

    data class Available(val client: SupabaseClient) : ForumBackend

    data object NotConfigured : ForumBackend
}
