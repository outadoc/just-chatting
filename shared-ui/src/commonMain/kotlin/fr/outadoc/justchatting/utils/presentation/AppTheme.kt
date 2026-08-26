package fr.outadoc.justchatting.utils.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

@Composable
public fun AppTheme(
    isDarkTheme: Boolean = isAppInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = getAppColorScheme(isDarkTheme),
    ) {
        Surface {
            content()
        }
    }
}
