package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat

import fr.outadoc.justchatting.feature.chat.data.http.ChatSettings
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.data.TwitchClient
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Chat room settings (slow mode, sub-only, etc.). Replaces IRC's ROOMSTATE.
 *
 * The current settings are fetched from Helix once subscribed, and kept up to date by EventSub.
 */
internal class EventSubChatSettingsPlugin(
    private val json: Json,
    private val twitchClient: TwitchClient,
) : EventSubChatModerationPlugin() {
    override val subscriptionType = "channel.chat_settings.update"

    override fun parseEvent(
        event: JsonObject,
        timestamp: Instant,
    ): List<ChatEvent> = listOf(json.decodeFromJsonElement(ChatSettings.serializer(), event).map())

    override suspend fun getInitialEvents(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): List<ChatEvent> =
        twitchClient
            .getChatSettings(channelUserId = channelId)
            .onFailure { e -> logError<EventSubChatSettingsPlugin>(e) { "Failed to load chat settings" } }
            .getOrNull()
            ?.data
            ?.firstOrNull()
            ?.let { settings -> listOf(settings.map()) }
            .orEmpty()

    private fun ChatSettings.map(): ChatEvent.Command.RoomStateDelta {
        // Helix and EventSub use different names for the durations
        val followDurationMinutes = followerModeDuration ?: followerModeDurationMinutes
        val slowModeSeconds = slowModeWaitTime ?: slowModeWaitTimeSeconds

        return ChatEvent.Command.RoomStateDelta(
            isEmoteOnly = emoteMode,
            // Like IRC, -1 means followers-only mode is disabled
            minFollowDuration = if (followerMode) (followDurationMinutes ?: 0).minutes else (-1).minutes,
            uniqueMessagesOnly = uniqueChatMode,
            slowModeDuration = if (slowMode) (slowModeSeconds ?: 0).seconds else Duration.ZERO,
            isSubOnly = subscriberMode,
        )
    }
}
