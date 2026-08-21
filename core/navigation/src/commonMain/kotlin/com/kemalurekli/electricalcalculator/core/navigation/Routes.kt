package com.kemalurekli.electricalcalculator.core.navigation

import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes.
 *
 * Navigation Compose builds the route strings from these `@Serializable`
 * declarations, so destination arguments are checked by the compiler. A renamed
 * argument or a wrong type is a build error rather than a crash on a device.
 *
 * Adding a destination means adding a class here and one `composable<Route>`
 * entry in [ElecNavHost]; there is no string constant to keep in sync.
 */
sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object Calculators : Route

    /**
     * A specific calculator's form.
     *
     * Carries [CalculatorId.key] rather than the enum itself: the key is the
     * app's stable persisted identifier, so a deep link or a restored back
     * stack keeps working across releases that reorder the enum.
     *
     * [recordId] opens the form on a saved calculation from the history. Null is
     * the ordinary case — a blank form with its defaults.
     */
    @Serializable
    data class Calculator(
        val calculatorKey: String,
        val recordId: Long? = null,
    ) : Route {
        companion object {
            fun of(id: CalculatorId, recordId: Long? = null) = Calculator(id.key, recordId)
        }
    }

    @Serializable
    data object Converter : Route

    @Serializable
    data object References : Route

    /**
     * One reference topic's tables.
     *
     * Carries [ReferenceTopic.key] for the same reason [Calculator] carries a
     * calculator key: it is the stable identifier, so a restored back stack
     * survives a release that reorders the catalog.
     */
    @Serializable
    data class Reference(val topicKey: String) : Route

    /**
     * The glossary, optionally opened onto one term.
     *
     * A search hit for a term has to land on that term rather than at the top
     * of an A–Z of 117 entries, so the key travels with the route.
     */
    @Serializable
    data class Glossary(val termKey: String? = null) : Route

    /**
     * The field notes, optionally opened onto one card.
     *
     * Carries [FieldNote.key] for the same reason [Glossary] carries a term key:
     * a search hit has to land on the note it matched rather than at the top of
     * a list the reader then has to scan.
     */
    @Serializable
    data class FieldNotes(val noteKey: String? = null) : Route

    @Serializable
    data object Theory : Route

    /**
     * One theory topic's page.
     *
     * A separate destination rather than a card that opens in place, which is
     * what the glossary and the general-info notes do: a topic carries a form,
     * a result and a derivation, and putting a keyboard inside a list row makes
     * both the row and the list awkward.
     *
     * Carries [TheoryTopic.key] for the reason every other detail route carries a
     * key rather than an index — a restored back stack survives a release that
     * reorders the catalog.
     */
    @Serializable
    data class TheoryTopic(val topicKey: String) : Route

    /** The forum's sections, for the language the app is showing. */
    @Serializable
    data object Forum : Route

    /**
     * One section's threads.
     *
     * Carries the title alongside the id so the top bar has something to show
     * before the first request comes back. A screen that opens on a blank bar
     * and fills it a second later reads as a stutter.
     */
    @Serializable
    data class ForumCategory(val categoryId: String, val title: String) : Route

    /**
     * One thread and its messages.
     *
     * Carries `isLocked` for the same reason it carries the title: the screen
     * needs it on the first frame. The thread row is already loaded in the list
     * that navigated here, and the detail screen only fetches posts — so
     * without this the reply box renders for a locked thread and the server
     * refuses whatever gets typed into it.
     */
    @Serializable
    data class ForumThread(
        val threadId: String,
        val title: String,
        val isLocked: Boolean = false,
        val authorId: String = "",
        /**
         * The section the thread sits in, for the app bar.
         *
         * The bar used to carry the thread title, which is a sentence and gets
         * cut to a few words at that size. A title is a heading and belongs in
         * the content where it can wrap; the bar says where you are.
         */
        val categoryTitle: String = "",
    ) : Route

    /**
     * Composing a new thread in a category.
     *
     * Carries the language rather than letting the compose screen re-derive it
     * from the device: the thread belongs to the forum it was opened from, and
     * switching the app language midway must not move it to the other one.
     */
    @Serializable
    data class ForumComposeThread(val categoryId: String, val language: String) : Route

    /**
     * A forum member, their own or anyone else's.
     *
     * One route for both, because the screen is the same and only the actions
     * differ. Whether it is the reader's own is derived from the session rather
     * than carried here — a stale flag in a back stack entry would offer a
     * rename button that renames somebody else.
     */
    @Serializable
    data class ForumProfile(val userId: String) : Route

    /** The list of jobs. */
    @Serializable
    data object Projects : Route

    /**
     * One job: its shared supply parameters and its circuit schedule.
     *
     * Carries a row id rather than a catalog key, because unlike every other
     * detail route in the app a project is the user's own content and has no
     * stable name to address it by.
     */
    @Serializable
    data class Project(val projectId: Long) : Route

    /**
     * One circuit's inputs and the design chain worked on them.
     *
     * The project id travels alongside so the screen can read the shared
     * parameters without waiting for a second lookup, and so a deep link that
     * names a deleted circuit still knows where to send the reader back to.
     */
    @Serializable
    data class Circuit(val projectId: Long, val circuitId: Long) : Route

    @Serializable
    data object Favorites : Route

    @Serializable
    data object History : Route

    /**
     * The reader's own account, as opposed to [ForumProfile].
     *
     * Its own screen rather than a block inside settings. Signing in is a form
     * with two steps and signing out sits beside deleting everything; both
     * were being squeezed into a settings card between the theme and the unit
     * system, where every other entry is a single row.
     */
    @Serializable
    data object ForumAccount : Route

    @Serializable
    data object Settings : Route

    /**
     * The shelves that did not earn a tab.
     *
     * A destination rather than a sheet, because it is a tab root: it has to be
     * somewhere the back stack can return to, and a sheet that reopens itself
     * on back is not that.
     */
    @Serializable
    data object More : Route
}
