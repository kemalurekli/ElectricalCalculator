package com.kemalurekli.electricalcalculator.core.common.result

/**
 * Why an input was rejected.
 *
 * Errors are modelled as data rather than pre-formatted strings so the domain
 * layer stays free of Android resources. The presentation layer maps each case
 * to a localised message, which is what keeps the app translation-ready.
 */
sealed interface ValidationError {

    /** The field was left blank. */
    data object Required : ValidationError

    /** The text could not be parsed as a number. */
    data object NotANumber : ValidationError

    /** A value of zero or less was supplied where only positives are meaningful. */
    data object MustBePositive : ValidationError

    /** A negative value was supplied where only zero or above is meaningful. */
    data object MustNotBeNegative : ValidationError

    /** The value fell outside an inclusive engineering range. */
    data class OutOfRange(val min: Double, val max: Double) : ValidationError

    /** The value was above the permitted maximum. */
    data class ExceedsMaximum(val max: Double) : ValidationError

    /** The value was below the permitted minimum. */
    data class BelowMinimum(val min: Double) : ValidationError
}
