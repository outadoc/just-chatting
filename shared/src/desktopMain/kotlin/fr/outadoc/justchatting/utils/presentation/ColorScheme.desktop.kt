package fr.outadoc.justchatting.utils.presentation

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
internal actual fun getAppColorScheme(isDarkTheme: Boolean): ColorScheme {
    return when {
        isDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
}
