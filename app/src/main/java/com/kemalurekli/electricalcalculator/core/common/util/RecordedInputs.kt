package com.kemalurekli.electricalcalculator.core.common.util

/**
 * Reading a saved calculation's inputs back into a form.
 *
 * [com.kemalurekli.electricalcalculator.core.domain.model.CalculationRecord]
 * stores inputs as the raw text the reader typed, precisely so that reopening a
 * calculation needs no reverse parsing — "0.85" goes back into the field as
 * "0.85", not as a re-formatted 0,85. These two helpers are the whole of the
 * reading side.
 *
 * Both fall back rather than failing. A record written by an older release may
 * be missing a key the form has since gained; the form should open with its
 * default there and everything else restored, not refuse to open at all.
 */

/** The stored text for [key], or [fallback] when it is absent or empty. */
fun Map<String, String>.pick(key: String, fallback: String): String =
    this[key]?.takeIf { it.isNotBlank() } ?: fallback

/**
 * The enum constant named by [key], or null when it is absent or unrecognised.
 *
 * Matches on [Enum.name] because that is what the calculators write. A constant
 * that has been renamed since the record was saved reads back as null, which the
 * caller turns into the form's current value.
 */
inline fun <reified E : Enum<E>> Map<String, String>.enumOrNull(key: String): E? =
    this[key]?.let { stored -> enumValues<E>().firstOrNull { it.name == stored } }
