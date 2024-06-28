package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews

@ThemePreviews
@Composable
internal fun SettingsTextPreview() {
    AppTheme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsText(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                onClickLabel = "",
                title = {
                    Text("Lorem ipsum")
                },
            )
        }
    }
}

@ThemePreviews
@Composable
internal fun SettingsTextSubtitlePreview() {
    AppTheme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsText(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                onClickLabel = "",
                title = { Text("Lorem ipsum") },
                subtitle = { Text("Dolor sit amet") },
            )
        }
    }
}

@Composable
internal fun SettingsText(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onClickLabel: String? = null,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = onClickLabel) { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleMedium,
                ) {
                    title()
                }

                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodySmall,
                    LocalContentColor provides LocalContentColor.current.copy(alpha = 0.8f),
                ) {
                    subtitle()
                }
            }

            trailingIcon()
        }
    }
}
