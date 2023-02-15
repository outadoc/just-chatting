package fr.outadoc.justchatting.component.chatapi.common

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Immutable
sealed interface ChatEvent {

    sealed class Message : ChatEvent {

        abstract val body: Body?
        abstract val timestamp: Instant

        @Immutable
        data class Simple(
            override val body: Body,
            override val timestamp: Instant,
        ) : Message()

        @Immutable
        data class Highlighted(
            val header: String?,
            val headerIconResId: Int? = null,
            override val body: Body?,
            override val timestamp: Instant,
        ) : Message()

        @Immutable
        data class Body(
            val messageId: String?,
            val message: String?,
            val userId: String?,
            val userName: String,
            val userLogin: String,
            val isAction: Boolean = false,
            val color: String? = null,
            val embeddedEmotes: ImmutableList<Emote> = persistentListOf(),
            val badges: ImmutableList<Badge> = persistentListOf(),
            val inReplyTo: InReplyTo? = null,
        ) {
            @Immutable
            data class InReplyTo(
                val id: String,
                val message: String,
                val userId: String,
                val userLogin: String,
                val userName: String,
            )
        }
    }

    @Immutable
    data class RoomStateDelta(
        val isEmoteOnly: Boolean? = null,
        val minFollowDuration: Duration? = null,
        val uniqueMessagesOnly: Boolean? = null,
        val slowModeDuration: Duration? = null,
        val isSubOnly: Boolean? = null,
    ) : ChatEvent

    @Immutable
    data class UserState(
        val emoteSets: ImmutableList<String> = persistentListOf(),
    ) : ChatEvent

    @Immutable
    data class RemoveContent(
        val upUntil: Instant,
        val matchingUserId: String? = null,
        val matchingMessageId: String? = null,
    ) : ChatEvent

    @Immutable
    data class PollUpdate(
        val poll: Poll,
    ) : ChatEvent

    @Immutable
    data class BroadcastSettingsUpdate(
        val streamTitle: String,
        val gameName: String,
    ) : ChatEvent

    @Immutable
    data class ViewerCountUpdate(
        val viewerCount: Int,
    ) : ChatEvent

    @Immutable
    data class PredictionUpdate(
        val prediction: Prediction,
    ) : ChatEvent

    @Immutable
    data class RichEmbed(
        val messageId: String,
        val title: String,
        val requestUrl: String,
        val thumbnailUrl: String,
        val authorName: String,
        val channelName: String?,
    ) : ChatEvent
}
