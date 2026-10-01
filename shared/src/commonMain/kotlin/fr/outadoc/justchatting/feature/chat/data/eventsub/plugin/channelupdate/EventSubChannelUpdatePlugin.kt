package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.channelupdate

import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * Stream title and category changes. No authorization required.
 */
internal class EventSubChannelUpdatePlugin(
    private val json: Json,
) : EventSubPlugin {
    override val subscriptionType = "channel.update"
    override val subscriptionVersion = "2"

    override fun getCondition(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Map<String, String> = mapOf("broadcaster_user_id" to channelId)

    override fun parseEvent(
        event: JsonObject,
        timestamp: Instant,
    ): List<ChatEvent> {
        val update = json.decodeFromJsonElement(Event.serializer(), event)
        return listOf(
            ChatEvent.Message.BroadcastSettingsUpdate(
                timestamp = timestamp,
                streamTitle = update.title,
                categoryId = update.categoryId,
                categoryName = update.categoryName,
            ),
        )
    }

    @Serializable
    private data class Event(
        @SerialName("title")
        val title: String,
        @SerialName("category_id")
        val categoryId: String,
        @SerialName("category_name")
        val categoryName: String,
    )
}
