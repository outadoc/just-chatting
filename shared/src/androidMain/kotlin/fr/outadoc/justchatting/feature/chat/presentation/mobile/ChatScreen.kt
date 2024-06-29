package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel

@Composable
internal fun ChatScreen(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    showTimestamps: Boolean,
    onMessageLongClick: (ChatListItem.Message) -> Unit,
    onReplyToMessage: (ChatListItem.Message) -> Unit,
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
