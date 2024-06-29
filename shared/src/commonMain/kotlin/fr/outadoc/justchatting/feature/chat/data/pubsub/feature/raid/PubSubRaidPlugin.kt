package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.raid

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
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