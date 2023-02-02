package fr.outadoc.justchatting.feature.chat.presentation.mobile

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
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    onMessageLongClick: (ChatEvent.Message) -> Unit,
    onReplyToMessage: (ChatEvent.Message) -> Unit,
    insets: PaddingValues,
) {
    when (state) {
        ChatViewModel.State.Initial -> {
            ChatListPlaceholder(modifier = modifier.fillMaxSize())
        }

        is ChatViewModel.State.Chatting -> {
            if (state.chatMessages.isEmpty()) {
                ChatListPlaceholder(modifier = modifier.fillMaxSize())
            } else {
                ChatListContainer(
                    modifier = modifier,
                    state = state,
                    animateEmotes = animateEmotes,
                    showTimestamps = showTimestamps,
                    onMessageLongClick = onMessageLongClick,
                    onReplyToMessage = onReplyToMessage,
                    insets = insets,
                )
            }
        }
    }
}
