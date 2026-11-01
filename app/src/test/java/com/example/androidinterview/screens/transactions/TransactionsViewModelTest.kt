package com.example.androidinterview.screens.transactions

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
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
import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import manoellribeiro.dev.domain.usecase.GetActivityUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class TransactionsViewModelTest {

    private lateinit var getActivity: GetActivityUseCase
    private lateinit var viewModel: TransactionsViewModel

    @BeforeEach
    fun setup() {
        getActivity = mockk()

        viewModel = TransactionsViewModel(getActivity = getActivity)
    }

    @Test
    fun `if loadFirstPage was not called should start in Loading`() = runTest {
        // Given
        val fakeExpectedState = TransactionsUiState.Loading

        // When
        val fakeState = viewModel.uiState.value

        // Then
        assertEquals(fakeExpectedState, fakeState)
        coVerify(exactly = 0) { getActivity(any(), any()) }
    }

    @Test
    fun `if the first page has items should emit Loading then Content with Idle next page`() = runTest {
        // Given
        val fakePage = createFakePage(listOf("fake-first-id"), nextCursor = "fake-cursor", hasMore = true)
        coEvery { getActivity(any(), any()) } returns Result.success(fakePage)

        // When
        val fakeStates = collectStates(viewModel.uiState)
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // Then
        assertEquals(2, fakeStates.size)
        assertEquals(TransactionsUiState.Loading, fakeStates[0])
        val fakeContent = assertInstanceOf(TransactionsUiState.Content::class.java, fakeStates[1])
        assertEquals(NextPageState.Idle, fakeContent.nextPageState)
        assertEquals(listOf("fake-first-id"), rowKeys(fakeContent))
    }

    @Test
    fun `if the first page is requested should ask with no cursor and the page size`() = runTest {
        // Given
        val fakePage = createFakePage(listOf("fake-first-id"))
        coEvery { getActivity(any(), any()) } returns Result.success(fakePage)

        // When
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getActivity(null, 15) }
    }

    @Test
    fun `if the first page has no items should emit Loading then Empty`() = runTest {
        // Given
        val fakePage = createFakePage(emptyList())
        coEvery { getActivity(any(), any()) } returns Result.success(fakePage)

        // When
        val fakeStates = collectStates(viewModel.uiState)
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // Then
        assertEquals(listOf(TransactionsUiState.Loading, TransactionsUiState.Empty), fakeStates)
    }

    @Test
    fun `if the first page fails should emit Loading then Error`() = runTest {
        // Given
        coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())

        // When
        val fakeStates = collectStates(viewModel.uiState)
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // Then
        assertEquals(listOf(TransactionsUiState.Loading, TransactionsUiState.Error), fakeStates)
    }

    @Test
    fun `if the first page is retried after an Error should go through Loading and show Content`() = runTest {
        // Given
        val fakePage = createFakePage(listOf("fake-retried-id"))
        coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadFirstPage()
        advanceUntilIdle()
        coEvery { getActivity(any(), any()) } returns Result.success(fakePage)

        // When
        val fakeStates = collectStates(viewModel.uiState)
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // Then
        assertEquals(3, fakeStates.size)
        assertEquals(TransactionsUiState.Error, fakeStates[0])
        assertEquals(TransactionsUiState.Loading, fakeStates[1])
        assertEquals(listOf("fake-retried-id"), rowKeys(fakeStates[2]))
        coVerify(exactly = 2) { getActivity(null, 15) }
    }

    @Test
    fun `if Content is showing should ignore loadFirstPage and keep the list`() = runTest {
        // Given
        val fakePage = createFakePage(listOf("fake-first-id"), nextCursor = "fake-cursor", hasMore = true)
        coEvery { getActivity(any(), any()) } returns Result.success(fakePage)
        viewModel.loadFirstPage()
        advanceUntilIdle()
        val fakeContentBefore = viewModel.uiState.value

        // When
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // Then
        assertEquals(fakeContentBefore, viewModel.uiState.value)
        coVerify(exactly = 1) { getActivity(any(), any()) }
    }

    @Test
    fun `if a first page load is active should ignore another loadFirstPage`() = runTest {
        // Given
        val fakeResult = CompletableDeferred<Result<ActivityPage>>()
        coEvery { getActivity(any(), any()) } coAnswers { fakeResult.await() }
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // When
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getActivity(any(), any()) }
    }

    @Test
    fun `if loadFirstPage is called twice before the load starts should fetch only once`() = runTest {
        // Given
        coEvery { getActivity(any(), any()) } returns Result.success(createFakePage(listOf("fake-first-id")))

        // When
        viewModel.loadFirstPage()
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getActivity(any(), any()) }
    }

    @Test
    fun `if nothing was loaded should ignore loadNextPage`() = runTest {
        // Given
        val fakeExpectedState = TransactionsUiState.Loading

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        assertEquals(fakeExpectedState, viewModel.uiState.value)
        coVerify(exactly = 0) { getActivity(any(), any()) }
    }

    @Test
    fun `if the first page failed should ignore loadNextPage`() = runTest {
        // Given
        coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        assertEquals(TransactionsUiState.Error, viewModel.uiState.value)
        coVerify(exactly = 1) { getActivity(any(), any()) }
    }

    @Test
    fun `if the next page loads should pass the cursor on and append its items`() = runTest {
        // Given
        val fakeCursor = "fake-cursor"
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        val fakeSecondPage = createFakePage(listOf("fake-second-id"))
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } returns Result.success(fakeSecondPage)
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        val fakeContent = assertInstanceOf(TransactionsUiState.Content::class.java, viewModel.uiState.value)
        assertEquals(listOf("fake-first-id", "fake-second-id"), rowKeys(fakeContent))
        assertEquals(NextPageState.Idle, fakeContent.nextPageState)
        coVerify(exactly = 1) { getActivity(fakeCursor, 15) }
    }

    @Test
    fun `if the next page is loading should show Loading next page keeping the items`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-loading"
        val fakeSecondPageResult = CompletableDeferred<Result<ActivityPage>>()
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } coAnswers { fakeSecondPageResult.await() }
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        val fakeContent = assertInstanceOf(TransactionsUiState.Content::class.java, viewModel.uiState.value)
        assertEquals(NextPageState.Loading, fakeContent.nextPageState)
        assertEquals(listOf("fake-first-id"), rowKeys(fakeContent))
    }

    @Test
    fun `if each page has a next cursor should always pass on the latest one`() = runTest {
        // Given
        val fakeSecondCursor = "fake-cursor-2"
        val fakeThirdCursor = "fake-cursor-3"
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeSecondCursor, hasMore = true)
        val fakeSecondPage = createFakePage(listOf("fake-second-id"), nextCursor = fakeThirdCursor, hasMore = true)
        val fakeThirdPage = createFakePage(listOf("fake-third-id"))
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeSecondCursor, any()) } returns Result.success(fakeSecondPage)
        coEvery { getActivity(fakeThirdCursor, any()) } returns Result.success(fakeThirdPage)
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        assertEquals(listOf("fake-first-id", "fake-second-id", "fake-third-id"), rowKeys(viewModel.uiState.value))
        coVerify(exactly = 1) { getActivity(fakeSecondCursor, 15) }
        coVerify(exactly = 1) { getActivity(fakeThirdCursor, 15) }
    }

    @Test
    fun `if loadNextPage is called twice in a row should fetch the next page only once`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-twice"
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } returns Result.success(createFakePage(listOf("fake-second-id")))
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        assertEquals(listOf("fake-first-id", "fake-second-id"), rowKeys(viewModel.uiState.value))
        coVerify(exactly = 1) { getActivity(fakeCursor, any()) }
    }

    @Test
    fun `if the next page is in flight should ignore loadNextPage`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-in-flight"
        val fakeSecondPageResult = CompletableDeferred<Result<ActivityPage>>()
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } coAnswers { fakeSecondPageResult.await() }
        viewModel.loadFirstPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getActivity(fakeCursor, any()) }
    }

    @Test
    fun `if hasMore is false should not fetch another page even with a cursor`() = runTest {
        // Given
        val fakePage = createFakePage(listOf("fake-first-id"), nextCursor = "fake-stale-cursor", hasMore = false)
        coEvery { getActivity(null, any()) } returns Result.success(fakePage)
        viewModel.loadFirstPage()
        advanceUntilIdle()
        val fakeContentBefore = viewModel.uiState.value

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        assertEquals(fakeContentBefore, viewModel.uiState.value)
        coVerify(exactly = 1) { getActivity(any(), any()) }
    }

    @Test
    fun `if the page has no next cursor should not fetch another page`() = runTest {
        // Given
        val fakePage = createFakePage(listOf("fake-first-id"), nextCursor = null, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakePage)
        viewModel.loadFirstPage()
        advanceUntilIdle()
        val fakeContentBefore = viewModel.uiState.value

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        assertEquals(fakeContentBefore, viewModel.uiState.value)
        coVerify(exactly = 1) { getActivity(any(), any()) }
    }

    @Test
    fun `if the last page was reached should stop fetching`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-last"
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        val fakeLastPage = createFakePage(listOf("fake-last-id"), nextCursor = null, hasMore = false)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } returns Result.success(fakeLastPage)
        viewModel.loadFirstPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        assertEquals(listOf("fake-first-id", "fake-last-id"), rowKeys(viewModel.uiState.value))
        coVerify(exactly = 2) { getActivity(any(), any()) }
    }

    @Test
    fun `if the next page fails should show Error next page keeping the loaded items`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-fails"
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        val fakeContent = assertInstanceOf(TransactionsUiState.Content::class.java, viewModel.uiState.value)
        assertEquals(NextPageState.Error, fakeContent.nextPageState)
        assertEquals(listOf("fake-first-id"), rowKeys(fakeContent))
    }

    @Test
    fun `if the next page failed should not retry from loadNextPage`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-no-retry"
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadFirstPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        val fakeContent = assertInstanceOf(TransactionsUiState.Content::class.java, viewModel.uiState.value)
        assertEquals(NextPageState.Error, fakeContent.nextPageState)
        coVerify(exactly = 1) { getActivity(fakeCursor, any()) }
    }

    @Test
    fun `if the next page failed should keep the list when loadFirstPage is called`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-rotation"
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadFirstPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        val fakeContentBefore = viewModel.uiState.value

        // When
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // Then
        assertEquals(fakeContentBefore, viewModel.uiState.value)
        coVerify(exactly = 1) { getActivity(null, any()) }
    }

    @Test
    fun `if retryNextPage follows a failed page should fetch the same cursor and append the items`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-retry"
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadFirstPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        coEvery { getActivity(fakeCursor, any()) } returns Result.success(createFakePage(listOf("fake-second-id")))

        // When
        viewModel.retryNextPage()
        advanceUntilIdle()

        // Then
        val fakeContent = assertInstanceOf(TransactionsUiState.Content::class.java, viewModel.uiState.value)
        assertEquals(NextPageState.Idle, fakeContent.nextPageState)
        assertEquals(listOf("fake-first-id", "fake-second-id"), rowKeys(fakeContent))
        coVerify(exactly = 2) { getActivity(fakeCursor, 15) }
    }

    @Test
    fun `if retryNextPage fails again should show Error next page keeping the items`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-fails-again"
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } returns Result.failure(DataError.ServiceUnavailable())
        viewModel.loadFirstPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // When
        viewModel.retryNextPage()
        advanceUntilIdle()

        // Then
        val fakeContent = assertInstanceOf(TransactionsUiState.Content::class.java, viewModel.uiState.value)
        assertEquals(NextPageState.Error, fakeContent.nextPageState)
        assertEquals(listOf("fake-first-id"), rowKeys(fakeContent))
        coVerify(exactly = 2) { getActivity(fakeCursor, any()) }
    }

    @Test
    fun `if the next page state is Idle should ignore retryNextPage`() = runTest {
        // Given
        val fakePage = createFakePage(listOf("fake-first-id"), nextCursor = "fake-cursor-idle", hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakePage)
        viewModel.loadFirstPage()
        advanceUntilIdle()

        // When
        viewModel.retryNextPage()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getActivity(any(), any()) }
    }

    @Test
    fun `if the next page is in flight should ignore retryNextPage`() = runTest {
        // Given
        val fakeCursor = "fake-cursor-retry-in-flight"
        val fakeSecondPageResult = CompletableDeferred<Result<ActivityPage>>()
        val fakeFirstPage = createFakePage(listOf("fake-first-id"), nextCursor = fakeCursor, hasMore = true)
        coEvery { getActivity(null, any()) } returns Result.success(fakeFirstPage)
        coEvery { getActivity(fakeCursor, any()) } coAnswers { fakeSecondPageResult.await() }
        viewModel.loadFirstPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // When
        viewModel.retryNextPage()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getActivity(fakeCursor, any()) }
    }

    @Test
    fun `if nothing was loaded should ignore retryNextPage`() = runTest {
        // Given
        val fakeExpectedState = TransactionsUiState.Loading

        // When
        viewModel.retryNextPage()
        advanceUntilIdle()

        // Then
        assertEquals(fakeExpectedState, viewModel.uiState.value)
        coVerify(exactly = 0) { getActivity(any(), any()) }
    }

    private fun <T> TestScope.collectStates(stateFlow: StateFlow<T>): List<T> {
        val states = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            stateFlow.toList(states)
        }
        return states
    }

    private fun rowKeys(state: TransactionsUiState): List<String> =
        assertInstanceOf(TransactionsUiState.Content::class.java, state).items
            .filterIsInstance<TransactionListItem.Row>()
            .map { it.key }

    private fun createFakePage(
        itemIds: List<String>,
        nextCursor: String? = null,
        hasMore: Boolean = false,
    ): ActivityPage = ActivityPage(
        items = itemIds.map(::createFakeActivityItem),
        nextCursor = nextCursor,
        hasMore = hasMore,
    )

    private fun createFakeActivityItem(id: String): ActivityItem = ActivityItem(
        id = id,
        type = ActivityType.Deposit,
        amount = Money(minorUnits = 40_000, currency = Currency.GBP),
        date = Instant.parse("2026-01-23T10:00:00Z"),
        description = "Fake payment",
        status = ActivityStatus.Completed,
    )
}
