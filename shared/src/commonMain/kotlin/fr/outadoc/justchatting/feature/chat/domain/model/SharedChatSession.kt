package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * A shared chat session, in which several channels share a single chat room.
 *
 * @property participantChannelIds all channels in the session, including the host.
 */
@Immutable
public data class SharedChatSession(
    val hostChannelId: String,
    val participantChannelIds: ImmutableList<String>,
)
