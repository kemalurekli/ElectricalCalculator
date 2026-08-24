package com.kemalurekli.electricalcalculator.features.forum.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_new_thread_body_hint
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_new_thread_destination
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_new_thread_failed
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_new_thread_needs_body
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_new_thread_send
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_new_thread_title
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_new_thread_title_hint
import com.kemalurekli.electricalcalculator.feature.forum.generated.resources.forum_title_too_short
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Opening a thread: a title, and the question itself. */
@Composable
fun ForumComposeThreadRoute(
    onThreadCreated: (threadId: String, title: String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: ForumComposeThreadViewModel = koinViewModel(),
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val body by viewModel.body.collectAsStateWithLifecycle()
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val failed by viewModel.failed.collectAsStateWithLifecycle()
    val created by viewModel.created.collectAsStateWithLifecycle()

    // Navigating on a state change rather than from the button's onClick: the
    // send is asynchronous, and a click handler that navigates optimistically
    // would open a thread that may not exist.
    //
    // Inside a LaunchedEffect because navigation is a side effect, and running
    // it straight from the composable body means it fires again on every
    // recomposition that happens before the state clears.
    LaunchedEffect(created) {
        created?.let { id ->
            onThreadCreated(id, title)
            viewModel.onNavigated()
        }
    }

    ForumComposeThreadScreen(
        title = title,
        body = body,
        categoryTitle = viewModel.categoryTitle,
        categoryKey = viewModel.categoryKey,
        sending = sending,
        failed = failed,
        onTitleChange = viewModel::onTitleChange,
        onBodyChange = viewModel::onBodyChange,
        onSend = viewModel::onSend,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * A page to write on, rather than a form to fill in.
 *
 * The fields carry no outlines and no labels. Two boxed inputs of equal weight
 * gave a one-line title and a whole question the same standing, and left the
 * lower half of the screen empty under them — which is the shape of a form that
 * has run out of questions, not of somewhere to write. Here the title is set at
 * heading size, a rule separates it from the body, and the body takes every
 * pixel that is left and scrolls inside itself.
 *
 * What that removes in affordance is paid back three ways: the title takes
 * focus on arrival with the keyboard already up, both fields hold placeholder
 * text until they are written in, and the rule sits where the boundary is.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumComposeThreadScreen(
    title: String,
    body: String,
    sending: Boolean,
    failed: Boolean,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSend: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    categoryTitle: String = "",
    categoryKey: String = "",
) {
    val spacing = ElecTheme.spacing
    val titleFocus = remember { FocusRequester() }
    val bodyFocus = remember { FocusRequester() }
    val complete = composerHint(title, body) == ComposerHint.NONE

    // The keyboard is up before the writer taps anything. They came here to
    // write, and the first field is never the wrong one to start in.
    LaunchedEffect(Unit) { titleFocus.requestFocus() }

    ElecScreenScaffold(
        title = stringResource(Res.string.forum_new_thread_title),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        actions = {
            // In the bar rather than under the fields. At the bottom of
            // a growing body field it moved down with every newline
            // typed until it was off the screen — the writer had to
            // dismiss the keyboard and scroll to find the way to post.
            //
            // Filled rather than a text button: it is the one thing this
            // screen is for, and on a page that is otherwise all text it is
            // also the only colour.
            Button(
                onClick = onSend,
                enabled = complete && !sending,
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                modifier = Modifier.padding(end = spacing.sm),
            ) {
                Text(stringResource(Res.string.forum_new_thread_send))
            }
        },
        // No scroll behaviour: the page itself does not scroll. Only the body
        // does, inside its own bounds, and a bar that reacted to that would
        // collapse while somebody was simply typing.
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // No imePadding here. The Scaffold's safeDrawing insets
                // already include the keyboard, and adding it again counts the
                // keyboard twice — which pushes the field being typed into off
                // the top of the screen.
                .padding(innerPadding)
                .padding(horizontal = spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            // Where the question is going. Named on the screen because "Open a
            // thread" is true of all six sections and says nothing about which
            // one this is — and because a question written for the wrong board
            // is only discovered after it has been posted.
            if (categoryTitle.isNotEmpty()) {
                DestinationCard(categoryTitle = categoryTitle, categoryKey = categoryKey)
            }

            ComposerField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = stringResource(Res.string.forum_new_thread_title_hint),
                textStyle = MaterialTheme.typography.headlineSmall,
                maxLength = TITLE_MAX_LENGTH,
                singleLine = true,
                imeAction = ImeAction.Next,
                onImeAction = { bodyFocus.requestFocus() },
                modifier = Modifier.fillMaxWidth(),
                focusRequester = titleFocus,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ComposerField(
                value = body,
                onValueChange = onBodyChange,
                placeholder = stringResource(Res.string.forum_new_thread_body_hint),
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLength = BODY_MAX_LENGTH,
                // Takes what is left of the screen and scrolls inside itself
                // once the writing outgrows it. Nothing below it moves.
                modifier = Modifier.fillMaxWidth().weight(1f),
                focusRequester = bodyFocus,
            )

            ComposerFooter(
                title = title,
                body = body,
                failed = failed,
            )
        }
    }
}

/**
 * The section the thread will appear in.
 *
 * Carries the same glyph as the card the writer tapped to get here, so the two
 * screens read as one place rather than two.
 */
@Composable
private fun DestinationCard(categoryTitle: String, categoryKey: String) {
    val spacing = ElecTheme.spacing

    ElecCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(DESTINATION_ICON_BOX)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = forumCategoryIcon(categoryKey),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(DESTINATION_ICON),
                )
            }

            Column(modifier = Modifier.padding(start = spacing.md)) {
                Text(
                    text = stringResource(Res.string.forum_new_thread_destination),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

/**
 * One line under the page saying what is still missing, and how much room is
 * left when that starts to matter.
 *
 * The post button used to be grey with nothing to say why — the schema wants
 * five characters of title and two of body, and neither rule was anywhere on
 * the screen until it had already been broken. The counters used to be the
 * opposite problem: "0 / 8000" under an empty field is a limit nobody is near.
 *
 * The row keeps its height whether or not it has anything in it, so the writing
 * area above does not resize on the keystroke that satisfies the last rule.
 */
@Composable
private fun ComposerFooter(title: String, body: String, failed: Boolean) {
    val spacing = ElecTheme.spacing

    val hint = when {
        failed -> stringResource(Res.string.forum_new_thread_failed)
        else -> when (composerHint(title, body)) {
            ComposerHint.TITLE_TOO_SHORT ->
                stringResource(Res.string.forum_title_too_short, TITLE_MIN_LENGTH)

            ComposerHint.NEEDS_BODY -> stringResource(Res.string.forum_new_thread_needs_body)
            ComposerHint.NONE -> null
        }
    }

    val counter = composerCounter(title, body)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = spacing.sm)
            .defaultMinSize(minHeight = FOOTER_HEIGHT),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = hint.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = if (failed) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        if (counter != null) {
            Text(
                text = counter,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * A field with nothing around it: the text, a cursor, and a placeholder while
 * it is empty.
 *
 * Built on [BasicTextField] rather than a Material field with its decoration
 * turned off, because those keep their own 16dp of internal padding and the
 * title would sit indented from the rule under it.
 */
@Composable
private fun ComposerField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    maxLength: Int,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
) {
    Box(modifier) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        BasicTextField(
            value = value,
            onValueChange = { proposed ->
                // Enforced on input rather than reported afterwards: a title
                // silently truncated on the server is worse than one that
                // visibly stops growing.
                if (proposed.length <= maxLength) onValueChange(proposed)
            },
            modifier = Modifier
                // A single-line title sits in a box that wraps its own height;
                // the body's box was given the rest of the screen and the field
                // has to fill it, or the untouched part below is not tappable.
                .then(if (singleLine) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
                .focusRequester(focusRequester),
            textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(onNext = { onImeAction() }),
        )
    }
}

/**
 * What the page is still short of, in the order the writer will meet it.
 *
 * Named rather than derived at the point of drawing, because the same answer
 * decides two things that must never disagree: what the line under the page
 * says, and whether the post button works. When those were two expressions the
 * button could be grey with nothing above it saying why.
 */
internal enum class ComposerHint { TITLE_TOO_SHORT, NEEDS_BODY, NONE }

/** The schema's own checks, asked in reading order. */
internal fun composerHint(title: String, body: String): ComposerHint = when {
    title.trim().length < TITLE_MIN_LENGTH -> ComposerHint.TITLE_TOO_SHORT
    body.trim().length < BODY_MIN_LENGTH -> ComposerHint.NEEDS_BODY
    else -> ComposerHint.NONE
}

/**
 * "used / limit", for whichever field is near its ceiling — and nothing at all
 * while both have room.
 *
 * A count under an empty field is a limit nobody is anywhere near; the writer
 * reads it once, learns it never matters, and then does not read it on the one
 * occasion it does. It appears late so that it means something when it appears.
 */
internal fun composerCounter(title: String, body: String): String? = when {
    TITLE_MAX_LENGTH - title.length <= COUNTER_SHOWS_WITHIN ->
        "${title.length} / $TITLE_MAX_LENGTH"

    BODY_MAX_LENGTH - body.length <= BODY_COUNTER_SHOWS_WITHIN ->
        "${body.length} / $BODY_MAX_LENGTH"

    else -> null
}

/** Matches the schema's checks: title 5–140, body 2–8000. */
private const val TITLE_MIN_LENGTH = 5
private const val TITLE_MAX_LENGTH = 140
private const val BODY_MIN_LENGTH = 2
private const val BODY_MAX_LENGTH = 8000

/** How near a limit has to be before the count is worth the writer's attention. */
private const val COUNTER_SHOWS_WITHIN = 20

/** A body has far more to run through, so its warning starts proportionally sooner. */
private const val BODY_COUNTER_SHOWS_WITHIN = 200

private val FOOTER_HEIGHT = 20.dp
private val DESTINATION_ICON_BOX = 40.dp
private val DESTINATION_ICON = 22.dp
