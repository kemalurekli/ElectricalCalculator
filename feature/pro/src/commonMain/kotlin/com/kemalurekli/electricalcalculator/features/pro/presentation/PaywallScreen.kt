package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.document.PendingDocument
import com.kemalurekli.electricalcalculator.core.document.PdfPageSize
import com.kemalurekli.electricalcalculator.core.document.PdfOp
import com.kemalurekli.electricalcalculator.core.billing.domain.BillingFailure
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.navigation.PaywallReason
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.Res as DesignRes
import com.kemalurekli.electricalcalculator.core.designsystem.generated.resources.action_retry
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_preview_document_caption
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_calculation_title
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_future_title
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_pdf
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_pdf_title
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_schedule_title
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_buy_loading
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_no_connection
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_not_configured
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_owned
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_store
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_unknown
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_nothing_to_restore
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_one_time
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_owned
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_privacy
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_privacy_url
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_restore
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_restored
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_store_note
import androidx.compose.foundation.layout.width
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecSkeletonLine
import com.kemalurekli.electricalcalculator.core.designsystem.theme.NumericCompactTextStyle
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_unlock
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaywallRoute(
    reason: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = koinViewModel(),
) {
    val pending: PendingDocument = koinInject()

    // Read once per visit. The calculator writes the page before it navigates,
    // so it is already there; capturing it here means a rotation redraws the
    // same page rather than losing it to a lifecycle callback.
    //
    // A paywall opened any other way — from Settings, from a project — clears
    // it, so an old calculation can never turn up under a headline that is not
    // about it.
    val previewPage = remember(reason) {
        if (reason == PaywallReason.CALCULATION_EXPORT) {
            pending.page
        } else {
            pending.clear()
            null
        }
    }

    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val price by viewModel.price.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    // Closing on the entitlement rather than on the purchase call returning:
    // a restore, a purchase, and a receipt that arrived while the screen was
    // open all end the same way, and only one of them comes back through the
    // button that was pressed.
    LaunchedEffect(isPro) { if (isPro) onDone() }

    PaywallScreen(
        reason = reason,
        price = price,
        status = status,
        message = message,
        onBuy = viewModel::onBuy,
        onRetryPrice = viewModel::onRetryPrice,
        onRestore = viewModel::onRestore,
        onMessageShown = viewModel::onMessageShown,
        previewPage = previewPage,
        previewSize = pending.size,
        onClose = onDone,
        modifier = modifier,
    )
}

