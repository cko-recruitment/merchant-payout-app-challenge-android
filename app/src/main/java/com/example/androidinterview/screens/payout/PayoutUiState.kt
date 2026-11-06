package com.example.androidinterview.screens.payout

import com.example.androidinterview.core.biometric.BiometricResult
import com.example.androidinterview.core.formatting.toCurrencyText
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money

sealed interface PayoutUiState {

    data class Form(
        val amount: String,
        val currency: Currency,
        val iban: String,
        val isOnline: Boolean,
        val isConfirmEnabled: Boolean,
        val hasAmountError: Boolean = false,
        val hasIbanError: Boolean = false,
        val confirmation: PayoutConfirmationState? = null,
    ) : PayoutUiState

    data class Success(val formattedAmount: String) : PayoutUiState

    data class Failure(val reason: PayoutFailureReason) : PayoutUiState
}

/** The review dialog. It stays open while submitting, so the merchant can't leave and cancel the request. */
data class PayoutConfirmationState(
    val formattedAmount: String,
    val currency: Currency,
    val maskedIban: String,
    val isSubmitting: Boolean = false,
)

fun Money.mapToPayoutConfirmationState(maskedIban: String) = PayoutConfirmationState(
    formattedAmount = toCurrencyText(),
    currency = currency,
    maskedIban = maskedIban,
)

/** One-shot, so a SharedFlow with no replay: a rotation must not open the prompt again. */
sealed interface PayoutEvent {
    data object RequestBiometric : PayoutEvent
}

enum class PayoutFailureReason {
    ServiceUnavailable,
    InsufficientFunds,
    UnknownOutcome,
    Unexpected,
    BiometricCancelled,
    BiometricUnavailable,
}

fun Throwable.mapToPayoutFailureReason(): PayoutFailureReason = when (this) {
    is DataError.ServiceUnavailable -> PayoutFailureReason.ServiceUnavailable
    is DataError.InsufficientFunds -> PayoutFailureReason.InsufficientFunds
    is DataError.Network -> PayoutFailureReason.UnknownOutcome
    else -> PayoutFailureReason.Unexpected
}

/** Returns null for a success, which has no failure to show. */
fun BiometricResult.mapToPayoutFailureReason(): PayoutFailureReason? = when (this) {
    BiometricResult.Success -> null
    BiometricResult.Cancelled -> PayoutFailureReason.BiometricCancelled
    BiometricResult.Unavailable -> PayoutFailureReason.BiometricUnavailable
}
