package com.kemalurekli.electricalcalculator.features.calculators.solarstring.domain

/**
 * A validated set of PV string-sizing inputs.
 *
 * Everything here comes off two datasheets — the module's and the inverter's —
 * and nothing is assumed. The temperature coefficients in particular are
 * module-specific and vary by more than a factor of two between technologies, so
 * there is no default worth offering.
 *
 * @param moduleVocVolts open-circuit voltage at STC.
 * @param moduleVmpVolts voltage at maximum power at STC.
 * @param voltageCoefficientPercentPerK `β`, the change in Voc per kelvin, as a
 *   percentage. It is **negative**: a cold module produces more voltage, which
 *   is the whole reason this calculation exists.
 * @param minimumCellTemperatureC the coldest the cells will ever be — the site's
 *   record low, not its average winter. This is the case that breaks inverters.
 * @param maximumCellTemperatureC the hottest the cells will reach. Cell
 *   temperature runs well above air temperature in sun; 70 °C is ordinary for a
 *   roof-mounted module on a 35 °C day.
 * @param inverterMaxDcVolts the absolute maximum the inverter's DC input will
 *   survive. Exceeding it is not a derating, it is damage.
 * @param inverterMpptMinVolts the bottom of the MPPT window. Below it the
 *   inverter stops tracking and the array produces nothing useful.
 */
data class SolarStringInput(
    val moduleVocVolts: Double,
    val moduleVmpVolts: Double,
    val voltageCoefficientPercentPerK: Double,
    val minimumCellTemperatureC: Double,
    val maximumCellTemperatureC: Double,
    val inverterMaxDcVolts: Double,
    val inverterMpptMinVolts: Double,
) {
    companion object {
        /** Every module datasheet is referenced to 25 °C cell temperature. */
        const val STC_TEMPERATURE_C = 25.0
    }
}

/**
 * The outcome of a string-sizing calculation.
 *
 * Two limits come out, and a string has to satisfy both:
 *
 * - [maximumModules] keeps the **cold** open-circuit voltage under the
 *   inverter's absolute maximum. Exceed it and the inverter is damaged on the
 *   first cold clear morning, before the array has produced anything.
 * - [minimumModules] keeps the **hot** maximum-power voltage above the MPPT
 *   window. Fall below it and the array stops producing on exactly the days it
 *   should produce most.
 *
 * @param vocAtMinimumTemperature the cold-corrected open-circuit voltage of one
 *   module — the figure the maximum is derived from.
 * @param vmpAtMaximumTemperature the hot-corrected maximum-power voltage.
 * @param isFeasible false when no string length satisfies both limits, which
 *   means this module and this inverter do not go together.
 */
data class SolarStringResult(
    val vocAtMinimumTemperature: Double,
    val vmpAtMaximumTemperature: Double,
    val maximumModules: Int,
    val minimumModules: Int,
    val stringVocAtMaximum: Double,
    val stringVmpAtMinimum: Double,
) {
    val isFeasible: Boolean get() = maximumModules >= minimumModules && maximumModules > 0
}
