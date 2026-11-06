package com.example.androidinterview.core

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.androidinterview.core.navigation.MerchantApp

// BiometricPrompt needs a FragmentActivity.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as InterviewApplication).appContainer
        setContent { MerchantApp(appContainer) }
    }
}
