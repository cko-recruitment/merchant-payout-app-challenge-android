package manoellribeiro.dev.data.remote.model

import kotlinx.serialization.SerializationException
import manoellribeiro.dev.data.remote.apiJson
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.MerchantBalance
import manoellribeiro.dev.domain.model.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemoteMerchantResponseExtensionsTest {

    @Test
    fun `if the response is mapped should keep each balance in its own field`() {
        // Given
        val fakeResponse = RemoteMerchantResponse(
            availableBalance = 500_000,
            pendingBalance = 25_000,
            currency = RemoteCurrency.GBP,
        )

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        val fakeExpected = MerchantBalance(
            available = Money(500_000, Currency.GBP),
            pending = Money(25_000, Currency.GBP),
        )
        assertEquals(fakeExpected, fakeResult)
    }

    @Test
    fun `if the currency is EUR should use it for both balances`() {
        // Given
        val fakeResponse = RemoteMerchantResponse(
            availableBalance = 1_250,
            pendingBalance = 300,
            currency = RemoteCurrency.EUR,
        )

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        assertEquals(Currency.EUR, fakeResult.available.currency)
        assertEquals(Currency.EUR, fakeResult.pending.currency)
    }

    @Test
    fun `if both balances are zero should map them to zero`() {
        // Given
        val fakeResponse = RemoteMerchantResponse(
            availableBalance = 0,
            pendingBalance = 0,
            currency = RemoteCurrency.GBP,
        )

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        assertEquals(0, fakeResult.available.minorUnits)
        assertEquals(0, fakeResult.pending.minorUnits)
    }

    @Test
    fun `if the json has snake case fields and an unknown field should decode it`() {
        // Given
        val fakeJson = """
            {
              "available_balance": 500000,
              "pending_balance": 25000,
              "currency": "GBP",
              "activity": []
            }
        """.trimIndent()

        // When
        val fakeResult = apiJson.decodeFromString<RemoteMerchantResponse>(fakeJson)

        // Then
        val fakeExpected = RemoteMerchantResponse(
            availableBalance = 500_000,
            pendingBalance = 25_000,
            currency = RemoteCurrency.GBP,
        )
        assertEquals(fakeExpected, fakeResult)
    }

    @Test
    fun `if the json has no pending balance should throw a SerializationException`() {
        // Given
        val fakeJson = """{"available_balance": 500000, "currency": "GBP"}"""

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString<RemoteMerchantResponse>(fakeJson)
        }

        // Then
        assertNotNull(fakeThrown)
    }

    @Test
    fun `if the json has an unknown currency should throw a SerializationException`() {
        // Given
        val fakeJson = """{"available_balance": 500000, "pending_balance": 25000, "currency": "JPY"}"""

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString<RemoteMerchantResponse>(fakeJson)
        }

        // Then
        assertNotNull(fakeThrown)
    }
}
