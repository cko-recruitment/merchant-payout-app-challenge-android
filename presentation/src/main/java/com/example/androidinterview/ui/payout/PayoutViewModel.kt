package com.example.androidinterview.ui.payout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidinterview.domain.model.Currency
import com.example.androidinterview.domain.model.Payout
import com.example.androidinterview.domain.model.PayoutException
import com.example.androidinterview.domain.repository.PayoutRepository
import com.example.androidinterview.domain.util.IbanUtils
import com.example.androidinterview.domain.util.anonymised
import com.example.androidinterview.domain.util.formatAmount
import com.example.androidinterview.presentation.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PayoutViewModel @Inject constructor(
    private val repository: PayoutRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val amountInput = MutableStateFlow("")
    val currency = MutableStateFlow(Currency.GBP)
    val ibanInput = MutableStateFlow("")

    private val _uiState = MutableStateFlow<PayoutUiState>(PayoutUiState.Idle)
    val uiState: StateFlow<PayoutUiState> = _uiState.asStateFlow()

    val isFormValid: StateFlow<Boolean> = combine(amountInput, ibanInput) { amount, iban ->
        amount.toDoubleOrNull()?.let { it > 0 } == true && IbanUtils.isValidIban(iban)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private enum class Phase { IDLE, CONFIRMING, AWAITING_BIOMETRIC, SUBMITTING, DONE }

    private data class PayoutData(
        val amountPence: Int,
        val currency: Currency,
        val iban: String,
        val formattedAmount: String,
    )

    private val phase = MutableStateFlow(Phase.IDLE)
    private val payoutData = MutableStateFlow<PayoutData?>(null)
    private val doneResult = MutableStateFlow<Result<Payout>?>(null)

    init {
        generateUi()
    }

    private fun generateUi() {
        viewModelScope.launch {
            combine(phase, payoutData, doneResult) { ph, data, result ->
                when {
                    ph == Phase.CONFIRMING && data != null -> PayoutUiState.Confirming(
                        formattedAmount = data.formattedAmount,
                        currency = data.currency,
                        iban = data.iban.anonymised(),
                        amountPence = data.amountPence,
                    )
                    ph == Phase.AWAITING_BIOMETRIC && data != null -> PayoutUiState.AwaitingBiometric(
                        formattedAmount = data.formattedAmount,
                        currency = data.currency,
                        iban = data.iban.anonymised(),
                        amountPence = data.amountPence,
                    )
                    ph == Phase.SUBMITTING -> PayoutUiState.Submitting
                    ph == Phase.DONE && result != null -> result.fold(
                        onSuccess = { payout ->
                            PayoutUiState.Success(
                                formattedAmount = formatAmount(payout.currency, payout.amount),
                                currency = payout.currency,
                                iban = payout.iban,
                            )
                        },
                        onFailure = { t ->
                            when (t) {
                                is PayoutException.InsufficientFunds  -> PayoutUiState.Error.InsufficientFunds(t.message ?: "")
                                is PayoutException.ServiceUnavailable -> PayoutUiState.Error.ServiceUnavailable(t.message ?: "")
                                else -> PayoutUiState.Error.Generic(t.message ?: context.getString(R.string.error_something_went_wrong))
                            }
                        },
                    )
                    else -> PayoutUiState.Idle
                }
            }.collect { _uiState.value = it }
        }
    }

    fun onRequestPayout() {
        val pence = ((amountInput.value.toDoubleOrNull() ?: return) * 100).roundToInt()
        payoutData.value = PayoutData(
            amountPence = pence,
            currency = currency.value,
            iban = ibanInput.value,
            formattedAmount = formatAmount(currency.value, pence),
        )
        phase.value = Phase.CONFIRMING
    }

    fun onConfirm() {
        val data = payoutData.value ?: return
        if (data.amountPence >= 100_000) {
            phase.value = Phase.AWAITING_BIOMETRIC
        } else {
            submitPayout(data)
        }
    }

    fun onBiometricSuccess() {
        payoutData.value?.let { submitPayout(it) }
    }

    fun onBiometricFailure() {
        phase.value = Phase.CONFIRMING
    }

    fun onBiometricNotEnrolled() {
        doneResult.value = Result.failure(RuntimeException(context.getString(R.string.error_biometric_not_enrolled)))
        phase.value = Phase.DONE
    }

    private fun submitPayout(data: PayoutData) {
        phase.value = Phase.SUBMITTING
        viewModelScope.launch {
            repository.createPayout(data.amountPence, data.currency, data.iban)
                .onSuccess { doneResult.value = Result.success(it) ; phase.value = Phase.DONE }
                .onFailure { doneResult.value = Result.failure(it) ; phase.value = Phase.DONE }
        }
    }

    fun onReset() {
        phase.value = Phase.IDLE
        amountInput.value = ""
        ibanInput.value = ""
        currency.value = Currency.GBP
        payoutData.value = null
        doneResult.value = null
    }

    fun onRetry() {
        phase.value = Phase.IDLE
        doneResult.value = null
    }
}

