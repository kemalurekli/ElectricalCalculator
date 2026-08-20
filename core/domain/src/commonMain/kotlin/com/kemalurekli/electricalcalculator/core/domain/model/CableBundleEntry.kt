package com.kemalurekli.electricalcalculator.core.domain.model

/**
 * One kind of cable in a bundle, and how many of it there are.
 *
 * Lives in `core` because a bundle is the input to every containment
 * calculation — conduit fill and cable tray fill both describe the same set of
 * cables, and a second definition of "a diameter and a count" would let the two
 * drift apart.
 *
 * @param diameterMm the cable's overall outside diameter from its datasheet.
 * @param quantity how many cables of that diameter the containment carries.
 */
data class CableBundleEntry(
    val diameterMm: Double,
    val quantity: Int,
)
