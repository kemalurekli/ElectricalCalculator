package com.kemalurekli.electricalcalculator.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecMotion
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme

/**
 * A search field pinned above a list, with the edge that says so.
 *
 * ### Why the hairline
 *
 * A field that does not scroll and a list that does meet at a line the reader
 * cannot see, and what they get instead is a row of text cut in half eight
 * points below the box — which reads as a rendering fault rather than as
 * content passing underneath. `docs/design-language.md` already answers this
 * for the title bar: a hairline that fades in as content scrolls under it. The
 * boundary simply moved down the screen when a pinned field was put below the
 * bar, and the line did not move with it.
 *
 * It fades rather than appearing, and it is absent at rest, because at the top
 * of a list there is nothing passing under anything and a permanent rule there
 * is a line drawn for its own sake.
 *
 * ### Why the padding is here
 *
 * Four screens pinned a search field above a list and four of them spaced it
 * differently — one with a bottom gap, one with vertical, one with neither, one
 * with its own number. None of those was a decision; they are what happens when
 * the same arrangement is written four times. It is written once now.
 *
 * @param scrolled whether the list below has moved off its top. `LazyListState`
 *   answers this as `canScrollBackward`, which is what the caller should pass.
 */
@Composable
fun ElecSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    scrolled: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    val rule by animateColorAsState(
        targetValue = if (scrolled) {
            MaterialTheme.colorScheme.outlineVariant
        } else {
            Color.Transparent
        },
        animationSpec = ElecMotion.react(),
        label = "searchHeaderRule",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        ElecSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder,
            modifier = Modifier.padding(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                top = spacing.xs,
                bottom = spacing.md,
            ),
        )
        HorizontalDivider(color = rule)
    }
}
