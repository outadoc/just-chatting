package com.github.andreyasadchy.xtra.model.chat

import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed interface ChatCommand

sealed class Command(
    open val message: String? = null,
    open val duration: String? = null,
    open val type: String? = null,
    open val emotes: List<TwitchEmote>? = null,
    open val timestamp: Instant? = null,
    open val fullMsg: String? = null
) : ChatCommand {

    data class UserNotice(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
    ) : Command()

    data class Notice(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
    ) : Command()

    data class ClearChat(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
    ) : Command()

    data class Timeout(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
    ) : Command()

    data class Ban(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
    ) : Command()

    data class ClearMessage(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
    ) : Command()

    data class Join(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
    ) : Command()

    data class Disconnect(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
    ) : Command()

    data class SendMessageError(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
    ) : Command()

    data class SocketError(
        override val message: String? = null,
        override val duration: String? = null,
        override val type: String? = null,
        override val emotes: List<TwitchEmote>? = null,
        override val timestamp: Instant? = null,
        override val fullMsg: String? = null
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
    val fullMsg: String?
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
    override val fullMsg: String? = null,
    val isFirst: Boolean = false,
    val msgId: String? = null,
    val systemMsg: String? = null,
    val timestamp: Instant? = null,
    val rewardId: String? = null,
    var pointReward: PubSubPointReward? = null
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
    override val fullMsg: String? = null,
    val rewardTitle: String? = null,
    val rewardCost: Int? = null,
    private val rewardImage: RewardImage? = null,
    val timestamp: Instant? = null
) : ChatMessage {

    data class RewardImage(
        val url1: String? = null,
        val url2: String? = null,
        val url4: String? = null
    )

    private val urlForDensity: Map<Float, String?>
        get() = mapOf(
            1f to rewardImage?.url1,
            2f to rewardImage?.url2,
            4f to rewardImage?.url4,
        )

    fun getUrl(screenDensity: Float): String? {
        return urlForDensity
            .toList()
            .minByOrNull { density -> screenDensity - density.first }
            ?.second
    }
}

data class RoomState(
    val emote: Boolean = false,
    val followers: Duration? = null,
    val unique: Boolean = false,
    val slow: Duration? = null,
    val subs: Boolean = false
) : ChatCommand

data class UserState(val emoteSets: List<String>?) : ChatCommand
