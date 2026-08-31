package com.kemalurekli.electricalcalculator.features.pro.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kemalurekli.electricalcalculator.core.billing.domain.BillingFailure
import com.kemalurekli.electricalcalculator.core.billing.domain.EntitlementRepository
import com.kemalurekli.electricalcalculator.core.billing.domain.ProProduct
import com.kemalurekli.electricalcalculator.core.billing.domain.PurchaseOutcome
import com.kemalurekli.electricalcalculator.core.billing.domain.RestoreOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * What the paywall is doing, so the screen never offers two buttons at once.
 *
 * A store call can take seconds and opens a system sheet on top of the app.
 * Without a state the reader can press "buy" twice and be shown two sheets, and
 * the second one fails with an error that reads as if the first had.
 */
enum class PaywallStatus { IDLE, PURCHASING, RESTORING }

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

    val product: StateFlow<ProProduct?> = entitlements.product

    private val _status = MutableStateFlow(PaywallStatus.IDLE)
    val status: StateFlow<PaywallStatus> = _status.asStateFlow()

    private val _message = MutableStateFlow<PaywallMessage?>(null)
    val message: StateFlow<PaywallMessage?> = _message.asStateFlow()

    init {
        // The price is fetched on arrival rather than at launch: it is the one
        // thing on this screen that has to come off the network, and asking for
        // it on every cold start would spend a request most readers never see.
        viewModelScope.launch { entitlements.refresh() }
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
