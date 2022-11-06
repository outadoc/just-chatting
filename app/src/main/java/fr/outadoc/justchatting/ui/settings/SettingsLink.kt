package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
    onClickLabel: String?,
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
