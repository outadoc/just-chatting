package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.chat.presentation.UserInfoViewModel
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.timeline.presentation.ui.ContextualButton
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.chat_copyToClipboard
import fr.outadoc.justchatting.shared.internal.chat_messageActions_emotes
import fr.outadoc.justchatting.shared.internal.chat_replyTo
import fr.outadoc.justchatting.utils.presentation.formatFullDateTime
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentHashMap
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MessageActionsBottomSheet(
    modifier: Modifier = Modifier,
    message: ChatListItem.Message,
    appUser: AppUser.LoggedIn,
    pronouns: ImmutableMap<Chatter, Pronoun> = persistentMapOf(),
    badges: ImmutableList<TwitchBadge> = persistentListOf(),
    emotes: ImmutableMap<String, Emote> = persistentMapOf(),
    cheerEmotes: ImmutableMap<String, Emote> = persistentMapOf(),
    richEmbeds: ImmutableMap<String, ChatListItem.RichEmbed> = persistentMapOf(),
    onDismissRequest: () -> Unit = {},
    onReplyToMessage: (ChatListItem.Message) -> Unit = {},
    onCopyToClipboard: (ChatListItem.Message) -> Unit = {},
) {
    val body = message.body ?: return
    val canBeRepliedTo = body.messageId != null

    val viewModel: UserInfoViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(body.chatter.id) {
        viewModel.load(body.chatter.id)
    }

    val inlineContent: ImmutableMap<String, InlineTextContent> =
        remember(badges, emotes, cheerEmotes) {
            badges
                .associate { badge -> badge.inlineContentId to badgeTextContent(badge) }
                .toPersistentHashMap()
                .putAll(
                    emotes.mapValues { (_, emote) -> emoteTextContent(emote = emote) },
                ).putAll(
                    cheerEmotes.mapValues { (_, cheer) -> cheerEmoteTextContent(cheer) },
                )
        }

    // `body.embeddedEmotes` only covers first-party Twitch emotes, resolved from the IRC
    // `emotes` tag. Third-party emotes (BTTV/FFZ/7TV) are just plain words that happen to
    // match a name in the channel's emote set, so they need to be matched the same way.
    val usedEmotes: ImmutableList<Emote> =
        remember(body.message, body.embeddedEmotes, emotes) {
            (
                body.embeddedEmotes +
                    body.message
                        ?.split(' ')
                        ?.mapNotNull { word -> emotes[word] }
                        .orEmpty()
            ).toImmutableList()
        }

    ActionBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        header = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (val currentState = state) {
                    is UserInfoViewModel.State.Loaded -> {
                        BasicUserInfo(user = currentState.user)
                    }

                    else -> {
                        Text(
                            text = body.chatter.displayName,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ChatMessage(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                        message = message,
                        inlineContent = inlineContent,
                        pronouns = pronouns,
                        richEmbed = body.messageId?.let { messageId -> richEmbeds[messageId] },
                        showTimestamps = false,
                        background = MaterialTheme.colorScheme.surfaceVariant,
                        appUser = appUser,
                        maxLines = 10,
                    )

                    message.timestamp.formatFullDateTime()?.let { timestamp ->
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalContentColor.current.copy(alpha = 0.6f),
                        )
                    }
                }

                if (usedEmotes.isNotEmpty()) {
                    HorizontalDivider()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.chat_messageActions_emotes),
                            style = MaterialTheme.typography.labelLarge,
                        )

                        EmoteList(emotes = usedEmotes)
                    }
                }
            }
        },
        actions = { padding ->
            if (canBeRepliedTo) {
                ContextualButton(
                    contentPadding = padding,
                    onClick = {
                        onReplyToMessage(message)
                        onDismissRequest()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                        )
                    },
                    text = stringResource(Res.string.chat_replyTo),
                )
            }

            ContextualButton(
                contentPadding = padding,
                onClick = {
                    onCopyToClipboard(message)
                    onDismissRequest()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                    )
                },
                text = stringResource(Res.string.chat_copyToClipboard),
            )
        },
    )
}
