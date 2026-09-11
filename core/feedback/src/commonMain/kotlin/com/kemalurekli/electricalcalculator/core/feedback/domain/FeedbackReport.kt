package com.kemalurekli.electricalcalculator.core.feedback.domain

/**
 * One report of something being wrong in the app.
 *
 * ### What it deliberately does not carry
 *
 * Nothing the reader typed into a calculator. A report that carried the inputs
 * would be easier to act on — "the result is wrong" is a hard sentence to chase
 * — and it would also mean the one offline tool in this app quietly sending
 * somebody's site measurements to a server because they pressed a button
 * labelled "report a mistake". The reader can type the numbers if they want
 * them looked at.
 *
 * [appVersion], [platform] and [locale] are here for the opposite reason: they
 * are not about a person, and without them a report is unactionable. "The PDF
 * is broken" needs to know whether that is iOS, 1.4.2, Polish.
 */
data class FeedbackReport(
    /**
     * Which screen, as a stable untranslated key — see [FeedbackArea].
     *
     * The key rather than the title, because titles are translated and one
     * screen would otherwise arrive under twelve different names.
     */
    val area: String,
    val message: String,
    /**
     * The language the app is actually being read in, as a bare tag ("pl").
     *
     * Taken from the composition rather than from the language setting,
     * because the setting's usual value is "follow the device" — which is not
     * an answer to "which translation was on screen when this looked wrong".
     */
    val locale: String,
)

/**
 * The keys screens report under.
 *
 * Not an enum: a calculator's key carries its id, and there are thirty of
 * those. The shape is `kind:id`, so a query can group by the part before the
 * colon and see that theory has twenty reports without listing every topic.
 */
object FeedbackArea {

    fun calculator(id: String): String = "calculator:$id"

    fun theory(topicId: String): String = "theory:$topicId"

    fun reference(id: String): String = "reference:$id"

    const val CONVERTER: String = "converter"
    const val GLOSSARY: String = "glossary"
}

/** What happened when a report was sent. */
sealed interface FeedbackResult {

    data object Sent : FeedbackResult

    /**
     * Why it failed, in the only terms a reader can act on.
     *
     * A person who has just typed three sentences cares about exactly two
     * things: whether it went, and whether trying again is worth it. Anything
     * finer belongs in a log.
     */
    data class Failed(val reason: FeedbackFailure) : FeedbackResult
}

enum class FeedbackFailure {
    /** No backend in this build — a fork, or a checkout with no credentials. */
    NOT_CONFIGURED,

    /** The server refused a sixth report within the hour. */
    TOO_MANY,

    /** Everything else, which from here is indistinguishable from being offline. */
    NETWORK,
}

/** Files reports. There is no way to read them back; see `supabase/10_feedback.sql`. */
interface FeedbackRepository {
    suspend fun send(report: FeedbackReport): FeedbackResult
}
