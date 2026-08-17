package com.kemalurekli.electricalcalculator.features.forum.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kemalurekli.electricalcalculator.BuildConfig
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject

/** A Google ID token and the raw nonce it was requested with. */
data class GoogleCredential(val idToken: String, val rawNonce: String)

/**
 * Asks Google for an ID token, showing the account sheet.
 *
 * Kept out of the repository because this needs an Activity context and the
 * repository must not: the token exchange is then an ordinary suspend call that
 * a test can drive without any UI at all.
 */
class GoogleCredentialProvider @Inject constructor() {

    /**
     * Whether this build could sign anyone in.
     *
     * A checkout without the web client id can still read the forum; it simply
     * must not offer a button that opens an empty sheet.
     */
    val isConfigured: Boolean get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    /**
     * @param filterByAuthorizedAccounts show only accounts that have signed in
     *   here before. The first attempt asks for those, because returning
     *   readers are the common case and the sheet is then one tap; if there are
     *   none, the caller retries with `false` to offer every account on the
     *   device. Asking for all of them up front turns a one-tap return into a
     *   full account chooser every time.
     */
    suspend fun requestIdToken(
        activityContext: Context,
        filterByAuthorizedAccounts: Boolean = true,
    ): Result<GoogleCredential> {
        if (!isConfigured) {
            return Result.failure(NotConfiguredException())
        }

        // Google embeds the hash in the token; Supabase compares it against the
        // raw value. Sending the same string to both fails every time.
        val rawNonce = SecureRandom().let { random ->
            ByteArray(NONCE_BYTES).also(random::nextBytes)
        }.joinToString("") { "%02x".format(it) }
        val hashedNonce = MessageDigest.getInstance("SHA-256")
            .digest(rawNonce.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()

        return runCatching {
            val response = CredentialManager.create(activityContext).getCredential(
                context = activityContext,
                request = GetCredentialRequest.Builder().addCredentialOption(option).build(),
            )
            val token = GoogleIdTokenCredential
                .createFrom(response.credential.data)
                .idToken
            GoogleCredential(idToken = token, rawNonce = rawNonce)
        }
    }

    class NotConfiguredException : IllegalStateException("No Google web client id in this build")

    private companion object {
        const val NONCE_BYTES = 16
    }
}

/** Maps a Credential Manager throwable onto what the UI should say about it. */
fun Throwable.toGoogleSignInFailure(): ForumAuthFailure = when (this) {
    // Dismissing the sheet is a decision, not a fault. It gets no message.
    is GetCredentialCancellationException -> ForumAuthFailure.CANCELLED
    is NoCredentialException -> ForumAuthFailure.NO_ACCOUNT
    is GoogleCredentialProvider.NotConfiguredException -> ForumAuthFailure.NOT_CONFIGURED
    is IOException -> ForumAuthFailure.NO_CONNECTION
    else -> ForumAuthFailure.UNKNOWN
}
