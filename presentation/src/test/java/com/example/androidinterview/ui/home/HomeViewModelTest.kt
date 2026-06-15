package com.example.androidinterview.ui.home

import android.content.Context
import com.example.androidinterview.domain.model.Activity
import com.example.androidinterview.domain.model.ActivityStatus
import com.example.androidinterview.domain.model.ActivityType
import com.example.androidinterview.domain.model.Currency
import com.example.androidinterview.domain.model.Merchant
import com.example.androidinterview.domain.repository.MerchantRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val repository: MerchantRepository = mockk()
    private val mapper = HomeUiModelMapper()
    private val context: Context = mockk()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(any()) } returns ""
        coEvery { repository.getMerchant() } returns Result.success(fakeMerchant())
        viewModel = HomeViewModel(repository, mapper, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state is Loading before getMerchant responds`() {
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)
    }

    @Test
    fun `state is Success after getMerchant responds`() {
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HomeUiState.Success)
    }

    @Test
    fun `state is Error when getMerchant returns failure`() {
        coEvery { repository.getMerchant() } returns Result.failure(RuntimeException("Network error"))
        viewModel = HomeViewModel(repository, mapper, context)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }

    // region helpers

    private fun fakeMerchant(
        availableBalance: Int = 100_000,
        pendingBalance: Int = 5_000,
        currency: Currency = Currency.GBP,
        activity: List<Activity> = emptyList(),
    ) = Merchant(
        availableBalance = availableBalance,
        pendingBalance = pendingBalance,
        currency = currency,
        activity = activity,
    )

    private fun fakeActivity(
        id: String = "act-1",
        type: ActivityType = ActivityType.PAYOUT,
        amount: Int = -1000,
        currency: Currency = Currency.GBP,
        date: String = "2026-01-01T00:00:00Z",
        description: String = "Test activity",
        status: ActivityStatus = ActivityStatus.COMPLETED,
    ) = Activity(
        id = id,
        type = type,
        amount = amount,
        currency = currency,
        date = date,
        description = description,
        status = status,
    )

    // endregion
}
