package manoellribeiro.dev.data.repository

import android.content.SharedPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.IOException
import java.security.GeneralSecurityException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import manoellribeiro.dev.data.remote.MerchantApi
import manoellribeiro.dev.data.remote.model.RemoteDevice
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.testing.MainDispatcherExtension
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class DeviceIdentityRepositoryImplTest {

    private lateinit var api: MerchantApi
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var preferencesProvider: () -> SharedPreferences
    private lateinit var repository: DeviceIdentityRepositoryImpl

    @BeforeEach
    fun setup() {
        api = mockk()
        preferences = mockk()
        editor = mockk()
        preferencesProvider = mockk()
        repository = DeviceIdentityRepositoryImpl(
            api = api,
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
    fun `if a device id is stored should not call the api`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns "fake-stored-device-id-not-fetched"

        // When
        repository.getDeviceId()

        // Then
        coVerify(exactly = 0) { api.getDevice() }
    }

    @Test
    fun `if a device id is stored should not write to the preferences`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns "fake-stored-device-id-not-written"

        // When
        repository.getDeviceId()

        // Then
        verify(exactly = 0) { preferences.edit() }
    }

    @Test
    fun `if no device id is stored should return the id fetched from the api`() = runTest {
        // Given
        val fakeDeviceId = "fake-api-device-id"
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } returns RemoteDevice(deviceId = fakeDeviceId)
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", fakeDeviceId) } returns editor
        every { editor.commit() } returns true

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        assertEquals(fakeDeviceId, fakeResult.getOrNull())
        coVerify(exactly = 1) { api.getDevice() }
    }

    @Test
    fun `if no device id is stored should save the fetched id with commit`() = runTest {
        // Given
        val fakeDeviceId = "fake-api-device-id-to-save"
        val fakeSavedIdSlot = slot<String>()
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } returns RemoteDevice(deviceId = fakeDeviceId)
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", capture(fakeSavedIdSlot)) } returns editor
        every { editor.commit() } returns true

        // When
        repository.getDeviceId()

        // Then
        assertEquals(fakeDeviceId, fakeSavedIdSlot.captured)
        verify(exactly = 1) { editor.putString("device_id", any()) }
        verify(exactly = 1) { editor.commit() }
        verify(exactly = 0) { editor.apply() }
    }

    @Test
    fun `if an id was fetched and saved should return the same id on the next call without calling the api again`() = runTest {
        // Given
        val fakeDeviceId = "fake-api-device-id-reused"
        val fakeSavedIdSlot = slot<String>()
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } answers {
            if (fakeSavedIdSlot.isCaptured) fakeSavedIdSlot.captured else null
        }
        coEvery { api.getDevice() } returns RemoteDevice(deviceId = fakeDeviceId)
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", capture(fakeSavedIdSlot)) } returns editor
        every { editor.commit() } returns true

        // When
        val fakeFirstResult = repository.getDeviceId()
        val fakeSecondResult = repository.getDeviceId()

        // Then
        assertEquals(fakeDeviceId, fakeFirstResult.getOrNull())
        assertEquals(fakeDeviceId, fakeSecondResult.getOrNull())
        coVerify(exactly = 1) { api.getDevice() }
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun `if getDeviceId is called twice should open the preferences only once`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns "fake-stored-device-id-opened-once"

        // When
        repository.getDeviceId()
        repository.getDeviceId()

        // Then
        verify(exactly = 1) { preferencesProvider() }
    }

    @Test
    fun `if the api throws an IOException should return a Network failure and save nothing`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } throws IOException("fake connection failure")

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.Network::class.java, fakeResult.exceptionOrNull())
        verify(exactly = 0) { preferences.edit() }
    }

    @Test
    fun `if the api returns a 5xx should return a ServiceUnavailable failure and save nothing`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } throws createFakeHttpException(503, """{"code":"SERVICE_UNAVAILABLE"}""")

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.ServiceUnavailable::class.java, fakeResult.exceptionOrNull())
        verify(exactly = 0) { preferences.edit() }
    }

    @Test
    fun `if the api returns a 4xx should return an Unexpected failure and save nothing`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } throws createFakeHttpException(404, "{}")

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
        verify(exactly = 0) { preferences.edit() }
    }

    @Test
    fun `if the api response can not be parsed should return an Unexpected failure and save nothing`() = runTest {
        // Given
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } throws SerializationException("fake missing device_id")

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
        verify(exactly = 0) { preferences.edit() }
    }

    @Test
    fun `if the api failed should call it again on the next call`() = runTest {
        // Given
        val fakeDeviceId = "fake-api-device-id-after-failure"
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } throws IOException("fake connection failure") andThen RemoteDevice(deviceId = fakeDeviceId)
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", fakeDeviceId) } returns editor
        every { editor.commit() } returns true

        // When
        val fakeFirstResult = repository.getDeviceId()
        val fakeSecondResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.Network::class.java, fakeFirstResult.exceptionOrNull())
        assertEquals(fakeDeviceId, fakeSecondResult.getOrNull())
        coVerify(exactly = 2) { api.getDevice() }
    }

    @Test
    fun `if opening the preferences throws a GeneralSecurityException should return an Unexpected failure holding it and not call the api`() = runTest {
        // Given
        val fakeException = GeneralSecurityException("fake keystore failure")
        every { preferencesProvider() } throws fakeException

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        val fakeError = fakeResult.exceptionOrNull()
        assertInstanceOf(DataError.Unexpected::class.java, fakeError)
        assertSame(fakeException, fakeError?.cause)
        coVerify(exactly = 0) { api.getDevice() }
    }

    @Test
    fun `if opening the preferences throws an IOException should return an Unexpected failure holding it and not call the api`() = runTest {
        // Given
        val fakeException = IOException("fake disk failure")
        every { preferencesProvider() } throws fakeException

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        val fakeError = fakeResult.exceptionOrNull()
        assertInstanceOf(DataError.Unexpected::class.java, fakeError)
        assertSame(fakeException, fakeError?.cause)
        coVerify(exactly = 0) { api.getDevice() }
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
    fun `if reading the stored id throws a SecurityException should return an Unexpected failure holding it and not call the api`() = runTest {
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
        coVerify(exactly = 0) { api.getDevice() }
        verify(exactly = 0) { preferences.edit() }
    }

    @Test
    fun `if saving the fetched id throws a SecurityException should return an Unexpected failure holding it`() = runTest {
        // Given
        val fakeDeviceId = "fake-api-device-id-not-encrypted"
        val fakeException = SecurityException("fake value that could not be encrypted")
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } returns RemoteDevice(deviceId = fakeDeviceId)
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", fakeDeviceId) } throws fakeException

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
        val fakeDeviceId = "fake-api-device-id-not-committed"
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } returns RemoteDevice(deviceId = fakeDeviceId)
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", fakeDeviceId) } returns editor
        every { editor.commit() } returns false

        // When
        val fakeResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeResult.exceptionOrNull())
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun `if commit failed and a later call commits should fetch the id again and return it`() = runTest {
        // Given
        val fakeDeviceId = "fake-api-device-id-committed-later"
        every { preferencesProvider() } returns preferences
        every { preferences.getString("device_id", null) } returns null
        coEvery { api.getDevice() } returns RemoteDevice(deviceId = fakeDeviceId)
        every { preferences.edit() } returns editor
        every { editor.putString("device_id", fakeDeviceId) } returns editor
        every { editor.commit() } returns false andThen true

        // When
        val fakeFirstResult = repository.getDeviceId()
        val fakeSecondResult = repository.getDeviceId()

        // Then
        assertInstanceOf(DataError.Unexpected::class.java, fakeFirstResult.exceptionOrNull())
        assertEquals(fakeDeviceId, fakeSecondResult.getOrNull())
        coVerify(exactly = 2) { api.getDevice() }
        verify(exactly = 2) { editor.commit() }
    }

    private fun createFakeHttpException(code: Int, body: String) = HttpException(
        Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())),
    )
}
