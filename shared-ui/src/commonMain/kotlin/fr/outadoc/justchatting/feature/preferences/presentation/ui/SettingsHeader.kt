package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.outadoc.justchatting.utils.presentation.AppTheme

@Composable
internal fun SettingsHeader(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelMedium,
        LocalContentColor provides MaterialTheme.colorScheme.primary,
    ) {
        Box(modifier = modifier) {
            content()
        }
    }
}

@Preview
@Composable
internal fun SettingsHeaderPreview() {
    AppTheme {
        Surface {
            SettingsHeader {
                Text("Lorem ipsum")
            }
        }
    }
}
