package com.example.androidinterview.screens.transactions

import com.example.androidinterview.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import manoellribeiro.dev.domain.model.ActivityItem
import manoellribeiro.dev.domain.model.ActivityStatus
import manoellribeiro.dev.domain.model.ActivityType
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.Money
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransactionListItemTest {

    private lateinit var originalLocale: Locale
    private val fakeZone: ZoneId = ZoneId.of("UTC")
    private val fakeToday: LocalDate = LocalDate.of(2026, 1, 23)

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
    fun `if the list is empty should return an empty list`() {
        // Given
        val fakeItems = emptyList<ActivityItem>()

        // When
        val fakeResult = fakeItems.mapToTransactionListItems(fakeZone, fakeToday)

        // Then
        assertTrue(fakeResult.isEmpty())
    }

    @Test
    fun `if two items share a day should return one header followed by both rows`() {
        // Given
        val fakeItems = listOf(
            createFakeActivityItem(id = "fake-first-id", date = "2026-01-20T09:00:00Z"),
            createFakeActivityItem(id = "fake-second-id", date = "2026-01-20T18:00:00Z"),
        )

        // When
        val fakeResult = fakeItems.mapToTransactionListItems(fakeZone, fakeToday)

        // Then
        assertEquals(listOf("header-2026-01-20", "fake-first-id", "fake-second-id"), fakeResult.map { it.key })
    }

    @Test
    fun `if the items are on different days should return one header per day in the same order`() {
        // Given
        val fakeItems = listOf(
            createFakeActivityItem(id = "fake-newer-id", date = "2026-01-20T09:00:00Z"),
            createFakeActivityItem(id = "fake-older-id", date = "2026-01-19T09:00:00Z"),
        )

        // When
        val fakeResult = fakeItems.mapToTransactionListItems(fakeZone, fakeToday)

        // Then
        assertEquals(
            listOf("header-2026-01-20", "fake-newer-id", "header-2026-01-19", "fake-older-id"),
            fakeResult.map { it.key },
        )
    }

    @Test
    fun `if items of a day are not adjacent should group them under the first header of that day`() {
        // Given
        val fakeItems = listOf(
            createFakeActivityItem(id = "fake-first-id", date = "2026-01-20T09:00:00Z"),
            createFakeActivityItem(id = "fake-second-id", date = "2026-01-19T09:00:00Z"),
            createFakeActivityItem(id = "fake-third-id", date = "2026-01-20T07:00:00Z"),
        )

        // When
        val fakeResult = fakeItems.mapToTransactionListItems(fakeZone, fakeToday)

        // Then
        assertEquals(
            listOf("header-2026-01-20", "fake-first-id", "fake-third-id", "header-2026-01-19", "fake-second-id"),
            fakeResult.map { it.key },
        )
    }

    @Test
    fun `if the day is today should label the header as today`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(date = "2026-01-23T09:00:00Z"))

        // When
        val fakeHeader = fakeItems.mapToTransactionListItems(fakeZone, fakeToday).first()

        // Then
        val fakeDateHeader = assertInstanceOf(TransactionListItem.DateHeader::class.java, fakeHeader)
        assertEquals(R.string.transactions_today, fakeDateHeader.relativeLabelRes)
        assertEquals("23 Jan 2026", fakeDateHeader.formattedDate)
    }

    @Test
    fun `if the day is yesterday should label the header as yesterday`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(date = "2026-01-22T09:00:00Z"))

        // When
        val fakeHeader = fakeItems.mapToTransactionListItems(fakeZone, fakeToday).first()

        // Then
        val fakeDateHeader = assertInstanceOf(TransactionListItem.DateHeader::class.java, fakeHeader)
        assertEquals(R.string.transactions_yesterday, fakeDateHeader.relativeLabelRes)
    }

    @Test
    fun `if the day is before yesterday should not label the header`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(date = "2026-01-21T09:00:00Z"))

        // When
        val fakeHeader = fakeItems.mapToTransactionListItems(fakeZone, fakeToday).first()

        // Then
        val fakeDateHeader = assertInstanceOf(TransactionListItem.DateHeader::class.java, fakeHeader)
        assertNull(fakeDateHeader.relativeLabelRes)
    }

    @Test
    fun `if the day is after today should not label the header`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(date = "2026-01-24T09:00:00Z"))

        // When
        val fakeHeader = fakeItems.mapToTransactionListItems(fakeZone, fakeToday).first()

        // Then
        val fakeDateHeader = assertInstanceOf(TransactionListItem.DateHeader::class.java, fakeHeader)
        assertNull(fakeDateHeader.relativeLabelRes)
    }

    @Test
    fun `if the zone is ahead of UTC should group a late evening date on the next local day`() {
        // Given
        val fakeTokyoZone = ZoneId.of("Asia/Tokyo")
        val fakeItems = listOf(createFakeActivityItem(date = "2026-01-22T20:00:00Z"))

        // When
        val fakeHeader = fakeItems.mapToTransactionListItems(fakeTokyoZone, fakeToday).first()

        // Then
        val fakeDateHeader = assertInstanceOf(TransactionListItem.DateHeader::class.java, fakeHeader)
        assertEquals("header-2026-01-23", fakeDateHeader.key)
        assertEquals(R.string.transactions_today, fakeDateHeader.relativeLabelRes)
    }

    @Test
    fun `if the zone is behind UTC should group an early morning date on the previous local day`() {
        // Given
        val fakeNewYorkZone = ZoneId.of("America/New_York")
        val fakeItems = listOf(createFakeActivityItem(date = "2026-01-23T02:00:00Z"))

        // When
        val fakeHeader = fakeItems.mapToTransactionListItems(fakeNewYorkZone, fakeToday).first()

        // Then
        val fakeDateHeader = assertInstanceOf(TransactionListItem.DateHeader::class.java, fakeHeader)
        assertEquals("header-2026-01-22", fakeDateHeader.key)
        assertEquals(R.string.transactions_yesterday, fakeDateHeader.relativeLabelRes)
    }

    @Test
    fun `if an item is mapped should fill every field of its row`() {
        // Given
        val fakeItems = listOf(
            createFakeActivityItem(
                id = "fake-row-id",
                date = "2026-01-20T09:00:00Z",
                description = "Fake invoice",
                minorUnits = 12_345,
                type = ActivityType.Deposit,
                status = ActivityStatus.Completed,
            ),
        )

        // When
        val fakeRow = fakeItems.mapToTransactionListItems(fakeZone, fakeToday).last()

        // Then
        assertEquals(
            TransactionListItem.Row(
                key = "fake-row-id",
                typeLabelRes = R.string.activity_type_deposit,
                description = "Fake invoice",
                formattedDate = "20 Jan 2026",
                formattedAmount = "£123.45",
                statusLabelRes = R.string.activity_status_completed,
                isOutflow = false,
            ),
            fakeRow,
        )
    }

    @Test
    fun `if the amount is negative should mark the row as an outflow`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(minorUnits = -2_550))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertTrue(fakeRow.isOutflow)
        assertEquals("-£25.50", fakeRow.formattedAmount)
    }

    @Test
    fun `if the amount is zero should not mark the row as an outflow`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(minorUnits = 0))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertFalse(fakeRow.isOutflow)
    }

    @Test
    fun `if the type is Payout should use the payout label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(type = ActivityType.Payout))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_type_payout, fakeRow.typeLabelRes)
    }

    @Test
    fun `if the type is Deposit should use the deposit label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(type = ActivityType.Deposit))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_type_deposit, fakeRow.typeLabelRes)
    }

    @Test
    fun `if the type is Refund should use the refund label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(type = ActivityType.Refund))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_type_refund, fakeRow.typeLabelRes)
    }

    @Test
    fun `if the type is Fee should use the fee label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(type = ActivityType.Fee))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_type_fee, fakeRow.typeLabelRes)
    }

    @Test
    fun `if the type is Unknown should use the unknown label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(type = ActivityType.Unknown))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_type_unknown, fakeRow.typeLabelRes)
    }

    @Test
    fun `if the status is Completed should use the completed label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(status = ActivityStatus.Completed))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_status_completed, fakeRow.statusLabelRes)
    }

    @Test
    fun `if the status is Pending should use the pending label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(status = ActivityStatus.Pending))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_status_pending, fakeRow.statusLabelRes)
    }

    @Test
    fun `if the status is Processing should use the processing label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(status = ActivityStatus.Processing))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_status_processing, fakeRow.statusLabelRes)
    }

    @Test
    fun `if the status is Failed should use the failed label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(status = ActivityStatus.Failed))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_status_failed, fakeRow.statusLabelRes)
    }

    @Test
    fun `if the status is Unknown should use the unknown label`() {
        // Given
        val fakeItems = listOf(createFakeActivityItem(status = ActivityStatus.Unknown))

        // When
        val fakeRow = mapToSingleRow(fakeItems)

        // Then
        assertEquals(R.string.activity_status_unknown, fakeRow.statusLabelRes)
    }

    private fun mapToSingleRow(items: List<ActivityItem>): TransactionListItem.Row =
        items.mapToTransactionListItems(fakeZone, fakeToday)
            .filterIsInstance<TransactionListItem.Row>()
            .single()

    private fun createFakeActivityItem(
        id: String = "fake-activity-id",
        date: String = "2026-01-20T10:00:00Z",
        description: String = "Fake payment",
        minorUnits: Long = 40_000,
        type: ActivityType = ActivityType.Deposit,
        status: ActivityStatus = ActivityStatus.Completed,
    ): ActivityItem = ActivityItem(
        id = id,
        type = type,
        amount = Money(minorUnits = minorUnits, currency = Currency.GBP),
        date = Instant.parse(date),
        description = description,
        status = status,
    )
}
