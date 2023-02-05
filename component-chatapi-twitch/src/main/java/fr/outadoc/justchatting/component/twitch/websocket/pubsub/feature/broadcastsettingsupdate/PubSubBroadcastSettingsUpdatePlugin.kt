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

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubBroadcastSettingsUpdateMessage>(payload)) {
            is PubSubBroadcastSettingsUpdateMessage.Update -> {
                listOfNotNull(
                    if (message.status != message.oldStatus) {
                        ChatEvent.Message.Highlighted(
                            header = "Stream title changed to \"${message.status}\"",
                            timestamp = clock.now(),
                            body = null,
                        )
                    } else {
                        null
                    },
                    if (message.game != message.oldGame) {
                        ChatEvent.Message.Highlighted(
                            header = "Gamed changed to \"${message.game}\"",
                            timestamp = clock.now(),
                            body = null,
                        )
                    } else {
                        null
                    },
                )
            }
        }
}
