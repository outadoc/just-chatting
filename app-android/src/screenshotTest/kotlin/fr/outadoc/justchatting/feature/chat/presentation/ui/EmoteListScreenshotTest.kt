package fr.outadoc.justchatting.feature.chat.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf

@PreviewTest
@Preview
@Composable
internal fun EmoteListScreenshotTest() {
    AppTheme {
        EmoteListSample()
    }
}

@PreviewTest
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun EmoteListDarkScreenshotTest() {
    AppTheme(isDarkTheme = true) {
        EmoteListSample()
    }
}

@Composable
private fun EmoteListSample() {
    Surface {
        EmoteList(
            modifier = Modifier.padding(16.dp),
            emotes =
                persistentListOf(
                    Emote(
                        name = "Kappa",
                        urls = EmoteUrls(url = "https://static-cdn.jtvnw.net/emoticons/v2/25/default/dark/1.0"),
                    ),
                    Emote(
                        name = "LUL",
                        urls = EmoteUrls(url = "https://static-cdn.jtvnw.net/emoticons/v2/425618/default/dark/1.0"),
                    ),
                ),
        )
    }
}
