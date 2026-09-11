package com.kemalurekli.electricalcalculator.features.references.presentation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackArea
import com.kemalurekli.electricalcalculator.core.feedback.presentation.reportIssueItem
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.component.rememberElecScrollBehavior
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteKind
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSectionHeader
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecToolkitTheme
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle
import com.kemalurekli.electricalcalculator.core.ui.layout.currentWindowLayout
import com.kemalurekli.electricalcalculator.features.references.domain.CalloutKind
import com.kemalurekli.electricalcalculator.features.references.domain.DrawingSymbol
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceBlock
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceRow
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceText
import com.kemalurekli.electricalcalculator.features.references.domain.SymbolPair
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.dashboard_references_title
import com.kemalurekli.electricalcalculator.feature.references.generated.resources.ref_source_label
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignSystemRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_add
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_favorite_remove
import org.jetbrains.compose.resources.StringResource

/**
 * One reference topic's tables.
 *
 * The content is compile-time data, so the screen holds no state of its own; an
 * unknown key renders nothing rather than crashing, and a stale deep link or a
 * back stack from an older release degrades quietly.
 *
 * The one exception is the pin, which reads and writes a database. It lives in
 * [FavoriteToggleViewModel] rather than turning this into a stateful screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceDetailRoute(
    topicKey: String,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    favoriteViewModel: FavoriteToggleViewModel = koinViewModel(),
) {
    val topic = ReferenceCatalog.topicOrNull(topicKey)
    val isFavorite by favoriteViewModel.isFavorite.collectAsStateWithLifecycle()

    LaunchedEffect(topicKey) {
        favoriteViewModel.observe(FavoriteItem(FavoriteKind.REFERENCE, topicKey))
    }

    val spacing = ElecTheme.spacing
    val layout = currentWindowLayout()
    val scrollBehavior = rememberElecScrollBehavior()

    ElecScreenScaffold(
        title = topic?.let { stringResource(it.title) }
                        ?: stringResource(Res.string.dashboard_references_title),
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        actions = {
            if (topic != null) {
                IconButton(onClick = favoriteViewModel::toggle) {
                    Icon(
                        imageVector = if (isFavorite) ElecIcons.FavoriteOn
                        else ElecIcons.FavoriteOff,
                        contentDescription = stringResource(
                            if (isFavorite) DesignSystemRes.string.action_favorite_remove
                            else DesignSystemRes.string.action_favorite_add,
                        ),
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->
        if (topic == null) return@ElecScreenScaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            item(key = "source") {
                // Named up front: a reference table is only as trustworthy
                // as the document behind it, and one that cites nothing
                // invites more trust than it has earned.
                Text(
                    text = stringResource(
                        Res.string.ref_source_label,
                        stringResource(topic.source),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = spacing.screenHorizontal,
                        vertical = spacing.xs,
                    ),
                )
            }

            topic.sections.forEachIndexed { sectionIndex, section ->
                item(key = "header-$sectionIndex") {
                    ElecSectionHeader(title = stringResource(section.title))
                }

                section.blocks.forEachIndexed { blockIndex, block ->
                    val blockKey = "$sectionIndex-$blockIndex"
                    when (block) {
                        is ReferenceBlock.Table -> items(
                            count = block.rows.size,
                            key = { index -> "$blockKey-row-$index" },
                        ) { index ->
                            TableRow(block.rows[index], striped = index % 2 == 1)
                        }

                        is ReferenceBlock.Prose -> items(
                            count = block.paragraphs.size,
                            key = { index -> "$blockKey-para-$index" },
                        ) { index ->
                            Paragraph(block.paragraphs[index])
                        }

                        is ReferenceBlock.Ordered -> items(
                            count = block.steps.size,
                            key = { index -> "$blockKey-step-$index" },
                        ) { index ->
                            ProcedureStep(number = index + 1, text = block.steps[index])
                        }

                        is ReferenceBlock.Comparison -> item(key = blockKey) {
                            ComparisonTable(block)
                        }

                        is ReferenceBlock.SymbolGrid -> items(
                            count = block.symbols.size,
                            key = { index -> "$blockKey-sym-${block.symbols[index].key}" },
                        ) { index ->
                            SymbolRow(block.symbols[index])
                        }

                        is ReferenceBlock.SymbolComparison -> {
                            item(key = "$blockKey-head") {
                                SymbolComparisonHeader(block)
                            }
                            items(
                                count = block.pairs.size,
                                key = { index -> "$blockKey-pair-${block.pairs[index].key}" },
                            ) { index ->
                                SymbolPairRow(block.pairs[index])
                            }
                        }

                        is ReferenceBlock.Callout -> item(key = blockKey) {
                            Callout(kind = block.kind, text = block.text)
                        }
                    }
                }

                section.footnote?.let { footnote ->
                    item(key = "footnote-$sectionIndex") {
                        Text(
                            text = stringResource(footnote),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = spacing.screenHorizontal,
                                vertical = spacing.sm,
                            ),
                        )
                    }
                }

                item(key = "divider-$sectionIndex") {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
                    )
                }
            }

            // A reference table is the one kind of page here whose mistakes are
            // somebody else's too: these figures are transcribed from standards
            // and manufacturers' guides, and a transcription is exactly the sort
            // of thing a reader in the trade spots first.
            reportIssueItem(FeedbackArea.reference(topicKey))
        }
    }
}

@Composable
private fun Paragraph(text: StringResource) {
    val spacing = ElecTheme.spacing
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(
            horizontal = spacing.screenHorizontal,
            vertical = spacing.xs,
        ),
    )
}

/**
 * One step of a procedure, numbered by position.
 *
 * The number is drawn rather than written into the string so a translator
 * cannot renumber the sequence by accident, and so inserting a step does not
 * mean editing every string after it.
 */
