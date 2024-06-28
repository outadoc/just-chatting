package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.raid

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

internal class PubSubRaidPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubRaidMessage> {

    override fun getTopic(channelId: String): String =
        "raid.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> {
        val message = json.decodeFromString<PubSubRaidMessage>(payload)
        return listOf(
            ChatEvent.RaidUpdate(raid = message.map()),
        )
    }
}
