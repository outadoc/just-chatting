package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
internal fun SettingsTextScreenshotTest() {
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

@PreviewTest
@Preview
@Composable
internal fun SettingsTextSubtitleScreenshotTest() {
    AppTheme {
        Column(modifier = Modifier.width(256.dp)) {
            SettingsText(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                onClickLabel = "",
                title = { Text("Lorem ipsum") },
                subtitle = { Text("Dolor sit amet") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
