package com.github.andreyasadchy.xtra.model.chat

import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed interface ChatCommand

sealed class Command : ChatCommand {

    data class UserNotice(
        val systemMsg: String?,
        val timestamp: Instant?,
        val userMessage: ChatMessage?,
        val msgId: String?
    ) : Command()

    data class Notice(
        val message: String?,
        val timestamp: Instant?,
        val messageId: String?
    ) : Command()

    data class ClearChat(
        val timestamp: Instant?
    ) : Command()

    data class Timeout(
        val duration: Duration?,
        val timestamp: Instant?,
        val userLogin: String?
    ) : Command()

    data class Ban(
        val timestamp: Instant?,
        val userLogin: String?
    ) : Command()

    data class ClearMessage(
        val message: String?,
        val timestamp: Instant?,
        val userLogin: String?
    ) : Command()

    data class Join(
        val channelName: String?,
        val timestamp: Instant?
    ) : Command()

    data class Disconnect(
        val channelName: String?,
        val timestamp: Instant?,
        val throwable: Throwable?
    ) : Command()

    data class SendMessageError(
        val timestamp: Instant?,
        val throwable: Throwable?
    ) : Command()

    data class SocketError(
        val timestamp: Instant?,
        val throwable: Throwable?
    ) : Command()
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
    override val id: String?,
    override val userId: String?,
    override val userLogin: String?,
    override val userName: String?,
    override val message: String?,
    override val color: String?,
    override val isAction: Boolean = false,
    override val emotes: List<TwitchEmote>?,
    override val badges: List<Badge>?,
    val isFirst: Boolean = false,
    val systemMsg: String?,
    val timestamp: Instant?,
    val rewardId: String?,
    val inReplyTo: InReplyTo?,
    val msgId: String?
) : ChatMessage {
    data class InReplyTo(
        val id: String,
        val userName: String,
        val message: String,
        val userId: String,
        val userLogin: String
    )
}

object PingCommand : ChatCommand

data class PubSubPointReward(
    override val id: String?,
    override val userId: String?,
    override val userLogin: String?,
    override val userName: String?,
    override val message: String?,
    override val color: String?,
    override val isAction: Boolean = false,
    override val emotes: List<TwitchEmote>?,
    override val badges: List<Badge>?,
    val rewardTitle: String?,
    val rewardCost: Int?,
    val rewardImage: RewardImage?,
    val timestamp: Instant?
) : ChatMessage {

    data class RewardImage(
        val url1: String?,
        val url2: String?,
        val url4: String?
    ) : RemoteImage {

        private val urlForDensity: Map<Float, String?>
            get() = mapOf(
                1f to url1,
                2f to url2,
                4f to url4
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
