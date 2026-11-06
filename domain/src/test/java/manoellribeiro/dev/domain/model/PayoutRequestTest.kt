package manoellribeiro.dev.domain.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PayoutRequestTest {

    @Test
    fun `if the request is printed should not contain the iban`() {
        // Given
        val fakeAmount = Money(minorUnits = 40_000, currency = Currency.GBP)
        val fakeIban = "GB29NWBK60161331926819"
        val fakeDeviceId = "fake-device-id"
        val fakeExpectedIbanText = "iban=***"
        val payoutRequest = PayoutRequest(amount = fakeAmount, iban = fakeIban, deviceId = fakeDeviceId)

        // When
        val fakeText = payoutRequest.toString()

        // Then
        assertFalse(fakeText.contains(fakeIban))
        assertTrue(fakeText.contains(fakeExpectedIbanText))
    }

    @Test
    fun `if the request is printed should not contain the device id`() {
        // Given
        val fakeAmount = Money(minorUnits = 40_000, currency = Currency.GBP)
        val fakeIban = "GB29NWBK60161331926819"
        val fakeDeviceId = "fake-device-id"
        val fakeExpectedDeviceIdText = "deviceId=***"
        val payoutRequest = PayoutRequest(amount = fakeAmount, iban = fakeIban, deviceId = fakeDeviceId)

        // When
        val fakeText = payoutRequest.toString()

        // Then
        assertFalse(fakeText.contains(fakeDeviceId))
        assertTrue(fakeText.contains(fakeExpectedDeviceIdText))
    }

    @Test
    fun `if the request is printed should still contain the amount`() {
        // Given
        val fakeAmount = Money(minorUnits = 40_000, currency = Currency.GBP)
        val fakeIban = "GB29NWBK60161331926819"
        val fakeDeviceId = "fake-device-id"
        val fakeExpectedAmountText = "amount=$fakeAmount"
        val payoutRequest = PayoutRequest(amount = fakeAmount, iban = fakeIban, deviceId = fakeDeviceId)

        // When
        val fakeText = payoutRequest.toString()

        // Then
        assertTrue(fakeText.contains(fakeExpectedAmountText))
    }
}
