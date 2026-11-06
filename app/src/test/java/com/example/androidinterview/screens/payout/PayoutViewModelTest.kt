package com.example.androidinterview.screens.payout

import androidx.lifecycle.SavedStateHandle
import com.example.androidinterview.core.biometric.BiometricResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.Payout
import manoellribeiro.dev.domain.model.PayoutStatus
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import manoellribeiro.dev.domain.usecase.CreatePayoutUseCase
import manoellribeiro.dev.domain.usecase.IsPayoutFormCompleteUseCase
import manoellribeiro.dev.domain.usecase.MaskIbanUseCase
import manoellribeiro.dev.domain.usecase.ObserveIsOnlineUseCase
import manoellribeiro.dev.domain.usecase.ParsePayoutAmountUseCase
import manoellribeiro.dev.domain.usecase.ShouldRequestBiometricUseCase
import manoellribeiro.dev.domain.usecase.ValidateIbanUseCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class PayoutViewModelTest {

    private lateinit var originalLocale: Locale
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var isPayoutFormComplete: IsPayoutFormCompleteUseCase
    private lateinit var parsePayoutAmount: ParsePayoutAmountUseCase
    private lateinit var validateIban: ValidateIbanUseCase
    private lateinit var maskIban: MaskIbanUseCase
    private lateinit var shouldRequestBiometric: ShouldRequestBiometricUseCase
    private lateinit var createPayout: CreatePayoutUseCase
    private lateinit var observeIsOnline: ObserveIsOnlineUseCase
    private lateinit var viewModel: PayoutViewModel

    @BeforeEach
    fun setup() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.UK)

        savedStateHandle = SavedStateHandle()
        isPayoutFormComplete = mockk()
        parsePayoutAmount = mockk()
        validateIban = mockk()
        maskIban = mockk()
        shouldRequestBiometric = mockk()
        createPayout = mockk()
        observeIsOnline = mockk()

        // Only behavior configured in setup: PayoutViewModel calls it when it is created. A test overrides it when needed.
        every { isPayoutFormComplete(any(), any()) } returns false

        viewModel = PayoutViewModel(
            savedStateHandle = savedStateHandle,
            isPayoutFormComplete = isPayoutFormComplete,
            parsePayoutAmount = parsePayoutAmount,
            validateIban = validateIban,
            maskIban = maskIban,
            shouldRequestBiometric = shouldRequestBiometric,
            createPayout = createPayout,
            observeIsOnline = observeIsOnline,
        )
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `if nothing was saved should start with an empty GBP form`() = runTest {
        // Given
        val fakeExpectedForm = PayoutUiState.Form(
            amount = "",
            currency = Currency.GBP,
            iban = "",
            isOnline = true,
            isConfirmEnabled = false,
        )

        // When
        val fakeState = viewModel.uiState.value

        // Then
        assertEquals(fakeExpectedForm, fakeState)
    }

    // Confirm button

    @Test
    fun `if the form is complete and online should enable Confirm`() = runTest {
        // Given
        every { isPayoutFormComplete(any(), any()) } returns true

        // When
        viewModel.onAmountChange("400")

        // Then
        assertTrue(currentForm().isConfirmEnabled)
    }

    @Test
    fun `if the form is not complete and online should disable Confirm`() = runTest {
        // Given
        every { isPayoutFormComplete(any(), any()) } returns false

        // When
        viewModel.onAmountChange("400")

        // Then
        assertFalse(currentForm().isConfirmEnabled)
    }

    @Test
    fun `if the form is complete and offline should disable Confirm`() = runTest {
        // Given
        every { isPayoutFormComplete(any(), any()) } returns true
        every { observeIsOnline() } returns MutableStateFlow(false)
        viewModel.startObservingConnectivity()
        advanceUntilIdle()

        // When
        viewModel.onAmountChange("400")

        // Then
        assertFalse(currentForm().isConfirmEnabled)
    }

    // Form fields

    @Test
    fun `if the amount changes should update the form and save it`() = runTest {
        // Given
        val fakeAmountText = "400.50"

        // When
        viewModel.onAmountChange(fakeAmountText)

        // Then
        assertEquals(fakeAmountText, currentForm().amount)
        assertEquals(fakeAmountText, savedStateHandle.get<String>("amount"))
    }

    @Test
    fun `if the currency changes should update the form and save it`() = runTest {
        // Given
        val fakeCurrency = Currency.EUR

        // When
        viewModel.onCurrencyChange(fakeCurrency)

        // Then
        assertEquals(fakeCurrency, currentForm().currency)
        assertEquals(fakeCurrency, savedStateHandle.get<Currency>("currency"))
    }

    @Test
    fun `if the iban has whitespace should strip it from the form and the saved value`() = runTest {
        // Given
        val fakeInput = " GB29 NWBK\t6016\n1331 9268 19 "
        val fakeStrippedIban = "GB29NWBK60161331926819"

        // When
        viewModel.onIbanChange(fakeInput)

        // Then
        assertEquals(fakeStrippedIban, currentForm().iban)
        assertEquals(fakeStrippedIban, savedStateHandle.get<String>("iban"))
    }

    @Test
    fun `if the iban has whitespace should check if the form is complete with the stripped value`() = runTest {
        // Given
        val fakeInput = "GB29 NWBK 6016 1331 9268 19"
        val fakeStrippedIban = "GB29NWBK60161331926819"

        // When
        viewModel.onIbanChange(fakeInput)

        // Then
        verify(exactly = 1) { isPayoutFormComplete("", fakeStrippedIban) }
    }

    // Inline errors

    @Test
    fun `if the amount is invalid on confirm should show the amount error and no dialog`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns null
        every { validateIban(any()) } returns true

        // When
        viewModel.onConfirmClick()

        // Then
        assertTrue(currentForm().hasAmountError)
        assertFalse(currentForm().hasIbanError)
        assertNull(currentForm().confirmation)
        verify(exactly = 0) { maskIban(any()) }
    }

    @Test
    fun `if the iban is invalid on confirm should show the iban error and no dialog`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns false

        // When
        viewModel.onConfirmClick()

        // Then
        assertFalse(currentForm().hasAmountError)
        assertTrue(currentForm().hasIbanError)
        assertNull(currentForm().confirmation)
        verify(exactly = 0) { maskIban(any()) }
    }

    @Test
    fun `if the amount and the iban are invalid on confirm should show both errors`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns null
        every { validateIban(any()) } returns false

        // When
        viewModel.onConfirmClick()

        // Then
        assertTrue(currentForm().hasAmountError)
        assertTrue(currentForm().hasIbanError)
        assertNull(currentForm().confirmation)
    }

    @Test
    fun `if the amount changes should clear only the amount error`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns null
        every { validateIban(any()) } returns false
        viewModel.onConfirmClick()

        // When
        viewModel.onAmountChange("400")

        // Then
        assertFalse(currentForm().hasAmountError)
        assertTrue(currentForm().hasIbanError)
    }

    @Test
    fun `if the currency changes should clear only the amount error`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns null
        every { validateIban(any()) } returns false
        viewModel.onConfirmClick()

        // When
        viewModel.onCurrencyChange(Currency.EUR)

        // Then
        assertFalse(currentForm().hasAmountError)
        assertTrue(currentForm().hasIbanError)
    }

    @Test
    fun `if the iban changes should clear only the iban error`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns null
        every { validateIban(any()) } returns false
        viewModel.onConfirmClick()

        // When
        viewModel.onIbanChange("GB29NWBK60161331926819")

        // Then
        assertTrue(currentForm().hasAmountError)
        assertFalse(currentForm().hasIbanError)
    }

    // Confirmation dialog

    @Test
    fun `if the form is valid on confirm should open the dialog with the masked iban`() = runTest {
        // Given
        val fakeIban = "GB29NWBK60161331926819"
        val fakeMaskedIban = "GB29**************6819"
        every { parsePayoutAmount("400", Currency.GBP) } returns createFakeMoney(minorUnits = 40_000)
        every { validateIban(fakeIban) } returns true
        every { maskIban(fakeIban) } returns fakeMaskedIban

        // When
        fillFormAndOpenConfirmation(amountText = "400", iban = fakeIban)

        // Then
        assertEquals(
            PayoutConfirmationState(
                formattedAmount = "£400.00",
                currency = Currency.GBP,
                maskedIban = fakeMaskedIban,
                isSubmitting = false,
            ),
            currentForm().confirmation,
        )
        assertFalse(currentForm().hasAmountError)
        assertFalse(currentForm().hasIbanError)
    }

    @Test
    fun `if a previous error was fixed on confirm should remove it and open the dialog`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns null
        every { validateIban(any()) } returns false
        viewModel.onConfirmClick()
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"

        // When
        viewModel.onConfirmClick()

        // Then
        assertFalse(currentForm().hasAmountError)
        assertFalse(currentForm().hasIbanError)
        assertNotNull(currentForm().confirmation)
    }

    @Test
    fun `if the currency is EUR on confirm should parse the amount with it`() = runTest {
        // Given
        every { parsePayoutAmount("500", Currency.EUR) } returns Money(50_000, Currency.EUR)
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        viewModel.onCurrencyChange(Currency.EUR)

        // When
        fillFormAndOpenConfirmation(amountText = "500")

        // Then
        assertEquals("€500.00", currentForm().confirmation?.formattedAmount)
        assertEquals(Currency.EUR, currentForm().confirmation?.currency)
        verify(exactly = 1) { parsePayoutAmount("500", Currency.EUR) }
    }

    @Test
    fun `if the dialog is open should close it on cancel keeping the fields`() = runTest {
        // Given
        val fakeAmountText = "400"
        val fakeIban = "GB29NWBK60161331926819"
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        fillFormAndOpenConfirmation(amountText = fakeAmountText, iban = fakeIban)

        // When
        viewModel.onCancelConfirmation()

        // Then
        assertNull(currentForm().confirmation)
        assertEquals(fakeAmountText, currentForm().amount)
        assertEquals(fakeIban, currentForm().iban)
    }

    @Test
    fun `if the dialog is closed should ignore cancel`() = runTest {
        // Given
        val fakeStateBefore = viewModel.uiState.value

        // When
        viewModel.onCancelConfirmation()

        // Then
        assertEquals(fakeStateBefore, viewModel.uiState.value)
    }

    @Test
    fun `if the payout is submitting should ignore cancel`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns true
        fillFormAndOpenConfirmation()
        viewModel.onSubmit()
        advanceUntilIdle()

        // When
        viewModel.onCancelConfirmation()

        // Then
        assertEquals(true, currentForm().confirmation?.isSubmitting)
    }

    // Submit

    @Test
    fun `if the dialog is closed should ignore submit`() = runTest {
        // Given
        val fakeStateBefore = viewModel.uiState.value

        // When
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(fakeStateBefore, viewModel.uiState.value)
        coVerify(exactly = 0) { createPayout(any(), any()) }
    }

    @Test
    fun `if submit is called twice should send the payout only once`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.success(createFakePayout())
        fillFormAndOpenConfirmation()

        // When
        viewModel.onSubmit()
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { createPayout(any(), any()) }
    }

    @Test
    fun `if submit is called twice over the threshold should request biometrics only once`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns true
        fillFormAndOpenConfirmation()

        // When
        val fakeEvents = collectEmissions(viewModel.events)
        viewModel.onSubmit()
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(listOf(PayoutEvent.RequestBiometric), fakeEvents)
        verify(exactly = 1) { shouldRequestBiometric(any()) }
    }

    @Test
    fun `if the app went offline with the dialog open should ignore submit`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { observeIsOnline() } returns MutableStateFlow(false)
        fillFormAndOpenConfirmation()
        viewModel.startObservingConnectivity()
        advanceUntilIdle()

        // When
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(false, currentForm().confirmation?.isSubmitting)
        verify(exactly = 0) { shouldRequestBiometric(any()) }
        coVerify(exactly = 0) { createPayout(any(), any()) }
    }

    @Test
    fun `if the threshold is not met should send directly with the amount and iban`() = runTest {
        // Given
        val fakeMoney = createFakeMoney()
        val fakeIban = "GB29NWBK60161331926819"
        every { parsePayoutAmount(any(), any()) } returns fakeMoney
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(fakeMoney) } returns false
        coEvery { createPayout(any(), any()) } returns Result.success(createFakePayout())
        fillFormAndOpenConfirmation(iban = fakeIban)

        // When
        val fakeEvents = collectEmissions(viewModel.events)
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertTrue(fakeEvents.isEmpty())
        coVerify(exactly = 1) { createPayout(fakeMoney, fakeIban) }
    }

    @Test
    fun `if the payout was sent and is waiting for the answer should show the dialog as submitting`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.success(createFakePayout())
        fillFormAndOpenConfirmation()

        // When
        viewModel.onSubmit()

        // Then
        assertEquals(true, currentForm().confirmation?.isSubmitting)
    }

    @Test
    fun `if the payout succeeds should show Success with the amount of the payout`() = runTest {
        // Given
        val fakePayout = createFakePayout(amount = Money(minorUnits = 123_456, currency = Currency.EUR))
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.success(fakePayout)
        fillFormAndOpenConfirmation()

        // When
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(PayoutUiState.Success("€1,234.56"), viewModel.uiState.value)
    }

    @Test
    fun `if the payout succeeds should clear the saved fields`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.success(createFakePayout())
        viewModel.onCurrencyChange(Currency.EUR)
        fillFormAndOpenConfirmation()

        // When
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertNull(savedStateHandle.get<String>("amount"))
        assertNull(savedStateHandle.get<Currency>("currency"))
        assertNull(savedStateHandle.get<String>("iban"))
    }

    @Test
    fun `if the payout fails should keep the saved fields`() = runTest {
        // Given
        val fakeAmountText = "400"
        val fakeIban = "GB29NWBK60161331926819"
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.onCurrencyChange(Currency.EUR)
        fillFormAndOpenConfirmation(amountText = fakeAmountText, iban = fakeIban)

        // When
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(fakeAmountText, savedStateHandle.get<String>("amount"))
        assertEquals(Currency.EUR, savedStateHandle.get<Currency>("currency"))
        assertEquals(fakeIban, savedStateHandle.get<String>("iban"))
    }

    @Test
    fun `if createPayout fails with ServiceUnavailable should show its failure reason`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())
        fillFormAndOpenConfirmation()

        // When
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(PayoutUiState.Failure(PayoutFailureReason.ServiceUnavailable), viewModel.uiState.value)
    }

    @Test
    fun `if createPayout fails with InsufficientFunds should show its failure reason`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.failure(DataError.InsufficientFunds())
        fillFormAndOpenConfirmation()

        // When
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(PayoutUiState.Failure(PayoutFailureReason.InsufficientFunds), viewModel.uiState.value)
    }

    @Test
    fun `if createPayout fails with Network should show the UnknownOutcome failure reason`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.failure(DataError.Network(IOException("fake")))
        fillFormAndOpenConfirmation()

        // When
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(PayoutUiState.Failure(PayoutFailureReason.UnknownOutcome), viewModel.uiState.value)
    }

    @Test
    fun `if createPayout fails with Unexpected should show its failure reason`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.failure(DataError.Unexpected())
        fillFormAndOpenConfirmation()

        // When
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(PayoutUiState.Failure(PayoutFailureReason.Unexpected), viewModel.uiState.value)
    }

    // Biometrics

    @Test
    fun `if the threshold is met should emit one biometric request and send nothing`() = runTest {
        // Given
        val fakeMoney = createFakeMoney(minorUnits = 100_100)
        every { parsePayoutAmount(any(), any()) } returns fakeMoney
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(fakeMoney) } returns true
        fillFormAndOpenConfirmation()

        // When
        val fakeEvents = collectEmissions(viewModel.events)
        viewModel.onSubmit()
        advanceUntilIdle()

        // Then
        assertEquals(listOf(PayoutEvent.RequestBiometric), fakeEvents)
        assertEquals(true, currentForm().confirmation?.isSubmitting)
        coVerify(exactly = 0) { createPayout(any(), any()) }
    }

    @Test
    fun `if the biometric result is Success should send the payout once`() = runTest {
        // Given
        val fakeMoney = createFakeMoney(minorUnits = 100_100)
        val fakeIban = "GB29NWBK60161331926819"
        every { parsePayoutAmount(any(), any()) } returns fakeMoney
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns true
        coEvery { createPayout(any(), any()) } returns Result.success(createFakePayout(amount = fakeMoney))
        fillFormAndOpenConfirmation(iban = fakeIban)
        viewModel.onSubmit()
        advanceUntilIdle()

        // When
        viewModel.onBiometricResult(BiometricResult.Success)
        advanceUntilIdle()

        // Then
        assertEquals(PayoutUiState.Success("£1,001.00"), viewModel.uiState.value)
        coVerify(exactly = 1) { createPayout(fakeMoney, fakeIban) }
    }

    @Test
    fun `if the biometric result is Success and the payout fails should show its failure reason`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns true
        coEvery { createPayout(any(), any()) } returns Result.failure(DataError.InsufficientFunds())
        fillFormAndOpenConfirmation()
        viewModel.onSubmit()
        advanceUntilIdle()

        // When
        viewModel.onBiometricResult(BiometricResult.Success)
        advanceUntilIdle()

        // Then
        assertEquals(PayoutUiState.Failure(PayoutFailureReason.InsufficientFunds), viewModel.uiState.value)
    }

    @Test
    fun `if the biometric result is Cancelled should show its failure reason and never send`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns true
        fillFormAndOpenConfirmation()
        viewModel.onSubmit()
        advanceUntilIdle()

        // When
        viewModel.onBiometricResult(BiometricResult.Cancelled)
        advanceUntilIdle()

        // Then
        assertEquals(PayoutUiState.Failure(PayoutFailureReason.BiometricCancelled), viewModel.uiState.value)
        coVerify(exactly = 0) { createPayout(any(), any()) }
    }

    @Test
    fun `if the biometric result is Unavailable should show its failure reason and never send`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns true
        fillFormAndOpenConfirmation()
        viewModel.onSubmit()
        advanceUntilIdle()

        // When
        viewModel.onBiometricResult(BiometricResult.Unavailable)
        advanceUntilIdle()

        // Then
        assertEquals(PayoutUiState.Failure(PayoutFailureReason.BiometricUnavailable), viewModel.uiState.value)
        coVerify(exactly = 0) { createPayout(any(), any()) }
    }

    @Test
    fun `if no prompt was requested should ignore a Success biometric result`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        fillFormAndOpenConfirmation()
        val fakeStateBefore = viewModel.uiState.value

        // When
        viewModel.onBiometricResult(BiometricResult.Success)
        advanceUntilIdle()

        // Then
        assertEquals(fakeStateBefore, viewModel.uiState.value)
        coVerify(exactly = 0) { createPayout(any(), any()) }
    }

    @Test
    fun `if no prompt was requested should ignore a Cancelled biometric result`() = runTest {
        // Given
        val fakeStateBefore = viewModel.uiState.value

        // When
        viewModel.onBiometricResult(BiometricResult.Cancelled)
        advanceUntilIdle()

        // Then
        assertEquals(fakeStateBefore, viewModel.uiState.value)
    }

    @Test
    fun `if the threshold was not met should ignore a biometric result during the send`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.success(createFakePayout())
        fillFormAndOpenConfirmation()
        viewModel.onSubmit()

        // When
        viewModel.onBiometricResult(BiometricResult.Success)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { createPayout(any(), any()) }
    }

    @Test
    fun `if a second biometric Success arrives should send the payout only once`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns true
        coEvery { createPayout(any(), any()) } returns Result.success(createFakePayout())
        fillFormAndOpenConfirmation()
        viewModel.onSubmit()
        advanceUntilIdle()

        // When
        viewModel.onBiometricResult(BiometricResult.Success)
        viewModel.onBiometricResult(BiometricResult.Success)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { createPayout(any(), any()) }
    }

    @Test
    fun `if a Cancelled result was handled should ignore a later Success result`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns true
        every { isPayoutFormComplete(any(), any()) } returns true
        fillFormAndOpenConfirmation()
        viewModel.onSubmit()
        advanceUntilIdle()
        viewModel.onBiometricResult(BiometricResult.Cancelled)
        viewModel.onStartAgain()

        // When
        viewModel.onBiometricResult(BiometricResult.Success)
        advanceUntilIdle()

        // Then
        assertInstanceOf(PayoutUiState.Form::class.java, viewModel.uiState.value)
        coVerify(exactly = 0) { createPayout(any(), any()) }
    }

    // Start again

    @Test
    fun `if onStartAgain follows a failed payout should return to the filled form without resending`() = runTest {
        // Given
        val fakeAmountText = "400"
        val fakeIban = "GB29NWBK60161331926819"
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        every { isPayoutFormComplete(any(), any()) } returns true
        coEvery { createPayout(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.onCurrencyChange(Currency.EUR)
        fillFormAndOpenConfirmation(amountText = fakeAmountText, iban = fakeIban)
        viewModel.onSubmit()
        advanceUntilIdle()

        // When
        viewModel.onStartAgain()
        advanceUntilIdle()

        // Then
        assertEquals(
            PayoutUiState.Form(
                amount = fakeAmountText,
                currency = Currency.EUR,
                iban = fakeIban,
                isOnline = true,
                isConfirmEnabled = true,
            ),
            viewModel.uiState.value,
        )
        coVerify(exactly = 1) { createPayout(any(), any()) }
    }

    @Test
    fun `if onStartAgain follows a cancelled biometric should return to the filled form`() = runTest {
        // Given
        val fakeAmountText = "1001"
        val fakeIban = "GB29NWBK60161331926819"
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns true
        fillFormAndOpenConfirmation(amountText = fakeAmountText, iban = fakeIban)
        viewModel.onSubmit()
        advanceUntilIdle()
        viewModel.onBiometricResult(BiometricResult.Cancelled)

        // When
        viewModel.onStartAgain()

        // Then
        assertEquals(fakeAmountText, currentForm().amount)
        assertEquals(fakeIban, currentForm().iban)
        assertNull(currentForm().confirmation)
    }

    @Test
    fun `if onStartAgain follows a successful payout should return to an empty form`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.success(createFakePayout())
        viewModel.onCurrencyChange(Currency.EUR)
        fillFormAndOpenConfirmation()
        viewModel.onSubmit()
        advanceUntilIdle()
        every { isPayoutFormComplete("", "") } returns false

        // When
        viewModel.onStartAgain()

        // Then
        assertEquals(
            PayoutUiState.Form(
                amount = "",
                currency = Currency.GBP,
                iban = "",
                isOnline = true,
                isConfirmEnabled = false,
            ),
            viewModel.uiState.value,
        )
    }

    // Fields on the outcome screens

    @Test
    fun `if the outcome is showing should ignore field changes and not save them`() = runTest {
        // Given
        val fakeAmountText = "400"
        val fakeIban = "GB29NWBK60161331926819"
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        coEvery { createPayout(any(), any()) } returns Result.failure(DataError.Unexpected())
        fillFormAndOpenConfirmation(amountText = fakeAmountText, iban = fakeIban)
        viewModel.onSubmit()
        advanceUntilIdle()

        // When
        viewModel.onAmountChange("999")
        viewModel.onIbanChange("FR7630006000011234567890189")
        viewModel.onCurrencyChange(Currency.EUR)

        // Then
        assertEquals(PayoutUiState.Failure(PayoutFailureReason.Unexpected), viewModel.uiState.value)
        assertEquals(fakeAmountText, savedStateHandle.get<String>("amount"))
        assertEquals(fakeIban, savedStateHandle.get<String>("iban"))
        assertNull(savedStateHandle.get<Currency>("currency"))
    }

    // Connectivity

    @Test
    fun `if the connection drops should update the form to offline and disable Confirm`() = runTest {
        // Given
        every { isPayoutFormComplete(any(), any()) } returns true
        val fakeIsOnline = MutableStateFlow(true)
        every { observeIsOnline() } returns fakeIsOnline
        viewModel.startObservingConnectivity()
        viewModel.onAmountChange("400")
        advanceUntilIdle()

        // When
        fakeIsOnline.value = false
        advanceUntilIdle()

        // Then
        assertFalse(currentForm().isOnline)
        assertFalse(currentForm().isConfirmEnabled)
    }

    @Test
    fun `if the connection returns should update the form to online and enable Confirm`() = runTest {
        // Given
        every { isPayoutFormComplete(any(), any()) } returns true
        val fakeIsOnline = MutableStateFlow(false)
        every { observeIsOnline() } returns fakeIsOnline
        viewModel.startObservingConnectivity()
        advanceUntilIdle()

        // When
        fakeIsOnline.value = true
        advanceUntilIdle()

        // Then
        assertTrue(currentForm().isOnline)
        assertTrue(currentForm().isConfirmEnabled)
    }

    @Test
    fun `if the connection drops should keep the fields and the open dialog`() = runTest {
        // Given
        val fakeAmountText = "400"
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        val fakeIsOnline = MutableStateFlow(true)
        every { observeIsOnline() } returns fakeIsOnline
        viewModel.startObservingConnectivity()
        advanceUntilIdle()
        fillFormAndOpenConfirmation(amountText = fakeAmountText)
        val fakeConfirmation = currentForm().confirmation

        // When
        fakeIsOnline.value = false
        advanceUntilIdle()

        // Then
        assertEquals(fakeAmountText, currentForm().amount)
        assertEquals(fakeConfirmation, currentForm().confirmation)
    }

    @Test
    fun `if startObservingConnectivity is called twice should observe only once`() = runTest {
        // Given
        every { observeIsOnline() } returns MutableStateFlow(true)

        // When
        viewModel.startObservingConnectivity()
        viewModel.startObservingConnectivity()
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { observeIsOnline() }
    }

    @Test
    fun `if the connection drops during an outcome should keep the outcome and use it on the next form`() = runTest {
        // Given
        every { parsePayoutAmount(any(), any()) } returns createFakeMoney()
        every { validateIban(any()) } returns true
        every { maskIban(any()) } returns "fake-masked-iban"
        every { shouldRequestBiometric(any()) } returns false
        every { isPayoutFormComplete(any(), any()) } returns true
        coEvery { createPayout(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())
        val fakeIsOnline = MutableStateFlow(true)
        every { observeIsOnline() } returns fakeIsOnline
        viewModel.startObservingConnectivity()
        advanceUntilIdle()
        fillFormAndOpenConfirmation()
        viewModel.onSubmit()
        advanceUntilIdle()

        // When
        fakeIsOnline.value = false
        advanceUntilIdle()
        val fakeOutcomeState = viewModel.uiState.value
        viewModel.onStartAgain()

        // Then
        assertEquals(PayoutUiState.Failure(PayoutFailureReason.ServiceUnavailable), fakeOutcomeState)
        assertFalse(currentForm().isOnline)
        assertFalse(currentForm().isConfirmEnabled)
    }

    // Restored from SavedStateHandle

    @Nested
    inner class RestoredFromSavedState {

        private val fakeSavedAmount = "400"
        private val fakeSavedIban = "GB29NWBK60161331926819"

        @BeforeEach
        fun setup() {
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "amount" to fakeSavedAmount,
                    "currency" to Currency.EUR,
                    "iban" to fakeSavedIban,
                ),
            )

            // Only behavior configured in setup: PayoutViewModel calls it when it is created.
            every { isPayoutFormComplete(fakeSavedAmount, fakeSavedIban) } returns true

            viewModel = PayoutViewModel(
                savedStateHandle = savedStateHandle,
                isPayoutFormComplete = isPayoutFormComplete,
                parsePayoutAmount = parsePayoutAmount,
                validateIban = validateIban,
                maskIban = maskIban,
                shouldRequestBiometric = shouldRequestBiometric,
                createPayout = createPayout,
                observeIsOnline = observeIsOnline,
            )
        }

        @Test
        fun `if every field was saved should start the form with them and Confirm enabled`() = runTest {
            // Given
            val fakeExpectedForm = PayoutUiState.Form(
                amount = fakeSavedAmount,
                currency = Currency.EUR,
                iban = fakeSavedIban,
                isOnline = true,
                isConfirmEnabled = true,
            )

            // When
            val fakeState = viewModel.uiState.value

            // Then
            assertEquals(fakeExpectedForm, fakeState)
        }

        @Test
        fun `if the form was restored should not open the dialog or show errors`() = runTest {
            // Given
            val fakeRestoredForm = viewModel.uiState.value

            // When
            val fakeForm = assertInstanceOf(PayoutUiState.Form::class.java, fakeRestoredForm)

            // Then
            assertNull(fakeForm.confirmation)
            assertFalse(fakeForm.hasAmountError)
            assertFalse(fakeForm.hasIbanError)
        }
    }

    @Nested
    inner class RestoredFromPartialSavedState {

        private val fakeSavedAmount = "250"

        @BeforeEach
        fun setup() {
            savedStateHandle = SavedStateHandle(mapOf("amount" to fakeSavedAmount))

            viewModel = PayoutViewModel(
                savedStateHandle = savedStateHandle,
                isPayoutFormComplete = isPayoutFormComplete,
                parsePayoutAmount = parsePayoutAmount,
                validateIban = validateIban,
                maskIban = maskIban,
                shouldRequestBiometric = shouldRequestBiometric,
                createPayout = createPayout,
                observeIsOnline = observeIsOnline,
            )
        }

        @Test
        fun `if only the amount was saved should use the defaults for the other fields`() = runTest {
            // Given
            val fakeExpectedForm = PayoutUiState.Form(
                amount = fakeSavedAmount,
                currency = Currency.GBP,
                iban = "",
                isOnline = true,
                isConfirmEnabled = false,
            )

            // When
            val fakeState = viewModel.uiState.value

            // Then
            assertEquals(fakeExpectedForm, fakeState)
        }
    }

    private fun <T> TestScope.collectEmissions(flow: Flow<T>): List<T> {
        val emissions = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            flow.toList(emissions)
        }
        return emissions
    }

    private fun currentForm(): PayoutUiState.Form =
        assertInstanceOf(PayoutUiState.Form::class.java, viewModel.uiState.value)

    private fun fillFormAndOpenConfirmation(
        amountText: String = "400",
        iban: String = "GB29NWBK60161331926819",
    ) {
        viewModel.onAmountChange(amountText)
        viewModel.onIbanChange(iban)
        viewModel.onConfirmClick()
    }

    private fun createFakeMoney(minorUnits: Long = 40_000): Money =
        Money(minorUnits = minorUnits, currency = Currency.GBP)

    private fun createFakePayout(amount: Money = createFakeMoney()): Payout = Payout(
        id = "fake-payout-id",
        status = PayoutStatus.Pending,
        amount = amount,
        iban = "GB29NWBK60161331926819",
        createdAt = Instant.parse("2026-01-23T10:00:00Z"),
    )
}
