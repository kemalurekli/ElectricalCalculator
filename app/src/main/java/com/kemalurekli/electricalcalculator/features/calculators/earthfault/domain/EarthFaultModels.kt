package com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain

import com.kemalurekli.electricalcalculator.core.domain.model.CableInsulation
import com.kemalurekli.electricalcalculator.core.domain.model.ConductorMaterial

/**
 * How the protective device is judged to disconnect in time.
 *
 * For an overcurrent device the question is whether enough current flows to
 * reach its instantaneous trip. The multipliers are the *upper* end of each
 * type's magnetic band from IEC 60898 — the current at which tripping is
 * guaranteed rather than merely possible — which is why they are derived and
 * not transcribed:
 *
 * ```
 * I_a      = multiplier · I_n
 * Z_s,max  = c_min · U₀ / I_a
 * ```
 *
 * A residual current device is judged on touch voltage instead, because it
 * responds to the imbalance and not to the magnitude of the fault current:
 *
 * ```
 * Z_s,max  = U_L / I_Δn
 * ```
 *
 * [CUSTOM] exists so that fuses, MCCBs and anything with a published
 * time/current curve are covered without transcribing a table per device: the
 * user reads `I_a` off the curve at the disconnection time their circuit needs.
 */
enum class ProtectiveDeviceType(val instantaneousMultiplier: Double?) {
    /** IEC 60898 Type B — trips magnetically by 5·In. Cable and heating loads. */
    MCB_TYPE_B(instantaneousMultiplier = 5.0),

    /** Type C — by 10·In. The usual choice where inrush would trip a Type B. */
    MCB_TYPE_C(instantaneousMultiplier = 10.0),

    /** Type D — by 20·In. Transformers and heavy motor inrush. */
    MCB_TYPE_D(instantaneousMultiplier = 20.0),

    /** Operating current read from the device's own curve. */
    CUSTOM(instantaneousMultiplier = null),

    /** Judged on touch voltage rather than on operating current. */
    RCD(instantaneousMultiplier = null),
    ;

    /** True when the rating field means residual current rather than In or Ia. */
    val isResidualCurrent: Boolean get() = this == RCD
}

/**
 * A validated set of earth fault loop inputs.
 *
 * @param externalImpedanceOhms `Ze`, the loop impedance outside the
 *   installation. Measured at the origin, or taken from the distributor's
 *   declared maximum. Not defaulted, because it is the one figure that is
 *   genuinely different at every site.
 * @param phaseVoltage `U₀`, line-to-earth. A fault to earth is driven by the
 *   phase voltage whatever the system's line-to-line value.
 * @param deviceRatingAmps meaning follows [deviceType]: `In` for an MCB, `Ia`
 *   read off the curve for [ProtectiveDeviceType.CUSTOM], and `IΔn` for
 *   [ProtectiveDeviceType.RCD].
 * @param clearingTimeSeconds how long the device takes to clear at the fault
 *   current, used only by the adiabatic check. An MCB in its magnetic range is
 *   typically 0.01–0.1 s; a fuse is read from its curve.
 * @param protectiveCrossSectionMm2 the PE conductor being verified.
 */
data class EarthFaultInput(
    val externalImpedanceOhms: Double,
    val phaseVoltage: Double,
    val lengthMetres: Double,
    val lineCrossSectionMm2: Double,
    val protectiveCrossSectionMm2: Double,
    val parallelConductors: Int,
    val material: ConductorMaterial,
    val insulation: CableInsulation,
    val deviceType: ProtectiveDeviceType,
    val deviceRatingAmps: Double,
    val clearingTimeSeconds: Double,
) {
    companion object {
        /**
         * The voltage factor for a minimum fault current, from IEC 60909 and
         * used by BS 7671 to derive its maximum Zs tables. The same 0.95 the
         * short-circuit calculator uses, so the two agree.
         */
        const val VOLTAGE_FACTOR_MIN = 0.95

        /**
         * The touch voltage a residual current device is sized against, from
         * IEC 60364-4-41. 50 V AC is the conventional limit for dry locations.
         */
        const val TOUCH_VOLTAGE_LIMIT = 50.0
    }
}

/**
 * The outcome of an earth fault loop calculation.
 *
 * Two verdicts come out, and a circuit needs **both**:
 *
 * - [disconnectsInTime] — enough current flows to operate the device.
 * - [protectiveConductorWithstands] — the protective conductor survives the
 *   energy that flows while the device is doing it.
 *
 * A cable can pass the first and fail the second, which is how an installation
 * ends up disconnecting correctly and melting its earth on the way.
 *
 * @param loopImpedanceOhms `Zs` at the conductor's operating temperature.
 * @param maximumPermittedOhms the largest `Zs` that still disconnects.
 * @param faultCurrentAmps the current that actually flows at [loopImpedanceOhms].
 * @param operatingCurrentAmps `Ia`, the current that operates the device. Null
 *   for an RCD, which does not work that way.
 * @param adiabaticMinimumMm2 the smallest protective conductor that survives
 *   the fault, from `S = √(I²t) / k`.
 * @param adiabaticFactor the `k` used, which depends on conductor material and
 *   insulation.
 * @param tabulatedMinimumMm2 the protective conductor size the simple table
 *   rule of IEC 60364-5-54 would give. Offered alongside the calculated route
 *   because the standard permits either, and the table is usually the larger.
 * @param protectiveCrossSectionMm2 the conductor that was checked, carried so
 *   the withstand verdict is derived here rather than recomputed at each screen.
 */
data class EarthFaultResult(
    val loopImpedanceOhms: Double,
    val maximumPermittedOhms: Double,
    val faultCurrentAmps: Double,
    val operatingCurrentAmps: Double?,
    val lineResistanceOhms: Double,
    val protectiveResistanceOhms: Double,
    val adiabaticMinimumMm2: Double,
    val adiabaticFactor: Double,
    val tabulatedMinimumMm2: Double,
    val protectiveCrossSectionMm2: Double,
) {
    /** True when the loop is low enough for the device to operate. */
    val disconnectsInTime: Boolean get() = loopImpedanceOhms <= maximumPermittedOhms

    /** True when the protective conductor survives the let-through energy. */
    val protectiveConductorWithstands: Boolean
        get() = adiabaticMinimumMm2 <= protectiveCrossSectionMm2

    /** Both checks passed. */
    val isCompliant: Boolean get() = disconnectsInTime && protectiveConductorWithstands

    /**
     * How much of the permitted loop impedance is used. Above 1 the circuit
     * fails; approaching 1 it has no margin for a warm day or a loose terminal.
     */
    val impedanceUtilisation: Double get() = loopImpedanceOhms / maximumPermittedOhms
}
