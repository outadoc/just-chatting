package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.utils.core.isOdd
import fr.outadoc.justchatting.utils.core.roundUpOddToEven
import fr.outadoc.justchatting.utils.logging.logDebug
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlin.time.Instant

internal class ChatStateReducer {

    fun reduce(action: ChatViewModel.Action, state: ChatViewModel.State): ChatViewModel.State {
        logDebug<ChatStateReducer> { "reduce: $action" }
        return when (action) {
            is ChatViewModel.Action.AddMessages -> action.reduce(state)
            is ChatViewModel.Action.ChangeConnectionStatus -> action.reduce(state)
            is ChatViewModel.Action.ChangeRecentEmotes -> action.reduce(state)
            is ChatViewModel.Action.ChangeRoomState -> action.reduce(state)
            is ChatViewModel.Action.ChangeUserState -> action.reduce(state)
            is ChatViewModel.Action.RemoveContent -> action.reduce(state)
            is ChatViewModel.Action.UpdatePoll -> action.reduce(state)
            is ChatViewModel.Action.UpdatePrediction -> action.reduce(state)
            is ChatViewModel.Action.UpdateStreamMetadata -> action.reduce(state)
            is ChatViewModel.Action.UpdateChatterPronouns -> action.reduce(state)
            is ChatViewModel.Action.AddRichEmbed -> action.reduce(state)
            is ChatViewModel.Action.LoadChat -> action.reduce(state)
            is ChatViewModel.Action.UpdateEmotes -> action.reduce(state)
            is ChatViewModel.Action.UpdateStreamDetails -> action.reduce(state)
            is ChatViewModel.Action.UpdateRaidAnnouncement -> action.reduce(state)
            is ChatViewModel.Action.UpdatePinnedMessage -> action.reduce(state)
            is ChatViewModel.Action.ShowUserInfo -> action.reduce(state)
            is ChatViewModel.Action.UpdateStreamInfoVisibility -> action.reduce(state)
            is ChatViewModel.Action.UpdateUser -> action.reduce(state)
        }
    }

    fun reduce(action: ChatViewModel.InputAction, state: ChatViewModel.InputState): ChatViewModel.InputState = when (action) {
        is ChatViewModel.InputAction.AppendChatter -> action.reduce(state)
        is ChatViewModel.InputAction.AppendEmote -> action.reduce(state)
        is ChatViewModel.InputAction.ChangeMessageInput -> action.reduce(state)
        is ChatViewModel.InputAction.ReplyToMessage -> action.reduce(state)
        is ChatViewModel.InputAction.ClearAfterSubmit -> action.reduce(state)
        is ChatViewModel.InputAction.UpdateAutoCompleteItems -> action.reduce(state)
        is ChatViewModel.InputAction.ReplaceInputWithLastSentMessage -> action.reduce(state)
    }

    // Action reducers

