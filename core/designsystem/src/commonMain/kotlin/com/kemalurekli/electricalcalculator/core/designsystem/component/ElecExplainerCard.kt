package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.calculator_steps
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Everything a calculator has to say about the number it just produced: the
 * worked solution, the equation behind it, and the conditions under which the
 * answer stops being true.
 *
 * ### Why one card and not three
 *
 * These were three collapsible cards, stacked, all closed. Three identical
 * closed boxes in a column is the most generic thing a screen can end with —
 * and worse, it hid the part of this app that is worth paying for. Nobody
 * opened them, because nothing about a closed box says what is inside is more
 * than boilerplate.
 *
 * One card with three tabs shows the content immediately and reads as an
 * authored section rather than as three accessories. The reader loses the
 * ability to have two open at once, which is worth less than having any of
 * them open at all.
 *
 * ### Which tab opens
 *
 * Whichever the reader last chose, and before they have chosen, the leftmost
 * that exists. Steps only exist after a calculation, so a form that has not
 * been submitted opens on the formula — and the moment a result lands, the
 * card moves to the worked solution, which is what somebody who has just
 * pressed Calculate wants to see.
 *
 * The tabs are [ElecOptionPill]s, the same control a calculator's choices are
 * made of, so the app has one way of showing "one of these" rather than two.
 *
 * @param steps the worked solution. Empty before a calculation, which removes
 *   the tab rather than showing an empty one.
 * @param notes engineering caveats. Empty removes the tab.
 * @param formulaLabel and [notesLabel] come from the caller because the
 *   strings live in the calculator feature, beside every other word on these
 *   screens. The steps label is this module's own.
 */
@Composable
fun ElecExplainerCard(
    formula: String,
    variables: ImmutableList<FormulaVariable>,
    formulaLabel: String,
    notesLabel: String,
    modifier: Modifier = Modifier,
    steps: ImmutableList<CalculationStep> = persistentListOf(),
    notes: ImmutableList<String> = persistentListOf(),
    links: ImmutableList<NoteLink> = persistentListOf(),
    onLinkClick: (String) -> Unit = {},
) {
    val spacing = ElecTheme.spacing

    val tabs = buildList {
        if (steps.isNotEmpty()) add(ExplainerTab.Steps)
        add(ExplainerTab.Formula)
        if (notes.isNotEmpty()) add(ExplainerTab.Notes)
    }.toImmutableList()

    // Null until the reader picks one, so a tab appearing or disappearing
    // cannot leave the card showing nothing.
    var chosen by rememberSaveable { mutableStateOf<ExplainerTab?>(null) }
    val current = chosen?.takeIf { it in tabs } ?: tabs.first()

    ElecCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                tabs.forEach { tab ->
                    ElecOptionPill(
                        text = when (tab) {
                            ExplainerTab.Steps -> stringResource(Res.string.calculator_steps)
                            ExplainerTab.Formula -> formulaLabel
                            ExplainerTab.Notes -> notesLabel
                        },
                        selected = tab == current,
                        onSelect = { chosen = tab },
                    )
                }
            }

            AnimatedContent(
                targetState = current,
                // The tab the reader pressed is already lit; the body follows
                // it rather than announcing itself.
                transitionSpec = {
                    fadeIn(tween(durationMillis = 150, delayMillis = 60)) togetherWith
                        fadeOut(tween(durationMillis = 60))
                },
                label = "explainerBody",
            ) { tab ->
                when (tab) {
                    ExplainerTab.Steps -> StepsBody(steps = steps)
                    ExplainerTab.Formula -> FormulaBody(formula = formula, variables = variables)
                    ExplainerTab.Notes -> NotesBody(
                        notes = notes,
                        links = links,
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }
    }
}

/** The three things a calculator can explain about itself. */
private enum class ExplainerTab { Steps, Formula, Notes }

@Preview(showBackground = true)
@Composable
private fun ElecExplainerCardPreview() {
    ElecToolkitTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ElecExplainerCard(
                formula = "S = k · U · I",
                variables = persistentListOf(
                    FormulaVariable("S", "Apparent power", "VA"),
                    FormulaVariable("k", "2 for DC, √3 for three phase", "—"),
                ),
                formulaLabel = "Formula",
                notesLabel = "Engineering notes",
                notes = persistentListOf("Line values throughout."),
            )
        }
    }
}
