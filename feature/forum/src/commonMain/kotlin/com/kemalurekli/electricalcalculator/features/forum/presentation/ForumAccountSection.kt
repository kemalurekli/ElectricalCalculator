package com.kemalurekli.electricalcalculator.features.forum.presentation

import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_failed
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_failed_invalid_code
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_failed_invalid_email
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_failed_no_account
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_failed_no_account_apple
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_failed_offline
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_failed_too_many
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_sign_in_failed_unconfigured
import com.kemalurekli.electricalcalculator.features.forum.auth.SignInProvider
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure
import org.jetbrains.compose.resources.StringResource

/**
 * The message for a failed sign-in, or null when there is nothing to say.
 *
 * Cancelling returns null on purpose. Dismissing the Google sheet is a
 * decision, and reporting it back as an error tells the reader they did
 * something wrong when they did not.
 */
internal fun ForumAuthFailure.message(): StringResource? = when (this) {
    ForumAuthFailure.CANCELLED -> null
    ForumAuthFailure.NO_ACCOUNT -> Res.string.forum_sign_in_failed_no_account
    ForumAuthFailure.NO_CONNECTION -> Res.string.forum_sign_in_failed_offline
    ForumAuthFailure.NOT_CONFIGURED -> Res.string.forum_sign_in_failed_unconfigured
    ForumAuthFailure.INVALID_EMAIL -> Res.string.forum_sign_in_failed_invalid_email
    ForumAuthFailure.INVALID_CODE -> Res.string.forum_sign_in_failed_invalid_code
    ForumAuthFailure.TOO_MANY_REQUESTS -> Res.string.forum_sign_in_failed_too_many
    ForumAuthFailure.UNKNOWN -> Res.string.forum_sign_in_failed
}

/**
 * Credential Manager needs the Activity, not the application context — it has
 * to put a sheet on top of something.
 */
