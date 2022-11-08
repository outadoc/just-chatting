package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.composepreview.ThemePreviews

@ThemePreviews
@Composable
fun SettingsHeaderPreview() {
    Mdc3Theme {
        Surface {
            SettingsHeader {
                Text("Lorem ipsum")
            }
        }
    }
}

@Composable
fun SettingsHeader(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelMedium,
        LocalContentColor provides MaterialTheme.colorScheme.primary
    ) {
        Box(modifier = modifier) {
            content()
        }
    }
}
