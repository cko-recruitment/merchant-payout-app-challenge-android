package manoellribeiro.dev.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.repository.MerchantRepository
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class GetActivityUseCaseTest {

    private lateinit var merchantRepository: MerchantRepository
    private lateinit var getActivity: GetActivityUseCase

    @BeforeEach
    fun setup() {
        merchantRepository = mockk()

        getActivity = GetActivityUseCase(merchantRepository = merchantRepository)
    }

    @Test
    fun `if a cursor is given should fetch that page with the limit`() = runTest {
        // Given
        val fakeCursor = "fake-cursor"
        val fakeLimit = 15
        val fakePage = ActivityPage(items = emptyList(), nextCursor = "fake-next-cursor", hasMore = true)
        coEvery { merchantRepository.getActivity(fakeCursor, fakeLimit) } returns Result.success(fakePage)

        // When
        val fakeResult = getActivity(fakeCursor, fakeLimit)

        // Then
        assertEquals(fakePage, fakeResult.getOrNull())
        coVerify(exactly = 1) { merchantRepository.getActivity(fakeCursor, fakeLimit) }
    }

    @Test
    fun `if the cursor is null should fetch the first page`() = runTest {
        // Given
        val fakeLimit = 3
        val fakePage = ActivityPage(items = emptyList(), nextCursor = null, hasMore = false)
        coEvery { merchantRepository.getActivity(null, fakeLimit) } returns Result.success(fakePage)

        // When
        val fakeResult = getActivity(null, fakeLimit)

        // Then
        assertEquals(fakePage, fakeResult.getOrNull())
        coVerify(exactly = 1) { merchantRepository.getActivity(null, fakeLimit) }
    }

    @Test
    fun `if the repository fails should return its failure`() = runTest {
        // Given
        val fakeError = DataError.ServiceUnavailable()
        coEvery { merchantRepository.getActivity(any(), any()) } returns Result.failure(fakeError)

        // When
        val fakeResult = getActivity("fake-failing-cursor", 15)

        // Then
        assertEquals(fakeError, fakeResult.exceptionOrNull())
    }
}
