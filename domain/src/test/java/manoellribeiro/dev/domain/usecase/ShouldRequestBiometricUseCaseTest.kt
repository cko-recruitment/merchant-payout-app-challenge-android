package manoellribeiro.dev.domain.usecase

import java.math.BigDecimal
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShouldRequestBiometricUseCaseTest {

    private lateinit var shouldRequestBiometric: ShouldRequestBiometricUseCase

    @BeforeEach
    fun setup() {
        shouldRequestBiometric = ShouldRequestBiometricUseCase()
    }

    @Test
    fun `if the amount is 999 pounds should not request biometrics`() {
        // Given
        val fakeAmount = Money(minorUnits = 99_900, currency = Currency.GBP)

        // When
        val fakeResult = shouldRequestBiometric(fakeAmount)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the amount is exactly 1000 pounds should not request biometrics`() {
        // Given
        val fakeAmount = Money(minorUnits = 100_000, currency = Currency.GBP)

        // When
        val fakeResult = shouldRequestBiometric(fakeAmount)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the amount is one penny over 1000 pounds should request biometrics`() {
        // Given
        val fakeAmount = Money(minorUnits = 100_001, currency = Currency.GBP)

        // When
        val fakeResult = shouldRequestBiometric(fakeAmount)

        // Then
        assertTrue(fakeResult)
    }

    @Test
    fun `if the amount is 1001 pounds should request biometrics`() {
        // Given
        val fakeAmount = Money(minorUnits = 100_100, currency = Currency.GBP)

        // When
        val fakeResult = shouldRequestBiometric(fakeAmount)

        // Then
        assertTrue(fakeResult)
    }

    @Test
    fun `if the amount is exactly 1000 euros should not request biometrics`() {
        // Given
        val fakeAmount = Money(minorUnits = 100_000, currency = Currency.EUR)

        // When
        val fakeResult = shouldRequestBiometric(fakeAmount)

        // Then
        assertFalse(fakeResult)
    }

    @Test
    fun `if the amount is 1001 euros should request biometrics`() {
        // Given
        val fakeAmount = Money(minorUnits = 100_100, currency = Currency.EUR)

        // When
        val fakeResult = shouldRequestBiometric(fakeAmount)

        // Then
        assertTrue(fakeResult)
    }

    @Nested
    inner class WithOnlyAPoundsThreshold {

        @BeforeEach
        fun setup() {
            shouldRequestBiometric = ShouldRequestBiometricUseCase(
                thresholdsInMajorUnits = mapOf(Currency.GBP to BigDecimal(500)),
            )
        }

        @Test
        fun `if the currency is missing from the thresholds should request biometrics`() {
            // Given
            val fakeAmount = Money(minorUnits = 1, currency = Currency.EUR)

            // When
            val fakeResult = shouldRequestBiometric(fakeAmount)

            // Then
            assertTrue(fakeResult)
        }

        @Test
        fun `if the amount equals the injected threshold should not request biometrics`() {
            // Given
            val fakeAmount = Money(minorUnits = 50_000, currency = Currency.GBP)

            // When
            val fakeResult = shouldRequestBiometric(fakeAmount)

            // Then
            assertFalse(fakeResult)
        }

        @Test
        fun `if the amount is over the injected threshold should request biometrics`() {
            // Given
            val fakeAmount = Money(minorUnits = 50_001, currency = Currency.GBP)

            // When
            val fakeResult = shouldRequestBiometric(fakeAmount)

            // Then
            assertTrue(fakeResult)
        }
    }
}
