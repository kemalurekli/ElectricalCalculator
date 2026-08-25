package com.kemalurekli.electricalcalculator.features.projects.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.asRelativeTime
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecEmptyState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecLoadingState
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.domain.repository.ProjectSummary
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.destination_projects
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_circuit_count
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_circuit_count_none
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_circuit_count_one
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_empty_message
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_empty_title
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_new
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_supply_volts
import com.kemalurekli.electricalcalculator.feature.projects.generated.resources.projects_untitled

@Composable
fun ProjectsRoute(
    onOpenProject: (Long) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val created by viewModel.created.collectAsStateWithLifecycle()

    // A new project opens straight into its own screen. Dropping the user back
    // on a list with one more blank row would make them find and tap it.
    LaunchedEffect(created) {
        created?.let {
            viewModel.onCreatedHandled()
            onOpenProject(it)
        }
    }

    ProjectsScreen(
        uiState = uiState,
        onOpenProject = onOpenProject,
        onCreateProject = viewModel::onCreateProject,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    uiState: ProjectsUiState,
    onOpenProject: (Long) -> Unit,
    onCreateProject: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = rememberElecScrollBehavior()
    val spacing = ElecTheme.spacing

    ElecScreenScaffold(
        title = stringResource(Res.string.destination_projects),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        scrollBehavior = scrollBehavior,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateProject,
                icon = { Icon(ElecIcons.Add, contentDescription = null) },
                text = { Text(stringResource(Res.string.projects_new)) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> ElecLoadingState()

                uiState.projects.isEmpty() -> ElecEmptyState(
                    title = stringResource(Res.string.projects_empty_title),
                    message = stringResource(Res.string.projects_empty_message),
                    icon = ElecIcons.Projects,
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = spacing.fabClearance),
                ) {
                    items(uiState.projects, key = { it.project.id }) { summary ->
                        ProjectCard(
                            summary = summary,
                            onClick = { onOpenProject(summary.project.id) },
                        )
                    }
                }
            }
        }
    }

}

/**
 * One job, as a card.
 *
 * The list used to be three rows of "Untitled project" behind three identical
 * folder glyphs, which is the app's own work told apart by nothing. A project
 * carries plenty that would distinguish it and none of it reached the list: the
 * supply it was set up for, how much is in it, and when it was last touched.
 *
 * A card rather than a row because a project is an object with state and not a
 * doorway to a page — which is the rule the rest of the app's lists now follow.
 *
 * Read as one accessibility node, so it is announced the way it behaves: one
 * target, one sentence.
 */
@Composable
private fun ProjectCard(summary: ProjectSummary, onClick: () -> Unit) {
    val spacing = ElecTheme.spacing
    val title = summary.title()
    val supply = summary.supply()
    val state = summary.state()
    val site = summary.project.site

    ElecCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm)
            .clearAndSetSemantics {
                contentDescription = listOfNotNull(
                    title,
                    site.takeIf { it.isNotBlank() },
                    supply,
                    state,
                ).joinToString(". ")
                role = Role.Button
            },
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (site.isNotBlank()) {
                Text(
                    text = site,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // What every circuit in the job is designed against. Two projects
            // with the same name are still two different jobs if one is 230 V
            // single phase and the other 400 V three phase in aluminium.
            Text(
                text = supply,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // How much is in it, and when it was last touched — which is what
            // tells an untitled job from the two untitled jobs beside it.
            Text(
                text = state,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** A project the user has not named yet still needs something to be called. */
@Composable
private fun ProjectSummary.title(): String =
    project.reference.ifBlank { stringResource(Res.string.projects_untitled) }

/** The supply every circuit in the job is sized against. */
@Composable
private fun ProjectSummary.supply(): String = listOf(
    stringResource(Res.string.projects_supply_volts, project.systemVoltage),
    stringResource(project.system.label()),
    stringResource(project.material.label()),
).joinToString(SEPARATOR)

/** How much is in the job, and when it was last worked on. */
@Composable
private fun ProjectSummary.state(): String = listOf(
    when (circuitCount) {
        0 -> stringResource(Res.string.projects_circuit_count_none)
        1 -> stringResource(Res.string.projects_circuit_count_one)
        else -> stringResource(Res.string.projects_circuit_count, circuitCount)
    },
    project.updatedAt.asRelativeTime(),
).joinToString(SEPARATOR)

/** The app's separator for a run of short facts. */
private const val SEPARATOR = " · "
