package fr.outadoc.justchatting.utils.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme =
        if (Build.VERSION.SDK_INT >= 31) {
            when {
                isDarkTheme -> dynamicDarkColorScheme(context)
                else -> dynamicLightColorScheme(context)
            }
        } else {
            when {
                isDarkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
