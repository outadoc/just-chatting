package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Pronoun
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.ChatMessagePreviewProvider
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.previewBadges
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.formatTimestamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentHashMap

@ThemePreviews
@Composable
fun ChatMessagePreview(
    @PreviewParameter(ChatMessagePreviewProvider::class) message: ChatEvent.Message,
) {
    val inlineBadges = previewBadges
        .associateWith { previewTextContent() }
        .toPersistentHashMap()

    AppTheme {
        ChatMessage(
            message = message,
            inlineContent = inlineBadges,
            showTimestamps = true,
            appUser = AppUser.LoggedIn(
                id = "123",
                login = "outadoc",
                helixToken = "",
            ),
        )
    }
}

@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: ChatEvent.Message,
    inlineContent: ImmutableMap<String, InlineTextContent> = persistentMapOf(),
    removedContent: ImmutableList<ChatEvent.RemoveContent> = persistentListOf(),
    knownChatters: PersistentSet<Chatter> = persistentSetOf(),
    pronouns: ImmutableMap<Chatter, Pronoun> = persistentMapOf(),
    richEmbed: ChatEvent.RichEmbed? = null,
    showTimestamps: Boolean,
    background: Color = Color.Transparent,
    backgroundHint: Color = MaterialTheme.colorScheme.surface,
    appUser: AppUser.LoggedIn,
    onShowUserInfoForLogin: (String) -> Unit = {},
) {
    val shouldRedactContents: Boolean =
        removedContent
            .filter { rule -> rule.upUntil > message.timestamp }
            .filter { rule -> rule.matchingMessageId == null || rule.matchingMessageId == message.body?.messageId }
            .any { rule -> rule.matchingUserId == null || rule.matchingUserId == message.body?.chatter?.id }

    Row(
        modifier = modifier
            .redactable(redact = shouldRedactContents)
            .background(MaterialTheme.colorScheme.surface)
            .background(background)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        message.timestamp
            .formatTimestamp()
            ?.takeIf { showTimestamps }
            ?.let { timestamp ->
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = timestamp,
                    color = LocalContentColor.current.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

        when (message) {
            is ChatEvent.Message.Highlighted -> {
                UserNoticeMessage(
                    title = message.title,
                    titleIcon = message.titleIcon,
                    subtitle = message.subtitle,
                ) {
                    message.body?.let { data ->
                        ChatMessageBody(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            body = data,
                            inlineContent = inlineContent,
                            knownChatters = knownChatters,
                            pronouns = pronouns,
                            appUser = appUser,
                            backgroundHint = backgroundHint,
                            richEmbed = richEmbed,
                            onShowUserInfoForLogin = onShowUserInfoForLogin,
                        )
                    }
                }
            }

            is ChatEvent.Message.Notice -> {
                NoticeMessage(
                    text = message.text,
                )
            }

            is ChatEvent.Message.Simple -> {
                SimpleMessage {
                    ChatMessageBody(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 4.dp,
                                vertical = 6.dp,
                            ),
                        body = message.body,
                        inlineContent = inlineContent,
                        appUser = appUser,
                        backgroundHint = backgroundHint,
                        knownChatters = knownChatters,
                        pronouns = pronouns,
                        richEmbed = richEmbed,
                        onShowUserInfoForLogin = onShowUserInfoForLogin,
                    )
                }
            }
        }
    }
}
