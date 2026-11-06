package com.example.androidinterview.core.formatting

import java.util.Locale
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoneyFormatterExtensionsTest {

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
    fun `if the currency is GBP should format with the pound symbol and two decimals`() {
        // Given
        val fakeMoney = Money(minorUnits = 500_000, currency = Currency.GBP)

        // When
        val fakeText = fakeMoney.toCurrencyText()

        // Then
        assertEquals("£5,000.00", fakeText)
    }

    @Test
    fun `if the currency is EUR should format with the euro symbol`() {
        // Given
        val fakeMoney = Money(minorUnits = 123_456, currency = Currency.EUR)

        // When
        val fakeText = fakeMoney.toCurrencyText()

        // Then
        assertEquals("€1,234.56", fakeText)
    }

    @Test
    fun `if the amount is zero should format it with two decimals`() {
        // Given
        val fakeMoney = Money(minorUnits = 0, currency = Currency.GBP)

        // When
        val fakeText = fakeMoney.toCurrencyText()

        // Then
        assertEquals("£0.00", fakeText)
    }

    @Test
    fun `if the amount is below one major unit should keep the leading zero`() {
        // Given
        val fakeMoney = Money(minorUnits = 5, currency = Currency.GBP)

        // When
        val fakeText = fakeMoney.toCurrencyText()

        // Then
        assertEquals("£0.05", fakeText)
    }

    @Test
    fun `if the amount is negative should format it with a minus sign`() {
        // Given
        val fakeMoney = Money(minorUnits = -2_550, currency = Currency.GBP)

        // When
        val fakeText = fakeMoney.toCurrencyText()

        // Then
        assertEquals("-£25.50", fakeText)
    }

    @Test
    fun `if the amount is in the millions should group every three digits`() {
        // Given
        val fakeMoney = Money(minorUnits = 123_456_789_00, currency = Currency.GBP)

        // When
        val fakeText = fakeMoney.toCurrencyText()

        // Then
        assertEquals("£123,456,789.00", fakeText)
    }

    @Nested
    inner class WithGermanLocale {

        @BeforeEach
        fun setup() {
            Locale.setDefault(Locale.GERMANY)
        }

        @Test
        fun `if the default locale is German should follow its separators and symbol position`() {
            // Given
            val fakeMoney = Money(minorUnits = 123_456, currency = Currency.EUR)

            // When
            val fakeText = fakeMoney.toCurrencyText()

            // Then
            assertEquals("1.234,56 €", fakeText)
        }

        @Test
        fun `if the default locale is German should keep the currency of the money`() {
            // Given
            val fakeMoney = Money(minorUnits = 123_456, currency = Currency.GBP)

            // When
            val fakeText = fakeMoney.toCurrencyText()

            // Then
            assertEquals("1.234,56 £", fakeText)
        }
    }

    @Nested
    inner class WithUsLocale {

        @BeforeEach
        fun setup() {
            Locale.setDefault(Locale.US)
        }

        @Test
        fun `if the default locale is US should show GBP with the pound symbol and not the dollar`() {
            // Given
            val fakeMoney = Money(minorUnits = 500_000, currency = Currency.GBP)

            // When
            val fakeText = fakeMoney.toCurrencyText()

            // Then
            assertEquals("£5,000.00", fakeText)
        }
    }
}
