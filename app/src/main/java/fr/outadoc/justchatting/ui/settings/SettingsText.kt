package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.composepreview.ThemePreviews
import fr.outadoc.justchatting.ui.theme.AppTheme

@ThemePreviews
@Composable
fun SettingsTextPreview() {
    AppTheme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsText(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                onClickLabel = "",
                title = {
                    Text("Lorem ipsum")
                }
            )
        }
    }
}

@ThemePreviews
@Composable
fun SettingsTextSubtitlePreview() {
    AppTheme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsText(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                onClickLabel = "",
                title = { Text("Lorem ipsum") },
                subtitle = { Text("Dolor sit amet") }
            )
        }
    }
}

@Composable
fun SettingsText(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onClickLabel: String? = null,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = onClickLabel) { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleMedium
            ) {
                title()
            }

            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodySmall,
                LocalContentColor provides LocalContentColor.current.copy(alpha = 0.8f)
            ) {
                subtitle()
            }
        }
    }
}
