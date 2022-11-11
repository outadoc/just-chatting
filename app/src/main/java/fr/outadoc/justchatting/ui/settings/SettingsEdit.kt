package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.composepreview.ThemePreviews

@ThemePreviews
@Composable
fun SettingsEditPreview() {
    MaterialTheme {
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
