package fr.outadoc.justchatting.feature.chat.domain.eventsub

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

internal interface EventSubPlugin {
    /**
     * The EventSub subscription type, e.g. `channel.update`.
     */
    val subscriptionType: String

    val subscriptionVersion: String

    fun getCondition(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Map<String, String>

    /**
     * @param timestamp when Twitch sent the notification.
     */
    fun parseEvent(
        event: JsonObject,
        timestamp: Instant,
    ): List<ChatEvent>

    /**
     * Events to emit once the subscription has been created on a new session,
     * e.g. to fetch the current state that subsequent notifications will update.
     */
    suspend fun getInitialEvents(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): List<ChatEvent> = emptyList()
}
