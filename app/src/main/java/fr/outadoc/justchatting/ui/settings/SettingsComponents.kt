package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter3.Mdc3Theme

@Preview
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

@Preview
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

@Preview
@Composable
fun SettingsSliderPreview() {
    Mdc3Theme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsSlider(
                modifier = Modifier.fillMaxWidth(),
                value = 0.3f,
                onValueChange = {}
            ) {
                Text("Lorem ipsum")
            }

            SettingsSlider(
                modifier = Modifier.fillMaxWidth(),
                value = 300f,
                valueRange = 10f..1000f,
                steps = 10,
                onValueChange = {}
            ) {
                Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.")
            }

            SettingsSlider(
                modifier = Modifier.fillMaxWidth(),
                value = 1f,
                onValueChange = {}
            ) {
                Text("Lorem ipsum")
            }
        }
    }
}

@Composable
fun SettingsSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    valueContent: @Composable (Float) -> Unit = {},
    title: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.bodyLarge
    ) {
        Column(modifier = modifier) {
            title()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    modifier = Modifier.weight(1f, fill = true),
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    steps = steps
                )

                valueContent(value)
            }
        }
    }
}

@Preview
@Composable
fun SettingsEditPreview() {
    Mdc3Theme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsEdit(
                modifier = Modifier.fillMaxWidth(),
                value = "l9klwmh97qgn0s0me276ezsft5szp2",
                onValueChange = {}
            ) {
                Text("Lorem ipsum")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsEdit(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.bodyLarge
    ) {
        Column(modifier = modifier.padding(vertical = 8.dp)) {
            title()

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = placeholder
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsLinksPreview() {
    Mdc3Theme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsLink(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                onClickLabel = ""
            ) {
                Text("Lorem ipsum")
            }
        }
    }
}

@Composable
fun SettingsLink(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onClickLabel: String,
    title: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.bodyLarge
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = onClickLabel) { onClick() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(modifier = modifier) {
                title()
            }
        }
    }
}
