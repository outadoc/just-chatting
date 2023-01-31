package fr.outadoc.justchatting.component.twitch.websocket.irc.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.Emote
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed interface IrcEvent

sealed class Command : IrcEvent {

    data class UserNotice(
        val systemMsg: String?,
        val timestamp: Instant,
        val userMessage: ChatMessage?,
        val msgId: String?,
    ) : Command()

    data class Notice(
        val message: String?,
        val timestamp: Instant,
        val messageId: String?,
    ) : Command()

    data class ClearChat(
        val timestamp: Instant,
    ) : Command()

    data class Timeout(
        val duration: Duration?,
        val timestamp: Instant,
        val userLogin: String?,
    ) : Command()

    data class Ban(
        val timestamp: Instant,
        val userLogin: String?,
    ) : Command()

    data class ClearMessage(
        val message: String?,
        val timestamp: Instant,
        val userLogin: String?,
    ) : Command()

    data class SendMessageError(
        val timestamp: Instant,
        val throwable: Throwable?,
    ) : Command()
}

data class ChatMessage(
    val id: String?,
    val userId: String?,
    val userLogin: String,
    val userName: String,
    val message: String?,
    val color: String?,
    val isAction: Boolean = false,
    val embeddedEmotes: List<Emote>?,
    val badges: List<Badge>?,
    val isFirst: Boolean = false,
    val systemMsg: String?,
    val timestamp: Instant,
    val rewardId: String?,
    val inReplyTo: InReplyTo?,
    val msgId: String?,
) : IrcEvent {

    @Immutable
    data class InReplyTo(
        val id: String,
        val userName: String,
        val message: String,
        val userId: String,
        val userLogin: String,
    )
}

object PingCommand : IrcEvent

data class RoomStateDelta(
    val isEmoteOnly: Boolean? = null,
    val minFollowDuration: Duration? = null,
    val uniqueMessagesOnly: Boolean? = null,
    val slowModeDuration: Duration? = null,
    val isSubOnly: Boolean? = null,
) : IrcEvent

data class UserState(
    val emoteSets: List<String> = emptyList(),
) : IrcEvent

data class HostModeState(
    val targetChannelLogin: String?,
    val viewerCount: Int?,
) : IrcEvent
