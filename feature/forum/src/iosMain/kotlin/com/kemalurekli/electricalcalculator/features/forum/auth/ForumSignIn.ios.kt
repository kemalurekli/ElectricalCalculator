package com.kemalurekli.electricalcalculator.features.forum.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationErrorFailed
import platform.AuthenticationServices.ASAuthorizationErrorCanceled
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.darwin.NSObject

/**
 * Sign in with Apple.
 *
 * ### Why Apple and not Google
 *
 * App Store guideline 4.8 requires an equivalent option beside any third-party
 * sign-in, so an iOS build offering only Google would be rejected. Offering
 * only Apple satisfies it outright and needs no OAuth client of its own — the
 * entitlement comes from the provisioning profile — so it is both the smaller
 * build and the one that cannot be refused.
 *
 * The two platforms therefore sign in through different providers. That is
 * visible to the reader and worth being honest about: an account created here
 * is an Apple account, and signing in on Android with Google produces a
 * different one. Linking them is a Supabase-side job and is not done.
 */
@Composable
actual fun rememberForumSignIn(): ForumSignIn = remember { AppleSignIn() }

private class AppleSignIn : ForumSignIn {

    override val provider: SignInProvider = SignInProvider.APPLE

    /** Granted by the provisioning profile; there is no key to be missing. */
    override val isConfigured: Boolean = true

    /**
     * [onlyPreviousAccounts] is ignored. Apple shows one sheet and decides for
     * itself whether the reader has used this app before — there is no
     * narrow-then-widen retry to make, which is why the parameter has a default
     * and Android is the only caller that passes it.
     */
    override suspend fun requestIdToken(onlyPreviousAccounts: Boolean): Result<SignInCredential> {
        val rawNonce = randomNonce()
        val request = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            // Apple embeds the hash in the token; Supabase compares against the
            // raw value. Sending the same string to both fails every time.
            nonce = rawNonce.sha256Hex()
        }

        return suspendCoroutine { continuation ->
            val delegate = Delegate(rawNonce) { continuation.resume(it) }
            ASAuthorizationController(listOf(request)).apply {
                // Held by the controller only weakly, so the delegate has to be
                // kept alive by something else for the length of the sheet —
                // the controller itself, here.
                this.delegate = delegate
                presentationContextProvider = delegate
                performRequests()
            }
        }
    }

    private class Delegate(
        private val rawNonce: String,
        private val onResult: (Result<SignInCredential>) -> Unit,
    ) : NSObject(),
        ASAuthorizationControllerDelegateProtocol,
        ASAuthorizationControllerPresentationContextProvidingProtocol {

        @OptIn(ExperimentalForeignApi::class)
        override fun authorizationController(
            controller: ASAuthorizationController,
            didCompleteWithAuthorization: ASAuthorization,
        ) {
            val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
            val token = credential?.identityToken?.let { data ->
                NSString.create(data = data, encoding = platform.Foundation.NSUTF8StringEncoding)?.toString()
            }
            onResult(
                if (token.isNullOrBlank()) {
                    Result.failure(IllegalStateException("Apple returned no identity token"))
                } else {
                    Result.success(SignInCredential(SignInProvider.APPLE, token, rawNonce))
                },
            )
        }

        override fun authorizationController(
            controller: ASAuthorizationController,
            didCompleteWithError: NSError,
        ) {
            onResult(Result.failure(AppleSignInException(didCompleteWithError)))
        }

        override fun presentationAnchorForAuthorizationController(
            controller: ASAuthorizationController,
        ): ASPresentationAnchor = UIApplication.sharedApplication.keyWindow
            ?: error("No window to present the Apple sign-in sheet from")
    }
}

/** Carries the Apple error code so [toSignInFailure] can read it. */
private class AppleSignInException(val error: NSError) : Exception(error.localizedDescription)

actual fun Throwable.toSignInFailure(): ForumAuthFailure = when {
    this !is AppleSignInException -> ForumAuthFailure.UNKNOWN
    error.code == ASAuthorizationErrorCanceled -> ForumAuthFailure.CANCELLED
    // `.failed` and `.notHandled` both mean the sheet could not complete, which
    // from the app's side is indistinguishable from being offline.
    error.code == ASAuthorizationErrorFailed -> ForumAuthFailure.NO_CONNECTION
    else -> ForumAuthFailure.UNKNOWN
}

/**
 * A random nonce, hex-encoded.
 *
 * `Random.nextBytes` rather than `SecRandomCopyBytes`: the nonce is a replay
 * guard between Apple and Supabase, not a key, and the default source on
 * Kotlin/Native is seeded from the system.
 */
private fun randomNonce(): String =
    Random.nextBytes(NONCE_BYTES).joinToString("") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
    }

@OptIn(ExperimentalForeignApi::class)
private fun String.sha256Hex(): String = memScoped {
    val bytes = encodeToByteArray()
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    bytes.usePinned { pinned ->
        digest.usePinned { out ->
            CC_SHA256(pinned.addressOf(0), bytes.size.toUInt(), out.addressOf(0))
        }
    }
    digest.joinToString("") { it.toInt().toString(16).padStart(2, '0') }
}

private const val NONCE_BYTES = 32
