package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.composepreview.ThemePreviews

@ThemePreviews
@Composable
fun SettingsSwitchPreview() {
    Mdc3Theme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsSwitch(
                modifier = Modifier.fillMaxWidth(),
                checked = true,
                onCheckedChange = {}
            ) {
                Text("Lorem ipsum")
            }

            SettingsSwitch(
                modifier = Modifier.fillMaxWidth(),
                checked = false,
                onCheckedChange = {}
            ) {
                Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.")
            }
        }
    }
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.bodyLarge
    ) {
        Box(
            modifier = Modifier.clickable { onCheckedChange(!checked) }
        ) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f, fill = true)
                ) {
                    title()
                }

                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }
        }
    }
}
