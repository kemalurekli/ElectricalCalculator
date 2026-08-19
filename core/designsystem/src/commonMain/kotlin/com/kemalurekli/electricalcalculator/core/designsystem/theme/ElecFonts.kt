package com.kemalurekli.electricalcalculator.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.inter_medium
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.inter_regular
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.inter_semibold
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.jetbrains_mono_medium
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.jetbrains_mono_regular
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.jetbrains_mono_semibold
import org.jetbrains.compose.resources.Font

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
 * ### Why these are composable
 *
 * On Android a font resource was an integer and a `FontFamily` could be a
 * top-level `val`. Compose Resources loads fonts through the composition
 * instead, so building a family is a `@Composable` call and every style derived
 * from one has to be read from a composition too.
 *
 * That is why [ElecTypography] and the numeric styles next door are composable
 * getters rather than constants. Call sites are unaffected — they all read
 * these from inside a `@Composable` already.
 *
 * The files themselves are static instances subset to the characters the app
 * displays, built by `scripts/build_fonts.py`. Both are SIL Open Font License
 * 1.1; the licence text ships in `app/src/main/assets/licenses/`.
 */
internal val InterFamily: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal),
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold),
    )

internal val JetBrainsMonoFamily: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(Res.font.jetbrains_mono_medium, FontWeight.Medium),
        Font(Res.font.jetbrains_mono_semibold, FontWeight.SemiBold),
    )
