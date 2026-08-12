package com.kemalurekli.electricalcalculator.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Typography for ElecToolkit.
 *
 * Body and label styles use the platform sans-serif so system font scaling and
 * per-locale fallbacks work untouched. Numeric output uses [NumericTextStyle],
 * a monospace style, so that digits stay column-aligned when results update —
 * proportional digits visibly jitter as values change.
 */

private val DefaultLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun elecTextStyle(
    fontWeight: FontWeight,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily = FontFamily.SansSerif,
): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    lineHeightStyle = DefaultLineHeightStyle,
    // Removing the built-in font padding makes measured text match its box,
    // which keeps dense engineering layouts optically aligned.
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

val ElecTypography = Typography(
    displayLarge = elecTextStyle(FontWeight.Normal, 57.sp, 64.sp, (-0.25).sp),
    displayMedium = elecTextStyle(FontWeight.Normal, 45.sp, 52.sp, 0.sp),
    displaySmall = elecTextStyle(FontWeight.Normal, 36.sp, 44.sp, 0.sp),

    headlineLarge = elecTextStyle(FontWeight.SemiBold, 32.sp, 40.sp, 0.sp),
    headlineMedium = elecTextStyle(FontWeight.SemiBold, 28.sp, 36.sp, 0.sp),
    headlineSmall = elecTextStyle(FontWeight.SemiBold, 24.sp, 32.sp, 0.sp),

    titleLarge = elecTextStyle(FontWeight.SemiBold, 22.sp, 28.sp, 0.sp),
    titleMedium = elecTextStyle(FontWeight.SemiBold, 16.sp, 24.sp, 0.15.sp),
    titleSmall = elecTextStyle(FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),

    bodyLarge = elecTextStyle(FontWeight.Normal, 16.sp, 24.sp, 0.5.sp),
    bodyMedium = elecTextStyle(FontWeight.Normal, 14.sp, 20.sp, 0.25.sp),
    bodySmall = elecTextStyle(FontWeight.Normal, 12.sp, 16.sp, 0.4.sp),

    labelLarge = elecTextStyle(FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),
    labelMedium = elecTextStyle(FontWeight.Medium, 12.sp, 16.sp, 0.5.sp),
    labelSmall = elecTextStyle(FontWeight.Medium, 11.sp, 16.sp, 0.5.sp),
)

/** Tabular style for primary result values. */
val NumericTextStyle: TextStyle = elecTextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp,
    fontFamily = FontFamily.Monospace,
)

/** Tabular style for secondary values and table cells. */
val NumericCompactTextStyle: TextStyle = elecTextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
    fontFamily = FontFamily.Monospace,
)

/** Style for rendered formulas and variable definitions. */
val FormulaTextStyle: TextStyle = elecTextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp,
    fontFamily = FontFamily.Monospace,
)
