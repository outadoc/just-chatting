package fr.outadoc.justchatting.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    MaterialTheme(
        colorScheme = when {
            isDarkTheme -> dynamicDarkColorScheme(context)
            else -> dynamicLightColorScheme(context)
        },
        content = content
    )
}
