package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat

import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * Base for chat moderation subscriptions, which all require the `user:read:chat` scope.
 */
internal abstract class EventSubChatModerationPlugin : EventSubPlugin {
    override val subscriptionVersion = "1"

    override fun getCondition(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Map<String, String> =
        mapOf(
            "broadcaster_user_id" to channelId,
            "user_id" to appUser.userId,
        )
}

/**
 * A single message was deleted. Replaces IRC's CLEARMSG.
 */
internal class EventSubMessageDeletePlugin(
    private val json: Json,
) : EventSubChatModerationPlugin() {
    override val subscriptionType = "channel.chat.message_delete"

    override fun parseEvent(
        event: JsonObject,
        timestamp: Instant,
    ): List<ChatEvent> {
        val delete = json.decodeFromJsonElement(Event.serializer(), event)
        return listOf(
            ChatEvent.Command.ClearMessage(
                timestamp = timestamp,
                targetMessage = null,
                targetMessageId = delete.messageId,
                targetUserLogin = delete.targetUserLogin,
            ),
        )
    }

    @Serializable
    private data class Event(
        @SerialName("target_user_login")
        val targetUserLogin: String,
        @SerialName("message_id")
        val messageId: String,
    )
}

/**
 * A user was banned or timed out. Replaces IRC's CLEARCHAT targeting a user.
 */
internal class EventSubClearUserMessagesPlugin(
    private val json: Json,
) : EventSubChatModerationPlugin() {
    override val subscriptionType = "channel.chat.clear_user_messages"

    override fun parseEvent(
        event: JsonObject,
        timestamp: Instant,
    ): List<ChatEvent> {
        val clear = json.decodeFromJsonElement(Event.serializer(), event)
        return listOf(
            ChatEvent.Command.ClearUserMessages(
                timestamp = timestamp,
                targetUserId = clear.targetUserId,
                targetUserLogin = clear.targetUserLogin,
            ),
        )
    }

    @Serializable
    private data class Event(
        @SerialName("target_user_id")
        val targetUserId: String,
        @SerialName("target_user_login")
        val targetUserLogin: String,
    )
}

/**
 * All messages were cleared. Replaces IRC's CLEARCHAT without a target.
 */
internal class EventSubChatClearPlugin : EventSubChatModerationPlugin() {
    override val subscriptionType = "channel.chat.clear"

    override fun parseEvent(
        event: JsonObject,
        timestamp: Instant,
    ): List<ChatEvent> =
        listOf(
            ChatEvent.Command.ClearChat(
                timestamp = timestamp,
                targetUserId = null,
                targetUserLogin = null,
                duration = null,
            ),
        )
}
