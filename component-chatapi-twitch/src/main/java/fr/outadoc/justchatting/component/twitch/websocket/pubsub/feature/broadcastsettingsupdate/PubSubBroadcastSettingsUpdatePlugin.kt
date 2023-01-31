package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.broadcastsettingsupdate

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PubSubBroadcastSettingsUpdatePlugin(
    private val clock: Clock,
    private val json: Json,
) : PubSubPlugin<PubSubBroadcastSettingsUpdateMessage> {

    override fun getTopic(channelId: String): String =
        "broadcast-settings-update.$channelId"

    override fun parseMessage(message: String): ChatEvent =
        when (val res = json.decodeFromString<PubSubBroadcastSettingsUpdateMessage>(message)) {
            is PubSubBroadcastSettingsUpdateMessage.Update -> {
                ChatEvent.Highlighted(
                    header = "Stream title changed to ${res.status}",
                    timestamp = clock.now(),
                    data = null,
                )
            }
        }
}
