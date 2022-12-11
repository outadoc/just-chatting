package fr.outadoc.justchatting.component.chat.data.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed interface ChatCommand

sealed class Command : ChatCommand {

    data class UserNotice(
        val systemMsg: String?,
        val timestamp: Instant,
        val userMessage: ChatMessage?,
        val msgId: String?
    ) : Command()

    data class Notice(
        val message: String?,
        val timestamp: Instant,
        val messageId: String?
    ) : Command()

    data class ClearChat(
        val timestamp: Instant
    ) : Command()

    data class Timeout(
        val duration: Duration?,
        val timestamp: Instant,
        val userLogin: String?
    ) : Command()

    data class Ban(
        val timestamp: Instant,
        val userLogin: String?
    ) : Command()

    data class ClearMessage(
        val message: String?,
        val timestamp: Instant,
        val userLogin: String?
    ) : Command()

    data class Join(
        val channelLogin: String?,
        val timestamp: Instant
    ) : Command()

    data class Disconnect(
        val channelLogin: String?,
        val timestamp: Instant,
        val throwable: Throwable?
    ) : Command()

    data class SendMessageError(
        val timestamp: Instant,
        val throwable: Throwable?
    ) : Command()

    data class SocketError(
        val timestamp: Instant,
        val throwable: Throwable?
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
    val emotes: List<TwitchChatEmote>?,
    val badges: List<Badge>?,
    val isFirst: Boolean = false,
    val systemMsg: String?,
    val timestamp: Instant,
    val rewardId: String?,
    val inReplyTo: InReplyTo?,
    val msgId: String?
) : ChatCommand {

    @Immutable
    data class InReplyTo(
        val id: String,
        val userName: String,
        val message: String,
        val userId: String,
        val userLogin: String
    )
}

object PingCommand : ChatCommand

data class PointReward(
    val id: String?,
    val userId: String?,
    val userLogin: String?,
    val userName: String?,
    val message: String?,
    val rewardTitle: String?,
    val rewardCost: Int?,
    val rewardImage: RewardImage?,
    val timestamp: Instant
) : ChatCommand {

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

data class RoomStateDelta(
    val isEmoteOnly: Boolean? = null,
    val minFollowDuration: Duration? = null,
    val uniqueMessagesOnly: Boolean? = null,
    val slowModeDuration: Duration? = null,
    val isSubOnly: Boolean? = null
) : ChatCommand

data class UserState(
    val emoteSets: List<String> = emptyList()
) : ChatCommand

data class HostModeState(
    val targetChannelLogin: String?,
    val viewerCount: Int?
) : ChatCommand
