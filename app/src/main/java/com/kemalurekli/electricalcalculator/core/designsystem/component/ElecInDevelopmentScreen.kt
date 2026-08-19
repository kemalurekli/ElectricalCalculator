package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.kemalurekli.electricalcalculator.R

/**
 * Screen shown for a section whose foundations are in place but whose feature
 * work lands in a later development phase.
 *
 * Used so the navigation graph is complete and every dashboard card leads
 * somewhere real, rather than to a blank screen or a dead button. Each screen
 * that adopts this is replaced wholesale by its own implementation — nothing
 * here is meant to survive into the finished feature.
 */
// The opt-in is for ElecScreenScaffold's `scrollBehavior` parameter, whose type
// is still experimental in Material 3 — every screen in the app carries it for
// the same reason.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElecInDevelopmentScreen(
    title: String,
    icon: ImageVector,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElecScreenScaffold(
        title = title,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            ElecEmptyState(
                title = stringResource(R.string.state_in_development_title),
                message = stringResource(R.string.state_in_development_message),
                icon = icon,
            )
        }
    }
}
