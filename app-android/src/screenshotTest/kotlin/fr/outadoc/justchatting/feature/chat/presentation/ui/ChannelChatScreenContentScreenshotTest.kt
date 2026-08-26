package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

@PreviewTest
@Preview
@Composable
internal fun ChannelChatScreenLoadingScreenshotTest() {
    AppTheme {
        ChannelChatScreenContent(
            state = ChatViewModel.State.Initial,
            inputState = ChatViewModel.InputState(),
            showTimestamps = true,
        )
    }
}

@PreviewTest
@Preview
@Composable
internal fun ChannelChatScreenChattingScreenshotTest() {
    AppTheme {
        ChannelChatScreenContent(
            state =
                ChatViewModel.State.Chatting(
                    user =
                        User(
                            id = "1",
                            login = "maghla",
                            displayName = "Maghla",
                            description = "",
                            profileImageUrl = "",
                            createdAt = Instant.DISTANT_PAST,
                            usedAt = Instant.DISTANT_PAST,
                        ),
                    appUser =
                        AppUser.LoggedIn(
                            userId = "123",
                            userLogin = "outadoc",
                            token = "",
                        ),
                    maxAdapterCount = 100,
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
                                                displayName = "컬러히에",
                                                id = "232421548",
                                                login = "kolorye",
                                            ),
                                        message =
                                            "@djessy728 il avait dit quand mathieu avait sortit sa vidéo après longtemps " +
                                                "qu'ils n'étaient plus en contact plus que ça, donc j'imagine que non",
                                        messageId = "4b3f4db7-5956-4ade-adba-ed282c22eb50",
                                        isAction = false,
                                        color = "#5F9EA0",
                                    ),
                                timestamp = Instant.fromEpochMilliseconds(1664399217864),
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
            showTimestamps = true,
        )
    }
}
