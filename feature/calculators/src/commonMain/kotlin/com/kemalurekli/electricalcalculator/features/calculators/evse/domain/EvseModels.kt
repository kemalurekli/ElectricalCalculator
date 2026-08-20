package com.kemalurekli.electricalcalculator.features.calculators.evse.domain

/**
 * How a charging point is connected.
 *
 * The two that matter for sizing. A single-phase point at 32 A is 7.4 kW; the
 * same 32 A on three phases is 22 kW, and the difference is the commonest
 * misunderstanding on a domestic installation.
 */
enum class EvseConnection(val phaseFactor: Double) {
    SINGLE_PHASE(phaseFactor = 1.0),
    THREE_PHASE(phaseFactor = 1.7320508075688772),
}

/**
 * Whether the charging equipment detects smooth DC residual current itself.
 *
 * The question IEC 60364-7-722 actually asks. A vehicle's on-board charger can
 * put smooth DC into the protective conductor, and a Type A RCD is blinded by
 * it — the core saturates and the device stops seeing the AC fault it was
 * installed for. Two ways out: a Type B RCD, which is expensive, or a Type A
 * plus a device that trips on 6 mA of DC, which most modern chargers have built
 * in and declare on their plate.
 *
 * Getting this wrong is not a nuisance. It is a residual current device that
 * looks installed and does nothing.
 */
enum class DcFaultDetection {
    /** The charger declares built-in 6 mA DC detection (an RDC-DD). */
    BUILT_IN_6MA,

    /** It does not, or nobody has checked. */
    NONE,
}

/** What the RCD in front of the point has to be. */
enum class RcdRequirement {
    /** Type A is enough, because the charger handles the DC itself. */
    TYPE_A,

    /** Type B, because nothing else in the circuit will see a smooth DC fault. */
    TYPE_B,
}

/**
 * A charging installation, as it is specified.
 *
 * @param simultaneityFactor the share of points assumed to draw at once. One
 *   means no diversity at all, which is the honest default: charging is a
 *   continuous load and the reason a bank of points is not a bank of sockets.
 *   Anything below one is a decision about *this* site, not a standard figure,
 *   and the app asks for it rather than choosing.
 */
data class EvseInput(
    val pointCount: Int,
    val ratedCurrentPerPoint: Double,
    val connection: EvseConnection,
    val supplyVoltage: Double,
    val simultaneityFactor: Double,
    val dcFaultDetection: DcFaultDetection,
)

/**
 * What the supply has to provide, and what has to protect it.
 *
 * @param designCurrentAmps the current the circuit is sized on, after
 *   simultaneity.
 * @param deviceRatingAmps the smallest standard device that will carry it, or
 *   null when the load is past the largest this app models.
 * @param continuousLoad always true, and stated rather than assumed: an EV
 *   draws its full current for hours, so none of the diversity habits that
 *   apply to socket circuits carry over.
 */
data class EvseResult(
    val powerPerPointKw: Double,
    val totalConnectedAmps: Double,
    val designCurrentAmps: Double,
    val totalConnectedKw: Double,
    val deviceRatingAmps: Double?,
    val rcdRequirement: RcdRequirement,
) {
    val continuousLoad: Boolean get() = true
}
