package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.domain.model.CheerEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.presentation.ChatEntry
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.utils.core.isOdd
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentHashMap

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    entries: ImmutableList<ChatEntry>,
    emotes: ImmutableMap<String, Emote>,
    cheerEmotes: ImmutableMap<String, CheerEmote>,
    badges: ImmutableList<TwitchBadge>,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    isDisconnected: Boolean,
    listState: LazyListState,
    onMessageLongClick: (ChatEntry) -> Unit,
    onReplyToMessage: (ChatEntry) -> Unit,
    roomState: RoomState,
    appUser: AppUser,
    insets: PaddingValues,
) {
    val inlinesEmotes: PersistentMap<String, InlineTextContent> =
        remember(emotes) {
            emotes.mapValues { (_, emote) ->
                emoteTextContent(
                    emote = emote,
                    animateEmotes = animateEmotes,
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
                    animateEmotes = animateEmotes,
                )
            }.toPersistentHashMap()
        }

    val inlineContent: PersistentMap<String, InlineTextContent> =
        remember(inlinesEmotes, inlineBadges, inlineCheerEmotes) {
            inlinesEmotes
                .putAll(inlineBadges)
                .putAll(inlineCheerEmotes)
        }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(
            bottom = insets.calculateBottomPadding(),
        ),
    ) {
        stickyHeader {
            Column(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Spacer(
                    modifier = Modifier.padding(
                        top = insets.calculateTopPadding(),
                    ),
                )
                AnimatedVisibility(
                    visible = !roomState.isDefault,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    RoomStateBanner(
                        modifier = Modifier.fillMaxWidth(),
                        roomState = roomState,
                    )
                }

                AnimatedVisibility(
                    visible = isDisconnected,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    SlimSnackbar(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(text = stringResource(R.string.connectionLost_error))
                    }
                }
            }
        }

        itemsIndexed(
            items = entries,
            key = { _, item -> item.hashCode() },
            contentType = { _, item ->
                when (item) {
                    is ChatEntry.Highlighted -> 1
                    is ChatEntry.Simple -> 2
                }
            },
        ) { index, item ->
            val background =
                if (index.isOdd) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.surface
                }

            val canBeRepliedTo = item.data?.messageId != null
            val replyToActionCd = stringResource(R.string.chat_replyTo)

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
                            onLongClickLabel = stringResource(R.string.chat_copyToClipboard),
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
                    animateEmotes = animateEmotes,
                    showTimestamps = showTimestamps,
                    background = background,
                    appUser = appUser,
                )
            }
        }
    }
}

private val TwitchBadge.inlineContentId: String
    get() = "badge_${id}_$version"