package com.kemalurekli.electricalcalculator.features.design.domain

/**
 * The ratings protective devices are actually manufactured in.
 *
 * A design cannot choose 23 A of protection. `Ib ≤ In ≤ Iz` only means anything
 * once `In` is drawn from the ratings that exist on a shelf, and a chain that
 * quietly used the load current in its place would report a cable one size too
 * small for the device that ends up in front of it.
 *
 * The series is the preferred one from IEC 60898-1 for miniature circuit
 * breakers, extended upward with the moulded-case ratings in common use. Fuse
 * ratings share most of it. A device larger than the top of this list is a
 * design that has left the domain this app models.
 */
object ProtectiveDeviceRatings {

    /** Ascending, in amperes. */
    val allAmps: List<Double> = listOf(
        6.0, 10.0, 13.0, 16.0, 20.0, 25.0, 32.0, 40.0, 50.0, 63.0,
        80.0, 100.0, 125.0, 160.0, 200.0, 250.0, 315.0, 400.0, 500.0, 630.0,
    )

    /**
     * The smallest rating that will carry [designCurrentAmps] continuously.
     *
     * Null when the load is beyond the largest device modelled, which the
     * caller must report rather than round down to — silently protecting a
     * 700 A load with a 630 A device is the one failure mode here worth
     * refusing outright.
     */
    fun smallestAtLeast(designCurrentAmps: Double): Double? =
        allAmps.firstOrNull { it >= designCurrentAmps }
}
