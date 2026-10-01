package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.sharedchat

import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.SharedChatSession
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * The channel joining, leaving, or seeing changes to a shared chat session.
 * No authorization required.
 *
 * Twitch uses one subscription type per [Kind], so this plugin is instantiated once for each.
 */
internal class EventSubSharedChatPlugin(
    private val json: Json,
    private val kind: Kind,
) : EventSubPlugin {
    enum class Kind(
        val subscriptionType: String,
    ) {
        Begin("channel.shared_chat.begin"),
        Update("channel.shared_chat.update"),
        End("channel.shared_chat.end"),
    }

    override val subscriptionType = kind.subscriptionType
    override val subscriptionVersion = "1"

    override fun getCondition(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Map<String, String> = mapOf("broadcaster_user_id" to channelId)

    override fun parseEvent(event: JsonObject): List<ChatEvent> {
        val session: SharedChatSession? =
            when (kind) {
                Kind.Begin, Kind.Update -> {
                    val session = json.decodeFromJsonElement(Event.serializer(), event)
                    SharedChatSession(
                        hostChannelId = session.hostBroadcasterUserId,
                        participantChannelIds =
                            session.participants
                                .map { participant -> participant.broadcasterUserId }
                                .toImmutableList(),
                    )
                }

                Kind.End -> {
                    null
                }
            }

        return listOf(ChatEvent.Command.SharedChatSessionUpdate(session = session))
    }

    @Serializable
    private data class Event(
        @SerialName("host_broadcaster_user_id")
        val hostBroadcasterUserId: String,
        @SerialName("participants")
        val participants: List<Participant>,
    ) {
        @Serializable
        data class Participant(
            @SerialName("broadcaster_user_id")
            val broadcasterUserId: String,
        )
    }
}
