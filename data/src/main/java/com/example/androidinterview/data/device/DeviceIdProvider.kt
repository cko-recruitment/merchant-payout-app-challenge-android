package com.example.androidinterview.data.device

import android.content.Context
import androidx.core.content.edit
import com.example.androidinterview.data.di.IoDispatcher
import com.example.androidinterview.data.network.MerchantApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val api: MerchantApi,
) {
    private val prefs by lazy {
        context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
    }

    suspend fun getDeviceId(): String? = withContext(ioDispatcher) {
        prefs.getString(KEY_DEVICE_ID, null)?.let { return@withContext it }
        runCatching { api.getDevice().deviceId }
            .getOrNull()
            ?.also { id -> prefs.edit { putString(KEY_DEVICE_ID, id) } }
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
    }
}
