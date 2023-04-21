package fr.outadoc.justchatting.component.twitch.websocket.irc.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.Emote
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed interface IrcEvent {

    sealed interface Message : IrcEvent {

        data class ChatMessage(
            val id: String?,
            val userId: String,
            val userLogin: String,
            val userName: String,
            val message: String?,
            val color: String?,
            val isAction: Boolean = false,
            val embeddedEmotes: List<Emote>?,
            val badges: List<Badge>?,
            val isFirstMessageByUser: Boolean = false,
            val timestamp: Instant,
            val rewardId: String?,
            val inReplyTo: InReplyTo?,
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
            val systemMsg: String,
            val timestamp: Instant,
            val userMessage: ChatMessage?,
            val msgId: String?,
        ) : Message

        data class IncomingRaid(
            val timestamp: Instant,
            val userDisplayName: String,
            val raidersCount: Int,
        ) : Message

        data class CancelledRaid(
            val timestamp: Instant,
            val userDisplayName: String,
        ) : Message

        data class Announcement(
            val timestamp: Instant,
            val userMessage: ChatMessage,
        ) : Message

        data class Subscription(
            val timestamp: Instant,
            val userDisplayName: String,
            val months: Int,
            val streakMonths: Int,
            val cumulativeMonths: Int,
            val subscriptionPlan: String,
            val userMessage: ChatMessage?,
        ) : Message

        data class SubscriptionConversion(
            val timestamp: Instant,
            val userDisplayName: String,
            val subscriptionPlan: String,
            val userMessage: ChatMessage?,
        ) : Message

        data class SubscriptionGift(
            val timestamp: Instant,
            val userDisplayName: String,
            val recipientDisplayName: String,
            val months: Int,
            val cumulativeMonths: Int,
            val subscriptionPlan: String,
        ) : Message

        data class GiftPayForward(
            val timestamp: Instant,
            val userDisplayName: String,
            val priorGifterDisplayName: String?,
        ) : Message

        data class MassSubscriptionGift(
            val timestamp: Instant,
            val userDisplayName: String,
            val giftCount: Int,
            val totalChannelGiftCount: Int,
            val subscriptionPlan: String,
        ) : Message

        data class HighlightedMessage(
            val timestamp: Instant,
            val userMessage: ChatMessage,
        ) : Message

        data class Notice(
            val message: String,
            val timestamp: Instant,
            val messageId: String?,
        ) : Message
    }

    sealed interface Command : IrcEvent {

        object Ping : Command

        data class RoomStateDelta(
            val isEmoteOnly: Boolean? = null,
            val minFollowDuration: Duration? = null,
            val uniqueMessagesOnly: Boolean? = null,
            val slowModeDuration: Duration? = null,
            val isSubOnly: Boolean? = null,
        ) : Command

        data class UserState(
            val emoteSets: List<String> = emptyList(),
        ) : Command

        data class ClearChat(
            val timestamp: Instant,
            val targetUserId: String?,
            val targetUserLogin: String?,
            val duration: Duration?,
        ) : Command

        data class ClearMessage(
            val timestamp: Instant,
            val targetMessage: String?,
            val targetMessageId: String?,
            val targetUserLogin: String?,
        ) : Command
    }
}
