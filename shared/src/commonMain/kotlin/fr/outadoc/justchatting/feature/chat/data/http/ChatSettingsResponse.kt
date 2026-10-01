package fr.outadoc.justchatting.feature.chat.data.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatSettingsResponse(
    @SerialName("data")
    val data: List<ChatSettings>,
)

/**
 * Chat room settings, as returned by Helix and by the `channel.chat_settings.update` EventSub event.
 */
@Serializable
internal data class ChatSettings(
    @SerialName("emote_mode")
    val emoteMode: Boolean,
    @SerialName("follower_mode")
    val followerMode: Boolean,
    @SerialName("follower_mode_duration")
    val followerModeDuration: Int? = null,
    @SerialName("follower_mode_duration_minutes")
    val followerModeDurationMinutes: Int? = null,
    @SerialName("slow_mode")
    val slowMode: Boolean,
    @SerialName("slow_mode_wait_time")
    val slowModeWaitTime: Int? = null,
    @SerialName("slow_mode_wait_time_seconds")
    val slowModeWaitTimeSeconds: Int? = null,
    @SerialName("subscriber_mode")
    val subscriberMode: Boolean,
    @SerialName("unique_chat_mode")
    val uniqueChatMode: Boolean,
)
