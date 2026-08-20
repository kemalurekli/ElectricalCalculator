package com.kemalurekli.electricalcalculator.core.domain.model

/**
 * Something the user has pinned.
 *
 * Favourites began as calculators only, which made the shelf that accumulates
 * the app's own content unusable for the content itself: a reader who keeps
 * returning to the ampacity table or to one glossary term had nowhere to put it.
 * A favourite is now any addressable thing, identified by what kind it is and
 * the stable key that kind already uses in its route.
 *
 * @param kind which shelf the item is on. Also decides where tapping it goes.
 * @param key the shelf's own stable identifier — a `CalculatorId.key`, a
 *   reference topic key, a glossary term key, and so on. Never an index or a
 *   title, for the same reason the routes do not carry those.
 */
data class FavoriteItem(
    val kind: FavoriteKind,
    val key: String,
)

/**
 * The shelves whose items can be pinned.
 *
 * Stored by name, so a constant is never renamed without a migration. A name
 * that no longer resolves is dropped when read rather than crashing — the same
 * treatment a retired calculator already gets.
 */
enum class FavoriteKind {
    CALCULATOR,
    REFERENCE,
    GLOSSARY,
    FIELD_NOTE,
    THEORY,
}

/** The favourite that pins this calculator. */
fun CalculatorId.asFavorite() = FavoriteItem(FavoriteKind.CALCULATOR, key)
