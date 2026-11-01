package manoellribeiro.dev.domain.model

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MoneyConverterTest {

    @Test
    fun `if the major amount is whole should return it in minor units`() {
        // Given
        val fakeMajor = BigDecimal("400")

        // When
        val fakeResult = MoneyConverter.toMinorUnits(fakeMajor, Currency.GBP)

        // Then
        assertEquals(40_000L, fakeResult)
    }

    @Test
    fun `if the major amount has two decimals should return it in minor units`() {
        // Given
        val fakeMajor = BigDecimal("10.99")

        // When
        val fakeResult = MoneyConverter.toMinorUnits(fakeMajor, Currency.GBP)

        // Then
        assertEquals(1_099L, fakeResult)
    }

    @Test
    fun `if the currency is EUR should convert the major amount with its fraction digits`() {
        // Given
        val fakeMajor = BigDecimal("25.50")

        // When
        val fakeResult = MoneyConverter.toMinorUnits(fakeMajor, Currency.EUR)

        // Then
        assertEquals(2_550L, fakeResult)
    }

    @Test
    fun `if the extra decimals of the major amount are zeros should not throw`() {
        // Given
        val fakeMajor = BigDecimal("10.990")

        // When
        val fakeResult = MoneyConverter.toMinorUnits(fakeMajor, Currency.GBP)

        // Then
        assertEquals(1_099L, fakeResult)
    }

    @Test
    fun `if the major amount is zero should return zero minor units`() {
        // Given
        val fakeMajor = BigDecimal.ZERO

        // When
        val fakeResult = MoneyConverter.toMinorUnits(fakeMajor, Currency.GBP)

        // Then
        assertEquals(0L, fakeResult)
    }

    @Test
    fun `if the major amount is negative should return negative minor units`() {
        // Given
        val fakeMajor = BigDecimal("-10.50")

        // When
        val fakeResult = MoneyConverter.toMinorUnits(fakeMajor, Currency.GBP)

        // Then
        assertEquals(-1_050L, fakeResult)
    }

    @Test
    fun `if the major amount is the largest a Long can hold should return Long MAX_VALUE`() {
        // Given
        val fakeMajor = BigDecimal("92233720368547758.07")

        // When
        val fakeResult = MoneyConverter.toMinorUnits(fakeMajor, Currency.GBP)

        // Then
        assertEquals(Long.MAX_VALUE, fakeResult)
    }

    @Test
    fun `if the major amount has more decimals than the currency allows should throw ArithmeticException`() {
        // Given
        val fakeMajor = BigDecimal("10.999")

        // When
        val fakeWhen = { MoneyConverter.toMinorUnits(fakeMajor, Currency.GBP) }

        // Then
        assertThrows<ArithmeticException> { fakeWhen() }
    }

    @Test
    fun `if the major amount overflows a Long should throw ArithmeticException`() {
        // Given
        val fakeMajor = BigDecimal("92233720368547758.08")

        // When
        val fakeWhen = { MoneyConverter.toMinorUnits(fakeMajor, Currency.GBP) }

        // Then
        assertThrows<ArithmeticException> { fakeWhen() }
    }

    @Test
    fun `if the money is in pounds should return the major amount with two decimals`() {
        // Given
        val fakeMoney = Money(minorUnits = 40_000, currency = Currency.GBP)

        // When
        val fakeResult = MoneyConverter.toMajorUnits(fakeMoney)

        // Then
        assertEquals(BigDecimal("400.00"), fakeResult)
    }

    @Test
    fun `if the money is in euros should return the major amount with two decimals`() {
        // Given
        val fakeMoney = Money(minorUnits = 2_550, currency = Currency.EUR)

        // When
        val fakeResult = MoneyConverter.toMajorUnits(fakeMoney)

        // Then
        assertEquals(BigDecimal("25.50"), fakeResult)
    }

    @Test
    fun `if the money is less than one major unit should keep the leading zero`() {
        // Given
        val fakeMoney = Money(minorUnits = 5, currency = Currency.GBP)

        // When
        val fakeResult = MoneyConverter.toMajorUnits(fakeMoney)

        // Then
        assertEquals(BigDecimal("0.05"), fakeResult)
    }

    @Test
    fun `if the money is zero should return zero with two decimals`() {
        // Given
        val fakeMoney = Money(minorUnits = 0, currency = Currency.GBP)

        // When
        val fakeResult = MoneyConverter.toMajorUnits(fakeMoney)

        // Then
        assertEquals(BigDecimal("0.00"), fakeResult)
    }

    @Test
    fun `if the money is negative should return a negative major amount`() {
        // Given
        val fakeMoney = Money(minorUnits = -1_050, currency = Currency.GBP)

        // When
        val fakeResult = MoneyConverter.toMajorUnits(fakeMoney)

        // Then
        assertEquals(BigDecimal("-10.50"), fakeResult)
    }
}
