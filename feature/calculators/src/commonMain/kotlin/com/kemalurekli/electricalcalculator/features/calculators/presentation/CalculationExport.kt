package com.kemalurekli.electricalcalculator.features.calculators.presentation

import androidx.compose.runtime.Composable
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.calculator_steps
import com.kemalurekli.electricalcalculator.core.designsystem.model.CalculationStep
import com.kemalurekli.electricalcalculator.core.document.DocumentNotice
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.calculator_formula
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDate
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_export_pdf
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.document_notice
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.document_notice_label
import com.kemalurekli.electricalcalculator.core.designsystem.platform.rememberFileSharing
import com.kemalurekli.electricalcalculator.core.designsystem.platform.safeFileName
import com.kemalurekli.electricalcalculator.core.document.DocumentBlock
import com.kemalurekli.electricalcalculator.core.document.PendingDocument
import com.kemalurekli.electricalcalculator.core.document.TextDocumentLayout
import com.kemalurekli.electricalcalculator.core.document.pdfTextMeasurer
import com.kemalurekli.electricalcalculator.core.document.renderPdf
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Turning a calculation into a document, from any of the twenty-two screens.
 *
 * ### Why a composition local rather than a parameter
 *
 * Every calculator screen already builds the text this needs: the same
 * translated, formatted summary that copy and share hand over. What it does not
 * have is the entitlement, the file sharing and the way to the paywall, and
 * threading those through twenty-two route signatures would be twenty-two
 * mechanical edits that say nothing about any of the calculators.
 *
 * So the capability is provided once, by the destination that dispatches to
 * them, and a screen reaches for it where it already reaches for copy and
 * share. Static, because it changes only when the reader buys.
 */
interface CalculationExport {

    /** Whether the document can be produced rather than sold. */
    val isPro: Boolean

    /**
     * Renders [body] as a PDF and offers it to another app.
     *
     * When Pro is not owned the page is laid out anyway and left where the
     * paywall can show it, so the reader is asked to pay for a document they
     * are looking at rather than one they have to imagine. The screens do not
     * check first: a locked action that silently does nothing is the failure
     * this is written to avoid, and one that shows its own output is a better
     * answer than one that explains itself.
     */
    fun export(
        title: String,
        formula: String,
        body: String,
        steps: List<DocumentStep>,
    )
}

/**
 * One line of working, with its labels already translated.
 *
 * Resolved in composition rather than carried as string resources, because the
 * export runs outside it: a `StringResource` is a key, and the thing that turns
 * a key into a sentence is a `@Composable`.
 */
data class DocumentStep(
    val label: String,
    val formula: String,
    val substitution: String,
    val result: String,
)

/** Translates a calculator's working into something the document can print. */
@Composable
fun rememberDocumentSteps(steps: ImmutableList<CalculationStep>): List<DocumentStep> =
    steps.map { step ->
        DocumentStep(
            label = stringResource(step.label),
            formula = step.formula,
            substitution = step.substitution,
            result = step.result,
        )
    }

/** No export at all, for a preview or a screen outside the destination. */
private object NoCalculationExport : CalculationExport {
    override val isPro: Boolean = true
    override fun export(
        title: String,
        formula: String,
        body: String,
        steps: List<DocumentStep>,
    ) = Unit
}

val LocalCalculationExport = staticCompositionLocalOf<CalculationExport> { NoCalculationExport }

/**
 * Builds the capability the calculator screens consume.
 *
 * @param onShowPaywall where an unpaid reader is sent. Taken rather than
 *   navigated to from here, because this module knows nothing about routes.
 */
