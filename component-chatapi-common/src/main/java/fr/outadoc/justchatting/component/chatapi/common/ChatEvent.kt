package fr.outadoc.justchatting.component.chatapi.common

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Immutable
sealed class ChatEvent {

    sealed class Message : ChatEvent() {
        abstract val data: Data?
        abstract val timestamp: Instant
    }

    @Immutable
    data class Data(
        val userId: String?,
        val userName: String,
        val userLogin: String,
        val isAction: Boolean,
        val message: String?,
        val messageId: String?,
        val color: String?,
        val embeddedEmotes: ImmutableList<Emote>?,
        val badges: ImmutableList<Badge>?,
        val inReplyTo: InReplyTo?,
    ) {
        @Immutable
        data class InReplyTo(
            val id: String,
            val userName: String,
            val message: String,
            val userId: String,
            val userLogin: String,
        )
    }

    @Immutable
    data class Simple(
        override val data: Data,
        override val timestamp: Instant,
    ) : Message()

    @Immutable
    data class Highlighted(
        val header: String?,
        val headerIconResId: Int? = null,
        override val data: Data?,
        override val timestamp: Instant,
    ) : Message()

    @Immutable
    data class RoomStateDelta(
        val isEmoteOnly: Boolean? = null,
        val minFollowDuration: Duration? = null,
        val uniqueMessagesOnly: Boolean? = null,
        val slowModeDuration: Duration? = null,
        val isSubOnly: Boolean? = null,
    ) : ChatEvent()

    @Immutable
    data class UserState(
        val emoteSets: ImmutableList<String> = persistentListOf(),
    ) : ChatEvent()

    @Immutable
    data class HostModeState(
        val targetChannelLogin: String?,
        val viewerCount: Int?,
    ) : ChatEvent()
}