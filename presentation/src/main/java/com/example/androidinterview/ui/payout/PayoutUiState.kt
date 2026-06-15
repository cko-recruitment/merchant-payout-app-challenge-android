package com.example.androidinterview.ui.payout

import com.example.androidinterview.domain.model.Currency

sealed class PayoutUiState {
    object Idle : PayoutUiState()
    data class Confirming(
        val formattedAmount: String,
        val currency: Currency,
        val iban: String,
        val amountPence: Int,
    ) : PayoutUiState()
    data class AwaitingBiometric(
        val formattedAmount: String,
        val currency: Currency,
        val iban: String,
        val amountPence: Int,
    ) : PayoutUiState()
    object Submitting : PayoutUiState()
    data class Success(
        val formattedAmount: String,
        val currency: Currency,
        val iban: String,
    ) : PayoutUiState()
    sealed class Error : PayoutUiState() {
        abstract val message: String
        data class InsufficientFunds(override val message: String) : Error()
        data class ServiceUnavailable(override val message: String) : Error()
        data class Generic(override val message: String) : Error()
    }
}
