package com.kemalurekli.electricalcalculator.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * ElecToolkit colour tokens.
 *
 * The palette is built around a deep "instrument blue" primary, which reads as
 * precise and technical without the saturated look of legacy engineering apps.
 * Values follow the Material 3 tonal system so that both schemes stay contrast
 * compliant (>= 4.5:1 for body text) in light and dark.
 */

// ---------------------------------------------------------------------------
// Light scheme
// ---------------------------------------------------------------------------
internal val PrimaryLight = Color(0xFF17539F)
internal val OnPrimaryLight = Color(0xFFFFFFFF)
internal val PrimaryContainerLight = Color(0xFFD8E2FF)
internal val OnPrimaryContainerLight = Color(0xFF001A41)

internal val SecondaryLight = Color(0xFF565E71)
internal val OnSecondaryLight = Color(0xFFFFFFFF)
internal val SecondaryContainerLight = Color(0xFFDAE2F9)
internal val OnSecondaryContainerLight = Color(0xFF131C2B)

internal val TertiaryLight = Color(0xFF705574)
internal val OnTertiaryLight = Color(0xFFFFFFFF)
internal val TertiaryContainerLight = Color(0xFFFAD8FD)
internal val OnTertiaryContainerLight = Color(0xFF28132E)

internal val ErrorLight = Color(0xFFBA1A1A)
internal val OnErrorLight = Color(0xFFFFFFFF)
internal val ErrorContainerLight = Color(0xFFFFDAD6)
internal val OnErrorContainerLight = Color(0xFF410002)

internal val BackgroundLight = Color(0xFFFDFBFF)
internal val OnBackgroundLight = Color(0xFF1A1B1F)
internal val SurfaceLight = Color(0xFFFDFBFF)
internal val OnSurfaceLight = Color(0xFF1A1B1F)
internal val SurfaceVariantLight = Color(0xFFE0E2EC)
internal val OnSurfaceVariantLight = Color(0xFF43474E)
internal val OutlineLight = Color(0xFF74777F)
internal val OutlineVariantLight = Color(0xFFC4C6D0)

internal val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val SurfaceContainerLowLight = Color(0xFFF7F9FF)
internal val SurfaceContainerLight = Color(0xFFF1F3FA)
internal val SurfaceContainerHighLight = Color(0xFFEBEDF5)
internal val SurfaceContainerHighestLight = Color(0xFFE5E8EF)

internal val InverseSurfaceLight = Color(0xFF2F3033)
internal val InverseOnSurfaceLight = Color(0xFFF1F0F4)
internal val InversePrimaryLight = Color(0xFFAEC6FF)
internal val ScrimLight = Color(0xFF000000)

// ---------------------------------------------------------------------------
// Dark scheme
// ---------------------------------------------------------------------------
internal val PrimaryDark = Color(0xFFAEC6FF)
internal val OnPrimaryDark = Color(0xFF002E6A)
internal val PrimaryContainerDark = Color(0xFF004494)
internal val OnPrimaryContainerDark = Color(0xFFD8E2FF)

internal val SecondaryDark = Color(0xFFBEC6DC)
internal val OnSecondaryDark = Color(0xFF283041)
internal val SecondaryContainerDark = Color(0xFF3E4759)
internal val OnSecondaryContainerDark = Color(0xFFDAE2F9)

internal val TertiaryDark = Color(0xFFDDBCE0)
internal val OnTertiaryDark = Color(0xFF3F2844)
internal val TertiaryContainerDark = Color(0xFF573E5B)
internal val OnTertiaryContainerDark = Color(0xFFFAD8FD)

internal val ErrorDark = Color(0xFFFFB4AB)
internal val OnErrorDark = Color(0xFF690005)
internal val ErrorContainerDark = Color(0xFF93000A)
internal val OnErrorContainerDark = Color(0xFFFFDAD6)

internal val BackgroundDark = Color(0xFF111318)
internal val OnBackgroundDark = Color(0xFFE3E2E6)
internal val SurfaceDark = Color(0xFF111318)
internal val OnSurfaceDark = Color(0xFFE3E2E6)
internal val SurfaceVariantDark = Color(0xFF43474E)
internal val OnSurfaceVariantDark = Color(0xFFC4C6D0)
internal val OutlineDark = Color(0xFF8E9099)
internal val OutlineVariantDark = Color(0xFF43474E)

internal val SurfaceContainerLowestDark = Color(0xFF0C0E13)
internal val SurfaceContainerLowDark = Color(0xFF191C20)
internal val SurfaceContainerDark = Color(0xFF1D2024)
internal val SurfaceContainerHighDark = Color(0xFF282A2F)
internal val SurfaceContainerHighestDark = Color(0xFF33353A)

internal val InverseSurfaceDark = Color(0xFFE3E2E6)
internal val InverseOnSurfaceDark = Color(0xFF2F3033)
internal val InversePrimaryDark = Color(0xFF17539F)
internal val ScrimDark = Color(0xFF000000)

// ---------------------------------------------------------------------------
// Semantic engineering colours
//
// Material 3 has no slot for "this result passes / this result is marginal", so
// the app carries its own semantic set. Exposed through [LocalElecColors] rather
// than hard-coded at call sites, so themes stay swappable.
// ---------------------------------------------------------------------------
internal val SuccessLight = Color(0xFF186B3A)
internal val OnSuccessLight = Color(0xFFFFFFFF)
internal val SuccessContainerLight = Color(0xFFA5F6B8)
internal val OnSuccessContainerLight = Color(0xFF00210C)

internal val WarningLight = Color(0xFF8A5100)
internal val OnWarningLight = Color(0xFFFFFFFF)
internal val WarningContainerLight = Color(0xFFFFDCBE)
internal val OnWarningContainerLight = Color(0xFF2C1600)

internal val InfoLight = Color(0xFF00639C)
internal val OnInfoLight = Color(0xFFFFFFFF)
internal val InfoContainerLight = Color(0xFFCFE5FF)
internal val OnInfoContainerLight = Color(0xFF001D33)

internal val SuccessDark = Color(0xFF8AD99E)
internal val OnSuccessDark = Color(0xFF00391A)
internal val SuccessContainerDark = Color(0xFF00522A)
internal val OnSuccessContainerDark = Color(0xFFA5F6B8)

internal val WarningDark = Color(0xFFFFB870)
internal val OnWarningDark = Color(0xFF4A2800)
internal val WarningContainerDark = Color(0xFF693C00)
internal val OnWarningContainerDark = Color(0xFFFFDCBE)

internal val InfoDark = Color(0xFF97CBFF)
internal val OnInfoDark = Color(0xFF003355)
internal val InfoContainerDark = Color(0xFF004A78)
internal val OnInfoContainerDark = Color(0xFFCFE5FF)
