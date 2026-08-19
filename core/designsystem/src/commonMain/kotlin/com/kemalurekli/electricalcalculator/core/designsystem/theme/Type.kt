package com.kemalurekli.electricalcalculator.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Typography for ElecToolkit.
 *
 * Text is set in Inter and figures in JetBrains Mono, both bundled — see
 * [InterFamily] for why the app carries typefaces rather than asking the
 * platform for one. Sizes are in `sp` throughout, so the reader's own font-size
 * setting still scales everything.
 *
 * ### On the tracking
 *
 * Material 3's body styles ship with 0.5sp of letter spacing at 16sp and 0.25sp
 * at 14sp. That looseness is tuned for Roboto and, more to the point, it is the
 * single most recognisable thing about a default Compose app after its colours:
 * paragraphs that read as slightly airy, the way a consumer app's do. Inter is
 * drawn tight and does not need the help. Body and label tracking is pulled in
 * to 0.1–0.2sp, which reads as denser and more instrument-like without
 * crossing into cramped.
 *
 * [titleLarge] came down from 22sp to 20sp for a related reason: it is the top
 * app bar's size, and at 22sp a Turkish screen title such as "Solar PV Dizi
 * Boyutlandırma" was one character from being ellipsized on a narrow phone.
 *
 * ### Why everything here is a composable getter
 *
 * The families these styles are built from are loaded through the composition —
 * see [InterFamily]. Call sites are unchanged, because every one of them reads
 * a style from inside a `@Composable` already.
 *
 * `PlatformTextStyle(includeFontPadding = false)` went with the move. It removed
 * Android's built-in font padding so that measured text matched its box, and it
 * has no counterpart in common code: the parameter exists only on Android. Its
 * absence shifts every line's vertical metrics slightly, which is why the
 * Android build wants comparing against a screenshot from before this change.
 */

private val DefaultLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

@Composable
private fun elecTextStyle(
    fontWeight: FontWeight,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily = InterFamily,
): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    lineHeightStyle = DefaultLineHeightStyle,
)

val ElecTypography: Typography
    @Composable get() = Typography(
        displayLarge = elecTextStyle(FontWeight.Normal, 57.sp, 64.sp, (-0.25).sp),
        displayMedium = elecTextStyle(FontWeight.Normal, 45.sp, 52.sp, 0.sp),
        displaySmall = elecTextStyle(FontWeight.Normal, 36.sp, 44.sp, 0.sp),

        headlineLarge = elecTextStyle(FontWeight.SemiBold, 32.sp, 40.sp, 0.sp),
        headlineMedium = elecTextStyle(FontWeight.SemiBold, 28.sp, 36.sp, 0.sp),
        headlineSmall = elecTextStyle(FontWeight.SemiBold, 24.sp, 32.sp, 0.sp),

        titleLarge = elecTextStyle(FontWeight.SemiBold, 20.sp, 26.sp, 0.sp),
        titleMedium = elecTextStyle(FontWeight.SemiBold, 16.sp, 24.sp, 0.1.sp),
        titleSmall = elecTextStyle(FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),

        bodyLarge = elecTextStyle(FontWeight.Normal, 16.sp, 24.sp, 0.1.sp),
        bodyMedium = elecTextStyle(FontWeight.Normal, 14.sp, 20.sp, 0.1.sp),
        bodySmall = elecTextStyle(FontWeight.Normal, 12.sp, 16.sp, 0.2.sp),

        labelLarge = elecTextStyle(FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),
        labelMedium = elecTextStyle(FontWeight.Medium, 12.sp, 16.sp, 0.2.sp),
        labelSmall = elecTextStyle(FontWeight.Medium, 11.sp, 16.sp, 0.2.sp),
    )

/** Tabular style for primary result values. */
val NumericTextStyle: TextStyle
    @Composable get() = elecTextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp,
    fontFamily = JetBrainsMonoFamily,
)

/** Tabular style for secondary values and table cells. */
val NumericCompactTextStyle: TextStyle
    @Composable get() = elecTextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
    fontFamily = JetBrainsMonoFamily,
)

/** Style for rendered formulas and variable definitions. */
val FormulaTextStyle: TextStyle
    @Composable get() = elecTextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp,
    fontFamily = JetBrainsMonoFamily,
)
