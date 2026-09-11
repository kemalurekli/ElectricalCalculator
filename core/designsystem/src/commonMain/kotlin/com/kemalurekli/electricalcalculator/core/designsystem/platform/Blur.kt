package com.kemalurekli.electricalcalculator.core.designsystem.platform

/**
 * Whether `Modifier.blur` actually blurs anything here.
 *
 * ### Why this has to be asked
 *
 * `Modifier.blur` is in common code and compiles everywhere, and on Android
 * below API 31 it is a **silent no-op**: no exception, no log line, and content
 * that was supposed to be obscured drawn perfectly legibly. This app's minSdk
 * is 28, so that is three Android versions where a blur used as a gate is not a
 * gate at all.
 *
 * A capability, not a version check at the call site, because the call sites
 * should be asking "can I blur" rather than "which Android is this" — and iOS,
 * which can, should not have to answer a question about Android.
 *
 * ### What the callers must do with the answer
 *
 * Use it to decide whether to *add* blur, never whether to *have* a gate. What
 * hides the content has to work everywhere; blur is the part that can be
 * missing. `docs/design-language.md` says the app looks the same on both
 * platforms, and this is the narrow exception the rule allows: a platform that
 * cannot draw a thing gets the same screen without that one effect, not a
 * different screen.
 */
expect val canBlurContent: Boolean
