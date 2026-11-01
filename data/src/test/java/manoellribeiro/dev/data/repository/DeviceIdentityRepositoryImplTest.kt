package manoellribeiro.dev.data.repository

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class DeviceIdentityRepositoryImplTest {

    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var preferencesProvider: () -> SharedPreferences
    private lateinit var repository: DeviceIdentityRepositoryImpl

    @BeforeEach
    fun setup() {
        preferences = mockk()
        editor = mockk()
        preferencesProvider = mockk()
        repository = DeviceIdentityRepositoryImpl(
            preferencesProvider = preferencesProvider,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun `if getDeviceId is never called should not open the preferences`() = runTest {
        // Given
        // The repository was created in setup, with a provider that has no behavior configured.

        // When
        // Nothing is called: opening the preferences touches the Keystore and disk, so it has to wait for the first call.

        // Then
        verify(exactly = 0) { preferencesProvider() }
    }

    @Test
    fun `if a device id is stored should return it`() = runTest {
        // Given
        val fakeDeviceId = "fake-stored-device-id"
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns fakeDeviceId

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        assertEquals(fakeDeviceId, fakeResult.getOrNull())
    }

    @Test
    fun `if a device id is stored should not write to the preferences`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns "fake-stored-device-id"

        // When
        repository.getDeviceId()

        // Then
        verify(exactly = 0) { preferences.edit() }
    }

    @Test
    fun `if no device id is stored should return a new UUID`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        every { preferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.commit() } returns true

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        val fakeDeviceId = fakeResult.getOrThrow()
        assertEquals(fakeDeviceId, UUID.fromString(fakeDeviceId).toString())
    }

    @Test
    fun `if no device id is stored should save the returned id with commit`() = runTest {
        // Given
        val fakeSavedIdSlot = slot<String>()
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", capture(fakeSavedIdSlot)) } returns editor
        every { editor.commit() } returns true

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        assertEquals(fakeSavedIdSlot.captured, fakeResult.getOrNull())
        verify(exactly = 1) { editor.putString("device_id", any()) }
        verify(exactly = 1) { editor.commit() }
        verify(exactly = 0) { editor.apply() }
    }

    @Test
    fun `if an id was created should return the same id on the next call`() = runTest {
        // Given
        val fakeSavedIdSlot = slot<String>()
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } answers {
            if (fakeSavedIdSlot.isCaptured) fakeSavedIdSlot.captured else null
        }
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", capture(fakeSavedIdSlot)) } returns editor
        every { editor.commit() } returns true

        // When
        val fakeFirstResult = repository.getDeviceId()
        val fakeSecondResult = repository.getDeviceId()

        // Then
        assertEquals(fakeFirstResult.getOrThrow(), fakeSecondResult.getOrThrow())
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun `if getDeviceId is called twice should open the preferences only once`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns "fake-stored-device-id"

        // When
        repository.getDeviceId()
        repository.getDeviceId()

        // Then
        verify(exactly = 1) { preferencesProvider() }
    }

    @Test
    fun `if opening the preferences throws a GeneralSecurityException should return an Unexpected failure holding it`() = runTest {
        // Given
        val fakeException = GeneralSecurityException("fake keystore failure")
        every { preferencesProvider() } throws fakeException

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        val fakeError = fakeResult.exceptionOrNull()
        assertInstanceOf(DataError.Unexpected::class.java, fakeError)
        assertSame(fakeException, fakeError?.cause)
    }

    @Test
    fun `if opening the preferences throws an IOException should return an Unexpected failure holding it`() = runTest {
        // Given
        val fakeException = IOException("fake disk failure")
        every { preferencesProvider() } throws fakeException

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        val fakeError = fakeResult.exceptionOrNull()
        assertInstanceOf(DataError.Unexpected::class.java, fakeError)
        assertSame(fakeException, fakeError?.cause)
    }

    @Test
    fun `if opening the preferences failed should try again on the next call`() = runTest {
        // Given
        val fakeDeviceId = "fake-recovered-device-id"
        every { preferencesProvider() } throws IOException("fake disk failure") andThen preferences
        every { preferences.getString("device_id", null) } returns fakeDeviceId

        // When
        val fakeFirstResult = repository.getDeviceId()
        val fakeSecondResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeFirstResult.exceptionOrNull())
        assertEquals(fakeDeviceId, fakeSecondResult.getOrNull())
        verify(exactly = 2) { preferencesProvider() }
    }

    @Test
    fun `if reading the stored id throws a SecurityException should return an Unexpected failure holding it`() = runTest {
        // Given
        val fakeException = SecurityException("fake value that could not be decrypted")
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } throws fakeException

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        val fakeError = fakeResult.exceptionOrNull()
        assertInstanceOf(DataError.Unexpected::class.java, fakeError)
        assertSame(fakeException, fakeError?.cause)
        verify(exactly = 0) { preferences.edit() }
    }

    @Test
    fun `if saving a new id throws a SecurityException should return an Unexpected failure holding it`() = runTest {
        // Given
        val fakeException = SecurityException("fake value that could not be encrypted")
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", any()) } throws fakeException

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        val fakeError = fakeResult.exceptionOrNull()
        assertInstanceOf(DataError.Unexpected::class.java, fakeError)
        assertSame(fakeException, fakeError?.cause)
        verify(exactly = 0) { editor.commit() }
    }

    @Test
    fun `if commit returns false should return an Unexpected failure`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", any()) } returns editor
        every { editor.commit() } returns false

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun `if commit failed and a later call commits should return a success with the saved id`() = runTest {
        // Given
        val fakeSavedIdSlot = slot<String>()
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", capture(fakeSavedIdSlot)) } returns editor
        every { editor.commit() } returns false andThen true

        // When
        val fakeFirstResult = repository.getDeviceId()
        val fakeSecondResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeFirstResult.exceptionOrNull())
        assertEquals(fakeSavedIdSlot.captured, fakeSecondResult.getOrNull())
        verify(exactly = 2) { editor.commit() }
    }
}