/**
 * The screen that asks for money.
 *
 * Written here rather than rendered from the dashboard by `purchases-kmp-ui`.
 * A remote paywall earns its keep when the copy and the price are being tested
 * against each other; with one product at one price it buys the ability to
 * change words without a release, and costs the app's own typography, palette
 * and containers on the one screen where looking like the rest of the app is
 * worth the most.
 *
 * The headline comes from [reason], so somebody who tapped "export as PDF" is
 * told about the export. Naming the tier instead — "VoltageBoard Pro" — answers
 * a question they did not ask.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    reason: String,
    price: PriceState,
    status: PaywallStatus,
    message: PaywallMessage?,
    onBuy: () -> Unit,
    onRetryPrice: () -> Unit,
    onRestore: () -> Unit,
    onMessageShown: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    previewPage: List<PdfOp>? = null,
    previewSize: PdfPageSize = PdfPageSize.A4_PORTRAIT,
) {
    val spacing = ElecTheme.spacing
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    val privacyUrl = stringResource(Res.string.pro_privacy_url)

    val messageText = message?.text()
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            onMessageShown()
        }
    }

    ElecScreenScaffold(
        // Empty on purpose. The bar and the content were both claiming to be
        // the title — "VoltageBoard Pro" over "Hand the schedule over as a
        // PDF" — and only one of them can be first. The headline below wins,
        // because it is the one that says what the reader is being offered.
        title = "",
        modifier = modifier,
        // The way out is the back arrow every other screen uses. A paywall that
        // is hard to dismiss is a rejection on iOS and a bad review everywhere.
        // The tab bar is hidden here — see ElecAppShell — so this is the exit.
        onNavigateBack = onClose,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        // Read straight down: what is on offer, then what it costs, then the
        // fine print. The price used to be pinned to the bottom edge with a
        // weighted spacer above it, which put a quarter of the screen of
        // nothing between the pitch and the ask. Space at the end of a page is
        // where a page ends; space in the middle of one is a hole.
        //
        // The minimum height is still what lets the column scroll when the text
        // is long or the reader's font is large.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val available = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = available)
                    .padding(bottom = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                // Full width, so the page reaches both edges. Everything below
                // it keeps the screen inset.
                ProShowcase(subline = stringResource(Res.string.pro_one_time)) {
                    // The reader's own page when they got here by trying to
                    // export one. A sample asks somebody to imagine their work
                    // in it; their own page does not have to be imagined.
                    //
                    // And when there is no page yet — the paywall opened from
                    // Settings — no figures at all, rather than a schedule of
                    // invented circuits standing in for work nobody has done.
                    if (previewPage != null) {
                        DocumentPreview(page = previewPage, size = previewSize)
                    } else {
                        PaperStack()
                    }
                }

                if (previewPage != null) {
                    Text(
                        text = stringResource(Res.string.pro_preview_document_caption),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
                    )
                }

                // Four benefit rows used to sit here. They answered "what do I
                // get" and left "what am I paying for" to inference — and left
                // unsaid the thing most worth saying, which is how much of the
                // app was never for sale.
                ProComparison(
                    modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.screenHorizontal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    // The price is a figure, so it is set in the face every
                    // figure in this app is set in. Inside the button it would
                    // have been drawn in the button's own face and stopped
                    // looking like one of this app's numbers.
                    when (price) {
                        // The store has not answered yet. A skeleton says the
                        // number is coming; a zero or a blank would each say
                        // something untrue.
                        PriceState.Loading ->
                            ElecSkeletonLine(modifier = Modifier.width(PRICE_SKELETON))

                        // Answered with nothing, or not at all. Said in words
                        // where the number would have been, because that is
                        // where the reader is already looking, and a failure
                        // announced somewhere else is a failure they have to
                        // go and find.
                        PriceState.Unavailable -> Text(
                            text = stringResource(Res.string.pro_error_store),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        // The figure alone. "One-time payment, no subscription"
                        // used to sit beside it and now leads the page, where
                        // it reassures somebody deciding whether to read on
                        // rather than somebody who has already decided.
                        is PriceState.Ready ->
                            Text(text = price.formattedPrice, style = NumericCompactTextStyle)
                    }

                    Button(
                        // The same button asks again when there was nothing to
                        // ask with. A reader who arrived offline should not
                        // have to leave the screen and come back to find out
                        // whether the signal is any better.
                        onClick = if (price is PriceState.Unavailable) onRetryPrice else onBuy,
                        // Disabled while the store is still being asked. A button
                        // that starts a payment without naming the amount is not one
                        // anybody should be asked to press.
                        enabled = price != PriceState.Loading && status == PaywallStatus.IDLE,
                        // Gold, and the only gold control in the app. Pro is
                        // gold on the mark, gold in the panel above, and this
                        // is the press that buys it; a navy button here was the
                        // same colour as every secondary action on every other
                        // screen.
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProGold,
                            contentColor = ON_GOLD,
                        ),
                        shape = MaterialTheme.shapes.large,
                        contentPadding = PaddingValues(vertical = BUTTON_PADDING),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (status == PaywallStatus.PURCHASING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(SPINNER),
                                strokeWidth = SPINNER_STROKE,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(
                                text = when (price) {
                                    PriceState.Loading -> stringResource(Res.string.pro_buy_loading)
                                    PriceState.Unavailable ->
                                        stringResource(DesignRes.string.action_retry)
                                    is PriceState.Ready -> stringResource(Res.string.pro_unlock)
                                },
                            )
                        }
                    }
                }

                // The fine print, and the two ways out of it. Restore sits here
                // rather than beside the buy button: it is for somebody who has
                // already paid, and next to the primary action it was a second
                // control competing for the same press.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.screenHorizontal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // "The calculators stay free" used to be said here in a
                    // sentence. The table says it in three ticked rows, which
                    // is both shorter and harder to disbelieve.
                    Text(
                        text = stringResource(Res.string.pro_store_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = spacing.xs),
                    )
                    Row {
                        TextButton(
                            onClick = onRestore,
                            enabled = status == PaywallStatus.IDLE,
                        ) {
                            Text(
                                text = stringResource(Res.string.pro_restore),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        TextButton(onClick = { uriHandler.openUri(privacyUrl) }) {
                            Text(
                                text = stringResource(Res.string.pro_privacy),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun PaywallMessage.text(): String = when (this) {
    PaywallMessage.Restored -> stringResource(Res.string.pro_restored)
    PaywallMessage.NothingToRestore -> stringResource(Res.string.pro_nothing_to_restore)
    is PaywallMessage.Failed -> when (reason) {
        BillingFailure.NO_CONNECTION -> stringResource(Res.string.pro_error_no_connection)
        BillingFailure.STORE_UNAVAILABLE -> stringResource(Res.string.pro_error_store)
        BillingFailure.ALREADY_OWNED -> stringResource(Res.string.pro_error_owned)
        BillingFailure.NOT_CONFIGURED -> stringResource(Res.string.pro_error_not_configured)
        BillingFailure.UNKNOWN -> stringResource(Res.string.pro_error_unknown)
    }
}

/** Roughly the width of a formatted price, so nothing jumps when one arrives. */
private val PRICE_SKELETON = 120.dp

private val SPINNER = 20.dp
private val SPINNER_STROKE = 2.dp

/** Near-black rather than white: gold is a light colour and carries dark text. */
private val ON_GOLD = Color(0xFF16222B)

private val BUTTON_PADDING = 16.dp
