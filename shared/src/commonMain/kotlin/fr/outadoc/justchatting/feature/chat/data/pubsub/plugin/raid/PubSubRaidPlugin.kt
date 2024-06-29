package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.raid

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

internal class PubSubRaidPlugin(
    private val json: Json,
    private val clock: Clock,
) : PubSubPlugin<PubSubRaidMessage> {

    override fun getTopic(channelId: String): String =
        "raid.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> {
        val message = json.decodeFromString<PubSubRaidMessage>(payload)
        return listOf(
            ChatEvent.Message.RaidUpdate(
                timestamp = clock.now(),
                raid = message.map(),
            ),
        )
    }
}
