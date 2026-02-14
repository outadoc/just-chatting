package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.broadcastsettingsupdate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class PubSubBroadcastSettingsMessage {
    @Serializable
    @SerialName("broadcast_settings_update")
    data class Update(
        @SerialName("channel_id")
        val channelId: String,
        @SerialName("channel")
        val channel: String,
        @SerialName("status")
        val status: String,
        @SerialName("game")
        val game: String,
        @SerialName("game_id")
        val gameId: Int,
        @SerialName("old_status")
        val oldStatus: String?,
        @SerialName("old_game")
        val oldGame: String?,
        @SerialName("old_game_id")
        val oldGameId: Int?,
    ) : PubSubBroadcastSettingsMessage()
}
