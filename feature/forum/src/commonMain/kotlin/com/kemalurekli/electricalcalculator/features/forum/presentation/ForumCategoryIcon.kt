package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.ui.graphics.vector.ImageVector
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons

/**
 * The section's own icon, chosen by its key rather than its title.
 *
 * Keys are stable and the same in both languages; titles are neither.
 *
 * Shared rather than kept private to the category list, because the compose
 * screen names the section a thread is being opened in. That has to be the
 * same glyph the writer tapped to get there — a different one would read as
 * somewhere else.
 */
internal fun forumCategoryIcon(key: String): ImageVector = when (key) {
    "installations" -> ElecIcons.ForumInstallations
    "protection" -> ElecIcons.ForumProtection
    "troubleshooting" -> ElecIcons.ForumTroubleshooting
    "design" -> ElecIcons.ForumDesign
    "standards" -> ElecIcons.ForumStandards
    "learning" -> ElecIcons.ForumLearning
    // A category added in the dashboard that this build has never heard of
    // still gets a row, just a generic one.
    else -> ElecIcons.Forum
}