@Composable
private fun ProcedureStep(number: Int, text: StringResource) {
    val spacing = ElecTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(min = STEP_NUMBER_WIDTH),
        )
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * A boxed aside.
 *
 * A safety note is coloured from the error role, which is the loudest thing the
 * theme has and is reserved for content where ignoring it hurts someone. A
 * warning and a tip step down from there, so the three are distinguishable at a
 * glance without any of them reading as body text.
 */
@Composable
private fun Callout(kind: CalloutKind, text: StringResource) {
    val spacing = ElecTheme.spacing
    val scheme = MaterialTheme.colorScheme
    val container = when (kind) {
        CalloutKind.SAFETY -> scheme.errorContainer
        CalloutKind.WARNING -> scheme.tertiaryContainer
        CalloutKind.TIP -> scheme.surfaceContainerHigh
    }
    val content = when (kind) {
        CalloutKind.SAFETY -> scheme.onErrorContainer
        CalloutKind.WARNING -> scheme.onTertiaryContainer
        CalloutKind.TIP -> scheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
        shape = RoundedCornerShape(CALLOUT_CORNER),
        color = container,
        contentColor = content,
    ) {
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(spacing.lg),
        )
    }
}

/**
 * Options side by side, scrolling sideways when they do not fit.
 *
 * ### Why it scrolls rather than reflows
 *
 * Four options will not fit across a phone, and the two ways out both cost more
 * than they save. Stacking one card per option destroys the comparison — the
 * whole point is that 2–4 × In is read against the 6–8 × In beside it. Shrinking
 * the text until it fits makes a table nobody reads on the device they are
 * holding.
 *
 * So the attribute column stays put, the options scroll, and the reader compares
 * one row at a time. Rows alternate shading because tracking a line across a
 * scrolling table is exactly where the eye slips.
 */
@Composable
private fun ComparisonTable(block: ReferenceBlock.Comparison) {
    val spacing = ElecTheme.spacing
    // One state shared by every row, so the columns move together. Built
    // row-major rather than column-major because a Row sizes itself to its
    // tallest cell: separate columns drift apart the moment one label wraps to
    // two lines, and then the reader is comparing the wrong figures.
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
    ) {
        ComparisonLine(
            label = "",
            cells = block.columns.map { stringResource(it) },
            scroll = scroll,
            isHeader = true,
            shaded = false,
        )

        block.rows.forEachIndexed { index, row ->
            ComparisonLine(
                label = stringResource(row.label),
                // Guarded by ReferenceCatalogTest; padded rather than crashing
                // if a ragged row ever ships.
                cells = List(block.columns.size) { column ->
                    row.cells.getOrNull(column)?.resolve().orEmpty()
                },
                scroll = scroll,
                isHeader = false,
                shaded = index % 2 == 1,
            )
        }
    }
}

@Composable
private fun ComparisonLine(
    label: String,
    cells: List<String>,
    scroll: ScrollState,
    isHeader: Boolean,
    shaded: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (shaded) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent,
            ),
    ) {
        ComparisonCell(
            text = label,
            isHeader = isHeader,
            emphasised = !isHeader,
            modifier = Modifier.width(COMPARISON_LABEL_WIDTH),
        )
        Row(modifier = Modifier.horizontalScroll(scroll)) {
            cells.forEach { cell ->
                ComparisonCell(
                    text = cell,
                    isHeader = isHeader,
                    emphasised = false,
                    modifier = Modifier.width(COMPARISON_CELL_WIDTH),
                )
            }
        }
    }
}

@Composable
private fun ComparisonCell(
    text: String,
    isHeader: Boolean,
    emphasised: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = ElecTheme.spacing
    Text(
        text = text,
        style = when {
            isHeader -> MaterialTheme.typography.labelLarge
            emphasised -> MaterialTheme.typography.labelMedium
            else -> MaterialTheme.typography.bodySmall
        },
        color = when {
            isHeader -> MaterialTheme.colorScheme.primary
            emphasised -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurface
        },
        modifier = modifier
            .heightIn(min = COMPARISON_ROW_MIN_HEIGHT)
            .padding(horizontal = spacing.sm, vertical = spacing.sm),
    )
}

