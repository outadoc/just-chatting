package fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.feature.broadcastsettingsupdate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class PubSubBroadcastSettingsUpdateMessage {

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
    ) : PubSubBroadcastSettingsUpdateMessage()
}
