package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Pronoun
import fr.outadoc.justchatting.feature.chat.presentation.OngoingEvents
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.isOdd
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentHashMap
import kotlinx.datetime.Clock

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChatList(
    modifier: Modifier = Modifier,
    entries: ImmutableList<ChatListItem.Message>,
    emotes: ImmutableMap<String, Emote>,
    cheerEmotes: ImmutableMap<String, Emote>,
    badges: ImmutableList<TwitchBadge>,
    removedContent: ImmutableList<ChatListItem.RemoveContent>,
    knownChatters: PersistentSet<Chatter>,
    pronouns: ImmutableMap<Chatter, Pronoun>,
    richEmbeds: ImmutableMap<String, ChatListItem.RichEmbed>,
    showTimestamps: Boolean,
    isDisconnected: Boolean,
    listState: LazyListState,
    onMessageLongClick: (ChatListItem.Message) -> Unit,
    onReplyToMessage: (ChatListItem.Message) -> Unit,
    onShowUserInfoForLogin: (String) -> Unit,
    roomState: RoomState,
    ongoingEvents: OngoingEvents,
    appUser: AppUser.LoggedIn,
    onListScrolledToBottom: (Boolean) -> Unit = {},
    insets: PaddingValues,
    clock: Clock = Clock.System,
) {
    val inlinesEmotes: PersistentMap<String, InlineTextContent> =
        remember(emotes) {
            emotes.mapValues { (_, emote) ->
                emoteTextContent(
                    emote = emote,
                )
            }.toPersistentHashMap()
        }

    val inlineBadges: PersistentMap<String, InlineTextContent> =
        remember(badges) {
            badges.associate { badge ->
                Pair(
                    badge.inlineContentId,
                    badgeTextContent(
                        badge = badge,
                    ),
                )
            }.toPersistentHashMap()
        }

    val inlineCheerEmotes: PersistentMap<String, InlineTextContent> =
        remember(cheerEmotes) {
            cheerEmotes.mapValues { (_, cheer) ->
                cheerEmoteTextContent(
                    cheer = cheer,
                )
            }.toPersistentHashMap()
        }

    val inlineContent: PersistentMap<String, InlineTextContent> =
        remember(inlinesEmotes, inlineBadges, inlineCheerEmotes) {
            inlinesEmotes
                .putAll(inlineBadges)
                .putAll(inlineCheerEmotes)
        }

    var size by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(size) {
        listState.scrollToItem(
            index = (entries.size - 1).coerceAtLeast(0),
        )
    }

    LazyColumn(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val newSize = coordinates.size
                if (size != newSize) {
                    size = newSize
                }
            },
        state = listState,
        contentPadding = PaddingValues(
            bottom = insets.calculateBottomPadding(),
        ),
    ) {
        stickyHeader {
            ChatEvents(
                modifier = Modifier.padding(horizontal = 6.dp),
                insets = insets,
                roomState = roomState,
                isDisconnected = isDisconnected,
                ongoingEvents = ongoingEvents,
                clock = clock,
                inlineContent = inlineContent,
                removedContent = removedContent,
                knownChatters = knownChatters,
                appUser = appUser,
                badges = badges,
            )
        }

        itemsIndexed(
            items = entries,
            key = { _, item -> item.hashCode() },
            contentType = { _, item ->
                when (item) {
                    is ChatListItem.Message.Highlighted -> 1
                    is ChatListItem.Message.Simple -> 2
                    is ChatListItem.Message.Notice -> 3
                }
            },
        ) { index, item ->
            val background =
                if (index.isOdd) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.surface
                }

            val canBeRepliedTo = item.body?.messageId != null
            val replyToActionCd = stringResource(MR.strings.chat_replyTo)

            SwipeToReply(
                onDismiss = { onReplyToMessage(item) },
                enabled = canBeRepliedTo,
            ) {
                ChatMessage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { onMessageLongClick(item) },
                            onLongClickLabel = stringResource(MR.strings.chat_copyToClipboard),
                        )
                        .semantics {
                            if (canBeRepliedTo) {
                                customActions = listOf(
                                    CustomAccessibilityAction(replyToActionCd) {
                                        onReplyToMessage(item)
                                        true
                                    },
                                )
                            }
                        },
                    message = item,
                    inlineContent = inlineContent,
                    removedContent = removedContent,
                    knownChatters = knownChatters,
                    pronouns = pronouns,
                    richEmbed = item.body?.messageId?.let { messageId -> richEmbeds[messageId] },
                    showTimestamps = showTimestamps,
                    background = background,
                    appUser = appUser,
                    onShowUserInfoForLogin = onShowUserInfoForLogin,
                )
            }
        }

        item(key = "visibility_trigger") {
            LaunchedEffect(Unit) {
                onListScrolledToBottom(true)
            }

            DisposableEffect(Unit) {
                onDispose {
                    onListScrolledToBottom(false)
                }
            }
        }
    }
}

private val TwitchBadge.inlineContentId: String
    get() = "badge_${setId}_$version"
