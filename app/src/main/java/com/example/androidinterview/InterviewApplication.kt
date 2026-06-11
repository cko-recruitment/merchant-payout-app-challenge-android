package com.example.androidinterview

import android.app.Application
import com.example.androidinterview.mock.MockServerManager
import kotlin.concurrent.thread

class InterviewApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        thread(name = "mock-server-init") { MockServerManager.start() }
    }
}
