package com.kemalurekli.electricalcalculator.core.data.repository

import android.util.Log
import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
import com.kemalurekli.electricalcalculator.core.data.network.di.ForumBackend
import com.kemalurekli.electricalcalculator.core.domain.repository.ForumAuthRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumFailure
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumProfile
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumResult
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumSession
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException
import kotlin.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForumAuthRepositoryImpl @Inject constructor(
    private val backend: ForumBackend,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ForumAuthRepository {

    /**
     * Supabase's session status, widened into a profile.
     *
     * `Authenticated` only carries the auth user, and the forum's statistics
     * live in `forum_profiles`. Rather than have every screen fetch the profile
     * itself, the fetch happens once here and the session carries the result.
     */
    override val session: Flow<ForumSession> = when (backend) {
        is ForumBackend.NotConfigured -> flowOf(ForumSession.SignedOut)
        is ForumBackend.Available -> flow {
            emit(ForumSession.Unknown)
            backend.client.auth.sessionStatus.collect { status ->
                emit(
                    when (status) {
                        is SessionStatus.Authenticated -> {
                            val id = status.session.user?.id
                            val profile = id?.let { fetchProfile(it) }
                            if (profile == null) ForumSession.SignedOut
                            else ForumSession.SignedIn(profile)
                        }
                        // Supabase reports the pre-restore moment as its own
                        // status. Passing it through as Unknown is what keeps a
                        // sign-in button from appearing for one frame on every
                        // launch by someone who is already signed in.
                        is SessionStatus.Initializing -> ForumSession.Unknown
                        else -> ForumSession.SignedOut
                    },
                )
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String, nonce: String): Result<Unit> {
        val client = (backend as? ForumBackend.Available)?.client
            ?: return Result.failure(IllegalStateException("Supabase is not configured"))

        return runCatching {
            withContext(ioDispatcher) {
                client.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                    // Google hashes the nonce it was given; Supabase compares
                    // against the raw one. Sending the hash back would fail
                    // every time, which is a confusing way to learn this.
                    this.nonce = nonce
                }
            }
        }
    }

    override suspend fun signOut() {
        val client = (backend as? ForumBackend.Available)?.client ?: return
        runCatching { withContext(ioDispatcher) { client.auth.signOut() } }
            .onFailure { Log.w(TAG, "sign-out failed; clearing locally anyway", it) }
    }

    override suspend fun profile(userId: String): ForumResult<ForumProfile> = query {
        fetchProfile(userId) ?: error("no profile row for $userId")
    }

    override suspend fun updateDisplayName(displayName: String): ForumResult<Unit> = query {
        val client = (backend as ForumBackend.Available).client
        val id = client.auth.currentUserOrNull()?.id ?: error("not signed in")
        client.postgrest.from(TABLE_PROFILES)
            .update(buildJsonObject { put("display_name", displayName) }) {
                filter { eq("id", id) }
            }
        Unit
    }

    override suspend fun deleteAccount(): ForumResult<Unit> = query {
        val client = (backend as ForumBackend.Available).client
        // The anon key cannot reach auth.users, and giving it that reach would
        // be far worse than the inconvenience. The server-side function runs as
        // definer and deletes only its own caller.
        client.postgrest.rpc(RPC_DELETE_ACCOUNT)
        client.auth.signOut()
        Unit
    }

    private suspend fun fetchProfile(userId: String): ForumProfile? {
        val client = (backend as? ForumBackend.Available)?.client ?: return null
        return runCatching {
            withContext(ioDispatcher) {
                client.postgrest.from(TABLE_PROFILES)
                    .select(
                        Columns.list(
                            "id",
                            "display_name",
                            "created_at",
                            "post_count",
                            "thanks_received",
                        ),
                    ) { filter { eq("id", userId) } }
                    .decodeSingleOrNull<ProfileDto>()
                    ?.toDomain()
            }
        }.onFailure { Log.w(TAG, "could not read profile $userId", it) }.getOrNull()
    }

    /** Same shape as [ForumRepositoryImpl.query]; see the reasoning there. */
    private suspend fun <T> query(block: suspend () -> T): ForumResult<T> {
        if (backend !is ForumBackend.Available) {
            return ForumResult.Failure(ForumFailure.NOT_CONFIGURED)
        }
        return try {
            ForumResult.Success(withContext(ioDispatcher) { block() })
        } catch (e: IOException) {
            Log.w(TAG, "forum auth call could not reach the server", e)
            ForumResult.Failure(ForumFailure.NO_CONNECTION)
        } catch (e: Exception) {
            Log.e(TAG, "forum auth call failed", e)
            ForumResult.Failure(ForumFailure.SERVER_ERROR)
        }
    }

    private companion object {
        const val TAG = "ForumAuthRepository"
        const val TABLE_PROFILES = "forum_profiles"
        const val RPC_DELETE_ACCOUNT = "forum_delete_account"
    }
}

@Serializable
private data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("post_count") val postCount: Int = 0,
    @SerialName("thanks_received") val thanksReceived: Int = 0,
) {
    fun toDomain() = ForumProfile(
        id = id,
        displayName = displayName,
        joinedAt = Instant.parse(createdAt),
        postCount = postCount,
        thanksReceived = thanksReceived,
    )
}
