package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.resources.StringDesc2
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Immutable
internal sealed interface ChatListItem {

    @Immutable
    sealed class Message : ChatListItem {

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
                val title: StringDesc2,
                val titleIcon: Icon? = null,
                val subtitle: StringDesc2?,
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
            val text: StringDesc2,
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
                val mentions: ImmutableList<String>,
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
    ) : ChatListItem

    @Immutable
    data class UserState(
        val emoteSets: ImmutableList<String> = persistentListOf(),
    ) : ChatListItem

    @Immutable
    data class RemoveContent(
        val upUntil: Instant,
        val matchingUserId: String? = null,
        val matchingMessageId: String? = null,
    ) : ChatListItem

    @Immutable
    data class PollUpdate(
        val poll: Poll,
    ) : ChatListItem

    @Immutable
    data class BroadcastSettingsUpdate(
        val streamTitle: String,
        val streamCategory: StreamCategory,
    ) : ChatListItem

    @Immutable
    data class ViewerCountUpdate(
        val viewerCount: Long,
    ) : ChatListItem

    @Immutable
    data class PredictionUpdate(
        val prediction: Prediction,
    ) : ChatListItem

    @Immutable
    data class RaidUpdate(
        val raid: Raid?,
    ) : ChatListItem

    @Immutable
    data class PinnedMessageUpdate(
        val pinnedMessage: PinnedMessage?,
    ) : ChatListItem

    @Immutable
    data class RichEmbed(
        val messageId: String,
        val title: String,
        val requestUrl: String,
        val thumbnailUrl: String,
        val authorName: String,
        val channelName: String?,
    ) : ChatListItem
}
