package com.kemalurekli.electricalcalculator.core.designsystem

/**
 * Stable handles for UI tests.
 *
 * A calculator form is a `LazyColumn`, which does not compose items that are
 * off screen. UI tests therefore cannot find a node by text and scroll to it —
 * they have to scroll the list itself with `performScrollToNode`, and that
 * needs a reliable way to address the list.
 *
 * Tags are declared here rather than as string literals at the call site so a
 * rename cannot silently break a test, and so it is obvious at a glance which
 * parts of the UI tests depend on.
 */
object ElecTestTags {
    /** The scrollable container holding a calculator's inputs and results. */
    const val CALCULATOR_FORM = "calculator_form"
}
