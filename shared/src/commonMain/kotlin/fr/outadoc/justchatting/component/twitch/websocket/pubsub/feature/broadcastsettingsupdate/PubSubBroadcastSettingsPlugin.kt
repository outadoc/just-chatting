package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.broadcastsettingsupdate

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PubSubBroadcastSettingsPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubBroadcastSettingsMessage> {

    override fun getTopic(channelId: String): String =
        "broadcast-settings-update.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubBroadcastSettingsMessage>(payload)) {
            is PubSubBroadcastSettingsMessage.Update -> {
                listOf(
                    ChatEvent.BroadcastSettingsUpdate(
                        streamTitle = message.status,
                        gameName = message.game,
                    ),
                )
            }
        }
}
