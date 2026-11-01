package manoellribeiro.dev.data.remote.model

import java.time.Instant
import kotlinx.serialization.SerializationException
import manoellribeiro.dev.data.remote.apiJson
import manoellribeiro.dev.domain.model.ActivityItem
import manoellribeiro.dev.domain.model.ActivityStatus
import manoellribeiro.dev.domain.model.ActivityType
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemoteActivityItemExtensionsTest {

    @Test
    fun `if the item is mapped should copy every field`() {
        // Given
        val fakeDate = Instant.parse("2026-01-23T10:00:00Z")
        val fakeItem = createFakeItem(type = "deposit", status = "completed", date = fakeDate)

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        val fakeExpected = ActivityItem(
            id = "fake-activity-id",
            type = ActivityType.Deposit,
            amount = Money(75_000, Currency.GBP),
            date = fakeDate,
            description = "Fake payment",
            status = ActivityStatus.Completed,
        )
        assertEquals(fakeExpected, fakeResult)
    }

    @Test
    fun `if the amount is negative should keep the sign`() {
        // Given
        val fakeItem = createFakeItem(amount = -12_500)

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(-12_500, fakeResult.amount.minorUnits)
    }

    @Test
    fun `if the currency is EUR should map the amount to EUR`() {
        // Given
        val fakeItem = createFakeItem(currency = RemoteCurrency.EUR)

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(Currency.EUR, fakeResult.amount.currency)
    }

    @Test
    fun `if the type is payout should map to Payout`() {
        // Given
        val fakeItem = createFakeItem(type = "payout")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityType.Payout, fakeResult.type)
    }

    @Test
    fun `if the type is deposit should map to Deposit`() {
        // Given
        val fakeItem = createFakeItem(type = "deposit")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityType.Deposit, fakeResult.type)
    }

    @Test
    fun `if the type is refund should map to Refund`() {
        // Given
        val fakeItem = createFakeItem(type = "refund")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityType.Refund, fakeResult.type)
    }

    @Test
    fun `if the type is fee should map to Fee`() {
        // Given
        val fakeItem = createFakeItem(type = "fee")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityType.Fee, fakeResult.type)
    }

    @Test
    fun `if the type is not known should map to Unknown`() {
        // Given
        val fakeItem = createFakeItem(type = "fake-new-type")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityType.Unknown, fakeResult.type)
    }

    @Test
    fun `if the type is empty should map to Unknown`() {
        // Given
        val fakeItem = createFakeItem(type = "")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityType.Unknown, fakeResult.type)
    }

    @Test
    fun `if the status is completed should map to Completed`() {
        // Given
        val fakeItem = createFakeItem(status = "completed")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityStatus.Completed, fakeResult.status)
    }

    @Test
    fun `if the status is pending should map to Pending`() {
        // Given
        val fakeItem = createFakeItem(status = "pending")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityStatus.Pending, fakeResult.status)
    }

    @Test
    fun `if the status is processing should map to Processing`() {
        // Given
        val fakeItem = createFakeItem(status = "processing")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityStatus.Processing, fakeResult.status)
    }

    @Test
    fun `if the status is failed should map to Failed`() {
        // Given
        val fakeItem = createFakeItem(status = "failed")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityStatus.Failed, fakeResult.status)
    }

    @Test
    fun `if the status is not known should map to Unknown`() {
        // Given
        val fakeItem = createFakeItem(status = "fake-new-status")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityStatus.Unknown, fakeResult.status)
    }

    @Test
    fun `if the status is empty should map to Unknown`() {
        // Given
        val fakeItem = createFakeItem(status = "")

        // When
        val fakeResult = fakeItem.toDomain()

        // Then
        assertEquals(ActivityStatus.Unknown, fakeResult.status)
    }

    @Test
    fun `if the json has a date with an offset and an unknown field should decode it`() {
        // Given
        val fakeJson = """
            {
              "id": "fake-activity-id",
              "type": "deposit",
              "amount": 75000,
              "currency": "GBP",
              "date": "2026-01-23T11:00:00+01:00",
              "description": "Fake payment",
              "status": "completed",
              "fake_new_field": true
            }
        """.trimIndent()

        // When
        val fakeResult = apiJson.decodeFromString<RemoteActivityItem>(fakeJson)

        // Then
        val fakeExpected = createFakeItem(type = "deposit", status = "completed")
        assertEquals(fakeExpected, fakeResult)
    }

    @Test
    fun `if the json has an unknown currency should throw a SerializationException`() {
        // Given
        val fakeJson = """
            {
              "id": "fake-activity-id",
              "type": "deposit",
              "amount": 75000,
              "currency": "JPY",
              "date": "2026-01-23T10:00:00Z",
              "description": "Fake payment",
              "status": "completed"
            }
        """.trimIndent()

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString<RemoteActivityItem>(fakeJson)
        }

        // Then
        assertNotNull(fakeThrown)
    }

    private fun createFakeItem(
        type: String = "deposit",
        amount: Long = 75_000,
        currency: RemoteCurrency = RemoteCurrency.GBP,
        status: String = "completed",
        date: Instant = Instant.parse("2026-01-23T10:00:00Z"),
    ) = RemoteActivityItem(
        id = "fake-activity-id",
        type = type,
        amount = amount,
        currency = currency,
        date = date,
        description = "Fake payment",
        status = status,
    )
}
