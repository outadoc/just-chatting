package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat

import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * Chat notifications: subs, gifts, raids, announcements, etc. Requires the `user:read:chat` scope.
 *
 * Replaces IRC's USERNOTICE.
 */
internal class EventSubChatNotificationPlugin(
    private val json: Json,
) : EventSubPlugin {
    private companion object {
        const val SHARED_CHAT_PREFIX = "shared_chat_"
        const val PLAN_PRIME = "Prime"
        const val ANONYMOUS_DISPLAY_NAME = "AnAnonymousGifter"
    }

    override val subscriptionType = "channel.chat.notification"
    override val subscriptionVersion = "1"

    override fun getCondition(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Map<String, String> =
        mapOf(
            "broadcaster_user_id" to channelId,
            "user_id" to appUser.userId,
        )

    override fun parseEvent(
        event: JsonObject,
        timestamp: Instant,
    ): List<ChatEvent> {
        val notification = json.decodeFromJsonElement(Event.serializer(), event)

        // In a shared chat session, notices from other channels are prefixed with "shared_chat_",
        // and their details are stored under that prefixed name.
        val details: JsonObject? = event[notification.noticeType] as? JsonObject
        val noticeType = notification.noticeType.removePrefix(SHARED_CHAT_PREFIX)

        val userDisplayName =
            notification.chatterUserName
                ?.takeUnless { notification.chatterIsAnonymous }
                ?: ANONYMOUS_DISPLAY_NAME

        val userMessage =
            notification.chatterUserId?.let { userId ->
                notification.message.toChatMessage(
                    timestamp = timestamp,
                    messageId = notification.messageId,
                    userId = userId,
                    userLogin = notification.chatterUserLogin ?: userDisplayName,
                    userName = userDisplayName,
                    color = notification.color,
                    badges = notification.badges,
                    sourceBadges = notification.sourceBadges,
                    sourceRoomId = notification.sourceBroadcasterUserId,
                )
            }

        val fallback =
            ChatEvent.Message.UserNotice(
                timestamp = timestamp,
                systemMsg = notification.systemMessage,
                userMessage = userMessage,
                msgId = noticeType,
            )

        fun <T> decode(serializer: KSerializer<T>): T? = details?.let { json.decodeFromJsonElement(serializer, it) }

        val parsed: ChatEvent.Message? =
            when (noticeType) {
                "sub" -> {
                    decode(Sub.serializer())?.let { sub ->
                        ChatEvent.Message.Subscription(
                            timestamp = timestamp,
                            userDisplayName = userDisplayName,
                            months = sub.durationMonths.coerceAtLeast(1),
                            streakMonths = 0,
                            cumulativeMonths = 1,
                            subscriptionPlan = if (sub.isPrime) PLAN_PRIME else sub.subTier,
                            userMessage = userMessage,
                        )
                    }
                }

                "resub" -> {
                    decode(Resub.serializer())?.let { resub ->
                        ChatEvent.Message.Subscription(
                            timestamp = timestamp,
                            userDisplayName = userDisplayName,
                            months = resub.durationMonths.coerceAtLeast(1),
                            streakMonths = resub.streakMonths ?: 0,
                            cumulativeMonths = resub.cumulativeMonths,
                            subscriptionPlan = if (resub.isPrime) PLAN_PRIME else resub.subTier,
                            userMessage = userMessage,
                        )
                    }
                }

                "sub_gift" -> {
                    decode(SubGift.serializer())?.let { gift ->
                        ChatEvent.Message.SubscriptionGift(
                            timestamp = timestamp,
                            userDisplayName = userDisplayName,
                            recipientDisplayName = gift.recipientUserName,
                            months = gift.durationMonths.coerceAtLeast(1),
                            cumulativeMonths = null,
                            subscriptionPlan = gift.subTier,
                        )
                    }
                }

                "community_sub_gift" -> {
                    decode(CommunitySubGift.serializer())?.let { gift ->
                        ChatEvent.Message.MassSubscriptionGift(
                            timestamp = timestamp,
                            userDisplayName = userDisplayName,
                            giftCount = gift.total,
                            totalChannelGiftCount = gift.cumulativeTotal,
                            subscriptionPlan = gift.subTier,
                        )
                    }
                }

                "prime_paid_upgrade" -> {
                    decode(PrimePaidUpgrade.serializer())?.let { upgrade ->
                        ChatEvent.Message.SubscriptionConversion(
                            timestamp = timestamp,
                            userDisplayName = userDisplayName,
                            subscriptionPlan = upgrade.subTier,
                            userMessage = userMessage,
                        )
                    }
                }

                "pay_it_forward" -> {
                    decode(PayItForward.serializer())?.let { payForward ->
                        ChatEvent.Message.GiftPayForward(
                            timestamp = timestamp,
                            userDisplayName = userDisplayName,
                            priorGifterDisplayName =
                                payForward.gifterUserName
                                    ?.takeUnless { payForward.gifterIsAnonymous },
                        )
                    }
                }

                "raid" -> {
                    decode(Raid.serializer())?.let { raid ->
                        ChatEvent.Message.IncomingRaid(
                            timestamp = timestamp,
                            userDisplayName = raid.userName,
                            raidersCount = raid.viewerCount,
                        )
                    }
                }

                "unraid" -> {
                    ChatEvent.Message.CancelledRaid(
                        timestamp = timestamp,
                        userDisplayName = userDisplayName,
                    )
                }

                "announcement" -> {
                    userMessage?.let {
                        ChatEvent.Message.Announcement(
                            timestamp = timestamp,
                            userMessage = userMessage,
                        )
                    }
                }

                "watch_streak" -> {
                    decode(WatchStreak.serializer())?.let { streak ->
                        ChatEvent.Message.WatchStreak(
                            timestamp = timestamp,
                            userDisplayName = userDisplayName,
                            streakLength = streak.streakCount,
                            userMessage = userMessage,
                        )
                    }
                }

                "modiversary" -> {
                    decode(Modiversary.serializer())?.let { modiversary ->
                        ChatEvent.Message.ModeratorAnniversary(
                            timestamp = timestamp,
                            userDisplayName = userDisplayName,
                            months = modiversary.months,
                        )
                    }
                }

                else -> {
                    null
                }
            }

        return listOf(parsed ?: fallback)
    }

    @Serializable
    private data class Event(
        @SerialName("chatter_user_id")
        val chatterUserId: String? = null,
        @SerialName("chatter_user_login")
        val chatterUserLogin: String? = null,
        @SerialName("chatter_user_name")
        val chatterUserName: String? = null,
        @SerialName("chatter_is_anonymous")
        val chatterIsAnonymous: Boolean = false,
        @SerialName("color")
        val color: String? = null,
        @SerialName("badges")
        val badges: List<EventSubChatBadge> = emptyList(),
        @SerialName("system_message")
        val systemMessage: String,
        @SerialName("message_id")
        val messageId: String,
        @SerialName("message")
        val message: EventSubChatMessageBody,
        @SerialName("notice_type")
        val noticeType: String,
        @SerialName("source_broadcaster_user_id")
        val sourceBroadcasterUserId: String? = null,
        @SerialName("source_badges")
        val sourceBadges: List<EventSubChatBadge>? = null,
    )

    @Serializable
    private data class Sub(
        @SerialName("sub_tier")
        val subTier: String,
        @SerialName("is_prime")
        val isPrime: Boolean = false,
        @SerialName("duration_months")
        val durationMonths: Int = 1,
    )

    @Serializable
    private data class Resub(
        @SerialName("cumulative_months")
        val cumulativeMonths: Int,
        @SerialName("duration_months")
        val durationMonths: Int = 1,
        @SerialName("streak_months")
        val streakMonths: Int? = null,
        @SerialName("sub_tier")
        val subTier: String,
        @SerialName("is_prime")
        val isPrime: Boolean = false,
    )

    @Serializable
    private data class SubGift(
        @SerialName("duration_months")
        val durationMonths: Int = 1,
        @SerialName("recipient_user_name")
        val recipientUserName: String,
        @SerialName("sub_tier")
        val subTier: String,
    )

    @Serializable
    private data class CommunitySubGift(
        @SerialName("total")
        val total: Int,
        @SerialName("sub_tier")
        val subTier: String,
        @SerialName("cumulative_total")
        val cumulativeTotal: Int? = null,
    )

    @Serializable
    private data class PrimePaidUpgrade(
        @SerialName("sub_tier")
        val subTier: String,
    )

    @Serializable
    private data class PayItForward(
        @SerialName("gifter_is_anonymous")
        val gifterIsAnonymous: Boolean = false,
        @SerialName("gifter_user_name")
        val gifterUserName: String? = null,
    )

    @Serializable
    private data class Raid(
        @SerialName("user_name")
        val userName: String,
        @SerialName("viewer_count")
        val viewerCount: Int,
    )

    @Serializable
    private data class WatchStreak(
        @SerialName("streak_count")
        val streakCount: Int,
    )

    @Serializable
    private data class Modiversary(
        @SerialName("months")
        val months: Int,
    )
}
