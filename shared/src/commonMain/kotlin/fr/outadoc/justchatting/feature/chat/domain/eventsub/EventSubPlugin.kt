package fr.outadoc.justchatting.feature.chat.domain.eventsub

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.serialization.json.JsonObject

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

    fun parseEvent(event: JsonObject): List<ChatEvent>
}
