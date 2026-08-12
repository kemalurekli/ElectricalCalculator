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
     */
    @Serializable
    data class Calculator(val calculatorKey: String) : Route {
        companion object {
            fun of(id: CalculatorId) = Calculator(id.key)
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

    @Serializable
    data object Favorites : Route

    @Serializable
    data object History : Route

    @Serializable
    data object Settings : Route
}
