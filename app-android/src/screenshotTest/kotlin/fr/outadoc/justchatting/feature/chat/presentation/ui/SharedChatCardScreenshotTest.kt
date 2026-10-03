package fr.outadoc.justchatting.feature.chat.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.presentation.SharedChatChannel
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@PreviewTest
@Preview
@Composable
internal fun SharedChatCardScreenshotTest() {
    AppTheme {
        SharedChatCardSample()
    }
}

@PreviewTest
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun SharedChatCardDarkScreenshotTest() {
    AppTheme(isDarkTheme = true) {
        SharedChatCardSample()
    }
}

@PreviewTest
@Preview(widthDp = 280)
@Composable
internal fun SharedChatCardLongNameScreenshotTest() {
    AppTheme {
        SharedChatCardSample(
            channels =
                persistentListOf(
                    SharedChatChannel(
                        user =
                            PreviewFixtures.user(
                                id = "2",
                                login = "averyveryverylongchannelname",
                                displayName = "AVeryVeryVeryLongChannelName",
                            ),
                        isHost = true,
                    ),
                ),
        )
    }
}

@Composable
private fun SharedChatCardSample(channels: ImmutableList<SharedChatChannel> = sampleSharedChatChannels) {
    Surface {
        SharedChatCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            channels = channels,
        )
    }
}

internal val sampleSharedChatChannels: ImmutableList<SharedChatChannel> =
    persistentListOf(
        SharedChatChannel(
            user = PreviewFixtures.user(id = "2", login = "sundae", displayName = "Sundae"),
            isHost = true,
        ),
        SharedChatChannel(
            user = PreviewFixtures.user(id = "3", login = "maghla", displayName = "Maghla"),
            isHost = false,
        ),
        SharedChatChannel(
            user = PreviewFixtures.user(id = "4", login = "antoinedaniel", displayName = "AntoineDaniel"),
            isHost = false,
        ),
    )