/**
 * One symbol: the drawing, its name, its designation letter, and the note that
 * tells it apart from the symbol it is mistaken for.
 *
 * Laid out as a row rather than a grid of tiles. A tile grid fits more symbols
 * on screen and has nowhere to put the note, and the note is the reason a reader
 * is on this page rather than looking at any of the hundred symbol charts on the
 * internet.
 */
@Composable
private fun SymbolRow(symbol: DrawingSymbol) {
    val spacing = ElecTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        Surface(
            shape = RoundedCornerShape(CALLOUT_CORNER),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Icon(
                imageVector = symbol.image,
                // The name beside it is the label; the drawing is decorative to
                // a screen reader.
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(spacing.sm)
                    .size(SYMBOL_TILE),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Text(
                    text = stringResource(symbol.name),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                )
                symbol.designation?.let { letter ->
                    Text(
                        text = letter,
                        style = NumericCompactTextStyle,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            symbol.note?.let { note ->
                Text(
                    text = stringResource(note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SymbolComparisonHeader(block: ReferenceBlock.SymbolComparison) {
    val spacing = ElecTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        Text(
            text = stringResource(block.leftLabel),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(SYMBOL_COLUMN),
        )
        Text(
            text = stringResource(block.rightLabel),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(SYMBOL_COLUMN),
        )
    }
}

/** One element in both conventions, with its name and the difference below. */
@Composable
private fun SymbolPairRow(pair: SymbolPair) {
    val spacing = ElecTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        // Name first: scrolling past a pair and having to look *below* the
        // drawings to find out what they are is the wrong way round.
        Text(
            text = stringResource(pair.name),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.lg)) {
            listOf(pair.left, pair.right).forEach { image ->
                Surface(
                    shape = RoundedCornerShape(CALLOUT_CORNER),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.width(SYMBOL_COLUMN),
                ) {
                    Icon(
                        imageVector = image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(spacing.sm)
                            .size(SYMBOL_TILE),
                    )
                }
            }
        }
        pair.note?.let { note ->
            Text(
                text = stringResource(note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val SYMBOL_COLUMN = 64.dp
private val SYMBOL_TILE = 44.dp
private val STEP_NUMBER_WIDTH = 24.dp
private val CALLOUT_CORNER = 12.dp
private val COMPARISON_LABEL_WIDTH = 104.dp
private val COMPARISON_CELL_WIDTH = 128.dp
private val COMPARISON_ROW_MIN_HEIGHT = 40.dp

/**
 * One row of a reference table.
 *
 * Striped on alternate rows. A run of ten label-and-value rows loose on the page
 * reads as ten separate facts; the banding is what makes it read as one table,
 * and it does so without a rule under every row or a card around the lot — both
 * of which are heavier than the data they would be holding.
 *
 * Painted before the inset rather than after, so the band runs to both edges the
 * way a table's does.
 */
@Composable
private fun TableRow(row: ReferenceRow, striped: Boolean) {
    val spacing = ElecTheme.spacing
    val label = row.label.resolve()
    val value = row.value.resolve()
    val note = row.note?.resolve()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (striped) {
                    MaterialTheme.colorScheme.surfaceContainerLow
                } else {
                    Color.Transparent
                },
            )
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm)
            // Read as one phrase: three separate nodes per row would make a
            // ten-row table thirty swipes to get through.
            .clearAndSetSemantics {
                contentDescription = listOfNotNull(label, value, note).joinToString(". ")
            },
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = NumericCompactTextStyle,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(LABEL_WEIGHT),
        )
        Column(modifier = Modifier.weight(VALUE_WEIGHT)) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
            if (note != null) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReferenceText.resolve(): String = when (this) {
    is ReferenceText.Symbol -> text
    is ReferenceText.Localized -> stringResource(res)
    // Formatted here rather than in the catalog so the decimal separator
    // follows the reader's locale, including after an in-app language change.
    is ReferenceText.Quantity ->
        NumberFormatter.formatSignificant(value, significantDigits).withUnit(unit)

    is ReferenceText.Range -> buildString {
        append(NumberFormatter.formatSignificant(low, significantDigits))
        append(" – ")
        append(NumberFormatter.formatSignificant(high, significantDigits).withUnit(unit))
    }
}

private fun String.withUnit(unit: String): String = if (unit.isEmpty()) this else "$this $unit"

private const val LABEL_WEIGHT = 1f
private const val VALUE_WEIGHT = 2.2f

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun ReferenceDetailPreview() {
    ElecToolkitTheme {
        ReferenceDetailRoute(topicKey = "ip_rating", onNavigateBack = {})
    }
}
