package com.kemalurekli.electricalcalculator.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii for ElecToolkit.
 *
 * Slightly softer than the Material 3 defaults at the large end, which is what
 * gives the dashboard cards their calm, product-grade feel.
 */
val ElecShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
