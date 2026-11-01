package manoellribeiro.dev.data.service

import android.net.ConnectivityManager
import android.net.Network
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class ConnectivityServiceImplTest {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var network: Network
    private lateinit var service: ConnectivityServiceImpl

    @BeforeEach
    fun setup() {
        connectivityManager = mockk()
        network = mockk()
        service = ConnectivityServiceImpl(connectivityManager)
    }

    @Test
    fun `if there is an active network should emit true first`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.registerDefaultNetworkCallback(any()) } just runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs

        // When
        val fakeIsOnline = service.observeIsOnline().first()

        // Then
        assertTrue(fakeIsOnline)
    }

    @Test
    fun `if there is no active network should emit false first`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.registerDefaultNetworkCallback(any()) } just runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs

        // When
        val fakeIsOnline = service.observeIsOnline().first()

        // Then
        assertFalse(fakeIsOnline)
    }

    @Test
    fun `if the flow is collected should register one default network callback`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.registerDefaultNetworkCallback(any()) } just runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs

        // When
        collectIsOnline()
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { connectivityManager.registerDefaultNetworkCallback(any()) }
    }

    @Test
    fun `if the flow is not collected should not register a callback`() = runTest {
        // Given
        // Strict mock with no behavior: touching the ConnectivityManager would throw.

        // When
        service.observeIsOnline()

        // Then
        verify(exactly = 0) { connectivityManager.registerDefaultNetworkCallback(any()) }
    }

    @Test
    fun `if a network becomes available while offline should emit true`() = runTest {
        // Given
        val fakeCallbackSlot = slot<ConnectivityManager.NetworkCallback>()
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.registerDefaultNetworkCallback(capture(fakeCallbackSlot)) } just runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs

        // When
        val fakeStates = collectIsOnline()
        advanceUntilIdle()
        fakeCallbackSlot.captured.onAvailable(network)
        advanceUntilIdle()

        // Then
        assertEquals(listOf(false, true), fakeStates)
    }

    @Test
    fun `if the network is lost while online should emit false`() = runTest {
        // Given
        val fakeCallbackSlot = slot<ConnectivityManager.NetworkCallback>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.registerDefaultNetworkCallback(capture(fakeCallbackSlot)) } just runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs

        // When
        val fakeStates = collectIsOnline()
        advanceUntilIdle()
        fakeCallbackSlot.captured.onLost(network)
        advanceUntilIdle()

        // Then
        assertEquals(listOf(true, false), fakeStates)
    }

    @Test
    fun `if the callback repeats the current value should not emit it again`() = runTest {
        // Given
        val fakeCallbackSlot = slot<ConnectivityManager.NetworkCallback>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.registerDefaultNetworkCallback(capture(fakeCallbackSlot)) } just runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs

        // When
        val fakeStates = collectIsOnline()
        advanceUntilIdle()
        fakeCallbackSlot.captured.onAvailable(network)
        fakeCallbackSlot.captured.onAvailable(network)
        advanceUntilIdle()

        // Then
        assertEquals(listOf(true), fakeStates)
    }

    @Test
    fun `if the connectivity keeps changing should emit every transition in order`() = runTest {
        // Given
        val fakeCallbackSlot = slot<ConnectivityManager.NetworkCallback>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.registerDefaultNetworkCallback(capture(fakeCallbackSlot)) } just runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs

        // When
        val fakeStates = collectIsOnline()
        advanceUntilIdle()
        fakeCallbackSlot.captured.onLost(network)
        fakeCallbackSlot.captured.onAvailable(network)
        fakeCallbackSlot.captured.onLost(network)
        advanceUntilIdle()

        // Then
        assertEquals(listOf(true, false, true, false), fakeStates)
    }

    @Test
    fun `if the collection is cancelled should unregister the registered callback`() = runTest {
        // Given
        val fakeCallbackSlot = slot<ConnectivityManager.NetworkCallback>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.registerDefaultNetworkCallback(capture(fakeCallbackSlot)) } just runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs
        val fakeJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            service.observeIsOnline().toList()
        }
        advanceUntilIdle()

        // When
        fakeJob.cancel()
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { connectivityManager.unregisterNetworkCallback(fakeCallbackSlot.captured) }
    }

    @Test
    fun `if the flow is collected should register the callback before reading the active network`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.registerDefaultNetworkCallback(any()) } just runs
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs

        // When
        collectIsOnline()
        advanceUntilIdle()

        // Then
        verifyOrder {
            connectivityManager.registerDefaultNetworkCallback(any())
            connectivityManager.activeNetwork
        }
        verify(exactly = 1) { connectivityManager.registerDefaultNetworkCallback(any()) }
        verify(exactly = 1) { connectivityManager.activeNetwork }
    }

    @Test
    fun `if the network is lost right after the callback is registered should end with false`() = runTest {
        // Given
        // Acts like the platform when the network drops right after the first call: only a callback that is already
        // registered hears about it, and a callback registered later stays silent because there is no network.
        val fakeCallbackSlot = slot<ConnectivityManager.NetworkCallback>()
        var fakeIsNetworkLost = false
        every { connectivityManager.registerDefaultNetworkCallback(capture(fakeCallbackSlot)) } answers {
            if (!fakeIsNetworkLost) {
                fakeIsNetworkLost = true
                fakeCallbackSlot.captured.onLost(network)
            }
        }
        every { connectivityManager.activeNetwork } answers {
            val fakeActiveNetwork = if (fakeIsNetworkLost) null else network
            fakeIsNetworkLost = true
            fakeActiveNetwork
        }
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs

        // When
        val fakeStates = collectIsOnline()
        advanceUntilIdle()

        // Then
        assertEquals(listOf(false), fakeStates)
    }

    private fun TestScope.collectIsOnline(): List<Boolean> {
        val states = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            service.observeIsOnline().toList(states)
        }
        return states
    }
}
