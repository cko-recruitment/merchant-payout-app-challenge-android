package manoellribeiro.dev.data.repository

import android.content.SharedPreferences
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.repository.DeviceIdentityRepository

/**
 * Takes a provider because opening EncryptedSharedPreferences touches the Keystore and disk, so it
 * has to happen on the IO dispatcher instead of when the app graph is built.
 */
internal class DeviceIdentityRepositoryImpl(
    preferencesProvider: () -> SharedPreferences,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DeviceIdentityRepository {

    private val preferences by lazy(preferencesProvider)

    override suspend fun getDeviceId(): Result<String> = withContext(ioDispatcher) {
        try {
            Result.success(readOrCreateDeviceId())
        } catch (e: GeneralSecurityException) {
            Result.failure(DataError.Unexpected(e))
        } catch (e: IOException) {
            Result.failure(DataError.Unexpected(e))
        } catch (e: SecurityException) {
            // EncryptedSharedPreferences reports a value it can't decrypt or encrypt this way.
            Result.failure(DataError.Unexpected(e))
        }
    }

    private fun readOrCreateDeviceId(): String =
        preferences.getString(KEY_DEVICE_ID, null)
            ?: UUID.randomUUID().toString().also { newId ->
                // An ID that wasn't saved would change on the next call, so it can't count as a success.
                val isSaved = preferences.edit().putString(KEY_DEVICE_ID, newId).commit()
                if (!isSaved) throw IOException("Couldn't save the device ID")
            }

    private companion object {
        const val KEY_DEVICE_ID = "device_id"
    }
}
