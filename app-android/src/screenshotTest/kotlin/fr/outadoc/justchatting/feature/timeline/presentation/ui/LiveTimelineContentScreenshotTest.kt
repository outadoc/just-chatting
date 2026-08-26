package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Clock
import kotlin.time.Instant

// A fixed clock keeps the rendered "time since" duration stable across the
// update/validate Gradle runs, instead of ticking with the real system clock.
private val fixedClock =
    object : Clock {
        override fun now(): Instant = Instant.parse("2022-01-01T18:00:00Z")
    }

@PreviewTest
@Preview
@Composable
internal fun LiveTimelineContentScreenshotTest() {
    val user =
        User(
            id = "1",
            login = "maghla",
            displayName = "Maghla",
            description = "",
            profileImageUrl = "",
            createdAt = Instant.DISTANT_PAST,
            usedAt = Instant.DISTANT_PAST,
        )

    AppTheme {
        LiveTimelineContent(
            live =
                persistentListOf(
                    UserStream(
                        user = user,
                        stream =
                            Stream(
                                id = "1",
                                userId = user.id,
                                category = StreamCategory(id = "1", name = "Powerwash Simulator"),
                                title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                                viewerCount = 5_305,
                                startedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
                            ),
                    ),
                ),
            isRefreshing = false,
            onRefresh = {},
            showRefreshIndicator = false,
            listState = rememberLazyListState(),
            clock = fixedClock,
            onChannelClick = {},
            onOpenInBubble = {},
        )
    }
}
