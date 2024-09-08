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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.localized
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.ChatMessagePreviewProvider
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.previewBadges
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.formatHourMinute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentHashMap
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
internal fun ChatMessagePreview(
    @PreviewParameter(ChatMessagePreviewProvider::class) message: ChatListItem.Message,
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
                userId = "123",
                userLogin = "outadoc",
                token = "",
            ),
        )
    }
}

@Composable
internal fun ChatMessage(
    modifier: Modifier = Modifier,
    message: ChatListItem.Message,
    inlineContent: ImmutableMap<String, InlineTextContent> = persistentMapOf(),
    removedContent: ImmutableList<ChatListItem.RemoveContent> = persistentListOf(),
    pronouns: ImmutableMap<Chatter, Pronoun> = persistentMapOf(),
    richEmbed: ChatListItem.RichEmbed? = null,
    showTimestamps: Boolean,
    background: Color = Color.Transparent,
    backgroundHint: Color = MaterialTheme.colorScheme.surface,
    appUser: AppUser.LoggedIn,
    maxLines: Int = Int.MAX_VALUE,
    onShowInfoForUserId: (String) -> Unit = {},
) {
    val shouldRedactContents: Boolean =
        remember(message, removedContent) {
            removedContent
                .filter { rule -> rule.upUntil > message.timestamp }
                .filter { rule -> rule.matchingMessageId == null || rule.matchingMessageId == message.body?.messageId }
                .any { rule -> rule.matchingUserId == null || rule.matchingUserId == message.body?.chatter?.id }
        }

    Row(
        modifier = modifier
            .redactable(redact = shouldRedactContents)
            .background(MaterialTheme.colorScheme.surface)
            .background(background)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        message.timestamp
            .formatHourMinute()
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
            is ChatListItem.Message.Highlighted -> {
                UserNoticeMessage(
                    title = message.metadata.title.localized(),
                    titleIcon = message.metadata.titleIcon?.toMaterialIcon(),
                    subtitle = message.metadata.subtitle?.localized(),
                    level = message.metadata.level,
                ) {
                    message.body?.let { data ->
                        ChatMessageBody(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            body = data,
                            inlineContent = inlineContent,
                            pronouns = pronouns,
                            appUser = appUser,
                            backgroundHint = backgroundHint,
                            richEmbed = richEmbed,
                            maxLines = maxLines,
                            onShowInfoForUserId = onShowInfoForUserId,
                        )
                    }
                }
            }

            is ChatListItem.Message.Notice -> {
                NoticeMessage(
                    text = message.text.localized(),
                )
            }

            is ChatListItem.Message.Simple -> {
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
                        pronouns = pronouns,
                        appUser = appUser,
                        backgroundHint = backgroundHint,
                        richEmbed = richEmbed,
                        maxLines = maxLines,
                        onShowInfoForUserId = onShowInfoForUserId,
                    )
                }
            }
        }
    }
}