    private fun ChatViewModel.Action.LoadChat.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state is ChatViewModel.State.Chatting && state.user.id == userId) return state
        return ChatViewModel.State.Loading(
            userId = userId,
            appUser = appUser,
            maxAdapterCount = maxAdapterCount,
        )
    }

    private fun ChatViewModel.Action.UpdateStreamDetails.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(stream = stream)
    }

    private fun ChatViewModel.Action.UpdateEmotes.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            cheerEmotes = cheerEmotes ?: state.cheerEmotes,
            pickableEmotes = pickableEmotes,
            channelBadges = channelBadges ?: state.channelBadges,
            globalBadges = globalBadges ?: state.globalBadges,
        )
    }

    private fun ChatViewModel.Action.AddMessages.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state

        val lastSentMessageInstant: Instant? =
            messages
                .lastOrNull { message ->
                    message.body != null && message.body?.chatter?.id == state.appUser.userId
                }?.timestamp

        val newChatters: PersistentSet<Chatter> =
            messages
                .asSequence()
                .mapNotNull { message -> message.body?.chatter }
                .toPersistentSet()

        val newMessages: PersistentList<ChatListItem> =
            state.chatMessages
                .addAll(
                    index = 0,
                    messages.asReversed(),
                ).distinct()
                .toPersistentList()

        val maxCount =
            state.maxAdapterCount.roundUpOddToEven() + if (newMessages.size.isOdd) 1 else 0

        return state.copy(
            chatMessages =
            newMessages
                .filterIsInstance<ChatListItem.Message>()
                .take(maxCount)
                .toPersistentList(),
            lastSentMessageInstant =
            lastSentMessageInstant
                ?: state.lastSentMessageInstant,
            chatters = state.chatters.addAll(newChatters),
        )
    }

    private fun ChatViewModel.Action.ChangeConnectionStatus.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(connectionStatus = connectionStatus)
    }

    private fun ChatViewModel.Action.ChangeUserState.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(userState = userState)
    }

    private fun ChatViewModel.Action.ChangeRoomState.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            roomState =
            RoomState(
                isEmoteOnly = delta.isEmoteOnly ?: state.roomState.isEmoteOnly,
                isSubOnly = delta.isSubOnly ?: state.roomState.isSubOnly,
                minFollowDuration = delta.minFollowDuration
                    ?: state.roomState.minFollowDuration,
                uniqueMessagesOnly = delta.uniqueMessagesOnly
                    ?: state.roomState.uniqueMessagesOnly,
                slowModeDuration = delta.slowModeDuration ?: state.roomState.slowModeDuration,
            ),
        )
    }

    private fun ChatViewModel.Action.RemoveContent.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            removedContent = state.removedContent.add(removedContent),
        )
    }

    private fun ChatViewModel.Action.ChangeRecentEmotes.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(recentEmotes = recentEmotes)
    }

    private fun ChatViewModel.Action.UpdatePoll.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            ongoingEvents =
            state.ongoingEvents.copy(
                poll = poll,
            ),
        )
    }

    private fun ChatViewModel.Action.UpdatePrediction.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            ongoingEvents =
            state.ongoingEvents.copy(
                prediction = prediction,
            ),
        )
    }

    private fun ChatViewModel.Action.UpdateStreamMetadata.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            stream =
            state.stream?.copy(
                title = streamTitle ?: state.stream.title,
                category = streamCategory ?: state.stream.category,
                viewerCount = viewerCount ?: state.stream.viewerCount,
            ),
        )
    }

    private fun ChatViewModel.Action.AddRichEmbed.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            richEmbeds =
            state.richEmbeds.put(
                key = richEmbed.messageId,
                value = richEmbed,
            ),
        )
    }

    private fun ChatViewModel.Action.UpdateChatterPronouns.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            pronouns = state.pronouns.putAll(pronouns),
        )
    }

    private fun ChatViewModel.Action.UpdatePinnedMessage.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state

        if (pinnedMessage == null) {
            return state.copy(
                ongoingEvents =
                state.ongoingEvents.copy(
                    pinnedMessage = null,
                ),
            )
        }

        val matchingMessage: ChatListItem.Message =
            state.chatMessages.findLast { message ->
                message.body?.messageId == pinnedMessage.message.messageId
            } ?: return state.copy(
                ongoingEvents =
                state.ongoingEvents.copy(
                    pinnedMessage = null,
                ),
            )

        return state.copy(
            ongoingEvents =
            state.ongoingEvents.copy(
                pinnedMessage =
                OngoingEvents.PinnedMessage(
                    message = matchingMessage,
                    endsAt = pinnedMessage.message.endsAt,
                ),
            ),
        )
    }

    private fun ChatViewModel.Action.UpdateRaidAnnouncement.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            ongoingEvents =
            state.ongoingEvents.copy(
                outgoingRaid = raid,
            ),
        )
    }

    private fun ChatViewModel.Action.ShowUserInfo.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            showInfoForUserId = userId,
        )
    }

    private fun ChatViewModel.Action.UpdateStreamInfoVisibility.reduce(state: ChatViewModel.State): ChatViewModel.State {
        if (state !is ChatViewModel.State.Chatting) return state
        return state.copy(
            isStreamInfoVisible = isVisible,
        )
    }

    private fun ChatViewModel.Action.UpdateUser.reduce(state: ChatViewModel.State): ChatViewModel.State = when (state) {
        is ChatViewModel.State.Initial,
        is ChatViewModel.State.Failed,
        -> {
            state
        }

        is ChatViewModel.State.Loading -> {
            ChatViewModel.State.Chatting(
                user = user,
                appUser = state.appUser,
                maxAdapterCount = state.maxAdapterCount,
                chatters =
                persistentSetOf(
                    Chatter(
                        id = user.id,
                        login = user.login,
                        displayName = user.displayName,
                    ),
                ),
            )
        }

        is ChatViewModel.State.Chatting -> {
            state.copy(
                user = user,
                chatters =
                state.chatters.add(
                    Chatter(
                        id = user.id,
                        login = user.login,
                        displayName = user.displayName,
                    ),
                ),
            )
        }
    }

    // InputAction reducers

    private fun ChatViewModel.InputAction.ClearAfterSubmit.reduce(inputState: ChatViewModel.InputState): ChatViewModel.InputState = inputState.copy(
        message = "",
        lastSentMessage = sentMessage,
        selectionRange = 0..0,
        replyingTo = null,
    )

    private fun ChatViewModel.InputAction.ChangeMessageInput.reduce(inputState: ChatViewModel.InputState): ChatViewModel.InputState = inputState.copy(
        message = message,
        selectionRange = selectionRange,
    )

    private fun ChatViewModel.InputAction.AppendEmote.reduce(inputState: ChatViewModel.InputState): ChatViewModel.InputState = appendTextToInput(
        inputState = inputState,
        text = emote.name,
        replaceLastWord = autocomplete,
    )

    private fun ChatViewModel.InputAction.AppendChatter.reduce(inputState: ChatViewModel.InputState): ChatViewModel.InputState = appendTextToInput(
        inputState = inputState,
        text = "${ChatPrefixConstants.ChatterPrefix}${chatter.displayName}",
        replaceLastWord = autocomplete,
    )

    private fun ChatViewModel.InputAction.ReplyToMessage.reduce(inputState: ChatViewModel.InputState): ChatViewModel.InputState = inputState.copy(replyingTo = chatListItem)

    private fun ChatViewModel.InputAction.UpdateAutoCompleteItems.reduce(inputState: ChatViewModel.InputState): ChatViewModel.InputState = inputState.copy(autoCompleteItems = items)

    @Suppress("UnusedReceiverParameter")
    private fun ChatViewModel.InputAction.ReplaceInputWithLastSentMessage.reduce(inputState: ChatViewModel.InputState): ChatViewModel.InputState {
        if (inputState.message.isNotEmpty()) return inputState
        val newMessage = inputState.lastSentMessage ?: return inputState

        return inputState.copy(
            message = newMessage,
            lastSentMessage = newMessage,
            selectionRange =
            IntRange(
                start = newMessage.length,
                endInclusive = newMessage.length,
            ),
        )
    }

    private fun appendTextToInput(
        inputState: ChatViewModel.InputState,
        text: String,
        replaceLastWord: Boolean,
    ): ChatViewModel.InputState {
        val previousWord =
            inputState.message
                .substring(startIndex = 0, endIndex = inputState.selectionRange.first)
                .takeLastWhile { it != ' ' }

        val textBefore =
            inputState.message
                .substring(startIndex = 0, endIndex = inputState.selectionRange.first)
                .removeSuffix(
                    if (replaceLastWord) previousWord else "",
                )

        val textAfter =
            inputState.message
                .substring(inputState.selectionRange.last)

        return inputState.copy(
            message = "${textBefore}$text $textAfter",
            selectionRange =
            IntRange(
                start = textBefore.length + text.length + 1,
                endInclusive = textBefore.length + text.length + 1,
            ),
        )
    }
}
