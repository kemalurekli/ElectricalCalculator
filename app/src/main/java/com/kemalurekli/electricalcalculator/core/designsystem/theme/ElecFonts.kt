package com.kemalurekli.electricalcalculator.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.kemalurekli.electricalcalculator.R

/**
 * The app's two typefaces, bundled rather than borrowed from the platform.
 *
 * `FontFamily.SansSerif` resolves to Roboto on Android and to SF or Helvetica
 * on iOS, and the two have different metrics — the same paragraph wraps in
 * different places, a two-line card title becomes three, and a table of figures
 * that lines up on one platform does not on the other. A shared design language
 * that breaks on its first line is not a shared design language, so the app
 * carries its own.
 *
 * **Inter** for everything read as language. It was drawn for screens at small
 * sizes, and it disambiguates the characters this app cannot afford to have
 * confused: a slashed zero is available, and `1`, `l` and `I` are drawn
 * distinctly — which matters when the text says "l = 30 m" or "IEC 60364-1".
 *
 * **JetBrains Mono** for figures. Tabular by construction, so a result that
 * changes from 5.17 to 18.40 does not shift the digits beside it, and formulas
 * keep their alignment.
 *
 * ### Why these are separate from [ElecTypography]
 *
 * This is the only file in the design system that names an Android resource.
 * On the Compose Multiplatform move it is the file that changes — `R.font.x`
 * becomes `Res.font.x` — and the type scale next door, which is where the
 * design decisions actually live, moves untouched.
 *
 * The files themselves are static instances subset to the characters the app
 * displays, built by `scripts/build_fonts.py`. Both are SIL Open Font License
 * 1.1; the licence text ships in `assets/licenses/`.
 */
internal val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

internal val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold),
)
