package fr.outadoc.justchatting.ui.chat

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry

@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry
) {
    when (message) {
        is ChatEntry.Highlighted -> {
            HighlightedMessage(
                modifier = modifier,
                message = message
            )
        }
        is ChatEntry.Simple -> {
            SimpleMessage(
                modifier = modifier,
                message = message
            )
        }
    }
}

@Composable
fun HighlightedMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry.Highlighted
) {
    Card {
        message.data?.let { data ->
            ChatMessageData(data = data)
        }
    }
}

@Composable
fun SimpleMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry.Simple
) {
    ChatMessageData(data = message.data)
}

@Composable
fun ChatMessageData(
    modifier: Modifier = Modifier,
    data: ChatEntry.Data
) {

}
