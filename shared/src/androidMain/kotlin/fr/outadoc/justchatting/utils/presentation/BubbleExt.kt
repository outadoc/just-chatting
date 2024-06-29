package fr.outadoc.justchatting.utils.presentation

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun canOpenInBubble(): Boolean {
    val context = LocalContext.current
    return remember {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            (context as? Activity)?.isLaunchedFromBubbleCompat != true
    }
}
