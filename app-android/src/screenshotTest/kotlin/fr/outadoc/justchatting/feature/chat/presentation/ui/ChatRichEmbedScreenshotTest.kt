package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun ChatRichEmbedScreenshotTest() {
    AppTheme {
        ChatRichEmbed(
            modifier = Modifier.width(640.dp),
            title = "Salut ma cocotte",
            authorName = "Name of a clipper",
            thumbnailUrl = "",
            requestUrl = "",
        )
    }
}
