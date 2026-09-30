package fr.outadoc.justchatting.landing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.presentation.ChatRowBackground
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.ui.ChannelChatScreenContent
import fr.outadoc.justchatting.feature.chat.presentation.ui.ChatInput
import fr.outadoc.justchatting.feature.chat.presentation.ui.ChatMessage
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.feature.shared.presentation.ui.DetailPaneCard
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainTopAppBar
import fr.outadoc.justchatting.feature.shared.presentation.ui.ProfileButton
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.feature.timeline.presentation.ui.FutureTimelineContent
import fr.outadoc.justchatting.feature.timeline.presentation.ui.LiveTimelineContent
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.timeline_future
import fr.outadoc.justchatting.shared.internal.timeline_live
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

// Screenshots for the landing page, one per section of the page. Update them with
// ./gradlew :app-android:updateDebugScreenshotTest

// Hero

@PreviewTest
@LandingPhonePreviews
@Composable
internal fun LandingHeroScreenshotTest() {
    ChannelChat(
        userStream = LandingFixtures.yarrowStream,
        seedColor = LandingFixtures.yarrowSeed,
        inputState = ChatViewModel.InputState(replyingTo = LandingFixtures.lami),
    )
}

// Features

@PreviewTest
@LandingPhonePreviews
@Composable
internal fun LandingDynamicColorsYarrowScreenshotTest() {
    ChannelChat(
        userStream = LandingFixtures.yarrowStream,
        seedColor = LandingFixtures.yarrowSeed,
    )
}

@PreviewTest
@LandingPhonePreviews
@Composable
internal fun LandingDynamicColorsSolanumScreenshotTest() {
    ChannelChat(
        userStream = LandingFixtures.solanumStream,
        seedColor = LandingFixtures.solanumSeed,
    )
}

@PreviewTest
@LandingPhonePreviews
@Composable
internal fun LandingDynamicColorsPokeScreenshotTest() {
    ChannelChat(
        userStream = LandingFixtures.pokeStream,
        seedColor = LandingFixtures.pokeSeed,
    )
}

@PreviewTest
@LandingFeaturePreviews
@Composable
internal fun LandingQuickEmotesScreenshotTest() {
    LandingTheme(seedColor = LandingFixtures.yarrowSeed) {
        ChatInput(
            modifier = Modifier.padding(bottom = InputBottomPadding),
            appUser = LandingFixtures.appUser,
            autoCompleteItems = LandingFixtures.recentEmoteSuggestions,
            contentPadding = InputContentPadding,
        )
    }
}

@PreviewTest
@LandingFeaturePreviews
@Composable
internal fun LandingReplyScreenshotTest() {
    LandingTheme(seedColor = LandingFixtures.yarrowSeed) {
        Column {
            Messages(LandingFixtures.ramie, LandingFixtures.plume)
            ChatInput(
                modifier = Modifier.padding(bottom = InputBottomPadding),
                appUser = LandingFixtures.appUser,
                replyingTo = LandingFixtures.plume,
                contentPadding = InputContentPadding,
            )
        }
    }
}

@PreviewTest
@LandingPhonePreviews
@Composable
internal fun LandingBubbleScreenshotTest() {
    // A chat open in a bubble is standalone: there is nowhere to go back to.
    ChannelChat(
        userStream = LandingFixtures.solanumStream,
        seedColor = LandingFixtures.solanumSeed,
        showBackButton = false,
    )
}

@PreviewTest
@LandingPhonePreviews
@Composable
internal fun LandingCustomEmotesScreenshotTest() {
    ChannelChat(
        userStream = LandingFixtures.yarrowStream,
        seedColor = LandingFixtures.yarrowSeed,
        isEmotePickerOpen = true,
        // The recent emotes would only repeat the channel's.
        recentEmotes = emptyList(),
    )
}

@PreviewTest
@LandingFeaturePreviews
@Composable
internal fun LandingPronounsScreenshotTest() {
    LandingTheme(seedColor = LandingFixtures.yarrowSeed) {
        Messages(LandingFixtures.solanumMessage, LandingFixtures.avens)
    }
}

@PreviewTest
@LandingPhonePreviews
@Composable
internal fun LandingScheduleScreenshotTest() {
    LandingTheme {
        MainNavigation(
            selectedScreen = Screen.Future,
            onSelectedTabChange = {},
            topBar = {
                MainTopAppBar(
                    title = stringResource(Res.string.timeline_future),
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null)
                        }
                    },
                    profileButton = { AppUserProfileButton() },
                )
            },
        ) { insets ->
            FutureTimelineContent(
                insets = insets,
                days = LandingFixtures.schedule,
                isRefreshing = false,
                onRefresh = {},
                showRefreshIndicator = false,
                listState = rememberLazyListState(),
            )
        }
    }
}

// Devices

@PreviewTest
@LandingPhonePreviews
@Composable
internal fun LandingPhoneScreenshotTest() {
    LandingTheme {
        WithStatusBar(color = MaterialTheme.colorScheme.surface) {
            LiveScreen()
        }
    }
}

