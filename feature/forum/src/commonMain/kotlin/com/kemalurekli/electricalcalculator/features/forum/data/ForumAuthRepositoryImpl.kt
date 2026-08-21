package com.kemalurekli.electricalcalculator.features.forum.data

import com.kemalurekli.electricalcalculator.features.forum.auth.SignInProvider
import com.kemalurekli.electricalcalculator.features.forum.auth.SignInCredential
import io.github.jan.supabase.auth.providers.Apple
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumBackend
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthRepository
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
import kotlin.time.Instant
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.SupabaseClient

class ForumAuthRepositoryImpl(
    private val backend: ForumBackend,
    private val ioDispatcher: CoroutineDispatcher,
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

    override suspend fun signIn(credential: SignInCredential): Result<Unit> {
        val client = (backend as? ForumBackend.Available)?.client
            ?: return Result.failure(IllegalStateException("Supabase is not configured"))

        return runCatching {
            withContext(ioDispatcher) {
                client.auth.signInWith(IDToken) {
                    this.idToken = credential.idToken
                    this.provider = when (credential.provider) {
                        SignInProvider.GOOGLE -> Google
                        SignInProvider.APPLE -> Apple
                    }
                    // Both providers hash the nonce they were given; Supabase
                    // compares against the raw one. Sending the hash back
                    // fails every time, which is a confusing way to learn it.
                    this.nonce = credential.rawNonce
                }
            }
        }
    }

    override suspend fun requestEmailCode(email: String): Result<Unit> =
        withClient { client ->
            client.auth.signInWith(OTP) {
                this.email = email
                // A first-time address becomes an account. Without this the
                // forum could never gain a member who had not already joined
                // some other way.
                createUser = true
            }
        }

    override suspend fun signInWithEmailCode(email: String, code: String): Result<Unit> =
        withClient { client ->
            // Three token types, and the app cannot know which one it holds.
            //
            // Supabase mints a different kind depending on which email it sent,
            // and rejects the wrong kind as `otp_expired` — a code that is
            // minutes old reported as stale, which sends everyone looking at
            // the clock. A returning reader gets the "Magic link or OTP"
            // template and a `magiclink` token; an address the project has
            // never seen gets "Confirm sign up" and a `signup` token; `email`
            // is what the documentation says covers both and in practice
            // covers neither of them here.
            //
            // Asking the server which one first would mean a round trip whose
            // only product is the knowledge that an address is registered,
            // which is not worth telling whoever asks. So they are tried in
            // order of how often each is right, returning readers first.
            var refusal: Throwable? = null
            for (type in EMAIL_OTP_TYPES) {
                val attempt = runCatching {
                    client.auth.verifyEmailOtp(type = type, email = email, token = code)
                }
                if (attempt.isSuccess) return@withClient
                // The FIRST refusal, not the last. If the token was the kind
                // the first attempt asked for, its failure is the real one and
                // the later types are refusing a token they were never given.
                if (refusal == null) refusal = attempt.exceptionOrNull()
            }
            throw refusal ?: IllegalStateException("no OTP type accepted the code")
        }

    /**
     * Runs [block] against the client, or fails the way the screen can read.
     *
     * Shared by the two calls above because they fail in the same vocabulary,
     * and because mapping a 429 to "something went wrong" would hide the one
     * failure here whose remedy is to wait.
     */
    private suspend fun withClient(block: suspend (SupabaseClient) -> Unit): Result<Unit> {
        val client = (backend as? ForumBackend.Available)?.client
            ?: return Result.failure(IllegalStateException("Supabase is not configured"))

        return runCatching { withContext(ioDispatcher) { block(client) } }
    }

    override suspend fun signOut() {
        val client = (backend as? ForumBackend.Available)?.client ?: return
        runCatching { withContext(ioDispatcher) { client.auth.signOut() } }
            
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
        }.getOrNull()
    }

    /** Same shape as [ForumRepositoryImpl.query]; see the reasoning there. */
    private suspend fun <T> query(block: suspend () -> T): ForumResult<T> {
        if (backend !is ForumBackend.Available) {
            return ForumResult.Failure(ForumFailure.NOT_CONFIGURED)
        }
        return try {
            ForumResult.Success(withContext(ioDispatcher) { block() })
        } catch (e: kotlinx.io.IOException) {
            ForumResult.Failure(ForumFailure.NO_CONNECTION)
        } catch (e: Exception) {
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

/**
 * The kinds of emailed token, most likely first.
 *
 * Every miss costs a verification attempt against a limit of 360 an hour, so
 * the order is the frequency: a member signs up once and signs in for as long
 * as they stay.
 */
private val EMAIL_OTP_TYPES = listOf(
    OtpType.Email.MAGIC_LINK,
    OtpType.Email.SIGNUP,
    OtpType.Email.EMAIL,
)
