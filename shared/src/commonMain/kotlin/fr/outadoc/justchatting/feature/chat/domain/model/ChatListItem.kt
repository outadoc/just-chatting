package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.resources.StringDesc
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
public sealed interface ChatListItem {
    @Immutable
    public sealed class Message : ChatListItem {
        public abstract val body: Body?
        public abstract val timestamp: Instant

        @Immutable
        public data class Simple(
            override val body: Body,
            override val timestamp: Instant,
        ) : Message()

        @Immutable
        public data class Highlighted(
            override val timestamp: Instant,
            override val body: Body?,
            val metadata: Metadata,
        ) : Message() {
            public data class Metadata(
                val title: StringDesc,
                val titleIcon: Icon? = null,
                val subtitle: StringDesc?,
                val level: Level = Level.Base,
            )

            @Immutable
            public enum class Level {
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
        public data class Notice(
            override val timestamp: Instant,
            val text: StringDesc,
        ) : Message() {
            override val body: Body? = null
        }

        @Immutable
        public data class Body(
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
            public data class InReplyTo(
                val message: String?,
                val mentions: ImmutableList<String>,
            )
        }
    }

    @Immutable
    public data class RoomStateDelta(
        val isEmoteOnly: Boolean? = null,
        val minFollowDuration: Duration? = null,
        val uniqueMessagesOnly: Boolean? = null,
        val slowModeDuration: Duration? = null,
        val isSubOnly: Boolean? = null,
    ) : ChatListItem

    @Immutable
    public data class UserState(
        val emoteSets: ImmutableList<String> = persistentListOf(),
    ) : ChatListItem

    @Immutable
    public data class RemoveContent(
        val upUntil: Instant,
        val matchingUserId: String? = null,
        val matchingMessageId: String? = null,
    ) : ChatListItem

    @Immutable
    public data class PollUpdate(
        val poll: Poll,
    ) : ChatListItem

    @Immutable
    public data class BroadcastSettingsUpdate(
        val streamTitle: String,
        val streamCategory: StreamCategory,
    ) : ChatListItem

    @Immutable
    public data class ViewerCountUpdate(
        val viewerCount: Long,
    ) : ChatListItem

    @Immutable
    public data class PredictionUpdate(
        val prediction: Prediction,
    ) : ChatListItem

    @Immutable
    public data class RaidUpdate(
        val raid: Raid?,
    ) : ChatListItem

    @Immutable
    public data class PinnedMessageUpdate(
        val pinnedMessage: PinnedMessage?,
    ) : ChatListItem

    @Immutable
    public data class RichEmbed(
        val messageId: String,
        val title: String,
        val requestUrl: String,
        val thumbnailUrl: String,
        val authorName: String,
        val channelName: String?,
    ) : ChatListItem
}
