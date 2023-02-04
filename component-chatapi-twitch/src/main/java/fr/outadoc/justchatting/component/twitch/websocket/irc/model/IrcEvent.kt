package fr.outadoc.justchatting.component.twitch.websocket.irc.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.Emote
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed interface IrcEvent

sealed interface Message : IrcEvent {

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
    ) : Message {

        @Immutable
        data class InReplyTo(
            val id: String,
            val userName: String,
            val message: String,
            val userId: String,
            val userLogin: String,
        )
    }

    data class UserNotice(
        val systemMsg: String?,
        val timestamp: Instant,
        val userMessage: ChatMessage?,
        val msgId: String?,
    ) : Message

    data class Notice(
        val message: String?,
        val timestamp: Instant,
        val messageId: String?,
    ) : Message
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

data class ClearChat(
    val timestamp: Instant,
    val targetUserId: String?,
    val targetUserLogin: String?,
    val duration: Duration?,
) : IrcEvent

data class ClearMessage(
    val timestamp: Instant,
    val targetMessage: String?,
    val targetMessageId: String?,
    val targetUserLogin: String?,
) : IrcEvent
