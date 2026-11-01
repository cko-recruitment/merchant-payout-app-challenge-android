package manoellribeiro.dev.domain.usecase

import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ParsePayoutAmountUseCaseTest {

    private lateinit var parsePayoutAmount: ParsePayoutAmountUseCase

    @BeforeEach
    fun setup() {
        parsePayoutAmount = ParsePayoutAmountUseCase()
    }

    @Test
    fun `if the input is a whole number should return it in minor units`() {
        // Given
        val fakeInput = "400"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertEquals(Money(minorUnits = 40_000, currency = Currency.GBP), fakeResult)
    }

    @Test
    fun `if the input has two decimals should return it in minor units`() {
        // Given
        val fakeInput = "10.99"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertEquals(Money(minorUnits = 1_099, currency = Currency.GBP), fakeResult)
    }

    @Test
    fun `if the input has one decimal should return it in minor units`() {
        // Given
        val fakeInput = "10.5"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertEquals(Money(minorUnits = 1_050, currency = Currency.GBP), fakeResult)
    }

    @Test
    fun `if the input is the smallest amount of the currency should return one minor unit`() {
        // Given
        val fakeInput = "0.01"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertEquals(Money(minorUnits = 1, currency = Currency.GBP), fakeResult)
    }

    @Test
    fun `if the currency is EUR should return the amount in that currency`() {
        // Given
        val fakeInput = "25.50"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.EUR)

        // Then
        assertEquals(Money(minorUnits = 2_550, currency = Currency.EUR), fakeResult)
    }

    @Test
    fun `if the input has surrounding whitespace should ignore it`() {
        // Given
        val fakeInput = "  75.25  "

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertEquals(Money(minorUnits = 7_525, currency = Currency.GBP), fakeResult)
    }

    @Test
    fun `if the input has more decimals than the currency allows should return null`() {
        // Given
        val fakeInput = "10.999"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertNull(fakeResult)
    }

    @Test
    fun `if the input is zero should return null`() {
        // Given
        val fakeInput = "0"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertNull(fakeResult)
    }

    @Test
    fun `if the input is zero with decimals should return null`() {
        // Given
        val fakeInput = "0.00"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertNull(fakeResult)
    }

    @Test
    fun `if the input is negative should return null`() {
        // Given
        val fakeInput = "-5.00"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertNull(fakeResult)
    }

    @Test
    fun `if the input is empty should return null`() {
        // Given
        val fakeInput = ""

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertNull(fakeResult)
    }

    @Test
    fun `if the input is only whitespace should return null`() {
        // Given
        val fakeInput = "   "

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertNull(fakeResult)
    }

    @Test
    fun `if the input is not numeric should return null`() {
        // Given
        val fakeInput = "fake-amount"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertNull(fakeResult)
    }

    @Test
    fun `if the input uses a thousands separator should return null`() {
        // Given
        val fakeInput = "1,000"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertNull(fakeResult)
    }

    @Test
    fun `if the input is the largest amount a Long can hold should return it`() {
        // Given
        val fakeInput = "92233720368547758.07"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertEquals(Money(minorUnits = Long.MAX_VALUE, currency = Currency.GBP), fakeResult)
    }

    @Test
    fun `if the input overflows a Long by one minor unit should return null`() {
        // Given
        val fakeInput = "92233720368547758.08"

        // When
        val fakeResult = parsePayoutAmount(fakeInput, Currency.GBP)

        // Then
        assertNull(fakeResult)
    }
}
