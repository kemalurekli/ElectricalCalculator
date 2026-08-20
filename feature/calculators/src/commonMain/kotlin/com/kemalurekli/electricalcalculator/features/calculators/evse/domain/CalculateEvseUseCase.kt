package com.kemalurekli.electricalcalculator.features.calculators.evse.domain

import com.kemalurekli.electricalcalculator.core.domain.table.ProtectiveDeviceRatings

/**
 * Sizes a charging installation, and says what the RCD has to be.
 *
 * ### Why this is not just a load calculation
 *
 * Two things make an EV point different from every other socket outlet, and
 * both of them are the sort of difference that gets missed because the circuit
 * looks so ordinary.
 *
 * It is a **continuous load**. A vehicle draws its full rated current for
 * hours, so the diversity habits that make a ring final circuit work do not
 * carry over — a bank of points is not a bank of sockets, and the app declines
 * to apply a simultaneity factor of its own accord.
 *
 * And it can produce **smooth DC residual current**. A vehicle's on-board
 * charger can put DC into the protective conductor, which saturates the core of
 * a Type A RCD and blinds it to the AC fault it was installed for. IEC
 * 60364-7-722 answers this with a Type B RCD, or a Type A alongside a device
 * that trips at 6 mA of DC. The second is what most modern chargers have built
 * in — but only if the plate says so, which is why this is an input and not an
 * assumption.
 *
 * ### What is left out
 *
 * Load management, which changes the answer entirely: a controller that shares
 * a fixed supply between points makes the design current a setting rather than
 * a sum. The cable itself, which the cable-size calculator and the project
 * chain already do better than a bolt-on here would.
 */
class CalculateEvseUseCase() {

    operator fun invoke(input: EvseInput): EvseResult {
        val perPointKw = input.connection.phaseFactor *
            input.supplyVoltage *
            input.ratedCurrentPerPoint / 1000.0

        val totalConnected = input.ratedCurrentPerPoint * input.pointCount
        val design = totalConnected * input.simultaneityFactor

        return EvseResult(
            powerPerPointKw = perPointKw,
            totalConnectedAmps = totalConnected,
            designCurrentAmps = design,
            totalConnectedKw = perPointKw * input.pointCount,
            deviceRatingAmps = ProtectiveDeviceRatings.smallestAtLeast(design),
            rcdRequirement = when (input.dcFaultDetection) {
                DcFaultDetection.BUILT_IN_6MA -> RcdRequirement.TYPE_A
                DcFaultDetection.NONE -> RcdRequirement.TYPE_B
            },
        )
    }
}