@PreviewTest
@LandingTabletPreviews
@Composable
internal fun LandingTabletScreenshotTest() {
    ListAndChat(listWidth = 480.dp, hasMouse = false)
}

@PreviewTest
@LandingDesktopPreviews
@Composable
internal fun LandingDesktopScreenshotTest() {
    ListAndChat(listWidth = 560.dp, hasMouse = true)
}

/**
 * The chat of a channel, full screen, as on a phone.
 */
@Composable
private fun ChannelChat(
    userStream: UserStream,
    seedColor: Color,
    inputState: ChatViewModel.InputState = ChatViewModel.InputState(),
    isEmotePickerOpen: Boolean = false,
    recentEmotes: List<Emote> = LandingFixtures.recentEmotes,
    showBackButton: Boolean = true,
) {
    LandingTheme(seedColor = seedColor) {
        ChannelChatScreenContent(
            state = LandingFixtures.chatting(userStream = userStream, recentEmotes = recentEmotes),
            inputState = inputState,
            isEmotePickerOpen = isEmotePickerOpen,
            showBackButton = showBackButton,
            showTimestamps = false,
        )
    }
}

/**
 * The live streams list and an open chat side by side, as on wide screens. Built from the same
 * pieces as MainRouter, which is wired to Koin and Navigation3 and can't be used here.
 */
@Composable
private fun ListAndChat(
    listWidth: Dp,
    hasMouse: Boolean,
) {
    LandingTheme {
        if (hasMouse) {
            ListAndChatContent(listWidth = listWidth, hasMouse = true)
        } else {
            // Tablets have a status bar; desktop windows don't.
            WithStatusBar(color = MaterialTheme.colorScheme.surface) {
                ListAndChatContent(listWidth = listWidth, hasMouse = false)
            }
        }
    }
}

@Composable
private fun ListAndChatContent(
    listWidth: Dp,
    hasMouse: Boolean,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.width(listWidth)) {
            LiveScreen(
                selectedChannelId = LandingFixtures.yarrow.id,
                hasMouse = hasMouse,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            DetailPaneCard {
                LandingTheme(seedColor = LandingFixtures.yarrowSeed) {
                    ChannelChatScreenContent(
                        state = LandingFixtures.chatting(userStream = LandingFixtures.yarrowStream),
                        inputState = ChatViewModel.InputState(),
                        showBackButton = false,
                        showTimestamps = false,
                    )
                }
            }
        }
    }
}

/**
 * The Live tab, as in LiveTimelineScreen.
 *
 * @param hasMouse whether to show the refresh button, as on devices with a mouse.
 */
@Composable
private fun LiveScreen(
    selectedChannelId: String? = null,
    hasMouse: Boolean = false,
) {
    MainNavigation(
        selectedScreen = Screen.Live,
        onSelectedTabChange = {},
        topBar = {
            MainTopAppBar(
                title = stringResource(Res.string.timeline_live),
                actions = {
                    if (hasMouse) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Sync, contentDescription = null)
                        }
                    }
                },
                profileButton = { AppUserProfileButton() },
            )
        },
    ) { insets ->
        LiveStreams(
            insets = insets,
            selectedChannelId = selectedChannelId,
        )
    }
}

@Composable
private fun AppUserProfileButton() {
    ProfileButton(
        profileImageUrl = LandingFixtures.appUserAvatar,
        onClick = {},
    )
}

@Composable
private fun LiveStreams(
    insets: PaddingValues,
    selectedChannelId: String?,
) {
    LiveTimelineContent(
        insets = insets,
        live = LandingFixtures.liveStreams,
        isRefreshing = false,
        onRefresh = {},
        showRefreshIndicator = false,
        listState = rememberLazyListState(),
        selectedChannelId = selectedChannelId,
        clock = LandingFixtures.clock,
        onChannelClick = {},
        onOpenInBubble = {},
    )
}

// Same as in ChannelChatScreenContent, which has room for the navigation bar below the input.
private val InputContentPadding = 12.dp
private val InputBottomPadding = 12.dp

/**
 * Messages, oldest first, with the same alternating backgrounds as in the chat list.
 */
@Composable
private fun Messages(vararg messages: ChatListItem.Message.Simple) {
    Column {
        messages.forEachIndexed { index, message ->
            // The chat list counts from the newest message.
            val isAlternate =
                ChatRowBackground.isAlternate(
                    index = messages.lastIndex - index,
                    messageCount = messages.size,
                )

            ChatMessage(
                // Badges are drawn from the chat's badge list, which only the full chat screen has.
                message = message.copy(body = message.body.copy(badges = persistentListOf())),
                pronouns = LandingFixtures.pronouns,
                showTimestamps = false,
                background =
                    if (isAlternate) {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                appUser = LandingFixtures.appUser,
            )
        }
    }
}
