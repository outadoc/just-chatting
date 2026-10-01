package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Another channel taking part in the same shared chat session as the current one.
 */
@Immutable
public data class SharedChatChannel(
    val user: User,
    val isHost: Boolean,
)

/**
 * The other channels in the current shared chat session, or an empty list if there is none.
 *
 * Channels whose details haven't been loaded yet are left out.
 */
public val ChatViewModel.State.Chatting.sharedChatChannels: ImmutableList<SharedChatChannel>
    get() {
        val session = sharedChatSession ?: return persistentListOf()
        return session.participantChannelIds
            .filter { channelId -> channelId != user.id }
            .mapNotNull { channelId ->
                sourceChannels[channelId]?.let { channel ->
                    SharedChatChannel(
                        user = channel,
                        isHost = channelId == session.hostChannelId,
                    )
                }
            }.toImmutableList()
    }
