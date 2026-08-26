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
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.ui.ChannelChatScreenContent
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.feature.timeline.presentation.ui.LiveTimelineContent
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

// Recreates the wide-screen home layout (nav rail, live streams list, and an open chat in
// the details pane) directly from the already-public, already-tested pieces, since the real
// MainRouter is wired to Koin and Navigation3 and can't be screenshot-tested as-is. Mirrors
// MainRouter's actual composition shape: MainNavigation wraps only the list pane (as it does
// for LiveTimelineScreen); the details pane is a sibling, not nested inside it.
@PreviewTest
@Preview(widthDp = 1280, heightDp = 800)
@Composable
internal fun HomeScreenWideScreenshotTest() {
    AppTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(580.dp)) {
                MainNavigation(
                    selectedScreen = Screen.Live,
                    onSelectedTabChange = {},
                ) { insets ->
                    LiveTimelineContent(
                        insets = insets,
                        live =
                            persistentListOf(
                                UserStream(
                                    user = PreviewFixtures.sampleUser,
                                    stream = PreviewFixtures.sampleStream,
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
                        selectedChannelId = PreviewFixtures.sampleUser.id,
                        clock = PreviewFixtures.fixedClock,
                        onChannelClick = {},
                        onOpenInBubble = {},
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                // Matches MainRouter's wide-screen detail pane, which wraps its content
                // in a DetailPaneCard whenever the list and detail panes are both visible.
                DetailPaneCard {
                    ChannelChatScreenContent(
                        state =
                            ChatViewModel.State.Chatting(
                                user = PreviewFixtures.sampleUser,
                                appUser = PreviewFixtures.sampleLoggedInUser,
                                maxAdapterCount = 100,
                                connectionStatus =
                                    ConnectionStatus(
                                        isAlive = true,
                                        registeredListeners = 1,
                                        aliveConnections = 1,
                                    ),
                                chatMessages =
                                    persistentListOf(
                                        PreviewFixtures.sampleChatMessage,
                                        PreviewFixtures.sampleChatMessageAlternate,
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
