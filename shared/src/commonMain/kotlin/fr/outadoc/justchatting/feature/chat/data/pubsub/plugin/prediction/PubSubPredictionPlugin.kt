package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.prediction

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json
import kotlin.time.Instant

internal class PubSubPredictionPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubPredictionMessage> {

    override fun getTopic(channelId: String): String = "predictions-channel-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> {
        val message = json.decodeFromString<PubSubPredictionMessage>(payload)
        return listOfNotNull(
            ChatEvent.Message.PredictionUpdate(
                timestamp = Instant.parse(message.data.timestampIso),
                prediction = message.map(),
            ),
        )
    }
}
