package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.presentation.ChatEntry
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    onMessageLongClick: (ChatEntry) -> Unit,
    onReplyToMessage: (ChatEntry) -> Unit,
    insets: PaddingValues,
) {
    when (state) {
        ChatViewModel.State.Initial -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }

        is ChatViewModel.State.Chatting -> {
            if (state.chatMessages.isEmpty()) {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
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
