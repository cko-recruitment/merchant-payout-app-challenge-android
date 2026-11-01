package com.example.androidinterview.core.security

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect

/** Blocks screenshots and recordings while the calling screen is resumed, so the rest of the app stays capturable. */
@Composable
fun SecureScreenEffect() {
    val window = LocalActivity.current?.window ?: return
    LifecycleResumeEffect(window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onPauseOrDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

/** Android can only report screenshots from API 34. Older versions are still protected by [SecureScreenEffect]. */
@Composable
fun ScreenshotDetectedEffect(onScreenshotDetected: () -> Unit) {
    val activity = LocalActivity.current ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        ObserveScreenshots(activity, onScreenshotDetected)
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
private fun ObserveScreenshots(activity: Activity, onScreenshotDetected: () -> Unit) {
    val currentOnScreenshotDetected by rememberUpdatedState(onScreenshotDetected)
    LifecycleStartEffect(activity) {
        val callback = Activity.ScreenCaptureCallback { currentOnScreenshotDetected() }
        activity.registerScreenCaptureCallback(ContextCompat.getMainExecutor(activity), callback)
        onStopOrDispose { activity.unregisterScreenCaptureCallback(callback) }
    }
}
