package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.chat_loadError
import org.jetbrains.compose.resources.stringResource

private enum class ChatScreenMode { Error, Placeholder, Content }

@Composable
internal fun ChatScreen(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    showTimestamps: Boolean,
    onMessageClick: (ChatListItem.Message) -> Unit,
    onMessageLongClick: (ChatListItem.Message) -> Unit,
    onReplyToMessage: (ChatListItem.Message) -> Unit,
    insets: PaddingValues,
) {
    val mode: ChatScreenMode =
        when {
            state is ChatViewModel.State.Failed -> ChatScreenMode.Error
            state !is ChatViewModel.State.Chatting || state.chatMessages.isEmpty() -> ChatScreenMode.Placeholder
            else -> ChatScreenMode.Content
        }

    Crossfade(
        targetState = mode,
        label = "Chat list placeholder loader",
    ) { targetMode ->
        when (targetMode) {
            ChatScreenMode.Error -> {
                Column(
                    modifier =
                        modifier
                            .padding(insets)
                            .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(Res.string.chat_loadError))
                }
            }

            ChatScreenMode.Placeholder -> {
                ChatListPlaceholder(
                    modifier =
                        modifier
                            .padding(insets)
                            .fillMaxSize(),
                )
            }

            ChatScreenMode.Content -> {
                if (state is ChatViewModel.State.Chatting) {
                    ChatListContainer(
                        modifier = modifier,
                        state = state,
                        showTimestamps = showTimestamps,
                        onMessageClick = onMessageClick,
                        onMessageLongClick = onMessageLongClick,
                        onReplyToMessage = onReplyToMessage,
                        insets = insets,
                    )
                }
            }
        }
    }
}
