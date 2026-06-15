package com.example.androidinterview.mock

import okhttp3.mockwebserver.MockWebServer

/**
 * Starts a local OkHttp MockWebServer that intercepts all API calls.
 * Mirrors the MSW (Mock Service Worker) setup in the React Native version of this challenge.
 *
 * Usage: call MockServerManager.baseUrl as your Retrofit/OkHttp base URL.
 */
object MockServerManager {

    private val server = MockWebServer()

    @Volatile
    private var _baseUrl: String = ""

    val baseUrl: String
        get() = _baseUrl

    fun start() {
        server.dispatcher = MockDispatcher()
        server.start()
        _baseUrl = server.url("/").toString()
    }

    fun shutdown() {
        runCatching { server.shutdown() }
    }
}
