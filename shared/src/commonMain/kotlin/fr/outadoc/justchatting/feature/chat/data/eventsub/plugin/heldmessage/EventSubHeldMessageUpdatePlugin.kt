package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.heldmessage

import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock

/**
 * Moderators approving or denying one of the user's messages held by AutoMod.
 * Requires the `user:read:chat` scope.
 *
 * We don't subscribe to `channel.chat.user_message_hold`: the hold itself is
 * already reported when sending the message.
 */
internal class EventSubHeldMessageUpdatePlugin(
    private val json: Json,
    private val clock: Clock,
) : EventSubPlugin {
    override val subscriptionType = "channel.chat.user_message_update"
    override val subscriptionVersion = "1"

    override fun getCondition(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Map<String, String> =
        mapOf(
            "broadcaster_user_id" to channelId,
            "user_id" to appUser.userId,
        )

    override fun parseEvent(event: JsonObject): List<ChatEvent> {
        val update = json.decodeFromJsonElement(Event.serializer(), event)
        val status =
            when (update.status) {
                "approved" -> ChatEvent.Message.HeldMessageUpdate.Status.Approved
                "denied" -> ChatEvent.Message.HeldMessageUpdate.Status.Denied
                "invalid" -> ChatEvent.Message.HeldMessageUpdate.Status.Expired
                else -> return emptyList()
            }

        return listOf(
            ChatEvent.Message.HeldMessageUpdate(
                timestamp = clock.now(),
                status = status,
                messageText = update.message.text,
            ),
        )
    }

    @Serializable
    private data class Event(
        @SerialName("status")
        val status: String,
        @SerialName("message")
        val message: Message,
    ) {
        @Serializable
        data class Message(
            @SerialName("text")
            val text: String,
        )
    }
}
