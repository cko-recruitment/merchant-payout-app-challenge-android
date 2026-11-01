package com.example.androidinterview.screens.payout

import com.example.androidinterview.core.biometric.BiometricResult
import java.io.IOException
import manoellribeiro.dev.domain.error.DataError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PayoutFailureReasonTest {

    @Test
    fun `if the error is ServiceUnavailable should return ServiceUnavailable`() {
        // Given
        val fakeError = DataError.ServiceUnavailable()

        // When
        val fakeReason = fakeError.mapToPayoutFailureReason()

        // Then
        assertEquals(PayoutFailureReason.ServiceUnavailable, fakeReason)
    }

    @Test
    fun `if the error is InsufficientFunds should return InsufficientFunds`() {
        // Given
        val fakeError = DataError.InsufficientFunds()

        // When
        val fakeReason = fakeError.mapToPayoutFailureReason()

        // Then
        assertEquals(PayoutFailureReason.InsufficientFunds, fakeReason)
    }

    @Test
    fun `if the error is Network should return UnknownOutcome`() {
        // Given
        val fakeError = DataError.Network(IOException("fake"))

        // When
        val fakeReason = fakeError.mapToPayoutFailureReason()

        // Then
        assertEquals(PayoutFailureReason.UnknownOutcome, fakeReason)
    }

    @Test
    fun `if the error is Unexpected should return Unexpected`() {
        // Given
        val fakeError = DataError.Unexpected()

        // When
        val fakeReason = fakeError.mapToPayoutFailureReason()

        // Then
        assertEquals(PayoutFailureReason.Unexpected, fakeReason)
    }

    @Test
    fun `if the error is not a DataError should return Unexpected`() {
        // Given
        val fakeError = IllegalStateException("fake")

        // When
        val fakeReason = fakeError.mapToPayoutFailureReason()

        // Then
        assertEquals(PayoutFailureReason.Unexpected, fakeReason)
    }

    @Test
    fun `if the biometric result is Success should return null`() {
        // Given
        val fakeResult = BiometricResult.Success

        // When
        val fakeReason = fakeResult.mapToPayoutFailureReason()

        // Then
        assertNull(fakeReason)
    }

    @Test
    fun `if the biometric result is Cancelled should return BiometricCancelled`() {
        // Given
        val fakeResult = BiometricResult.Cancelled

        // When
        val fakeReason = fakeResult.mapToPayoutFailureReason()

        // Then
        assertEquals(PayoutFailureReason.BiometricCancelled, fakeReason)
    }

    @Test
    fun `if the biometric result is Unavailable should return BiometricUnavailable`() {
        // Given
        val fakeResult = BiometricResult.Unavailable

        // When
        val fakeReason = fakeResult.mapToPayoutFailureReason()

        // Then
        assertEquals(PayoutFailureReason.BiometricUnavailable, fakeReason)
    }
}
