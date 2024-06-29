package fr.outadoc.justchatting.component.chatapi.common

import androidx.compose.runtime.Immutable
import dev.icerock.moko.resources.desc.StringDesc
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Immutable
internal sealed interface ChatEvent {

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
            override val timestamp: Instant,
            override val body: Body?,
            val metadata: Metadata,
        ) : Message() {

            data class Metadata(
                val title: StringDesc,
                val titleIcon: Icon? = null,
                val subtitle: StringDesc?,
                val level: Level = Level.Base,
            )

            @Immutable
            enum class Level {
                Base,
                One,
                Two,
                Three,
                Four,
                Five,
                Six,
                Seven,
                Eight,
                Nine,
                Ten,
            }
        }

        @Immutable
        data class Notice(
            override val timestamp: Instant,
            val text: StringDesc,
        ) : Message() {
            override val body: Body? = null
        }

        @Immutable
        data class Body(
            val messageId: String?,
            val message: String?,
            val chatter: Chatter,
            val isAction: Boolean = false,
            val color: String? = null,
            val embeddedEmotes: ImmutableList<Emote> = persistentListOf(),
            val badges: ImmutableList<Badge> = persistentListOf(),
            val inReplyTo: InReplyTo? = null,
        ) {
            @Immutable
            data class InReplyTo(
                val message: String?,
                val mentions: List<String>,
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
    data class RaidUpdate(
        val raid: Raid?,
    ) : ChatEvent

    @Immutable
    data class PinnedMessageUpdate(
        val pinnedMessage: PinnedMessage?,
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
