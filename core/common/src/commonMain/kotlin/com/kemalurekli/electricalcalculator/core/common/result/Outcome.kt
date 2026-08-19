package com.kemalurekli.electricalcalculator.core.common.result

/**
 * The result of an operation that can fail in a way the domain understands.
 *
 * Used instead of exceptions for expected failures (invalid input, a value out
 * of engineering range) so that failure paths are part of the type signature
 * and cannot be silently ignored at a call site.
 */
sealed interface Outcome<out T> {

    data class Success<out T>(val value: T) : Outcome<T>

    data class Failure(val error: ValidationError) : Outcome<Nothing>

    val isSuccess: Boolean get() = this is Success

    companion object {
        fun <T> success(value: T): Outcome<T> = Success(value)
        fun failure(error: ValidationError): Outcome<Nothing> = Failure(error)
    }
}

/** The success value, or `null` when this outcome is a failure. */
fun <T> Outcome<T>.getOrNull(): T? = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> null
}

/** The error, or `null` when this outcome succeeded. */
fun <T> Outcome<T>.errorOrNull(): ValidationError? = when (this) {
    is Outcome.Success -> null
    is Outcome.Failure -> error
}

/** The success value, or [fallback] when this outcome is a failure. */
fun <T> Outcome<T>.getOrElse(fallback: T): T = getOrNull() ?: fallback

/** Transforms a success value, leaving a failure untouched. */
inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

/** Chains another fallible operation onto a success. */
inline fun <T, R> Outcome<T>.flatMap(transform: (T) -> Outcome<R>): Outcome<R> = when (this) {
    is Outcome.Success -> transform(value)
    is Outcome.Failure -> this
}

/** Collapses both branches into a single value. */
inline fun <T, R> Outcome<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (ValidationError) -> R,
): R = when (this) {
    is Outcome.Success -> onSuccess(value)
    is Outcome.Failure -> onFailure(error)
}
