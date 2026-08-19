package com.kemalurekli.electricalcalculator.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * ElecToolkit colour tokens.
 *
 * ### Why these values are written out rather than generated
 *
 * The palette used to be what the Material 3 tonal generator produces from a
 * blue seed: a `#17539F` primary, `#D8E2FF` containers, and a `#FDFBFF`
 * background whose violet cast is the generator's signature. It was correct and
 * it was anonymous — the app looked like a Compose app rather than like itself,
 * because that is the palette a thousand other Compose apps also ship.
 *
 * These are chosen by hand instead. The base is a **steel navy**, the colour of
 * instrument housings and switchgear, and the accent is a **muted copper** —
 * the one material every reader of this app handles daily. The surfaces are
 * neutral-cool greys with no hue cast, so that a screen full of numbers reads as
 * paper rather than as tinted glass.
 *
 * ### Why the accent and the warning are not the same orange
 *
 * Copper (hue 21°) and the "marginal result" amber (hue 38°) sit close enough on
 * the wheel to worry about, and a brand accent that could be mistaken for a
 * status is worse than no accent. They are separated by saturation rather than
 * by hue alone: the accent is muted (53 %) and the warning is fully saturated
 * (100 %). A status colour is allowed to shout; a brand colour is not. `info`
 * moved from blue to teal for the same reason — it used to sit within a few
 * degrees of the primary, so "this is a note" and "this is the app" were the
 * same colour.
 *
 * ### Contrast
 *
 * Every foreground/background pair used for text clears 4.5:1 in both schemes,
 * and the outline clears 3:1 against its surface. The tightest pair is the light
 * warning at 5.01:1; nothing else is under 6:1.
 *
 * ### Both platforms
 *
 * Nothing here is derived at runtime from the device. The same hex values are
 * what the iOS build renders, which is the whole point of writing them down.
 */

// ---------------------------------------------------------------------------
// Light scheme
// ---------------------------------------------------------------------------
internal val PrimaryLight = Color(0xFF14496F)
internal val OnPrimaryLight = Color(0xFFFFFFFF)
internal val PrimaryContainerLight = Color(0xFFCFE3F7)
internal val OnPrimaryContainerLight = Color(0xFF04263F)

internal val SecondaryLight = Color(0xFF4A5A6B)
internal val OnSecondaryLight = Color(0xFFFFFFFF)
internal val SecondaryContainerLight = Color(0xFFDAE1E9)
internal val OnSecondaryContainerLight = Color(0xFF121C25)

internal val TertiaryLight = Color(0xFF8A4B2A)
internal val OnTertiaryLight = Color(0xFFFFFFFF)
internal val TertiaryContainerLight = Color(0xFFFFDBC8)
internal val OnTertiaryContainerLight = Color(0xFF331200)

internal val ErrorLight = Color(0xFFBA1A1A)
internal val OnErrorLight = Color(0xFFFFFFFF)
internal val ErrorContainerLight = Color(0xFFFFDAD6)
internal val OnErrorContainerLight = Color(0xFF410002)

internal val BackgroundLight = Color(0xFFFBFCFD)
internal val OnBackgroundLight = Color(0xFF12181F)
internal val SurfaceLight = Color(0xFFFBFCFD)
internal val OnSurfaceLight = Color(0xFF12181F)
internal val SurfaceVariantLight = Color(0xFFDFE4EA)
internal val OnSurfaceVariantLight = Color(0xFF414B55)
internal val OutlineLight = Color(0xFF6F7B86)
internal val OutlineVariantLight = Color(0xFFC6CED6)

internal val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val SurfaceContainerLowLight = Color(0xFFF5F7FA)
internal val SurfaceContainerLight = Color(0xFFEFF2F6)
internal val SurfaceContainerHighLight = Color(0xFFE8ECF1)
internal val SurfaceContainerHighestLight = Color(0xFFE1E6ED)

internal val InverseSurfaceLight = Color(0xFF262C33)
internal val InverseOnSurfaceLight = Color(0xFFF0F2F5)
internal val InversePrimaryLight = Color(0xFF9DC6EE)
internal val ScrimLight = Color(0xFF000000)

