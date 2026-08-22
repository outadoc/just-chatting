package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.chat.presentation.OngoingEvents
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.ui.ContextualActionBox
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.chat_copyToClipboard
import fr.outadoc.justchatting.shared.internal.chat_replyTo
import fr.outadoc.justchatting.utils.core.isEven
import fr.outadoc.justchatting.utils.core.isOdd
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentHashMap
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
internal fun ChatList(
    modifier: Modifier = Modifier,
    entries: ImmutableList<ChatListItem.Message>,
    emotes: ImmutableMap<String, Emote>,
    cheerEmotes: ImmutableMap<String, Emote>,
    badges: ImmutableList<TwitchBadge>,
    removedContent: ImmutableList<ChatListItem.RemoveContent>,
    pronouns: ImmutableMap<Chatter, Pronoun>,
    sourceChannels: ImmutableMap<String, User>,
    sourceChannelBadges: ImmutableMap<String, ImmutableList<TwitchBadge>>,
    richEmbeds: ImmutableMap<String, ChatListItem.RichEmbed>,
    showTimestamps: Boolean,
    isDisconnected: Boolean,
    listState: LazyListState,
    onMessageClick: (ChatListItem.Message) -> Unit,
    onMessageLongClick: (ChatListItem.Message) -> Unit,
    onReplyToMessage: (ChatListItem.Message) -> Unit,
    roomState: RoomState,
    ongoingEvents: OngoingEvents,
    appUser: AppUser.LoggedIn,
    onListScrolledToBottom: (Boolean) -> Unit = {},
    insets: PaddingValues,
    clock: Clock = Clock.System,
) {
    val haptic = LocalHapticFeedback.current

    val inlinesEmotes: PersistentMap<String, InlineTextContent> =
        remember(emotes) {
            emotes
                .mapValues { (_, emote) ->
                    emoteTextContent(
                        emote = emote,
                    )
                }.toPersistentHashMap()
        }

    val inlineBadges: PersistentMap<String, InlineTextContent> =
        remember(badges) {
            badges
                .associate { badge ->
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
            cheerEmotes
                .mapValues { (_, cheer) ->
                    cheerEmoteTextContent(
                        cheer = cheer,
                    )
                }.toPersistentHashMap()
        }

    val inlineSourceChannels: PersistentMap<String, InlineTextContent> =
        remember(sourceChannels) {
            sourceChannels
                .filterValues { user -> user.profileImageUrl.isNotBlank() }
                .entries
                .associate { (roomId, user) ->
                    Pair(
                        sourceChannelInlineContentId(roomId),
                        sourceChannelTextContent(user),
                    )
                }.toPersistentHashMap()
        }

    val inlineSourceBadges: PersistentMap<String, InlineTextContent> =
        remember(sourceChannelBadges) {
            sourceChannelBadges.entries
                .flatMap { (roomId, roomBadges) ->
                    roomBadges.map { badge ->
                        Pair(
                            badge.sourceInlineContentId(roomId),
                            badgeTextContent(badge),
                        )
                    }
                }.toMap()
                .toPersistentHashMap()
        }

    val inlineContent: PersistentMap<String, InlineTextContent> =
        remember(
            inlinesEmotes,
            inlineBadges,
            inlineCheerEmotes,
            inlineSourceChannels,
            inlineSourceBadges,
        ) {
            inlinesEmotes
                .putAll(inlineBadges)
                .putAll(inlineCheerEmotes)
                .putAll(inlineSourceChannels)
                .putAll(inlineSourceBadges)
        }

    var size by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(size) {
        listState.scrollToItem(index = 0)
    }

    val density = LocalDensity.current
    var chatEventsHeightPx by remember { mutableStateOf(0) }
    val chatEventsHeight = with(density) { chatEventsHeightPx.toDp() }

    Box {
        LazyColumn(
            modifier =
                modifier
                    .onGloballyPositioned { coordinates ->
                        val newSize = coordinates.size
                        if (size != newSize) {
                            size = newSize
                        }
                    },
            state = listState,
            reverseLayout = true,
            contentPadding =
                PaddingValues(
                    top = chatEventsHeight,
                    bottom = insets.calculateBottomPadding(),
                ),
        ) {
            item(key = "visibility_trigger") {
                // This item will become visible when the list is scrolled to the bottom;
                // it's used to trigger the visibility of the "scroll to bottom" FAB

                LaunchedEffect(Unit) {
                    onListScrolledToBottom(true)
                }

                DisposableEffect(Unit) {
                    onDispose {
                        onListScrolledToBottom(false)
                    }
                }
            }

            itemsIndexed(
                items = entries,
                // Lazy list keys must be unique; a raw hashCode() would crash the whole
                // screen on the first collision between two distinct messages.
                key = { _, item ->
                    item.body?.messageId
                        ?: "${item.timestamp.toEpochMilliseconds()}-${item.hashCode()}"
                },
                contentType = { _, item ->
                    when (item) {
                        is ChatListItem.Message.Highlighted -> 1
                        is ChatListItem.Message.Simple -> 2
                        is ChatListItem.Message.Notice -> 3
                    }
                },
            ) { index, item ->
                // Alternate the background of each chat row.
                // We want the colors to keep consistent for every message, so we alternate the
                // logic every time we add a new message to the list.
                val background: Color =
                    if ((entries.size.isOdd && index.isOdd) || (entries.size.isEven && index.isEven)) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }

                val canBeRepliedTo = item.body?.messageId != null
                val replyToActionCd = stringResource(Res.string.chat_replyTo)

                ContextualActionBox(
                    modifier = Modifier.animateItem(),
                    onSwiped = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReplyToMessage(item)
                    },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Reply,
                            contentDescription = stringResource(Res.string.chat_replyTo),
                        )
                    },
                    enabled = canBeRepliedTo,
                ) {
                    ChatMessage(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    enabled = item.body != null,
                                    onClick = { onMessageClick(item) },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onMessageLongClick(item)
                                    },
                                    onLongClickLabel = stringResource(Res.string.chat_copyToClipboard),
                                ).semantics {
                                    if (canBeRepliedTo) {
                                        customActions =
                                            listOf(
                                                CustomAccessibilityAction(replyToActionCd) {
                                                    onReplyToMessage(item)
                                                    true
                                                },
                                            )
                                    }
                                },
                        message = item,
                        inlineContent = inlineContent,
                        emotes = emotes,
                        removedContent = removedContent,
                        pronouns = pronouns,
                        richEmbed = item.body?.messageId?.let { messageId -> richEmbeds[messageId] },
                        showTimestamps = showTimestamps,
                        background = background,
                        appUser = appUser,
                    )
                }
            }
        }

        ChatEvents(
            modifier =
                Modifier
                    .padding(horizontal = 6.dp)
                    .onSizeChanged { newSize -> chatEventsHeightPx = newSize.height },
            insets = insets,
            roomState = roomState,
            isDisconnected = isDisconnected,
            ongoingEvents = ongoingEvents,
            clock = clock,
            inlineContent = inlineContent,
            removedContent = removedContent,
            appUser = appUser,
            badges = badges,
        )
    }
}

private fun TwitchBadge.sourceInlineContentId(roomId: String): String = "source_badge:$roomId:${setId}_$version"
