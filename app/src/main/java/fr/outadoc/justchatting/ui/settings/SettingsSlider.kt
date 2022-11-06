package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter3.Mdc3Theme

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
                    steps = valueRange.endInclusive.toInt() / steps
                )

                valueContent(value)
            }
        }
    }
}