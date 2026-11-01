package manoellribeiro.dev.data.remote.model

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import manoellribeiro.dev.data.remote.apiJson
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.PayoutRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RemotePayoutRequestExtensionsTest {

    @Test
    fun `if the request is mapped should copy every field`() {
        // Given
        val fakeRequest = PayoutRequest(
            amount = Money(40_000, Currency.GBP),
            iban = "GB29NWBK60161331926819",
            deviceId = "fake-device-id",
        )

        // When
        val fakeResult = fakeRequest.toRemote()

        // Then
        val fakeExpected = RemotePayoutRequest(
            amount = 40_000,
            currency = RemoteCurrency.GBP,
            iban = "GB29NWBK60161331926819",
            deviceId = "fake-device-id",
        )
        assertEquals(fakeExpected, fakeResult)
    }

    @Test
    fun `if the amount is in EUR should map the currency to EUR`() {
        // Given
        val fakeRequest = PayoutRequest(
            amount = Money(10_050, Currency.EUR),
            iban = "GB29NWBK60161331926819",
            deviceId = "fake-eur-device-id",
        )

        // When
        val fakeResult = fakeRequest.toRemote()

        // Then
        assertEquals(10_050, fakeResult.amount)
        assertEquals(RemoteCurrency.EUR, fakeResult.currency)
    }

    @Test
    fun `if the request is encoded should use the field names of the api`() {
        // Given
        val fakeRequest = RemotePayoutRequest(
            amount = 40_000,
            currency = RemoteCurrency.GBP,
            iban = "GB29NWBK60161331926819",
            deviceId = "fake-device-id",
        )

        // When
        val fakeResult = apiJson.parseToJsonElement(apiJson.encodeToString(fakeRequest)).jsonObject

        // Then
        assertEquals(setOf("amount", "currency", "iban", "device_id"), fakeResult.keys)
        assertEquals(40_000, fakeResult.getValue("amount").jsonPrimitive.long)
        assertEquals("GBP", fakeResult.getValue("currency").jsonPrimitive.content)
        assertEquals("GB29NWBK60161331926819", fakeResult.getValue("iban").jsonPrimitive.content)
        assertEquals("fake-device-id", fakeResult.getValue("device_id").jsonPrimitive.content)
    }
}
