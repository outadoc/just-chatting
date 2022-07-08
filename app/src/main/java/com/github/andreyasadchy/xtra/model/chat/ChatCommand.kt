package com.github.andreyasadchy.xtra.model.chat

import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed interface ChatCommand

sealed class Command(
    open val message: String? = null,
    open val duration: String? = null,
    open val emotes: List<TwitchEmote>? = null,
    open val timestamp: Instant? = null
) : ChatCommand {

    data class UserNotice(
        override val message: String? = null,
        override val duration: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        val userMessage: ChatMessage? = null,
        val msgId: String? = null
    ) : Command()

    data class Notice(
        override val message: String? = null,
        override val duration: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null
    ) : Command()

    data class ClearChat(
        override val message: String? = null,
        override val duration: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null
    ) : Command()

    data class Timeout(
        override val message: String? = null,
        override val duration: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null
    ) : Command()

    data class Ban(
        override val message: String? = null,
        override val duration: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null
    ) : Command()

    data class ClearMessage(
        override val message: String? = null,
        override val duration: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null
    ) : Command()

    data class Join(
        override val message: String? = null,
        override val duration: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null
    ) : Command()

    data class Disconnect(
        override val message: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        val throwable: Throwable? = null
    ) : Command() {
        override val duration: String = throwable.toString()
    }

    data class SendMessageError(
        override val duration: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        val throwable: Throwable? = null
    ) : Command() {
        override val message: String = throwable.toString()
    }

    data class SocketError(
        override val duration: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        val throwable: Throwable? = null
    ) : Command() {
        override val message: String = throwable.toString()
    }
}

sealed interface ChatMessage : ChatCommand {
    val id: String?
    val userId: String?
    val userLogin: String?
    val userName: String?
    val message: String?
    val color: String?
    val isAction: Boolean
    val emotes: List<TwitchEmote>?
    val badges: List<Badge>?
}

data class LiveChatMessage(
    override val id: String? = null,
    override val userId: String? = null,
    override val userLogin: String? = null,
    override val userName: String? = null,
    override val message: String? = null,
    override val color: String? = null,
    override val isAction: Boolean = false,
    override val emotes: List<TwitchEmote>? = null,
    override val badges: List<Badge>? = null,
    val isFirst: Boolean = false,
    val systemMsg: String? = null,
    val timestamp: Instant? = null,
    val rewardId: String? = null
) : ChatMessage

object PingCommand : ChatCommand

data class PubSubPointReward(
    override val id: String? = null,
    override val userId: String? = null,
    override val userLogin: String? = null,
    override val userName: String? = null,
    override val message: String? = null,
    override val color: String? = null,
    override val isAction: Boolean = false,
    override val emotes: List<TwitchEmote>? = null,
    override val badges: List<Badge>? = null,
    val rewardTitle: String? = null,
    val rewardCost: Int? = null,
    val rewardImage: RewardImage? = null,
    val timestamp: Instant? = null
) : ChatMessage {

    data class RewardImage(
        val url1: String? = null,
        val url2: String? = null,
        val url4: String? = null
    ) : RemoteImage {

        private val urlForDensity: Map<Float, String?>
            get() = mapOf(
                1f to url1,
                2f to url2,
                4f to url4,
            )

        override fun getUrl(screenDensity: Float): String? {
            return urlForDensity
                .toList()
                .minByOrNull { density -> screenDensity - density.first }
                ?.second
        }
    }
}

data class RoomState(
    val isEmoteOnly: Boolean = false,
    val minFollowDuration: Duration? = null,
    val uniqueMessagesOnly: Boolean = false,
    val slowModeDuration: Duration? = null,
    val isSubOnly: Boolean = false
) : ChatCommand

data class UserState(val emoteSets: List<String>?) : ChatCommand
