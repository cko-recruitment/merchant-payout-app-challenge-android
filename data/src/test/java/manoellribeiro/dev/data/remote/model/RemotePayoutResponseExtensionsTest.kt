package manoellribeiro.dev.data.remote.model

import java.time.Instant
import kotlinx.serialization.SerializationException
import manoellribeiro.dev.data.remote.apiJson
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.Payout
import manoellribeiro.dev.domain.model.PayoutStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemotePayoutResponseExtensionsTest {

    @Test
    fun `if the response is mapped should copy every field`() {
        // Given
        val fakeCreatedAt = Instant.parse("2026-02-10T09:30:00Z")
        val fakeResponse = createFakeResponse(status = "completed", createdAt = fakeCreatedAt)

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        val fakeExpected = Payout(
            id = "fake-payout-id",
            status = PayoutStatus.Completed,
            amount = Money(40_000, Currency.GBP),
            iban = "GB29NWBK60161331926819",
            createdAt = fakeCreatedAt,
        )
        assertEquals(fakeExpected, fakeResult)
    }

    @Test
    fun `if the status is pending should map to Pending`() {
        // Given
        val fakeResponse = createFakeResponse(status = "pending")

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        assertEquals(PayoutStatus.Pending, fakeResult.status)
    }

    @Test
    fun `if the status is processing should map to Processing`() {
        // Given
        val fakeResponse = createFakeResponse(status = "processing")

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        assertEquals(PayoutStatus.Processing, fakeResult.status)
    }

    @Test
    fun `if the status is completed should map to Completed`() {
        // Given
        val fakeResponse = createFakeResponse(status = "completed")

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        assertEquals(PayoutStatus.Completed, fakeResult.status)
    }

    @Test
    fun `if the status is failed should map to Failed`() {
        // Given
        val fakeResponse = createFakeResponse(status = "failed")

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        assertEquals(PayoutStatus.Failed, fakeResult.status)
    }

    @Test
    fun `if the status is not known should map to Unknown`() {
        // Given
        val fakeResponse = createFakeResponse(status = "fake-new-status")

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        assertEquals(PayoutStatus.Unknown, fakeResult.status)
    }

    @Test
    fun `if the status is empty should map to Unknown`() {
        // Given
        val fakeResponse = createFakeResponse(status = "")

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        assertEquals(PayoutStatus.Unknown, fakeResult.status)
    }

    @Test
    fun `if the currency is EUR should map the amount to EUR`() {
        // Given
        val fakeResponse = createFakeResponse(status = "pending", currency = RemoteCurrency.EUR)

        // When
        val fakeResult = fakeResponse.toDomain()

        // Then
        assertEquals(Money(40_000, Currency.EUR), fakeResult.amount)
    }

    @Test
    fun `if the json has a created_at field should decode it`() {
        // Given
        val fakeJson = """
            {
              "id": "fake-payout-id",
              "status": "completed",
              "amount": 40000,
              "currency": "GBP",
              "iban": "GB29NWBK60161331926819",
              "created_at": "2026-02-10T09:30:00Z"
            }
        """.trimIndent()

        // When
        val fakeResult = apiJson.decodeFromString<RemotePayoutResponse>(fakeJson)

        // Then
        assertEquals(createFakeResponse(status = "completed"), fakeResult)
    }

    @Test
    fun `if the json has an invalid created_at should throw a SerializationException`() {
        // Given
        val fakeJson = """
            {
              "id": "fake-payout-id",
              "status": "completed",
              "amount": 40000,
              "currency": "GBP",
              "iban": "GB29NWBK60161331926819",
              "created_at": "fake-date"
            }
        """.trimIndent()

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString<RemotePayoutResponse>(fakeJson)
        }

        // Then
        assertNotNull(fakeThrown)
    }

    private fun createFakeResponse(
        status: String,
        currency: RemoteCurrency = RemoteCurrency.GBP,
        createdAt: Instant = Instant.parse("2026-02-10T09:30:00Z"),
    ) = RemotePayoutResponse(
        id = "fake-payout-id",
        status = status,
        amount = 40_000,
        currency = currency,
        iban = "GB29NWBK60161331926819",
        createdAt = createdAt,
    )
}
