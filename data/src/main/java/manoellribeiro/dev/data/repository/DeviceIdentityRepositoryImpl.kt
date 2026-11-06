package manoellribeiro.dev.data.repository

import android.content.SharedPreferences
import java.io.IOException
import java.security.GeneralSecurityException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import manoellribeiro.dev.data.remote.MerchantApi
import manoellribeiro.dev.data.remote.apiCall
import manoellribeiro.dev.domain.error.DataError
import manoellribeiro.dev.domain.repository.DeviceIdentityRepository

/**
 * Fetches the device ID from the API on the first payout and saves it, so it stays the same across launches.
 * Takes a provider because opening EncryptedSharedPreferences touches the Keystore and disk, so it
 * has to happen on the IO dispatcher instead of when the app graph is built.
 */
internal class DeviceIdentityRepositoryImpl(
    private val api: MerchantApi,
    preferencesProvider: () -> SharedPreferences,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DeviceIdentityRepository {

    private val preferences by lazy(preferencesProvider)

    override suspend fun getDeviceId(): Result<String> {
        val savedId = storageCall { preferences.getString(KEY_DEVICE_ID, null) }
            .getOrElse { return Result.failure(it) }
        if (savedId != null) return Result.success(savedId)
        val newId = apiCall { api.getDevice().deviceId }.getOrElse { return Result.failure(it) }
        return storageCall {
            // An ID that wasn't saved would change on the next call, so it can't count as a success.
            val isSaved = preferences.edit().putString(KEY_DEVICE_ID, newId).commit()
            if (!isSaved) throw IOException("Couldn't save the device ID")
            newId
        }
    }

    /** Runs Keystore and disk work on the IO dispatcher, and turns its failures into a [DataError]. */
    private suspend fun <T> storageCall(block: () -> T): Result<T> = withContext(ioDispatcher) {
        try {
            Result.success(block())
        } catch (e: GeneralSecurityException) {
            Result.failure(DataError.Unexpected(e))
        } catch (e: IOException) {
            Result.failure(DataError.Unexpected(e))
        } catch (e: SecurityException) {
            // EncryptedSharedPreferences reports a value it can't decrypt or encrypt this way.
            Result.failure(DataError.Unexpected(e))
        }
    }

    private companion object {
        const val KEY_DEVICE_ID = "device_id"
    }
}
