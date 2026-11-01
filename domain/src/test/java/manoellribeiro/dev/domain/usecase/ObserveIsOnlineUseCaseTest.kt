package manoellribeiro.dev.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import manoellribeiro.dev.domain.service.ConnectivityService
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class ObserveIsOnlineUseCaseTest {

    private lateinit var connectivityService: ConnectivityService
    private lateinit var observeIsOnline: ObserveIsOnlineUseCase

    @BeforeEach
    fun setup() {
        connectivityService = mockk()

        observeIsOnline = ObserveIsOnlineUseCase(connectivityService = connectivityService)
    }

    @Test
    fun `if the connectivity changes should emit every value in order`() = runTest {
        // Given
        val fakeValues = listOf(true, false, true)
        every { connectivityService.observeIsOnline() } returns flowOf(true, false, true)

        // When
        val fakeResult = observeIsOnline().toList()

        // Then
        assertEquals(fakeValues, fakeResult)
        verify(exactly = 1) { connectivityService.observeIsOnline() }
    }
}
