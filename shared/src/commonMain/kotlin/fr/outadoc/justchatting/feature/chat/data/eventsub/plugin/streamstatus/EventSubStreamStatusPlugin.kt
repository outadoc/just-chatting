package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.streamstatus

import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * The channel going live or offline. No authorization required.
 *
 * Twitch uses one subscription type per direction, so this plugin is
 * instantiated once for each with [isLive] set accordingly.
 */
internal class EventSubStreamStatusPlugin(
    private val json: Json,
    private val isLive: Boolean,
) : EventSubPlugin {
    override val subscriptionType = if (isLive) "stream.online" else "stream.offline"
    override val subscriptionVersion = "1"

    override fun getCondition(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Map<String, String> = mapOf("broadcaster_user_id" to channelId)

    override fun parseEvent(
        event: JsonObject,
        timestamp: Instant,
    ): List<ChatEvent> {
        val status = json.decodeFromJsonElement(Event.serializer(), event)
        return listOf(
            ChatEvent.Message.StreamStatusUpdate(
                timestamp = timestamp,
                isLive = isLive,
                broadcasterDisplayName = status.broadcasterUserName,
            ),
        )
    }

    @Serializable
    private data class Event(
        @SerialName("broadcaster_user_name")
        val broadcasterUserName: String,
    )
}
