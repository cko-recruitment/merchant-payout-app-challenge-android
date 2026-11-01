package manoellribeiro.dev.domain.model

import java.time.Instant
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PayoutTest {

    @Test
    fun `if the payout is printed should not contain the iban`() {
        // Given
        val fakeId = "fake-payout-id"
        val fakeAmount = Money(minorUnits = 40_000, currency = Currency.GBP)
        val fakeIban = "GB29NWBK60161331926819"
        val fakeExpectedIbanText = "iban=***"
        val payout = Payout(
            id = fakeId,
            status = PayoutStatus.Completed,
            amount = fakeAmount,
            iban = fakeIban,
            createdAt = Instant.parse("2026-01-23T10:00:00Z"),
        )

        // When
        val fakeText = payout.toString()

        // Then
        assertFalse(fakeText.contains(fakeIban))
        assertTrue(fakeText.contains(fakeExpectedIbanText))
    }

    @Test
    fun `if the payout is printed should still contain the fields needed to debug it`() {
        // Given
        val fakeId = "fake-payout-id"
        val fakeAmount = Money(minorUnits = 40_000, currency = Currency.GBP)
        val fakeIban = "GB29NWBK60161331926819"
        val fakeExpectedTexts = listOf(
            "id=$fakeId",
            "status=${PayoutStatus.Completed}",
            "amount=$fakeAmount",
            "createdAt=2026-01-23T10:00:00Z",
        )
        val payout = Payout(
            id = fakeId,
            status = PayoutStatus.Completed,
            amount = fakeAmount,
            iban = fakeIban,
            createdAt = Instant.parse("2026-01-23T10:00:00Z"),
        )

        // When
        val fakeText = payout.toString()

        // Then
        fakeExpectedTexts.forEach { assertTrue(fakeText.contains(it)) }
    }
}
