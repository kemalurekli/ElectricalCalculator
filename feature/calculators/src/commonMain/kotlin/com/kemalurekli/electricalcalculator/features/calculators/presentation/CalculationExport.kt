package com.kemalurekli.electricalcalculator.features.calculators.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.core.common.util.formatAsDate
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_export_pdf
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.document_notice
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
    fun export(title: String, body: String)
}

/** No export at all, for a preview or a screen outside the destination. */
private object NoCalculationExport : CalculationExport {
    override val isPro: Boolean = true
    override fun export(title: String, body: String) = Unit
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
    val notice = stringResource(DesignRes.string.document_notice)
    val chooserTitle = stringResource(DesignRes.string.action_export_pdf)

    return remember(paid, sharing, notice, chooserTitle, onShowPaywall, pending) {
        object : CalculationExport {
            override val isPro: Boolean = paid

            override fun export(title: String, body: String) {
                val blocks = body.lines().drop(1).map { line ->
                    // The first line is the title, which the page already has.
                    // The separators are a phone's idea of a rule and become a
                    // real one on paper.
                    if (line.isSeparator()) DocumentBlock.Divider else DocumentBlock.Line(line)
                }
                val pages = TextDocumentLayout.pages(
                    title = title,
                    subtitle = "$APP_NAME · ${Clock.System.now().formatAsDate()}",
                    body = blocks,
                    notice = notice,
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

/** Untranslated, like the name on the launcher. */
private const val APP_NAME = "VoltageBoard"

private const val MIME_PDF = "application/pdf"
