package com.example.androidinterview.core

import android.app.Application
import com.example.androidinterview.core.mock.MockServerManager
import kotlin.concurrent.thread

class InterviewApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // The server can't start on the main thread, and the base URL only exists once it has. Waiting for it here
        // is only for the mock: a real app would have a fixed URL.
        lateinit var baseUrl: String
        thread(name = "mock-server-init") {
            MockServerManager.start()
            baseUrl = MockServerManager.baseUrl
        }.join()
        appContainer = AppContainer(baseUrl, this)
    }
}
