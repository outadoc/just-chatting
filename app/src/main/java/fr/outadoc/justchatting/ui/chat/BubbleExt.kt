package fr.outadoc.justchatting.ui.chat

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import fr.outadoc.justchatting.utils.core.isLaunchedFromBubbleCompat

@Composable
fun canOpenInBubble(): Boolean {
    val context = LocalContext.current
    return remember {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            (context as? Activity)?.isLaunchedFromBubbleCompat != true
    }
}
