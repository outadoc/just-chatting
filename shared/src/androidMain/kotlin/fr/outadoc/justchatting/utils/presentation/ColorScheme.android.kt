package fr.outadoc.justchatting.utils.presentation

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun getAppColorScheme(isDarkTheme: Boolean): ColorScheme {
    return if (Build.VERSION.SDK_INT >= 31) {
        val context = LocalContext.current
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
}

@get:Composable
internal actual val ColorScheme.isDark: Boolean
    get() = background.luminance() < 0.5
