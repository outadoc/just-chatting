package fr.outadoc.justchatting.feature.timeline.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

@PreviewTest
@Preview
@Composable
internal fun LiveTimelineContentScreenshotTest() {
    AppTheme {
        LiveTimelineContentSample()
    }
}

@PreviewTest
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun LiveTimelineContentDarkScreenshotTest() {
    AppTheme(isDarkTheme = true) {
        LiveTimelineContentSample()
    }
}

@Composable
private fun LiveTimelineContentSample() {
    LiveTimelineContent(
        timeZone = PreviewFixtures.timeZone,
        live =
            persistentListOf(
                UserStream(
                    user = PreviewFixtures.sampleUser,
                    stream =
                        PreviewFixtures.sampleStream.copy(
                            tags =
                                persistentListOf(
                                    "English",
                                    "Anime",
                                    "Singing",
                                    "puertorico",
                                    "Vtuber",
                                    "Latino",
                                    "DropsEnabled",
                                ),
                        ),
                ),
                UserStream(
                    user =
                        PreviewFixtures.user(
                            id = "2",
                            login = "hortyunderscore",
                            displayName = "HortyUnderscore",
                        ),
                    stream =
                        Stream(
                            id = "2",
                            userId = "2",
                            category = StreamCategory(id = "2", name = "Just Chatting"),
                            title = PreviewFixtures.sampleTextLong,
                            viewerCount = 1_204,
                            startedAt = Instant.parse("2022-01-01T15:00:00.00Z"),
                            tags = persistentListOf("Français", "DropsActivés"),
                        ),
                ),
                UserStream(
                    user =
                        PreviewFixtures.user(
                            id = "3",
                            login = "antoinedaniel",
                            displayName = "AntoineDaniel",
                        ),
                    stream =
                        Stream(
                            id = "3",
                            userId = "3",
                            category = null,
                            title = "Lorem ipsum dolor sit amet",
                            viewerCount = 42,
                            startedAt = Instant.parse("2022-01-01T17:40:00.00Z"),
                        ),
                ),
            ),
        isRefreshing = false,
        onRefresh = {},
        showRefreshIndicator = false,
        listState = rememberLazyListState(),
        clock = PreviewFixtures.fixedClock,
        onChannelClick = {},
        onOpenInBubble = {},
    )
}
