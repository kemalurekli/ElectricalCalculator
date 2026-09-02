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
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kemalurekli.electricalcalculator.core.billing.domain.BillingFailure
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecCard
import com.kemalurekli.electricalcalculator.core.designsystem.component.ElecScreenScaffold
import com.kemalurekli.electricalcalculator.core.designsystem.icon.ElecIcons
import com.kemalurekli.electricalcalculator.core.designsystem.theme.ElecTheme
import com.kemalurekli.electricalcalculator.core.navigation.PaywallReason
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_free
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_future
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_benefit_pdf
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_buy_loading
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_no_connection
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_not_configured
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_owned
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_store
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_error_unknown
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_headline_general
import com.kemalurekli.electricalcalculator.feature.pro.generated.resources.pro_headline_pdf
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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaywallRoute(
    reason: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = koinViewModel(),
) {
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val product by viewModel.product.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    // Closing on the entitlement rather than on the purchase call returning:
    // a restore, a purchase, and a receipt that arrived while the screen was
    // open all end the same way, and only one of them comes back through the
    // button that was pressed.
    LaunchedEffect(isPro) { if (isPro) onDone() }

    PaywallScreen(
        reason = reason,
        price = product?.formattedPrice,
        status = status,
        message = message,
        onBuy = viewModel::onBuy,
        onRestore = viewModel::onRestore,
        onMessageShown = viewModel::onMessageShown,
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
    price: String?,
    status: PaywallStatus,
    message: PaywallMessage?,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    onMessageShown: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
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
                    .padding(horizontal = spacing.screenHorizontal)
                    .padding(bottom = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                Text(
                    text = stringResource(reason.headline()),
                    style = MaterialTheme.typography.headlineSmall,
                )

                SchedulePreview()

                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    Benefit(stringResource(Res.string.pro_benefit_pdf))
                    Benefit(stringResource(Res.string.pro_benefit_future))
                    Benefit(stringResource(Res.string.pro_benefit_free))
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    // The price is a figure, so it is set in the face every
                    // figure in this app is set in. Inside the button it would
                    // have been drawn in the button's own face and stopped
                    // looking like one of this app's numbers.
                    if (price == null) {
                        // The store has not answered. A skeleton says the
                        // number is coming; a zero or a blank would each say
                        // something untrue.
                        ElecSkeletonLine(modifier = Modifier.width(PRICE_SKELETON))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = price, style = NumericCompactTextStyle)
                            Text(
                                text = stringResource(Res.string.pro_one_time),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = spacing.sm),
                            )
                        }
                    }

                    Button(
                        onClick = onBuy,
                        // Disabled until the store has said what it costs. A button
                        // that starts a payment without naming the amount is not one
                        // anybody should be asked to press.
                        enabled = price != null && status == PaywallStatus.IDLE,
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
                                text = if (price == null) {
                                    stringResource(Res.string.pro_buy_loading)
                                } else {
                                    stringResource(Res.string.pro_unlock)
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.pro_store_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
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
private fun Benefit(text: String) {
    val spacing = ElecTheme.spacing
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = ElecIcons.StagePass,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(BENEFIT_ICON),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = spacing.md),
        )
    }
}

/** The first line, chosen by what the reader was reaching for. */
private fun String.headline() = when (this) {
    PaywallReason.PDF_EXPORT -> Res.string.pro_headline_pdf
    else -> Res.string.pro_headline_general
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

private val BENEFIT_ICON = 20.dp
private val SPINNER = 20.dp
private val SPINNER_STROKE = 2.dp
