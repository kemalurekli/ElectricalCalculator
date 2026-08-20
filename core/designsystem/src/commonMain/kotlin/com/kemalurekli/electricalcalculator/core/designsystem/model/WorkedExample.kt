package com.kemalurekli.electricalcalculator.core.designsystem.model

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource

/**
 * A realistic job a calculator can be loaded with in one tap.
 *
 * ### Why these exist
 *
 * A calculator that opens on an empty form asks the reader to already know what
 * a plausible input looks like. That is fine for someone who does this daily and
 * useless for everyone else, including the professional meeting an unfamiliar
 * calculation for the first time. An example answers the question the empty form
 * cannot: *what does a real one of these look like?*
 *
 * Paired with the step-by-step card the effect compounds — one tap produces a
 * filled form, a result, and the arithmetic that connects them. That is a
 * complete worked example, which is what the empty form was silently asking the
 * reader to supply for themselves.
 *
 * ### Why the fill is a function of the current state
 *
 * `(S) -> S` rather than a ready-made state, so applying an example keeps what
 * belongs to the user rather than the calculation — whether the calculator is
 * pinned, which unit they prefer. An example sets up a job; it does not reset
 * the app.
 *
 * ### Choosing the scenarios
 *
 * Two or three per calculator, and they are not all comfortable. At least one is
 * chosen to *fail* or to sit near a limit — a run too long for its cable, a
 * circuit that will not disconnect — because a calculator that only ever
 * demonstrates success teaches nothing about the boundary the reader is actually
 * being paid to find.
 *
 * @param key stable identifier, used for list keys and in tests.
 * @param titleRes a short name for the job, in the reader's language.
 * @param fill applies the scenario's inputs to the form.
 */
@Immutable
data class WorkedExample<S>(
    val key: String,
    val title: StringResource,
    val fill: (S) -> S,
)
