package com.kemalurekli.electricalcalculator.features.forum.auth

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import android.content.ContextWrapper
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kemalurekli.electricalcalculator.core.backend.BackendConfig
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom

/** A Google ID token and the raw nonce it was requested with. */
/**
 * Asks Google for an ID token, showing the account sheet.
 *
 * Kept out of the repository because this needs an Activity context and the
 * repository must not: the token exchange is then an ordinary suspend call that
 * a test can drive without any UI at all.
 */
private class GoogleCredentialProvider(private val activity: Activity) : ForumSignIn {

    /**
     * Whether this build could sign anyone in.
     *
     * A checkout without the web client id can still read the forum; it simply
     * must not offer a button that opens an empty sheet.
     */
    override val provider: SignInProvider = SignInProvider.GOOGLE

    override val isConfigured: Boolean get() = BackendConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    /**
     * @param onlyPreviousAccounts show only accounts that have signed in
     *   here before. The first attempt asks for those, because returning
     *   readers are the common case and the sheet is then one tap; if there are
     *   none, the caller retries with `false` to offer every account on the
     *   device. Asking for all of them up front turns a one-tap return into a
     *   full account chooser every time.
     */
    override suspend fun requestIdToken(
        onlyPreviousAccounts: Boolean,
    ): Result<SignInCredential> {
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
            .setFilterByAuthorizedAccounts(onlyPreviousAccounts)
            .setServerClientId(BackendConfig.GOOGLE_WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()

        return runCatching {
            val response = CredentialManager.create(activity).getCredential(
                context = activity,
                request = GetCredentialRequest.Builder().addCredentialOption(option).build(),
            )
            val token = GoogleIdTokenCredential
                .createFrom(response.credential.data)
                .idToken
            SignInCredential(SignInProvider.GOOGLE, idToken = token, rawNonce = rawNonce)
        }.onFailure { failure ->
            // Credential Manager reports several quite different problems as
            // the same exception type, and the reason is only ever in the
            // message: "no credentials available" and "Developer console is not
            // set up correctly" arrive as one `NoCredentialException`.
            //
            // This block used to be empty under a comment claiming it logged,
            // so a sign-in that failed on a real device left no trace anywhere.
            // Diagnosing one then meant a USB cable and Google Play services'
            // own log, which is not a thing to ask of somebody who has already
            // hit a broken button.
            Log.w(TAG, "Google sign-in failed: ${failure::class.simpleName}", failure)
        }
    }

    class NotConfiguredException : IllegalStateException("No Google web client id in this build")

    private companion object {
        const val TAG = "GoogleCredentials"
        const val NONCE_BYTES = 16
    }
}

/** Maps a Credential Manager throwable onto what the UI should say about it. */
actual fun Throwable.toSignInFailure(): ForumAuthFailure = when (this) {
    // Dismissing the sheet is a decision, not a fault. It gets no message.
    is GetCredentialCancellationException -> ForumAuthFailure.CANCELLED
    // Credential Manager says "no credentials available" both when the device
    // genuinely has no Google account and when this build is not registered for
    // OAuth at all — the real reason is only in the log, never in the type. So
    // the message this maps to must not assert either one.
    is NoCredentialException -> ForumAuthFailure.NO_ACCOUNT
    is GoogleCredentialProvider.NotConfiguredException -> ForumAuthFailure.NOT_CONFIGURED
    is IOException -> ForumAuthFailure.NO_CONNECTION
    else -> ForumAuthFailure.UNKNOWN
}

/**
 * Credential Manager needs the Activity, not the application context: it shows
 * a sheet over what is on screen. Compose hands out a `Context` that may be a
 * wrapper around one, so it is unwrapped here rather than at the call site —
 * this is the only place that cares.
 */
@Composable
actual fun rememberForumSignIn(): ForumSignIn {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    return remember(activity) {
        activity?.let(::GoogleCredentialProvider) ?: UnavailableSignIn
    }
}

/** No Activity to present from, which should not happen and must not crash. */
private object UnavailableSignIn : ForumSignIn {
    override val provider: SignInProvider = SignInProvider.GOOGLE
    override val isConfigured: Boolean = false

    override suspend fun requestIdToken(onlyPreviousAccounts: Boolean): Result<SignInCredential> =
        Result.failure(IllegalStateException("No activity to present the sign-in sheet from"))
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
