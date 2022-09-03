package fr.outadoc.justchatting.util

import android.app.Activity
import android.os.Build
import android.view.Display

val Activity.isLaunchedFromBubbleCompat: Boolean
    get() = if (Build.VERSION.SDK_INT >= 31) {
        isLaunchedFromBubble
    } else {
        val displayId = if (Build.VERSION.SDK_INT >= 30) {
            display?.displayId
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.displayId
        }
        displayId != Display.DEFAULT_DISPLAY
    }
