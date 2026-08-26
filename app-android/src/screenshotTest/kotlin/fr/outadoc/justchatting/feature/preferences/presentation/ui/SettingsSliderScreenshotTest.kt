package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun SettingsSliderScreenshotTest() {
    AppTheme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsSlider(
                modifier = Modifier.fillMaxWidth(),
                value = 30,
                onValueChange = {},
            ) {
                Text("Lorem ipsum")
            }

            SettingsSlider(
                modifier = Modifier.fillMaxWidth(),
                value = 300,
                valueRange = 10..1000,
                steps = 10,
                onValueChange = {},
            ) {
                Text(PreviewFixtures.sampleTextLong)
            }

            SettingsSlider(
                modifier = Modifier.fillMaxWidth(),
                value = 1,
                onValueChange = {},
            ) {
                Text("Lorem ipsum")
            }
        }
    }
}
