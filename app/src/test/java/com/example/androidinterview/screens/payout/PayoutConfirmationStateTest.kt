package com.example.androidinterview.screens.payout

import java.util.Locale
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PayoutConfirmationStateTest {

    private lateinit var originalLocale: Locale

    @BeforeEach
    fun setup() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.UK)
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `if the money is in GBP should return a state that is not submitting`() {
        // Given
        val fakeMoney = Money(minorUnits = 40_000, currency = Currency.GBP)
        val fakeMaskedIban = "GB29**************6819"

        // When
        val fakeState = fakeMoney.mapToPayoutConfirmationState(fakeMaskedIban)

        // Then
        assertEquals(
            PayoutConfirmationState(
                formattedAmount = "£400.00",
                currency = Currency.GBP,
                maskedIban = fakeMaskedIban,
                isSubmitting = false,
            ),
            fakeState,
        )
    }

    @Test
    fun `if the money is in EUR should keep its currency and symbol`() {
        // Given
        val fakeMoney = Money(minorUnits = 123_456, currency = Currency.EUR)
        val fakeMaskedIban = "FR12*****1231"

        // When
        val fakeState = fakeMoney.mapToPayoutConfirmationState(fakeMaskedIban)

        // Then
        assertEquals("€1,234.56", fakeState.formattedAmount)
        assertEquals(Currency.EUR, fakeState.currency)
        assertEquals(fakeMaskedIban, fakeState.maskedIban)
    }
}
