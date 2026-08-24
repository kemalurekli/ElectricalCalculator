package com.kemalurekli.electricalcalculator.features.projects.presentation

import androidx.compose.foundation.layout.Box
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
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecListItem
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
                        ElecListItem(
                            title = summary.title(),
                            description = summary.description(),
                            icon = ElecIcons.Projects,
                            onClick = { onOpenProject(summary.project.id) },
                        )
                    }
                }
            }
        }
    }

}

/** A project the user has not named yet still needs something to be called. */
@Composable
private fun ProjectSummary.title(): String =
    project.reference.ifBlank { stringResource(Res.string.projects_untitled) }

/**
 * The site if there is one, otherwise how much is in the job.
 *
 * The count is the more useful of the two on a list of jobs that are all on the
 * same site, and the site name is the more useful when they are not — so
 * whichever the user has actually filled in is the one shown.
 */
@Composable
private fun ProjectSummary.description(): String = project.site.ifBlank {
    when (circuitCount) {
        0 -> stringResource(Res.string.projects_circuit_count_none)
        1 -> stringResource(Res.string.projects_circuit_count_one)
        else -> stringResource(Res.string.projects_circuit_count, circuitCount)
    }
}
