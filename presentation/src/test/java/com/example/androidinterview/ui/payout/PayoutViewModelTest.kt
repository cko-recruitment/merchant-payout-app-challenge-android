package com.example.androidinterview.ui.payout

import android.content.Context
import com.example.androidinterview.domain.model.Currency
import com.example.androidinterview.domain.model.Payout
import com.example.androidinterview.domain.model.PayoutStatus
import com.example.androidinterview.domain.repository.PayoutRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PayoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val repository: PayoutRepository = mockk()
    private val context: Context = mockk()

    private lateinit var viewModel: PayoutViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(any()) } returns ""
        viewModel = PayoutViewModel(repository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region onConfirm — biometric threshold

    @Test
    fun `onConfirm transitions to AwaitingBiometric when amount is exactly the threshold`() {
        putViewModelInConfirmingState(amountPence = 100_000)
        viewModel.onConfirm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PayoutUiState.AwaitingBiometric)
    }

    @Test
    fun `onConfirm transitions to AwaitingBiometric when amount exceeds the threshold`() {
        putViewModelInConfirmingState(amountPence = 100_001)
        viewModel.onConfirm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PayoutUiState.AwaitingBiometric)
    }

    @Test
    fun `onConfirm bypasses biometric and reaches Success when amount is below the threshold`() {
        coEvery { repository.createPayout(any(), any(), any()) } returns Result.success(fakePayout())
        putViewModelInConfirmingState(amountPence = 99_999)
        viewModel.onConfirm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PayoutUiState.Success)
    }

    // endregion

    // region helpers

    private fun putViewModelInConfirmingState(
        amountPence: Int = 50_000,
        currency: Currency = Currency.GBP,
        iban: String = "GB29NWBK60161331926819",
    ) {
        viewModel.amountInput.value = (amountPence / 100.0).toString()
        viewModel.currency.value = currency
        viewModel.ibanInput.value = iban
        viewModel.onRequestPayout()
    }

    private fun fakePayout(
        id: String = "pay-1",
        status: PayoutStatus = PayoutStatus.PENDING,
        amount: Int = 50_000,
        currency: Currency = Currency.GBP,
        iban: String = "GB29NWBK60161331926819",
        createdAt: String = "2026-01-01T00:00:00Z",
    ) = Payout(
        id = id,
        status = status,
        amount = amount,
        currency = currency,
        iban = iban,
        createdAt = createdAt,
    )

    // endregion
}
