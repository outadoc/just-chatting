package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf

@PreviewTest
@Preview
@Composable
internal fun LiveTimelineContentScreenshotTest() {
    AppTheme {
        LiveTimelineContent(
            live =
                persistentListOf(
                    UserStream(
                        user = PreviewFixtures.sampleUser,
                        stream = PreviewFixtures.sampleStream,
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
}
