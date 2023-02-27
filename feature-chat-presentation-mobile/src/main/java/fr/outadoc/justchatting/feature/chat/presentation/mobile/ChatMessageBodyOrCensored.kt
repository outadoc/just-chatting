package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.preferences.data.AppUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant

@Composable
fun ChatMessageBodyOrCensored(
    modifier: Modifier = Modifier,
    timestamp: Instant,
    body: ChatEvent.Message.Body,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    removedContent: ImmutableList<ChatEvent.RemoveContent> = persistentListOf(),
    appUser: AppUser,
    backgroundHint: Color,
    richEmbed: ChatEvent.RichEmbed? = null,
) {
    val shouldCensor: Boolean =
        removedContent
            .filter { rule -> rule.upUntil > timestamp }
            .filter { rule -> rule.matchingMessageId == null || rule.matchingMessageId == body.messageId }
            .any { rule -> rule.matchingUserId == null || rule.matchingUserId == body.userId }

    ChatMessageBody(
        modifier = modifier,
        body = if (shouldCensor) {
            body.copy(message = body.message?.length?.let { size -> "█".repeat(size) })
        } else {
            body
        },
        inlineContent = inlineContent,
        appUser = appUser,
        backgroundHint = backgroundHint,
        richEmbed = richEmbed,
    )
}
