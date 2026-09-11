package com.kemalurekli.electricalcalculator.core.document

/**
 * The document a reader was about to export when the paywall stopped them.
 *
 * ### Why this exists rather than a navigation argument
 *
 * The paywall shows the reader their own work — the page they would have got,
 * built from the numbers they just entered — instead of a sample with somebody
 * else's circuits in it. To do that it needs the laid-out page, and a laid-out
 * page is a few hundred draw operations. Navigation arguments are serialised
 * into the back stack entry; that is the wrong pipe for this, and the wrong
 * thing to keep if the reader backs out.
 *
 * So the calculator lays the page out, leaves it here, and navigates. The
 * paywall picks it up. It lives exactly as long as that one journey.
 *
 * ### Why it is not a flow
 *
 * Nothing observes it. The page is written before the navigation that causes
 * the paywall to compose, so by the time anything reads it, it is there. A
 * flow would add a subscription to a value that changes once and is then
 * looked at once.
 */
class PendingDocument {

    /** The page to show, or null when the paywall was reached another way. */
    var page: List<PdfOp>? = null
        private set

    /** The paper [page] was laid out for. */
    var size: PdfPageSize = PdfPageSize.A4_PORTRAIT
        private set

    /**
     * Holds the first page of a document.
     *
     * Only the first: the preview is one sheet of paper on a phone screen, and
     * a reader deciding whether the thing is worth paying for has decided by
     * the end of it.
     */
    fun hold(pages: List<List<PdfOp>>, size: PdfPageSize) {
        page = pages.firstOrNull()
        this.size = size
    }

    /** Forgotten once shown, so a later paywall does not reopen an old page. */
    fun clear() {
        page = null
    }
}
