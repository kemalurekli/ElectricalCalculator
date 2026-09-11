package com.kemalurekli.electricalcalculator.core.feedback.data

import com.kemalurekli.electricalcalculator.core.backend.AppBackend
import com.kemalurekli.electricalcalculator.core.datastore.UserPreferencesDataSource
import com.kemalurekli.electricalcalculator.core.feedback.domain.AppBuild
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackFailure
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackReport
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackRepository
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackResult
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One insert, and no way back.
 *
 * The table has an insert policy and nothing else, so there is no `read` here
 * to leave unimplemented — a reporter cannot see the queue, their own reports
 * included. That is the same shape as the forum's report table and for the
 * same reason: the anon key is in the shipped app, so anything readable is
 * readable by everybody.
 */
class FeedbackRepositoryImpl(
    private val backend: AppBackend,
    private val preferences: UserPreferencesDataSource,
    private val build: AppBuild,
    private val ioDispatcher: CoroutineDispatcher,
) : FeedbackRepository {

    override suspend fun send(report: FeedbackReport): FeedbackResult {
        val client = when (backend) {
            is AppBackend.Available -> backend.client
            AppBackend.NotConfigured -> return FeedbackResult.Failed(FeedbackFailure.NOT_CONFIGURED)
        }

        return withContext(ioDispatcher) {
            try {
                client.postgrest.from(TABLE).insert(
                    FeedbackDto(
                        // Null when nobody is signed in, which the insert policy
                        // allows and expects. Reporting a mistake in a
                        // calculator must not require an account for an app
                        // whose calculators do not.
                        userId = client.auth.currentUserOrNull()?.id,
                        installId = preferences.installId(),
                        area = report.area,
                        message = report.message.trim(),
                        appVersion = build.version,
                        platform = build.platform,
                        locale = report.locale,
                    ),
                )
                FeedbackResult.Sent
            } catch (e: kotlinx.io.IOException) {
                // No route, DNS, timeout: the request never got an answer, and
                // trying again from the same dialog is worth it.
                FeedbackResult.Failed(FeedbackFailure.NETWORK)
            } catch (e: Exception) {
                // The one server refusal worth telling apart, because the
                // answer to it is "wait", not "try again". Matched on the
                // message the trigger raises rather than on a status code,
                // which PostgREST reports as a plain 500.
                val tooMany = e.message?.contains(RATE_LIMIT_MARKER) == true
                FeedbackResult.Failed(
                    if (tooMany) FeedbackFailure.TOO_MANY else FeedbackFailure.NETWORK,
                )
            }
        }
    }

    @Serializable
    private data class FeedbackDto(
        @SerialName("user_id") val userId: String?,
        @SerialName("install_id") val installId: String,
        val area: String,
        val message: String,
        @SerialName("app_version") val appVersion: String,
        val platform: String,
        val locale: String,
    )

    private companion object {
        const val TABLE = "app_feedback"

        /** From the `raise exception` in `supabase/10_feedback.sql`. */
        const val RATE_LIMIT_MARKER = "app_feedback:"
    }
}
