package fr.outadoc.justchatting.utils.presentation

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

internal actual fun areBubblesSupported(): Boolean {
    return Build.VERSION.SDK_INT >= 29
}

@Composable
internal actual fun canOpenActivityInBubble(): Boolean {
    val context = LocalContext.current
    return remember {
        areBubblesSupported() && (context as? Activity)?.isLaunchedFromBubbleCompat != true
    }
}
