package com.example.androidinterview.core.biometric

enum class BiometricResult { Success, Cancelled, Unavailable }

/** Lives in :app because it's a UI interaction that needs an Activity. The result arrives through a callback. */
interface BiometricAuthenticator {
    fun authenticate(title: String, subtitle: String, cancelLabel: String)
}
