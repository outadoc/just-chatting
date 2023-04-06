package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.raid

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PubSubRaidPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubRaidMessage> {

    override fun getTopic(channelId: String): String =
        "raid.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubRaidMessage>(payload)) {
            is PubSubRaidMessage.Update -> {
                listOf(
                    ChatEvent.RaidUpdate(
                        raid = message.raid.map(),
                    ),
                )
            }

            is PubSubRaidMessage.Go -> {
                listOf(
                    ChatEvent.RaidUpdate(
                        raid = message.raid.map(),
                    ),
                )
            }
        }
}
