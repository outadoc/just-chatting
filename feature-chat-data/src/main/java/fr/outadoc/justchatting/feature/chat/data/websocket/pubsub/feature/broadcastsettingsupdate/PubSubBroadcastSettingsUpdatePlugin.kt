package fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.feature.broadcastsettingsupdate

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.model.Command
import fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.plugin.PubSubPlugin
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PubSubBroadcastSettingsUpdatePlugin(
    private val clock: Clock,
    private val json: Json,
) : PubSubPlugin<PubSubBroadcastSettingsUpdateMessage> {

    override fun getTopic(channelId: String): String =
        "broadcast-settings-update.$channelId"

    override fun parseMessage(message: String): ChatCommand =
        when (val res = json.decodeFromString<PubSubBroadcastSettingsUpdateMessage>(message)) {
            is PubSubBroadcastSettingsUpdateMessage.Update -> {
                Command.Notice(
                    message = "Stream title changed to ${res.status}",
                    timestamp = clock.now(),
                    messageId = null
                )
            }
        }
}
