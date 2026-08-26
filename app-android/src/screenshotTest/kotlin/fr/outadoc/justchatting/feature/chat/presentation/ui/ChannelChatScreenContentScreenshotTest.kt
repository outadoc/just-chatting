package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.preview.PreviewFixtures
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
                    user = PreviewFixtures.sampleUser,
                    appUser = PreviewFixtures.sampleLoggedInUser,
                    maxAdapterCount = 100,
                    chatMessages =
                        persistentListOf(
                            PreviewFixtures.sampleChatMessage,
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
                            PreviewFixtures.sampleChatMessageAlternate,
                        ),
                ),
            inputState = ChatViewModel.InputState(),
            showTimestamps = true,
        )
    }
}
