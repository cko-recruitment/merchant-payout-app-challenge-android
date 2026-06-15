package com.example.androidinterview.data.network

import com.example.androidinterview.mock.MockServerManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

private val json = Json { ignoreUnknownKeys = true }

internal object RetrofitClient {
    val merchantApi: MerchantApi by lazy {
        Retrofit.Builder()
            .baseUrl(MockServerManager.baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MerchantApi::class.java)
    }
}
