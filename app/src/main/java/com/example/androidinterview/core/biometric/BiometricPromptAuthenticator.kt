package com.example.androidinterview.core.biometric

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Creates the prompt with the screen, not when it's needed: after a rotation, the new prompt takes over the one on
 * screen, so its result still arrives.
 */
@Composable
fun rememberBiometricAuthenticator(onResult: (BiometricResult) -> Unit): BiometricAuthenticator {
    val activity = LocalActivity.current as FragmentActivity
    val currentOnResult by rememberUpdatedState(onResult)
    return remember(activity) { BiometricPromptAuthenticator(activity) { result -> currentOnResult(result) } }
}

private class BiometricPromptAuthenticator(
    private val activity: FragmentActivity,
    private val onResult: (BiometricResult) -> Unit,
) : BiometricAuthenticator {

    // Runs on the main thread, so the result can update UI state directly.
    private val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricResult.Success)
            }

            // Not overriding onAuthenticationFailed: a wrong finger keeps the prompt open for another try.
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(errorCode.mapToBiometricResult())
            }
        },
    )

    override fun authenticate(title: String, subtitle: String, cancelLabel: String) {
        if (BiometricManager.from(activity).canAuthenticate(BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            onResult(BiometricResult.Unavailable)
            return
        }
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(cancelLabel)
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .build()
        )
    }
}

private fun Int.mapToBiometricResult(): BiometricResult = when (this) {
    BiometricPrompt.ERROR_USER_CANCELED,
    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
    BiometricPrompt.ERROR_CANCELED,
    BiometricPrompt.ERROR_TIMEOUT,
    -> BiometricResult.Cancelled
    else -> BiometricResult.Unavailable
}
