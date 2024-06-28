package fr.outadoc.justchatting.component.twitch.websocket.irc.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.Emote
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal sealed interface IrcEvent {

    sealed interface Message : IrcEvent {

        abstract val timestamp: Instant

        data class ChatMessage(
            override val timestamp: Instant,
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
            val rewardId: String?,
            val inReplyTo: InReplyTo?,
            val paidMessageInfo: PaidMessageInfo?,
        ) : Message {

            @Immutable
            data class InReplyTo(
                val id: String,
                val message: String,
                val userId: String,
                val userLogin: String,
                val userDisplayName: String,
            )

            @Immutable
            data class PaidMessageInfo(
                val amount: Long,
                val currency: String,
                val exponent: Long,
                val isSystemMessage: Boolean,
                val level: String,
            )
        }

        data class UserNotice(
            override val timestamp: Instant,
            val systemMsg: String,
            val userMessage: ChatMessage?,
            val msgId: String?,
        ) : Message

        data class IncomingRaid(
            override val timestamp: Instant,
            val userDisplayName: String,
            val raidersCount: Int,
        ) : Message

        data class CancelledRaid(
            override val timestamp: Instant,
            val userDisplayName: String,
        ) : Message

        data class Announcement(
            override val timestamp: Instant,
            val userMessage: ChatMessage,
        ) : Message

        data class Subscription(
            override val timestamp: Instant,
            val userDisplayName: String,
            val months: Int,
            val streakMonths: Int,
            val cumulativeMonths: Int,
            val subscriptionPlan: String,
            val userMessage: ChatMessage?,
        ) : Message

        data class SubscriptionConversion(
            override val timestamp: Instant,
            val userDisplayName: String,
            val subscriptionPlan: String,
            val userMessage: ChatMessage?,
        ) : Message

        data class SubscriptionGift(
            override val timestamp: Instant,
            val userDisplayName: String,
            val recipientDisplayName: String,
            val months: Int,
            val cumulativeMonths: Int,
            val subscriptionPlan: String,
        ) : Message

        data class GiftPayForward(
            override val timestamp: Instant,
            val userDisplayName: String,
            val priorGifterDisplayName: String?,
        ) : Message

        data class MassSubscriptionGift(
            override val timestamp: Instant,
            val userDisplayName: String,
            val giftCount: Int,
            val totalChannelGiftCount: Int,
            val subscriptionPlan: String,
        ) : Message

        data class HighlightedMessage(
            override val timestamp: Instant,
            val userMessage: ChatMessage,
        ) : Message

        data class Notice(
            override val timestamp: Instant,
            val message: String,
            val messageId: String?,
        ) : Message
    }

    sealed interface Command : IrcEvent {

        data object Ping : Command

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
