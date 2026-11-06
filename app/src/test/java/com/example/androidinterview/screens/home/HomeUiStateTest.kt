package com.example.androidinterview.screens.home

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HomeUiStateTest {

    @Test
    fun `if both sections failed should return the single Error state`() {
        // Given
        val fakeBalance = BalanceSectionState.Error
        val fakeActivity = RecentActivitySectionState.Error

        // When
        val fakeState = mapToHomeUiState(fakeBalance, fakeActivity)

        // Then
        assertEquals(HomeUiState.Error, fakeState)
    }

    @Test
    fun `if only the balance failed should return Content keeping the activity visible`() {
        // Given
        val fakeBalance = BalanceSectionState.Error
        val fakeActivity = RecentActivitySectionState.Loaded(listOf(createFakeRecentActivityItem()))

        // When
        val fakeState = mapToHomeUiState(fakeBalance, fakeActivity)

        // Then
        assertEquals(HomeUiState.Content(fakeBalance, fakeActivity), fakeState)
    }

    @Test
    fun `if only the activity failed should return Content keeping the balance visible`() {
        // Given
        val fakeBalance = BalanceSectionState.Loaded(available = "£5,000.00", pending = "£100.00")
        val fakeActivity = RecentActivitySectionState.Error

        // When
        val fakeState = mapToHomeUiState(fakeBalance, fakeActivity)

        // Then
        assertEquals(HomeUiState.Content(fakeBalance, fakeActivity), fakeState)
    }

    @Test
    fun `if the balance failed and the activity is empty should return Content`() {
        // Given
        val fakeBalance = BalanceSectionState.Error
        val fakeActivity = RecentActivitySectionState.Empty

        // When
        val fakeState = mapToHomeUiState(fakeBalance, fakeActivity)

        // Then
        assertEquals(HomeUiState.Content(fakeBalance, fakeActivity), fakeState)
    }

    @Test
    fun `if both sections loaded should return Content with both of them`() {
        // Given
        val fakeBalance = BalanceSectionState.Loaded(available = "£1,000.00", pending = "£0.00")
        val fakeActivity = RecentActivitySectionState.Loaded(listOf(createFakeRecentActivityItem()))

        // When
        val fakeState = mapToHomeUiState(fakeBalance, fakeActivity)

        // Then
        assertEquals(HomeUiState.Content(fakeBalance, fakeActivity), fakeState)
    }

    private fun createFakeRecentActivityItem(): RecentActivityItem = RecentActivityItem(
        id = "fake-activity-id",
        description = "Fake payment",
        formattedAmount = "£400.00",
        isOutflow = false,
    )
}
