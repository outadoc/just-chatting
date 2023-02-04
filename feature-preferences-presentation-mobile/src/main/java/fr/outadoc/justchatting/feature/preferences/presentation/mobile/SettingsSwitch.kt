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
import androidx.compose.material3.Switch
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
fun SettingsSwitchPreview() {
    AppTheme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsSwitch(
                modifier = Modifier.fillMaxWidth(),
                checked = true,
                onCheckedChange = {},
                title = {
                    Text("Lorem ipsum")
                },
                subtitle = {
                    Text("Dolor sit amet")
                }
            )

            SettingsSwitch(
                modifier = Modifier.fillMaxWidth(),
                checked = false,
                onCheckedChange = {},
                title = {
                    Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.")
                },
            )
        }
    }
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit = {},
) {
    Box(
        modifier = Modifier.clickable { onCheckedChange(!checked) },
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f, fill = true),
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

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