// ---------------------------------------------------------------------------
// Dark scheme
//
// Not the light scheme inverted. The dark surface is a near-black with a cool
// cast (#0E1419) rather than the neutral charcoal a generator would pick,
// because an unlit workshop at night is what this scheme is actually read in.
// ---------------------------------------------------------------------------
internal val PrimaryDark = Color(0xFF9DC6EE)
internal val OnPrimaryDark = Color(0xFF04283F)
internal val PrimaryContainerDark = Color(0xFF0E3C5D)
internal val OnPrimaryContainerDark = Color(0xFFCFE3F7)

internal val SecondaryDark = Color(0xFFB5C4D2)
internal val OnSecondaryDark = Color(0xFF1F2A34)
internal val SecondaryContainerDark = Color(0xFF35414C)
internal val OnSecondaryContainerDark = Color(0xFFDAE1E9)

internal val TertiaryDark = Color(0xFFF0B394)
internal val OnTertiaryDark = Color(0xFF4A1F05)
internal val TertiaryContainerDark = Color(0xFF683418)
internal val OnTertiaryContainerDark = Color(0xFFFFDBC8)

internal val ErrorDark = Color(0xFFFFB4AB)
internal val OnErrorDark = Color(0xFF690005)
internal val ErrorContainerDark = Color(0xFF93000A)
internal val OnErrorContainerDark = Color(0xFFFFDAD6)

internal val BackgroundDark = Color(0xFF0E1419)
internal val OnBackgroundDark = Color(0xFFE3E8EE)
internal val SurfaceDark = Color(0xFF0E1419)
internal val OnSurfaceDark = Color(0xFFE3E8EE)
internal val SurfaceVariantDark = Color(0xFF414B55)
internal val OnSurfaceVariantDark = Color(0xFFBAC5D0)
internal val OutlineDark = Color(0xFF85919C)
internal val OutlineVariantDark = Color(0xFF3B444D)

internal val SurfaceContainerLowestDark = Color(0xFF090D11)
internal val SurfaceContainerLowDark = Color(0xFF151C22)
internal val SurfaceContainerDark = Color(0xFF191F26)
internal val SurfaceContainerHighDark = Color(0xFF232A32)
internal val SurfaceContainerHighestDark = Color(0xFF2D353E)

internal val InverseSurfaceDark = Color(0xFFE3E8EE)
internal val InverseOnSurfaceDark = Color(0xFF262C33)
internal val InversePrimaryDark = Color(0xFF14496F)
internal val ScrimDark = Color(0xFF000000)

// ---------------------------------------------------------------------------
// Semantic engineering colours
//
// Material 3 has no slot for "this result passes / this result is marginal", so
// the app carries its own semantic set. Exposed through [LocalElecSemanticColors]
// rather than hard-coded at call sites, so themes stay swappable.
//
// These are the loud half of the palette. Where the brand colours are muted so
// they can sit under a screen of numbers all day, these exist to be noticed the
// moment a result crosses a limit — which is why they run at full saturation.
// ---------------------------------------------------------------------------
internal val SuccessLight = Color(0xFF17663A)
internal val OnSuccessLight = Color(0xFFFFFFFF)
internal val SuccessContainerLight = Color(0xFFA8F0C0)
internal val OnSuccessContainerLight = Color(0xFF002110)

internal val WarningLight = Color(0xFF9A6100)
internal val OnWarningLight = Color(0xFFFFFFFF)
internal val WarningContainerLight = Color(0xFFFFDFB0)
internal val OnWarningContainerLight = Color(0xFF2E1C00)

internal val InfoLight = Color(0xFF006874)
internal val OnInfoLight = Color(0xFFFFFFFF)
internal val InfoContainerLight = Color(0xFFA9EEF9)
internal val OnInfoContainerLight = Color(0xFF001F25)

internal val SuccessDark = Color(0xFF8CD9A6)
internal val OnSuccessDark = Color(0xFF003820)
internal val SuccessContainerDark = Color(0xFF00522F)
internal val OnSuccessContainerDark = Color(0xFFA8F0C0)

internal val WarningDark = Color(0xFFF2C078)
internal val OnWarningDark = Color(0xFF4F3200)
internal val WarningContainerDark = Color(0xFF714800)
internal val OnWarningContainerDark = Color(0xFFFFDFB0)

internal val InfoDark = Color(0xFF82D3E0)
internal val OnInfoDark = Color(0xFF00363D)
internal val InfoContainerDark = Color(0xFF004F58)
internal val OnInfoContainerDark = Color(0xFFA9EEF9)
