package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    showTimestamps: Boolean,
    onMessageLongClick: (ChatEvent.Message) -> Unit,
    onReplyToMessage: (ChatEvent.Message) -> Unit,
    onShowUserInfoForLogin: (String) -> Unit,
    insets: PaddingValues,
) {
    val hasMessages: Boolean =
        (state as? ChatViewModel.State.Chatting)
            ?.chatMessages
            .isNullOrEmpty()

    Crossfade(
        targetState = hasMessages,
        label = "Chat list placeholder loader",
    ) { showPlaceholder ->
        when {
            showPlaceholder -> {
                ChatListPlaceholder(
                    modifier = modifier.fillMaxSize(),
                )
            }

            state is ChatViewModel.State.Chatting -> {
                ChatListContainer(
                    modifier = modifier,
                    state = state,
                    showTimestamps = showTimestamps,
                    onMessageLongClick = onMessageLongClick,
                    onReplyToMessage = onReplyToMessage,
                    onShowUserInfoForLogin = onShowUserInfoForLogin,
                    insets = insets,
                )
            }
        }
    }
}
