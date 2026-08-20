package com.kemalurekli.electricalcalculator.core.common.util

/**
 * Substitutes `%1$s`-style placeholders in an already-resolved template.
 *
 * `String.format` is a JVM method and does not exist in shared code. Almost
 * everywhere that is fine — Compose Resources substitutes arguments itself when
 * you ask it for a string, and [StringResolver.get] passes them through. This
 * is for the one shape it cannot cover: a template read once in composition and
 * applied to each row of a list, because the resource accessor is `@Composable`
 * and cannot be called from inside a loop's lambda.
 *
 * Positional only — `%1$s` and `%1$d`, not bare `%s`. Every string in this app
 * that takes an argument numbers it, because a translation reorders arguments
 * and an unnumbered placeholder cannot be reordered. A `%%` is a literal
 * percent, as it is everywhere else.
 *
 * An index past the end of [args] is left in place rather than throwing: a
 * mismatched translation should show its own placeholder on screen, where it
 * gets noticed and fixed, not crash the calculator that used it.
 */
fun String.formatPositional(vararg args: Any): String {
    val out = StringBuilder(length)
    var i = 0
    while (i < length) {
        val c = this[i]
        if (c != '%') {
            out.append(c)
            i++
            continue
        }
        if (i + 1 < length && this[i + 1] == '%') {
            out.append('%')
            i += 2
            continue
        }
        var j = i + 1
        while (j < length && this[j].isDigit()) j++
        // `%1$s`: digits, then the dollar, then one conversion character.
        if (j > i + 1 && j + 1 < length && this[j] == '$') {
            val index = substring(i + 1, j).toInt() - 1
            if (index in args.indices) {
                out.append(args[index].toString())
                i = j + 2
                continue
            }
        }
        out.append(c)
        i++
    }
    return out.toString()
}
