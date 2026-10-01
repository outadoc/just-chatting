package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.raid

import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock

/**
 * Raids going out of the current channel. No authorization required.
 *
 * Incoming raids are already announced over IRC, so we don't subscribe to them.
 */
internal class EventSubOutgoingRaidPlugin(
    private val json: Json,
    private val clock: Clock,
) : EventSubPlugin {
    override val subscriptionType = "channel.raid"
    override val subscriptionVersion = "1"

    override fun getCondition(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Map<String, String> = mapOf("from_broadcaster_user_id" to channelId)

    override fun parseEvent(event: JsonObject): List<ChatEvent> {
        val raid = json.decodeFromJsonElement(Event.serializer(), event)
        return listOf(
            ChatEvent.Message.RaidUpdate(
                timestamp = clock.now(),
                raid =
                    Raid.Go(
                        targetId = raid.toBroadcasterUserId,
                        targetLogin = raid.toBroadcasterUserLogin,
                        targetDisplayName = raid.toBroadcasterUserName,
                        targetProfileImageUrl = null,
                        viewerCount = raid.viewers,
                    ),
            ),
        )
    }

    @Serializable
    private data class Event(
        @SerialName("to_broadcaster_user_id")
        val toBroadcasterUserId: String,
        @SerialName("to_broadcaster_user_login")
        val toBroadcasterUserLogin: String,
        @SerialName("to_broadcaster_user_name")
        val toBroadcasterUserName: String,
        @SerialName("viewers")
        val viewers: Int,
    )
}
