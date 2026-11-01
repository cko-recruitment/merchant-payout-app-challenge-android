package com.example.androidinterview.screens.home

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.model.ActivityItem
import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.model.ActivityStatus
import manoellribeiro.dev.domain.model.ActivityType
import manoellribeiro.dev.domain.model.Currency
import manoellribeiro.dev.domain.model.MerchantBalance
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import manoellribeiro.dev.domain.usecase.GetActivityUseCase
import manoellribeiro.dev.domain.usecase.GetBalanceUseCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class HomeViewModelTest {

    private lateinit var originalLocale: Locale
    private lateinit var getBalance: GetBalanceUseCase
    private lateinit var getActivity: GetActivityUseCase
    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setup() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.UK)

        getBalance = mockk()
        getActivity = mockk()

        viewModel = HomeViewModel(
            getBalance = getBalance,
            getActivity = getActivity,
        )
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `if loadHome was not called should start in Loading`() = runTest {
        // Given
        val fakeExpectedState = HomeUiState.Loading

        // When
        val fakeState = viewModel.uiState.value

        // Then
        assertEquals(fakeExpectedState, fakeState)
        coVerify(exactly = 0) { getBalance() }
        coVerify(exactly = 0) { getActivity(any(), any()) }
    }

    @Test
    fun `if both sections load should emit Loading then the balance then both sections`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.success(createFakeBalance())
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(listOf(createFakeActivityItem())))
        val fakeLoadedBalance = BalanceSectionState.Loaded(available = "£5,000.00", pending = "£25.50")

        // When
        val fakeStates = collectStates(viewModel.uiState)
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        assertEquals(
            listOf(
                HomeUiState.Loading,
                HomeUiState.Content(fakeLoadedBalance, RecentActivitySectionState.Loading),
                HomeUiState.Content(
                    fakeLoadedBalance,
                    RecentActivitySectionState.Loaded(listOf(createFakeRecentActivityItem())),
                ),
            ),
            fakeStates,
        )
    }

    @Test
    fun `if loadHome is called should ask for the first three activity items`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.success(createFakeBalance())
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(emptyList()))

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getBalance() }
        coVerify(exactly = 1) { getActivity(null, 3) }
    }

    @Test
    fun `if the activity is empty should emit Content with an Empty activity section`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.success(createFakeBalance())
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(emptyList()))

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        assertEquals(
            HomeUiState.Content(
                BalanceSectionState.Loaded(available = "£5,000.00", pending = "£25.50"),
                RecentActivitySectionState.Empty,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `if the balance answered and the activity is slow should show the balance with the activity Loading`() = runTest {
        // Given
        val fakeActivityResult = CompletableDeferred<Result<ActivityPage>>()
        coEvery { getBalance() } returns Result.success(createFakeBalance())
        coEvery { getActivity(any(), any()) } coAnswers { fakeActivityResult.await() }

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        assertEquals(
            HomeUiState.Content(
                BalanceSectionState.Loaded(available = "£5,000.00", pending = "£25.50"),
                RecentActivitySectionState.Loading,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `if the activity answered and the balance is slow should stay in Loading`() = runTest {
        // Given
        val fakeBalanceResult = CompletableDeferred<Result<MerchantBalance>>()
        coEvery { getBalance() } coAnswers { fakeBalanceResult.await() }
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(emptyList()))

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `if both sections fail should end in the single Error state`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.failure(DataError.ServiceUnavailable())
        coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        assertEquals(HomeUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `if only the balance fails should keep the activity visible`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.failure(DataError.ServiceUnavailable())
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(listOf(createFakeActivityItem())))

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        assertEquals(
            HomeUiState.Content(
                BalanceSectionState.Error,
                RecentActivitySectionState.Loaded(listOf(createFakeRecentActivityItem())),
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `if only the activity fails should keep the balance visible`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.success(createFakeBalance())
        coEvery { getActivity(any(), any()) } returns Result.failure(DataError.Network(Exception("fake")))

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        assertEquals(
            HomeUiState.Content(
                BalanceSectionState.Loaded(available = "£5,000.00", pending = "£25.50"),
                RecentActivitySectionState.Error,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `if the retry follows the Error state should reload both sections and show them`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.failure(DataError.ServiceUnavailable())
        coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadHome()
        advanceUntilIdle()
        coEvery { getBalance() } returns Result.success(createFakeBalance())
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(emptyList()))

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        assertEquals(
            HomeUiState.Content(
                BalanceSectionState.Loaded(available = "£5,000.00", pending = "£25.50"),
                RecentActivitySectionState.Empty,
            ),
            viewModel.uiState.value,
        )
        coVerify(exactly = 2) { getBalance() }
        coVerify(exactly = 2) { getActivity(null, 3) }
    }

    @Test
    fun `if the retry follows a failed balance should also reload the activity that had loaded`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.failure(DataError.ServiceUnavailable())
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(emptyList()))
        viewModel.loadHome()
        advanceUntilIdle()

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 2) { getBalance() }
        coVerify(exactly = 2) { getActivity(null, 3) }
    }

    @Test
    fun `if the retry follows a failed activity should also reload the balance that had loaded`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.success(createFakeBalance())
        coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadHome()
        advanceUntilIdle()

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 2) { getBalance() }
        coVerify(exactly = 2) { getActivity(null, 3) }
    }

    @Test
    fun `if the retry starts should go back to Loading`() = runTest {
        // Given
        val fakeRetryBalance = CompletableDeferred<Result<MerchantBalance>>()
        coEvery { getBalance() } returns Result.failure(DataError.ServiceUnavailable())
        coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadHome()
        advanceUntilIdle()
        coEvery { getBalance() } coAnswers { fakeRetryBalance.await() }

        // When
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `if loadHome is called twice before the first load starts should fetch only once`() = runTest {
        // Given
        coEvery { getBalance() } returns Result.success(createFakeBalance())
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(emptyList()))

        // When
        viewModel.loadHome()
        viewModel.loadHome()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getBalance() }
        coVerify(exactly = 1) { getActivity(any(), any()) }
    }

    @Test
    fun `if a second loadHome starts during the first should ignore the answer of the first`() = runTest {
        // Given
        val fakeFirstBalance = CompletableDeferred<Result<MerchantBalance>>()
        val fakeSecondBalance = createFakeBalance(availableMinorUnits = 700_000)
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(emptyList()))
        coEvery { getBalance() } coAnswers { fakeFirstBalance.await() }
        viewModel.loadHome()
        advanceUntilIdle()
        coEvery { getBalance() } returns Result.success(fakeSecondBalance)

        // When
        viewModel.loadHome()
        advanceUntilIdle()
        fakeFirstBalance.complete(Result.success(createFakeBalance(availableMinorUnits = 100_000)))
        advanceUntilIdle()

        // Then
        assertEquals(
            HomeUiState.Content(
                BalanceSectionState.Loaded(available = "£7,000.00", pending = "£25.50"),
                RecentActivitySectionState.Empty,
            ),
            viewModel.uiState.value,
        )
    }

    private fun <T> TestScope.collectStates(stateFlow: StateFlow<T>): List<T> {
        val states = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            stateFlow.toList(states)
        }
        return states
    }

    private fun createFakeBalance(availableMinorUnits: Long = 500_000): MerchantBalance = MerchantBalance(
        available = Money(minorUnits = availableMinorUnits, currency = Currency.GBP),
        pending = Money(minorUnits = 2_550, currency = Currency.GBP),
    )

    private fun createFakePage(items: List<ActivityItem>): ActivityPage = ActivityPage(
        items = items,
        nextCursor = null,
        hasMore = false,
    )

    private fun createFakeActivityItem(): ActivityItem = ActivityItem(
        id = "fake-activity-id",
        type = ActivityType.Deposit,
        amount = Money(minorUnits = 40_000, currency = Currency.GBP),
        date = Instant.parse("2026-01-23T10:00:00Z"),
        description = "Fake payment",
        status = ActivityStatus.Completed,
    )

    private fun createFakeRecentActivityItem(): RecentActivityItem = RecentActivityItem(
        id = "fake-activity-id",
        description = "Fake payment",
        formattedAmount = "£400.00",
        isOutflow = false,
    )
}
