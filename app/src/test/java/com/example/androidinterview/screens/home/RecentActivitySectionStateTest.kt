package com.example.androidinterview.screens.home

import java.time.Instant
import java.util.Locale
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.model.ActivityItem
import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.model.ActivityStatus
import manoellribeiro.dev.domain.model.ActivityType
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RecentActivitySectionStateTest {

    private lateinit var originalLocale: Locale

    @BeforeEach
    fun setup() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.UK)
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `if the page has items should return Loaded with them mapped in the same order`() {
        // Given
        val fakeFirstItem = createFakeActivityItem(
            id = "fake-first-id",
            description = "Fake first payment",
            minorUnits = 40_000,
        )
        val fakeSecondItem = createFakeActivityItem(
            id = "fake-second-id",
            description = "Fake second payment",
            minorUnits = 1_250,
        )
        val fakeResult = Result.success(createFakePage(listOf(fakeFirstItem, fakeSecondItem)))

        // When
        val fakeState = fakeResult.mapToSectionState()

        // Then
        assertEquals(
            RecentActivitySectionState.Loaded(
                listOf(
                    RecentActivityItem(
                        id = "fake-first-id",
                        description = "Fake first payment",
                        formattedAmount = "£400.00",
                        isOutflow = false,
                    ),
                    RecentActivityItem(
                        id = "fake-second-id",
                        description = "Fake second payment",
                        formattedAmount = "£12.50",
                        isOutflow = false,
                    ),
                ),
            ),
            fakeState,
        )
    }

    @Test
    fun `if the amount is negative should mark the item as an outflow`() {
        // Given
        val fakeItem = createFakeActivityItem(minorUnits = -2_550)
        val fakeResult = Result.success(createFakePage(listOf(fakeItem)))

        // When
        val fakeState = fakeResult.mapToSectionState()

        // Then
        val fakeMappedItem = assertInstanceOf(RecentActivitySectionState.Loaded::class.java, fakeState).items.single()
        assertTrue(fakeMappedItem.isOutflow)
        assertEquals("-£25.50", fakeMappedItem.formattedAmount)
    }

    @Test
    fun `if the amount is zero should not mark the item as an outflow`() {
        // Given
        val fakeItem = createFakeActivityItem(minorUnits = 0)
        val fakeResult = Result.success(createFakePage(listOf(fakeItem)))

        // When
        val fakeState = fakeResult.mapToSectionState()

        // Then
        val fakeMappedItem = assertInstanceOf(RecentActivitySectionState.Loaded::class.java, fakeState).items.single()
        assertFalse(fakeMappedItem.isOutflow)
    }

    @Test
    fun `if the page has no items should return Empty`() {
        // Given
        val fakeResult = Result.success(createFakePage(emptyList()))

        // When
        val fakeState = fakeResult.mapToSectionState()

        // Then
        assertEquals(RecentActivitySectionState.Empty, fakeState)
    }

    @Test
    fun `if the result is a failure should return Error`() {
        // Given
        val fakeResult = Result.failure<ActivityPage>(DataError.Unexpected())

        // When
        val fakeState = fakeResult.mapToSectionState()

        // Then
        assertEquals(RecentActivitySectionState.Error, fakeState)
    }

    private fun createFakePage(items: List<ActivityItem>): ActivityPage = ActivityPage(
        items = items,
        nextCursor = null,
        hasMore = false,
    )

    private fun createFakeActivityItem(
        id: String = "fake-activity-id",
        description: String = "Fake payment",
        minorUnits: Long = 40_000,
    ): ActivityItem = ActivityItem(
        id = id,
        type = ActivityType.Deposit,
        amount = Money(minorUnits = minorUnits, currency = Currency.GBP),
        date = Instant.parse("2026-01-23T10:00:00Z"),
        description = description,
        status = ActivityStatus.Completed,
    )
}
