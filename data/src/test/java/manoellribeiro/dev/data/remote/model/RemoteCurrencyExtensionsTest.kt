package manoellribeiro.dev.data.remote.model

import kotlinx.serialization.SerializationException
import manoellribeiro.dev.data.remote.apiJson
import manoellribeiro.dev.domain.model.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemoteCurrencyExtensionsTest {

    @Test
    fun `if the remote currency is GBP should map to the domain GBP`() {
        // Given
        val fakeCurrency = RemoteCurrency.GBP

        // When
        val fakeResult = fakeCurrency.toDomain()

        // Then
        assertEquals(Currency.GBP, fakeResult)
    }

    @Test
    fun `if the remote currency is EUR should map to the domain EUR`() {
        // Given
        val fakeCurrency = RemoteCurrency.EUR

        // When
        val fakeResult = fakeCurrency.toDomain()

        // Then
        assertEquals(Currency.EUR, fakeResult)
    }

    @Test
    fun `if the domain currency is GBP should map to the remote GBP`() {
        // Given
        val fakeCurrency = Currency.GBP

        // When
        val fakeResult = fakeCurrency.toRemote()

        // Then
        assertEquals(RemoteCurrency.GBP, fakeResult)
    }

    @Test
    fun `if the domain currency is EUR should map to the remote EUR`() {
        // Given
        val fakeCurrency = Currency.EUR

        // When
        val fakeResult = fakeCurrency.toRemote()

        // Then
        assertEquals(RemoteCurrency.EUR, fakeResult)
    }

    @Test
    fun `if the json has an unknown currency should throw a SerializationException`() {
        // Given
        val fakeJson = "\"JPY\""

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString<RemoteCurrency>(fakeJson)
        }

        // Then
        assertTrue(fakeThrown.message.orEmpty().contains("JPY"))
    }
}
