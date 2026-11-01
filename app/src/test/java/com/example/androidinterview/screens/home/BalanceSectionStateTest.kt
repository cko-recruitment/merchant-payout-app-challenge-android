package com.example.androidinterview.screens.home

import java.util.Locale
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.MerchantBalance
import manoellribeiro.dev.domain.model.Money
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BalanceSectionStateTest {

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
    fun `if the result is a success should return Loaded with both amounts formatted`() {
        // Given
        val fakeBalance = MerchantBalance(
            available = Money(minorUnits = 500_000, currency = Currency.GBP),
            pending = Money(minorUnits = 2_550, currency = Currency.GBP),
        )
        val fakeResult = Result.success(fakeBalance)

        // When
        val fakeState = fakeResult.mapToSectionState()

        // Then
        assertEquals(BalanceSectionState.Loaded(available = "£5,000.00", pending = "£25.50"), fakeState)
    }

    @Test
    fun `if the balance is in EUR should format it with the euro symbol`() {
        // Given
        val fakeBalance = MerchantBalance(
            available = Money(minorUnits = 100_000, currency = Currency.EUR),
            pending = Money(minorUnits = 0, currency = Currency.EUR),
        )
        val fakeResult = Result.success(fakeBalance)

        // When
        val fakeState = fakeResult.mapToSectionState()

        // Then
        assertEquals(BalanceSectionState.Loaded(available = "€1,000.00", pending = "€0.00"), fakeState)
    }

    @Test
    fun `if the result is a failure should return Error`() {
        // Given
        val fakeResult = Result.failure<MerchantBalance>(DataError.ServiceUnavailable())

        // When
        val fakeState = fakeResult.mapToSectionState()

        // Then
        assertEquals(BalanceSectionState.Error, fakeState)
    }
}
