package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.billing.domain.BillingFailure
import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.domain.PurchaseOutcome
import com.kemalurekli.electricalcalculator.core.billing.domain.RestoreOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * What the paywall is doing, so the screen never offers two buttons at once.
 *
 * A store call can take seconds and opens a system sheet on top of the app.
 * Without a state the reader can press "buy" twice and be shown two sheets, and
 * the second one fails with an error that reads as if the first had.
 */
enum class PaywallStatus { IDLE, PURCHASING, RESTORING }

/**
 * What the screen knows about the price.
 *
 * A nullable `ProProduct` could not say this. Null meant both "the store has
 * not answered yet" and "the store is never going to answer", and the screen,
 * having no way to tell them apart, said the first one forever: a disabled
 * button reading "Contacting the store…" half a minute after the request had
 * already failed. Offline — which for this app means on a site, which is where
 * it is used — that was the whole paywall.
 */
sealed interface PriceState {

    data object Loading : PriceState

    data class Ready(val formattedPrice: String) : PriceState

    /** Asked and not answered. Recoverable: the reader can ask again. */
    data object Unavailable : PriceState
}

/** What the screen has to say after a store call came back. */
sealed interface PaywallMessage {

    data object Restored : PaywallMessage

    data object NothingToRestore : PaywallMessage

    data class Failed(val reason: BillingFailure) : PaywallMessage
}

class PaywallViewModel(
    private val entitlements: EntitlementRepository,
) : ViewModel() {

    val isPro: StateFlow<Boolean> = entitlements.isPro

    private val _priceAsked = MutableStateFlow(false)

    /**
     * Loading until the first ask has finished, then whatever it found.
     *
     * Derived rather than stored, so a price that arrives later — a retry, or a
     * refresh from somewhere else — moves the screen without anything having to
     * remember to.
     */
    val price: StateFlow<PriceState> = combine(entitlements.product, _priceAsked) { product, asked ->
        when {
            product != null -> PriceState.Ready(product.formattedPrice)
            asked -> PriceState.Unavailable
            else -> PriceState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), PriceState.Loading)

    private val _status = MutableStateFlow(PaywallStatus.IDLE)
    val status: StateFlow<PaywallStatus> = _status.asStateFlow()

    private val _message = MutableStateFlow<PaywallMessage?>(null)
    val message: StateFlow<PaywallMessage?> = _message.asStateFlow()

    init {
        // The price is fetched on arrival rather than at launch: it is the one
        // thing on this screen that has to come off the network, and asking for
        // it on every cold start would spend a request most readers never see.
        askForPrice()
    }

    /** Asks the store again, for a reader who was offline when they arrived. */
    fun onRetryPrice() {
        if (price.value == PriceState.Unavailable) askForPrice()
    }

    private fun askForPrice() {
        _priceAsked.value = false
        viewModelScope.launch {
            // `refresh` swallows its own failures and cannot throw, which
            // leaves one way for it never to come back: a request that is
            // neither answered nor refused. A captive wifi portal does exactly
            // that. The timeout is what makes "asked" eventually true.
            withTimeoutOrNull(PRICE_TIMEOUT_MILLIS) { entitlements.refresh() }
            _priceAsked.value = true
        }
    }

    fun onBuy() {
        if (_status.value != PaywallStatus.IDLE) return
        viewModelScope.launch {
            _status.value = PaywallStatus.PURCHASING
            when (val outcome = entitlements.purchase()) {
                // Nothing is said on success. The screen closes and the feature
                // the reader came for is open, which says it better.
                PurchaseOutcome.Purchased -> Unit

                // Backing out is a decision, not an error. Telling them it
                // failed would be telling them off.
                PurchaseOutcome.Cancelled -> Unit

                is PurchaseOutcome.Failed -> _message.value = PaywallMessage.Failed(outcome.reason)
            }
            _status.value = PaywallStatus.IDLE
        }
    }

    fun onRestore() {
        if (_status.value != PaywallStatus.IDLE) return
        viewModelScope.launch {
            _status.value = PaywallStatus.RESTORING
            _message.value = when (val outcome = entitlements.restore()) {
                RestoreOutcome.Restored -> PaywallMessage.Restored
                RestoreOutcome.NothingToRestore -> PaywallMessage.NothingToRestore
                is RestoreOutcome.Failed -> PaywallMessage.Failed(outcome.reason)
            }
            _status.value = PaywallStatus.IDLE
        }
    }

    fun onMessageShown() {
        _message.value = null
    }
}

/** How long the store gets before the screen stops claiming to be waiting. */
private const val PRICE_TIMEOUT_MILLIS = 15_000L

/** Keeps the derived price alive across a configuration change. */
private const val SUBSCRIPTION_TIMEOUT = 5_000L
