package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.ui.ChannelChatScreenContent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.feature.timeline.presentation.ui.LiveTimelineContent
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

private val maghla =
    User(
        id = "1",
        login = "maghla",
        displayName = "Maghla",
        description = "",
        profileImageUrl = "",
        createdAt = Instant.DISTANT_PAST,
        usedAt = Instant.DISTANT_PAST,
    )

// Recreates the wide-screen home layout (nav rail, live streams list, and an open chat in
// the details pane) directly from the already-public, already-tested pieces, since the real
// MainRouter is wired to Koin and Navigation3 and can't be screenshot-tested as-is.
@PreviewTest
@Preview(widthDp = 1280, heightDp = 800)
@Composable
internal fun HomeScreenWideScreenshotTest() {
    AppTheme {
        MainNavigation(
            selectedScreen = Screen.Live,
            onSelectedTabChange = {},
        ) { insets ->
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.width(500.dp)) {
                    LiveTimelineContent(
                        insets = insets,
                        live =
                            persistentListOf(
                                UserStream(
                                    user = maghla,
                                    stream =
                                        Stream(
                                            id = "1",
                                            userId = maghla.id,
                                            category = StreamCategory(id = "1", name = "Powerwash Simulator"),
                                            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                                            viewerCount = 5_305,
                                            startedAt = Instant.parse("2022-01-01T13:45:04.00Z"),
                                        ),
                                ),
                                UserStream(
                                    user =
                                        User(
                                            id = "2",
                                            login = "hortyunderscore",
                                            displayName = "HortyUnderscore",
                                            description = "",
                                            profileImageUrl = "",
                                            createdAt = Instant.DISTANT_PAST,
                                            usedAt = Instant.DISTANT_PAST,
                                        ),
                                    stream =
                                        Stream(
                                            id = "2",
                                            userId = "2",
                                            category = StreamCategory(id = "2", name = "Just Chatting"),
                                            title = "Lorem ipsum dolor sit amet",
                                            viewerCount = 1_204,
                                            startedAt = Instant.parse("2022-01-01T15:00:00.00Z"),
                                        ),
                                ),
                            ),
                        isRefreshing = false,
                        onRefresh = {},
                        showRefreshIndicator = false,
                        listState = rememberLazyListState(),
                        selectedChannelId = maghla.id,
                        clock = fixedClock,
                        onChannelClick = {},
                        onOpenInBubble = {},
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    ChannelChatScreenContent(
                        state =
                            ChatViewModel.State.Chatting(
                                user = maghla,
                                appUser =
                                    AppUser.LoggedIn(
                                        userId = "123",
                                        userLogin = "outadoc",
                                        token = "",
                                    ),
                                maxAdapterCount = 100,
                                connectionStatus =
                                    ConnectionStatus(
                                        isAlive = true,
                                        registeredListeners = 1,
                                        aliveConnections = 1,
                                    ),
                                chatMessages =
                                    persistentListOf(
                                        ChatListItem.Message.Simple(
                                            body =
                                                ChatListItem.Message.Body(
                                                    chatter =
                                                        Chatter(
                                                            displayName = "Hiccoz",
                                                            id = "68552712",
                                                            login = "hiccoz",
                                                        ),
                                                    message = "feur",
                                                    messageId = "b43bd9e5-ec6e-47fe-a5da-c3213540fe06",
                                                    isAction = false,
                                                    color = "#FF69B4",
                                                    badges =
                                                        persistentListOf(
                                                            Badge("subscriber", "48"),
                                                            Badge("sub-gifter", "100"),
                                                        ),
                                                ),
                                            timestamp = Instant.fromEpochMilliseconds(1664396374382),
                                        ),
                                        ChatListItem.Message.Simple(
                                            body =
                                                ChatListItem.Message.Body(
                                                    chatter =
                                                        Chatter(
                                                            displayName = "marion_11",
                                                            id = "280065659",
                                                            login = "marion_11",
                                                        ),
                                                    message = "ok att jen ai un comme vous",
                                                    messageId = "a5d36b3a-f663-4890-9d82-cbf1f89ce726",
                                                    isAction = false,
                                                    color = "#FF0000",
                                                ),
                                            timestamp = Instant.fromEpochMilliseconds(1664399218000),
                                        ),
                                    ),
                            ),
                        inputState = ChatViewModel.InputState(),
                        showBackButton = false,
                        showTimestamps = true,
                    )
                }
            }
        }
    }
}
