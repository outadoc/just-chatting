package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun AutoCompleteEmoteItemScreenshotTest() {
    AppTheme {
        Surface {
            AutoCompleteEmoteItem(
                onClick = {},
                emote =
                    Emote(
                        name = "Kappa",
                        urls = EmoteUrls(url = "https://static-cdn.jtvnw.net/emoticons/v2/25/default/dark/1.0"),
                    ),
            )
        }
    }
}
