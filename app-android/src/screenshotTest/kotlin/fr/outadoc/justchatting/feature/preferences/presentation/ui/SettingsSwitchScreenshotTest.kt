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
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun SettingsSwitchScreenshotTest() {
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
                },
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