@Composable
fun rememberCalculationExport(
    onShowPaywall: () -> Unit,
    entitlements: EntitlementRepository = koinInject(),
    pending: PendingDocument = koinInject(),
): CalculationExport {
    // Not named `isPro`. The object below has a member of that name, and an
    // unqualified read inside its methods binds to this local rather than to
    // the member — which is invisible while the two hold the same value and
    // is a screen that says "unlocked" and behaves "locked" the moment they
    // do not.
    val paid by entitlements.isPro.collectAsStateWithLifecycle()
    val sharing = rememberFileSharing()
    val chooserTitle = stringResource(DesignRes.string.action_export_pdf)
    val formulaLabel = stringResource(Res.string.calculator_formula)
    val stepsLabel = stringResource(DesignRes.string.calculator_steps)

    // The reader's own language, and English underneath it unless they are the
    // same. A document produced here may be read by somebody who does not share
    // the language it was made in, and the one paragraph that has to survive
    // that is the one saying the figures are not a design.
    val notices = buildList {
        val label = stringResource(DesignRes.string.document_notice_label)
        val body = stringResource(DesignRes.string.document_notice)
        add(DocumentNotice(label = label, body = body))
        if (body != ENGLISH_NOTICE) {
            add(DocumentNotice(label = ENGLISH_NOTICE_LABEL, body = ENGLISH_NOTICE))
        }
    }

    return remember(paid, sharing, notices, chooserTitle, onShowPaywall, pending) {
        object : CalculationExport {
            override val isPro: Boolean = paid

            override fun export(
                title: String,
                formula: String,
                body: String,
                steps: List<DocumentStep>,
            ) {
                val blocks = buildList {
                    // The formula first. Somebody checking a number wants to
                    // see what was applied before they see what came out, and
                    // the working after both, because it is only worth reading
                    // if one of the first two looks wrong.
                    if (formula.isNotBlank()) {
                        add(DocumentBlock.Heading(formulaLabel))
                        formula.lines().forEach { add(DocumentBlock.Expression(it)) }
                        add(DocumentBlock.Divider)
                    }
                    body.lines().drop(1).forEach { line ->
                        // The first line is the title, which the page already
                        // has. The separators are a phone's idea of a rule and
                        // become a real one on paper.
                        add(
                            if (line.isSeparator()) {
                                DocumentBlock.Divider
                            } else {
                                DocumentBlock.Line(line)
                            },
                        )
                    }
                    if (steps.isNotEmpty()) {
                        add(DocumentBlock.Divider)
                        add(DocumentBlock.Heading(stepsLabel))
                        steps.forEach { step ->
                            add(DocumentBlock.Line(step.label))
                            add(DocumentBlock.Expression(step.formula))
                            add(DocumentBlock.Expression("= ${step.substitution}"))
                            add(DocumentBlock.Expression("= ${step.result}"))
                        }
                    }
                }
                val pages = TextDocumentLayout.pages(
                    title = title,
                    body = blocks,
                    notices = notices,
                    measurer = pdfTextMeasurer,
                )
                if (!isPro) {
                    // Laid out with the same arithmetic as the real thing,
                    // because it *is* the real thing: the paywall draws these
                    // operations on a canvas instead of on paper.
                    pending.hold(pages, TextDocumentLayout.PAGE)
                    onShowPaywall()
                    return
                }

                sharing.share(
                    fileName = "${safeFileName(title, APP_NAME)}.pdf",
                    mimeType = MIME_PDF,
                    bytes = renderPdf(pages, TextDocumentLayout.PAGE),
                    chooserTitle = chooserTitle,
                )
            }
        }
    }
}

/**
 * The rule the calculators draw between inputs and results.
 *
 * Matched by shape rather than by the exact string: the separator is a run of
 * em dashes and spaces, and a screen that ever writes two instead of three
 * should still get a rule rather than a line of punctuation.
 */
private fun String.isSeparator(): Boolean {
    val trimmed = trim()
    return trimmed.isNotEmpty() && trimmed.all { it == '—' || it == ' ' }
}

/**
 * The disclaimer in English, as a constant rather than a resource.
 *
 * Every document carries it, whatever the reader's language, so it cannot come
 * from a string table that resolves to the language in use. The comparison
 * against the resolved body is what stops an English reader getting it twice —
 * exact, and needing no locale API to ask a question the strings already answer.
 */
private const val ENGLISH_NOTICE_LABEL = "Warning:"

private const val ENGLISH_NOTICE =
    "These results are not definitive. This application was produced with the help of " +
        "artificial-intelligence tools and may therefore contain errors. It must not be used " +
        "for any scientific, academic or engineering purpose. Checking the accuracy of every " +
        "result is the user's own responsibility, and VoltageBoard can in no way be held liable."

/** Untranslated, like the name on the launcher. */
private const val APP_NAME = "VoltageBoard"

private const val MIME_PDF = "application/pdf"
