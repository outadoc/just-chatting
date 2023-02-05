package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.preferences.data.AppUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant

@Composable
fun ChatMessageDataOrCensored(
    modifier: Modifier = Modifier,
    timestamp: Instant,
    data: ChatEvent.Message.Data,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    removedContent: ImmutableList<ChatEvent.RemoveContent> = persistentListOf(),
    appUser: AppUser,
    backgroundHint: Color,
) {
    val shouldCensor: Boolean =
        remember(timestamp, data.messageId, data.userId, removedContent) {
            removedContent
                .filter { rule -> rule.upUntil > timestamp }
                .filter { rule -> rule.matchingMessageId == null || rule.matchingMessageId == data.messageId }
                .any { rule -> rule.matchingUserId == null || rule.matchingUserId == data.userId }
        }

    if (shouldCensor) {
        ChatMessageCensoredData(
            modifier = modifier
        )
    } else {
        ChatMessageData(
            modifier = modifier,
            data = data,
            inlineContent = inlineContent,
            appUser = appUser,
            backgroundHint = backgroundHint
        )
    }
}
