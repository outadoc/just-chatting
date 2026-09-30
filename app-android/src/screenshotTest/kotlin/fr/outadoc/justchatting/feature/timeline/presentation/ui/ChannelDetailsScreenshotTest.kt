package fr.outadoc.justchatting.feature.timeline.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf

@PreviewTest
@Preview
@Composable
internal fun ChannelDetailsScreenshotTest() {
    AppTheme {
        ChannelDetailsSample()
    }
}

@PreviewTest
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ChannelDetailsDarkScreenshotTest() {
    AppTheme(isDarkTheme = true) {
        ChannelDetailsSample()
    }
}

@Composable
private fun ChannelDetailsSample() {
    val user =
        PreviewFixtures.user(
            id = "1",
            login = "maghla",
            displayName = "Maghla",
            description = PreviewFixtures.sampleTextShort,
            createdAt = PreviewFixtures.sampleTimestamp,
        )

    Surface {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChannelDetailsHeader(user = user)

            ChannelDetailsContent(
                timeZone = PreviewFixtures.timeZone,
                user = user,
                stream =
                    PreviewFixtures.sampleStream.copy(
                        tags = persistentListOf("Français", "DropsActivés"),
                    ),
            )

            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChannelDetailsActions(
                    onOpenChat = {},
                    onOpenInBubble = {},
                    onWatchLive = {},
                )
            }
        }
    }
}
