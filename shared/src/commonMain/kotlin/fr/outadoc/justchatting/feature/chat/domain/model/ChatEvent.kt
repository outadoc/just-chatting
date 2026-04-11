package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import kotlin.time.Duration
import kotlin.time.Instant

public sealed interface ChatEvent {
    public sealed interface Message : ChatEvent {
        public val timestamp: Instant

        public data class ChatMessage(
            override val timestamp: Instant,
            val id: String?,
            val userId: String,
            val userLogin: String,
            val userName: String,
            val message: String?,
            val color: String?,
            val isAction: Boolean = false,
            val embeddedEmotes: List<Emote>,
            val badges: List<Badge>?,
            val isFirstMessageByUser: Boolean = false,
            val rewardId: String?,
            val inReplyTo: InReplyTo?,
        ) : Message {
            @Immutable
            public data class InReplyTo(
                val id: String,
                val message: String,
                val userId: String,
                val userLogin: String,
                val userDisplayName: String,
            )
        }

        public data class UserNotice(
            override val timestamp: Instant,
            val systemMsg: String,
            val userMessage: ChatMessage?,
            val msgId: String?,
        ) : Message

        public data class IncomingRaid(
            override val timestamp: Instant,
            val userDisplayName: String,
            val raidersCount: Int,
        ) : Message

        public data class CancelledRaid(
            override val timestamp: Instant,
            val userDisplayName: String,
        ) : Message

        public data class Announcement(
            override val timestamp: Instant,
            val userMessage: ChatMessage,
        ) : Message

        public data class Subscription(
            override val timestamp: Instant,
            val userDisplayName: String,
            val months: Int,
            val streakMonths: Int,
            val cumulativeMonths: Int,
            val subscriptionPlan: String,
            val userMessage: ChatMessage?,
        ) : Message

        public data class SubscriptionConversion(
            override val timestamp: Instant,
            val userDisplayName: String,
            val subscriptionPlan: String,
            val userMessage: ChatMessage?,
        ) : Message

        public data class SubscriptionGift(
            override val timestamp: Instant,
            val userDisplayName: String,
            val recipientDisplayName: String,
            val months: Int,
            val cumulativeMonths: Int,
            val subscriptionPlan: String,
        ) : Message

        public data class GiftPayForward(
            override val timestamp: Instant,
            val userDisplayName: String,
            val priorGifterDisplayName: String?,
        ) : Message

        public data class MassSubscriptionGift(
            override val timestamp: Instant,
            val userDisplayName: String,
            val giftCount: Int,
            val totalChannelGiftCount: Int,
            val subscriptionPlan: String,
        ) : Message

        public data class HighlightedMessage(
            override val timestamp: Instant,
            val userMessage: ChatMessage,
        ) : Message

        public data class Notice(
            override val timestamp: Instant,
            val message: String,
            val messageId: String?,
        ) : Message

        public data class Join(
            override val timestamp: Instant,
            val channelLogin: String,
        ) : Message

        public data class SendError(
            override val timestamp: Instant,
        ) : Message

        public data class PollUpdate(
            override val timestamp: Instant,
            val poll: Poll,
        ) : Message

        public data class BroadcastSettingsUpdate(
            override val timestamp: Instant,
            val streamTitle: String,
            val categoryId: String,
            val categoryName: String,
        ) : Message

        public data class ViewerCountUpdate(
            override val timestamp: Instant,
            val viewerCount: Long,
        ) : Message

        public data class PredictionUpdate(
            override val timestamp: Instant,
            val prediction: Prediction,
        ) : Message

        public data class RaidUpdate(
            override val timestamp: Instant,
            val raid: Raid?,
        ) : Message

        public data class PinnedMessageUpdate(
            override val timestamp: Instant,
            val pinnedMessage: PinnedMessage?,
        ) : Message

        public data class RedemptionUpdate(
            override val timestamp: Instant,
            val redemption: Redemption,
        ) : Message

        public data class RichEmbed(
            override val timestamp: Instant,
            val messageId: String,
            val title: String,
            val requestUrl: String,
            val thumbnailUrl: String,
            val authorName: String,
            val channelName: String?,
        ) : Message
    }

    public sealed interface Command : ChatEvent {
        public data object Ping : Command

        public data class RoomStateDelta(
            val isEmoteOnly: Boolean? = null,
            val minFollowDuration: Duration? = null,
            val uniqueMessagesOnly: Boolean? = null,
            val slowModeDuration: Duration? = null,
            val isSubOnly: Boolean? = null,
        ) : Command

        public data class UserState(
            val emoteSets: List<String> = emptyList(),
        ) : Command

        public data class ClearChat(
            val timestamp: Instant,
            val targetUserId: String?,
            val targetUserLogin: String?,
            val duration: Duration?,
        ) : Command

        public data class ClearMessage(
            val timestamp: Instant,
            val targetMessage: String?,
            val targetMessageId: String?,
            val targetUserLogin: String?,
        ) : Command
    }
}
